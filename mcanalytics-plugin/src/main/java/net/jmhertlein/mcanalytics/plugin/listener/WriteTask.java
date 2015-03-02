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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author joshua
 */
public abstract class WriteTask extends BukkitRunnable {
    private MCAnalyticsPlugin p;
    private DataSource ds;

    public WriteTask(MCAnalyticsPlugin p, DataSource ds) {
        this.p = p;
        this.ds = ds;
    }

    public abstract void gather();

    protected abstract void write(Connection c) throws SQLException;

    @Override
    public final void run() {
        try(Connection c = ds.getConnection()) {
            write(c);
        } catch(SQLException ex) {
            Logger.getLogger(WritePlayerCountTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
