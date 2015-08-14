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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class CertTrustPromptDialog extends FXMLDialog {
    private final KeyStore trust;
    private final X509Pane certPane;

    @FXML
    private Pane container;

    public CertTrustPromptDialog(KeyStore ks, X509Certificate cert) {
        super("/fxml/CertTrustPrompt.fxml");

        certPane = new X509Pane(cert);
        this.trust = ks;
        container.getChildren().add(certPane);
        this.getDialogPane().layout();
    }

    @FXML
    public void onTrust() {
        try {
            trust.setCertificateEntry(UUID.randomUUID().toString(), certPane.getCert());
            setResult(true);
        } catch(KeyStoreException ex) {
            Logger.getLogger(CertTrustPromptDialog.class.getName()).log(Level.SEVERE, null, ex);
            setResult(false);
        }
        close();
    }

    @FXML
    public void onReject() {
        setResult(false);
        close();
    }

}
