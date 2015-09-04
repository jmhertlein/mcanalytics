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

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

/**
 *
 * @author joshua
 */
public class Dialogs {
    public static void showMessage(String title, String header, String message, String expandable) {
        Dialog d = new Dialog();
        d.setTitle(title);
        d.setHeaderText(header);
        d.setContentText(message);
        TextArea expanded = new TextArea(expandable);
        expanded.setEditable(false);
        d.getDialogPane().setExpandableContent(new ScrollPane(expanded));
        d.getDialogPane().getButtonTypes().add(ButtonType.OK);
        d.showAndWait();
    }

    public static void showMessage(String title, String header, String message) {
        Dialog d = new Dialog();
        d.setTitle(title);
        d.setHeaderText(header);
        d.setContentText(message);
        d.getDialogPane().getButtonTypes().add(ButtonType.OK);
        d.showAndWait();
    }
}
