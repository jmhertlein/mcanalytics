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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mcanalytics.plugin.daemon.ClientMonitor;
import net.jmhertlein.mcanalytics.plugin.listener.writer.AddUserTask;
import net.jmhertlein.reflective.CommandDefinition;
import net.jmhertlein.reflective.annotation.CommandMethod;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author joshua
 */
public class AnalyticsCommandDefinition implements CommandDefinition {
    private MCAnalyticsPlugin p;

    public AnalyticsCommandDefinition(MCAnalyticsPlugin p) {
        this.p = p;
    }

    @CommandMethod(path = "mca adduser", requiredArgs = 2, permNode = "mca.adduser")
    public void createNewUser(CommandSender s, String name, String password) {
        AddUserTask t = new AddUserTask(s, name, password, p);
        p.getServer().getScheduler().runTaskAsynchronously(p, t);
    }

    @CommandMethod(path = "mca connected", requiredArgs = 1, permNode = "mca.connected")
    public void listConnectedUsers(CommandSender s) {
        System.out.println("The following users are connected:");
        for(ClientMonitor m : p.getDaemon().getConnectedClients()) {
            s.sendMessage(m.getUsername());
        }
    }

    @CommandMethod(path = "mca deluser", requiredArgs = 1, permNode = "mca.deluser")
    public void deleteUser(CommandSender s, String username) {
        try(Connection c = p.getConnectionPool().getConnection();
            PreparedStatement stmt = c.prepareStatement(p.getStmts().get(SQLString.DELETE_USER))) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            s.sendMessage("User \"" + username + "\"deleted.");
        } catch(SQLException ex) {
            Logger.getLogger(AnalyticsCommandDefinition.class.getName()).log(Level.SEVERE, null, ex);
            s.sendMessage("Error:" + ex.getLocalizedMessage());
        }
    }

    @CommandMethod(path = "mca gc", permNode = "mca.debug")
    public void manualGC(CommandSender s) {
        System.gc();
        s.sendMessage(ChatColor.GREEN + "Manually running GC!!");
    }
}
