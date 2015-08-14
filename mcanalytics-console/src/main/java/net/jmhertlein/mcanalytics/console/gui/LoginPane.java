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

import net.jmhertlein.mcanalytics.console.gui.ServerDialog;
import net.jmhertlein.mcanalytics.console.gui.ChartPane;
import net.jmhertlein.mcanalytics.console.gui.HostPane;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.FutureRequest;
import net.jmhertlein.mcanalytics.api.auth.AuthenticationMethod;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.api.auth.UntrustedCertificateException;
import net.jmhertlein.mcanalytics.api.request.AuthenticationRequest;
import net.jmhertlein.mcanalytics.console.MCAConsoleApplication;
import net.jmhertlein.mcanalytics.console.gui.CertTrustPromptDialog;
import net.jmhertlein.mcanalytics.console.gui.FXMLPane;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class LoginPane extends FXMLPane {
    private static final Path DATA_PATH = Paths.get(System.getProperty("user.home"), ".local", "share", "mcanalytics-console"),
            TM_PATH = DATA_PATH.resolve("trust.jks");
    @FXML
    private Accordion serverList;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private CheckBox rememberLoginBox;

    private KeyStore trust;
    private MCAConsoleApplication app;

    public LoginPane(MCAConsoleApplication app) {
        super("/fxml/LoginScene.fxml");

        trust = SSLUtil.newKeyStore();
        this.app = app;

        File f = TM_PATH.toFile();
        if(f.exists()) {
            try(FileInputStream fis = new FileInputStream(f)) {
                trust.load(fis, new char[0]);
            } catch(IOException | NoSuchAlgorithmException | CertificateException ex) {
                System.err.println("Error loading trust source: " + ex.getLocalizedMessage());
            }
        } else {
            System.out.println("No trust.jks, generating new...");
            KeyPair pair = SSLUtil.newECDSAKeyPair();
            X509Certificate cert = SSLUtil.newSelfSignedCertificate(pair, SSLUtil.newX500Name(System.getProperty("user.name"), "<Servername>", "MCAnalytics Users"), false);
            try {
                trust.setCertificateEntry("client-public-selfsigned", cert);
                trust.setKeyEntry("client-private", pair.getPrivate(), new char[0], new Certificate[]{cert});
            } catch(KeyStoreException ex) {
                System.err.println("Error initializing keystore: " + ex.getLocalizedMessage());
            }
        }

        serverList.getPanes().add(new HostPane("Josh's Test Server", "localhost", 35555));

        if(!serverList.getPanes().isEmpty())
            serverList.setExpandedPane(serverList.getPanes().get(0));
    }

    @FXML
    public void onLoginButtonPressed(ActionEvent event) {
        HostPane selected = (HostPane) serverList.getExpandedPane();
        if(selected == null)
            return;

        try {
            SSLContext ctx = SSLUtil.buildClientContext(trust);
            SSLSocket raw = (SSLSocket) ctx.getSocketFactory().createSocket(selected.getUrl(), selected.getPort());
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
            FutureRequest<Boolean> login = sock.submit(new AuthenticationRequest(AuthenticationMethod.PASSWORD, usernameField.getText(), passwordField.getText()));
            System.out.println("Logging in with: " + usernameField.getText() + " + " + passwordField.getText());
            try {
                boolean success = login.get();
                if(success)
                    System.out.println("Login successful");
                else
                    System.out.println("Login failed.");
            } catch(InterruptedException | ExecutionException ex) {
                Logger.getLogger(LoginPane.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Login error.");
            }
            //auth done

            Stage window = (Stage) loginButton.getScene().getWindow();
            window.setScene(new Scene(new ChartPane(sock)));
            window.show();
        } catch(IOException ex) {
            Logger.getLogger(LoginPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void addNewServer(ActionEvent event) {
        ServerDialog d = new ServerDialog();
        HostPane p = (HostPane) d.showAndWait().get();

        if(p != null) {
            serverList.getPanes().add(p);
            System.out.println("Added server: " + p.toString());
        } else {
            System.out.println("Didn't add server?");
        }
    }

    @FXML
    public void editServer(ActionEvent event) {

    }

    @FXML
    public void deleteServer(ActionEvent event) {

    }
}
