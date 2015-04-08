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
import java.util.UUID;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author joshua
 */
public class WriteBounceUpdateTask extends WriteTask {
    private final UUID id;

    public WriteBounceUpdateTask(MCAnalyticsPlugin p, OfflinePlayer player) {
        super(p);
        id = player.getUniqueId();
    }

    @Override
    protected void write(Connection c, StatementProvider stmts) throws SQLException {
        try(PreparedStatement updateBounced = c.prepareStatement(stmts.get(SQLString.UPDATE_BOUNCED_PLAYER))) {
            updateBounced.setString(1, id.toString());
            updateBounced.executeUpdate();
        }
    }

}
