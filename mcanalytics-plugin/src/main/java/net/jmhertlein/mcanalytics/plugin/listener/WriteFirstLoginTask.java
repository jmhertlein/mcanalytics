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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class WriteFirstLoginTask extends WriteTask {
    private Player pl;
    private LocalDateTime loginTime;
    private UUID id;
    private String name;

    public WriteFirstLoginTask(Player pl, MCAnalyticsPlugin p, DataSource ds, StatementProvider stmts) {
        super(p, ds, stmts);
        this.pl = pl;
    }

    @Override
    public void gather() {
        loginTime = LocalDateTime.now();
        id = pl.getUniqueId();
        name = pl.getName();
    }

    @Override
    protected void write(Connection c, StatementProvider stmts) throws SQLException {
        try(PreparedStatement p = c.prepareStatement(stmts.get(SQLString.ADD_NEW_PLAYER_LOGIN))) {
            p.setTimestamp(1, Timestamp.valueOf(loginTime));
            p.setString(2, id.toString());
            p.setString(3, name);
            p.execute();
        }
    }

}
