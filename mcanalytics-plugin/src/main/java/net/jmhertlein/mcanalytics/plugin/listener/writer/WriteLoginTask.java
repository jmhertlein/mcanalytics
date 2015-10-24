/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mcanalytics.plugin.listener.writer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class WriteLoginTask extends WriteTask {
    private final LocalDateTime loginTime;
    private final UUID id;
    private final String name;

    public WriteLoginTask(Player pl, MCAnalyticsPlugin p) {
        super(p);
        loginTime = LocalDateTime.now();
        id = pl.getUniqueId();
        name = pl.getName();
    }

    @Override
    protected void write(Connection c, StatementProvider stmts) throws SQLException {
        try(PreparedStatement ps = c.prepareStatement(stmts.get(SQLString.ADD_LOGIN))) {
            ps.setTimestamp(1, Timestamp.valueOf(loginTime));
            ps.setString(2, id.toString());
            ps.setString(3, name);
            ps.execute();
        }
    }
    
}
