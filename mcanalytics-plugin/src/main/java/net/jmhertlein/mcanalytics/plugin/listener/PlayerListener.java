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

import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author joshua
 */
public class PlayerListener implements Listener {
    private final MCAnalyticsPlugin plugin;

    public PlayerListener(MCAnalyticsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void firstLoginHandler(PlayerJoinEvent e) {
        if(!e.getPlayer().hasPlayedBefore()) {
            WriteFirstLoginTask f = new WriteFirstLoginTask(e.getPlayer(), plugin);
            f.gather();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCountLoginListener(PlayerJoinEvent e) {
        writePlayerCount();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCountLogoutListener(PlayerQuitEvent e) {
        WritePlayerCountTask f = new WritePlayerCountTask(plugin);
        f.gather(Bukkit.getOnlinePlayers().size() - 1);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, f);
    }

    private void writePlayerCount() throws IllegalArgumentException {
        WritePlayerCountTask f = new WritePlayerCountTask(plugin);
        f.gather();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, f);
    }
}
