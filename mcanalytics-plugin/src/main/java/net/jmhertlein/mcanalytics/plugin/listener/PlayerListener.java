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

import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 *
 * @author joshua
 */
public class PlayerListener implements Listener {
    private final MCAnalyticsPlugin plugin;
    private final DataSource connections;
    private final StatementProvider stmts;

    public PlayerListener(MCAnalyticsPlugin plugin, DataSource connections, StatementProvider stmts) {
        this.plugin = plugin;
        this.connections = connections;
        this.stmts = stmts;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if(!e.getPlayer().hasPlayedBefore()) {
            WriteFirstLoginTask f = new WriteFirstLoginTask(e.getPlayer(), plugin, connections, stmts);
            f.gather();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, f);
        }
    }
}
