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
package net.jmhertlein.mcanalytics.plugin.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.Statement;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author joshua
 */
public class WritePlayerCountTask extends BukkitRunnable {
    private MCAnalyticsPlugin p;
    private PreparedStatement addNewCount;
    private Connection c;

    public WritePlayerCountTask(MCAnalyticsPlugin p, DataSource ds) {
        this.p = p;
        try {
            this.c = ds.getConnection();
            addNewCount = c.prepareStatement(Statement.ADD_HOURLY_PLAYER_COUNT.toString());
        } catch(SQLException ex) {
            Logger.getLogger(WritePlayerCountTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        int players = Bukkit.getOnlinePlayers().size();

        Bukkit.getScheduler().runTaskAsynchronously(p, () -> {
            try {
                addNewCount.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                addNewCount.setInt(2, players);
                addNewCount.execute();
                addNewCount.clearParameters();
            } catch(SQLException ex) {
                Logger.getLogger(WritePlayerCountTask.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if(c != null) {
                    try {
                        c.close();
                    } catch(SQLException ex) {
                        Logger.getLogger(WritePlayerCountTask.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

}
