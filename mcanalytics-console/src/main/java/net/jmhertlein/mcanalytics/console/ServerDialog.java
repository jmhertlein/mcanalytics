/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mcanalytics.console;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import net.jmhertlein.mcanalytics.console.gui.HostPane;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class ServerDialog extends Dialog {

    @FXML
    private TextField nicknameField, hostnameField, portField;
    
    public ServerDialog() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ServerDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
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
