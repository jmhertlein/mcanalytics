/*
 * Copyright (C) 2015 Joshua Michael Hertlein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.mcanalytics.console.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.FutureRequest;
import net.jmhertlein.mcanalytics.api.auth.AuthenticationResult;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.api.auth.UntrustedCertificateException;
import net.jmhertlein.mcanalytics.api.request.AuthenticationRequest;
import net.jmhertlein.mcanalytics.console.Dialogs;
import net.jmhertlein.mcanalytics.console.MCAConsoleApplication;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class LoginPane extends FXMLPane {
    @FXML
    private ListView<HostEntry> hostList;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private CheckBox rememberLoginBox;

    private final KeyStore trust;
    private final MCAConsoleApplication app;
    private final JSONObject config;

    public LoginPane(MCAConsoleApplication app, JSONObject config, KeyStore trust) {
        super("/fxml/LoginScene.fxml");

        this.trust = trust;
        this.app = app;
        this.config = config;

        hostList.setCellFactory(v -> new HostEntryCell());
        hostList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            usernameField.setDisable(n.hasCert());
            passwordField.setDisable(n.hasCert());
            rememberLoginBox.setDisable(n.hasCert());
        });

        loadHostPanes(config);
    }

    @FXML
    public void onLoginButtonPressed(ActionEvent event) {
        HostEntry selected = hostList.getSelectionModel().getSelectedItem();
        if(selected == null)
            return;

        try {
            SSLContext ctx = SSLUtil.buildClientContext(trust);
            SSLSocket raw = (SSLSocket) ctx.getSocketFactory().createSocket(selected.getUrl(), selected.getPort());
            raw.setWantClientAuth(true);
            try {
                System.out.println("Starting handshake...");
                raw.startHandshake();
            } catch(SSLException ssle) {
                if(ssle.getCause() instanceof UntrustedCertificateException) {
                    System.out.println("Got the correct exception");
                    UntrustedCertificateException uce = (UntrustedCertificateException) ssle.getCause();
                    CertTrustPromptDialog dlg = new CertTrustPromptDialog(trust, (X509Certificate) uce.getChain()[0]);
                    dlg.showAndWait();
                    System.out.println("DIALOG RETURNED");
                }
                return;
            }

            PrintWriter out = new PrintWriter(raw.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(raw.getInputStream()));
            APISocket sock = new APISocket(out, in);
            app.setAPISocket(sock);
            sock.startListener();

            //handle authentication
            boolean hasCert = false;
            FutureRequest<AuthenticationResult> login;
            if(trust.isCertificateEntry(selected.getUrl())) {
                try {
                    ((X509Certificate) trust.getCertificate(selected.getUrl())).checkValidity();
                    hasCert = true;
                } catch(CertificateExpiredException | CertificateNotYetValidException ex) {
                    Logger.getLogger(LoginPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            System.out.println("Has cert: " + hasCert);
            KeyPair newPair = null;
            String username;

            if(hasCert) {
                username = SSLUtil.getCNs((X509Certificate) trust.getCertificate(selected.getUrl())).iterator().next();
                login = sock.submit(new AuthenticationRequest(username));
                System.out.println("Logging in w/ cert. CN: " + username + ", URL: " + selected.getUrl());
            } else if(rememberLoginBox.isSelected()) {
                newPair = SSLUtil.newECDSAKeyPair();
                username = usernameField.getText();
                PKCS10CertificationRequest csr = SSLUtil.newCertificateRequest(SSLUtil.newX500Name(username, selected.getUrl(), "mcanalytics"), newPair);
                login = sock.submit(new AuthenticationRequest(usernameField.getText(), passwordField.getText(), csr));
                System.out.println("Logging in with: " + usernameField.getText() + " + " + passwordField.getText() + " and requesting a cert.");
            } else {
                username = usernameField.getText();
                login = sock.submit(new AuthenticationRequest(username, passwordField.getText()));
                System.out.println("Logging in with: " + username + " + " + passwordField.getText());
            }

            try {
                boolean success = login.get().getSuccess();
                if(success) {
                    System.out.println("Login successful");
                    if(login.get().hasCertificate()) {
                        trust.setCertificateEntry(selected.getUrl(), login.get().getCert());
                        trust.setKeyEntry(selected.getUrl() + "-private", newPair.getPrivate(), new char[0], new Certificate[]{login.get().getCert(), login.get().getCA()});
                        System.out.println("Stored a trusted cert from server.");
                    }
                } else {
                    System.out.println("Login failed.");
                    Dialog dlg = new Dialog();
                    dlg.setTitle("Login Failed");
                    dlg.setContentText("Could not login- invalid login credentials.");
                    dlg.showAndWait();
                    return;
                }
            } catch(InterruptedException | ExecutionException | KeyStoreException ex) {
                Logger.getLogger(LoginPane.class.getName()).log(Level.SEVERE, null, ex);
                Dialogs.showMessage("Connection Error", "Connection Error", ex.getMessage(), ex.toString());
                System.out.println("Login error.");
                return;
            }
            //auth done

            Stage window = (Stage) loginButton.getScene().getWindow();
            window.setScene(new Scene(new ChartPane(username, sock)));
            window.show();
        } catch(IOException | KeyStoreException ex) {
            Logger.getLogger(LoginPane.class.getName()).log(Level.SEVERE, null, ex);
            Dialog dlg = new Dialog();
            dlg.setTitle("Connection Error");
            dlg.setContentText(ex.getMessage());
            dlg.showAndWait();
            System.out.println("Login error.");
            return;
        }
    }

    @FXML
    public void addNewServer(ActionEvent event) {
        ServerDialog d = new ServerDialog();
        HostEntry h = (HostEntry) d.showAndWait().get();

        if(h != null) {
            hostList.getItems().add(h);
            config.getJSONArray("hosts").put(h.toJSON());
            System.out.println("Added server: " + h.toString());
        } else {
            System.out.println("Didn't add server?");
        }
    }

    @FXML
    public void deleteServer(ActionEvent event) {
        HostEntry remove = hostList.getItems().remove(hostList.getSelectionModel().getSelectedIndex());

        try {
            trust.deleteEntry(remove.getUrl());
            trust.deleteEntry(remove.getUrl() + "-private");
        } catch(KeyStoreException ex) {
            Logger.getLogger(LoginPane.class.getName()).log(Level.SEVERE, null, ex);
        }

        config.put("hosts", hostList.getItems().stream().map(entry -> entry.toJSON()).collect(Collectors.toList()));
    }

    private void loadHostPanes(JSONObject config) {

        if(config.has("hosts")) {
            JSONArray hosts = config.getJSONArray("hosts");
            for(int i = 0; i < hosts.length(); i++) {
                JSONObject host = hosts.getJSONObject(i);
                HostEntry entry = HostEntry.fromJSON(host);
                try {
                    entry.setHasCert(trust.containsAlias(entry.getUrl() + "-private"));
                } catch(KeyStoreException ex) {
                    Logger.getLogger(LoginPane.class.getName()).log(Level.SEVERE, null, ex);
                }
                hostList.getItems().add(entry);
            }
        }

        if(!hostList.getItems().isEmpty())
            hostList.getSelectionModel().select(0);
    }
}
