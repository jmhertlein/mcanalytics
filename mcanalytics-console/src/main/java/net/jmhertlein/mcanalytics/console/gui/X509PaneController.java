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

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class X509PaneController implements Initializable {
    private X509Certificate cert;

    @FXML
    private TextArea subjectDN;
    @FXML
    private TextArea issuerDN;
    @FXML
    private TextArea pubKeyArea;
    @FXML
    private DatePicker notBeforeField;
    @FXML
    private DatePicker notAfterField;
    @FXML
    private TextField caBasicConstraint;
    @FXML
    private TextField maxlenBasicConstraint;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void setCertificate(X509Certificate c) {
        this.cert = c;
        subjectDN.setText(cert.getSubjectX500Principal().toString());
        issuerDN.setText(cert.getIssuerX500Principal().toString());
        pubKeyArea.setText(Base64.encode(cert.getPublicKey().getEncoded()));
        notBeforeField.setValue(cert.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        notAfterField.setValue(cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        caBasicConstraint.setText(cert.getBasicConstraints() == -1 ? "No" : "Yes");
        maxlenBasicConstraint.setText(Integer.toString(cert.getBasicConstraints()));
    }

    public X509Certificate getCert() {
        return cert;
    }

}
