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

import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

/**
 *
 * @author joshua
 */
public class HostPane extends TitledPane {
    private final String displayName, url;
    private final int port;

    public HostPane(String displayName, String url, int port) {
        this.displayName = displayName;
        this.url = url;
        this.port = port;

        this.setText(displayName);
        this.setContent(new Label(url + ":" + port));
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
