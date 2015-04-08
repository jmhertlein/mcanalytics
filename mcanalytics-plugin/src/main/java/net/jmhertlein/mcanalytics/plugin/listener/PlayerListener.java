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

    @EventHandler(priority = EventPriority.MONITOR)
    public void firstLoginHandler(PlayerJoinEvent e) {
        if(e.getPlayer().hasPlayedBefore()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new WriteBounceUpdateTask(plugin, e.getPlayer()));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new WriteFirstLoginTask(e.getPlayer(), plugin));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCountLoginListener(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new WritePlayerCountTask(plugin));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCountLogoutListener(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new WritePlayerCountTask(plugin, Bukkit.getOnlinePlayers().size() - 1));
    }
}
