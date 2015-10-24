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
package net.jmhertlein.mcanalytics.plugin.listener.writer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;

/**
 *
 * @author joshua
 */
public class WriteWorldChangeTask extends WriteTask{
    private final LocalDateTime instant;
    private final UUID fromId, toId;
    private final String fromName, toName;
    private final UUID playerId;
    private final String playerName;

    public WriteWorldChangeTask(LocalDateTime instant, UUID fromId, UUID toId, String fromName, String toName, UUID playerId, String playerName, MCAnalyticsPlugin p) {
        super(p);
        this.instant = instant;
        this.fromId = fromId;
        this.toId = toId;
        this.fromName = fromName;
        this.toName = toName;
        this.playerId = playerId;
        this.playerName = playerName;
    }
    


    @Override
    protected void write(Connection c, StatementProvider stmts) throws SQLException {
        try(PreparedStatement addWorldChange = c.prepareStatement(stmts.get(SQLString.ADD_WORLD_CHANGE))) {
            addWorldChange.setTimestamp(1, Timestamp.valueOf(instant));
            addWorldChange.setString(2, fromId.toString());
            addWorldChange.setString(3, toId.toString());
            addWorldChange.setString(4, fromName);
            addWorldChange.setString(5, toName);
            addWorldChange.setString(6, playerId.toString());
            addWorldChange.setString(7, playerName);
            addWorldChange.execute();
        }
    }
    
}
