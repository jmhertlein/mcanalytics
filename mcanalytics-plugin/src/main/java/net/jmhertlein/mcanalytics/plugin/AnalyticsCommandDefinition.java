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
package net.jmhertlein.mcanalytics.plugin;

import net.jmhertlein.reflective.CommandDefinition;
import net.jmhertlein.reflective.annotation.CommandMethod;

/**
 *
 * @author joshua
 */
public class AnalyticsCommandDefinition implements CommandDefinition {

    @CommandMethod(path = "mca adduser", requiredArgs = 1, permNode = "mca.adduser")
    public void createNewUser(String name) {
        throw new UnsupportedOperationException();
    }

    @CommandMethod(path = "mca connected", requiredArgs = 1, permNode = "mca.connected")
    public void listConnectedUsers(String name) {
        throw new UnsupportedOperationException();
    }
}
