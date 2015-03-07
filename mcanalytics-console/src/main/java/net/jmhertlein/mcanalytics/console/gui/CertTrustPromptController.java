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

import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class CertTrustPromptController implements Initializable {
    private KeyStore trust;
    private X509PaneController certController;

    @FXML
    private Pane container;
    @FXML
    private Button trustButton;
    @FXML
    private Button rejectButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void setCertificate(X509Certificate c) {
        FXMLLoader l = new FXMLLoader();
        try {
            Parent load = (Parent) l.load(getClass().getResourceAsStream("/fxml/X509Pane.fxml"));
            certController = ((X509PaneController) l.getController());
            certController.setCertificate(c);
            container.getChildren().add(load);
            //container.layout();
        } catch(IOException ex) {
            Logger.getLogger(CertTrustPromptController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setKeyStore(KeyStore trust) {
        this.trust = trust;
    }

    @FXML
    private void onTrust(ActionEvent event) {
        try {
            trust.setCertificateEntry(UUID.randomUUID().toString(), certController.getCert());
        } catch(KeyStoreException ex) {
            Logger.getLogger(CertTrustPromptController.class.getName()).log(Level.SEVERE, null, ex);
        }

        container.getScene().getWindow().hide();
    }

    @FXML
    private void onReject(ActionEvent event) {
        container.getScene().getWindow().hide();
    }

}
