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

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author joshua
 */
public class HostEntryCell extends ListCell<HostEntry> {
    @Override
    public void updateItem(HostEntry item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null) {
            setText(item.getDisplayName());
            Circle indicator = new Circle(5);
            indicator.setFill(item.hasCert() ? Color.web("green") : Color.web("grey"));
            setGraphic(indicator);

            Tooltip.install(this, new Tooltip(item.getUrl() + ":" + item.getPort()));
        } else {
            setGraphic(null);
            setText("");
        }
    }
}
