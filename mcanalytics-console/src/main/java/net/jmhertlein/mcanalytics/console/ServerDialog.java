/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mcanalytics.console;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
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
        DialogPane pane = new DialogPane();
        fxmlLoader.setRoot(pane);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        }
        setDialogPane(pane);
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
