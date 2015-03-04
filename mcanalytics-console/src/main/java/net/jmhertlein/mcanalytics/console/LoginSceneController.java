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
package net.jmhertlein.mcanalytics.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author joshua
 */
public class LoginSceneController implements Initializable {
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    public void onLoginButtonPressed(ActionEvent event) {
        try {
            Socket raw = new Socket("localhost", 35555);
            PrintWriter out = new PrintWriter(raw.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(raw.getInputStream()));

            FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/ChartScene.fxml"));
            ((SceneController) l.getController()).setIO(out, in);
            Stage window = (Stage) loginButton.getScene().getWindow();
            window.setScene(new Scene(l.load()));
            window.show();
        } catch(IOException ex) {
            Logger.getLogger(LoginSceneController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No login 4 u");
        }
    }

}
