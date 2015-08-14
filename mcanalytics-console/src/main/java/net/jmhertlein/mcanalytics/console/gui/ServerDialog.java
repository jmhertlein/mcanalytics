/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mcanalytics.console.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import net.jmhertlein.mcanalytics.console.gui.FXMLDialog;
import net.jmhertlein.mcanalytics.console.gui.HostPane;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class ServerDialog extends FXMLDialog {

    @FXML
    private TextField nicknameField, hostnameField, portField;

    public ServerDialog() {
        super("/fxml/ServerDialog.fxml");
    }

    @FXML
    public void onOK() {
        this.setResult(new HostPane(nicknameField.getText(), hostnameField.getText(), Integer.parseInt(portField.getText())));
        this.close();
    }

    @FXML
    public void onCancel() {
        this.setResult(null);
        this.close();
    }
}
