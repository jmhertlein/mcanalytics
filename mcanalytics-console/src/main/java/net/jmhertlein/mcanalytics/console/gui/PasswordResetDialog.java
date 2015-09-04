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
import java.util.concurrent.ExecutionException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.FutureRequest;
import net.jmhertlein.mcanalytics.api.request.PasswordResetRequest;
import net.jmhertlein.mcanalytics.console.Dialogs;

/**
 *
 * @author joshua
 */
public class PasswordResetDialog extends FXMLDialog {
    @FXML
    private PasswordField currentPasswordField, newPasswordField, confirmNewPasswordField;

    private final APISocket socket;
    private final String username;

    public PasswordResetDialog(String username, APISocket s) {
        super("/fxml/PasswordResetDialog.fxml");

        socket = s;
        this.username = username;
    }

    @FXML
    private void onConfirm() {
        if(newPasswordField.getText().equals(confirmNewPasswordField.getText())) {
            try {
                FutureRequest<Boolean> req = socket.submit(new PasswordResetRequest(newPasswordField.getText(), currentPasswordField.getText(), username));
                if(req.get()) {
                    setResult(true);
                    close();
                } else {
                    Dialogs.showMessage("Error", "Incorrect Password", "The password was incorrect.");
                }
            } catch(ExecutionException | InterruptedException | IOException ex) {
                Dialogs.showMessage("Error Changing Password", ex.getClass().getName(), ex.getMessage(), ex.toString());
            }
        } else {
            Dialogs.showMessage("Passwords Mismatch", "Password Mismatch", "The new passwords do not match.");
        }
    }

    @FXML
    private void onCancel() {
        setResult(false);
        close();
    }
}
