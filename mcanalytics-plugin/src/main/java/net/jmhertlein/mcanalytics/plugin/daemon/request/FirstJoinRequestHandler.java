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
package net.jmhertlein.mcanalytics.plugin.daemon.request;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import net.jmhertlein.mcanalytics.plugin.daemon.ClientMonitor;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class FirstJoinRequestHandler extends RequestHandler {

    public FirstJoinRequestHandler(DataSource ds, StatementProvider stmts, RequestDispatcher d, JSONObject req) {
        super(ds, stmts, d, req);
    }

    @Override
    public JSONObject handle(Connection conn, StatementProvider stmts, JSONObject request, ClientMonitor client) throws Exception {
        //System.out.println("Handler: starting...");
        PreparedStatement stmt = conn.prepareStatement(stmts.get(SQLString.GET_NEW_PLAYER_LOGINS_HOURLY));

        stmt.clearParameters();
        stmt.setTimestamp(1, Timestamp.valueOf(LocalDate.parse(request.getString("start")).atStartOfDay()));
        stmt.setTimestamp(2, Timestamp.valueOf(LocalDate.parse(request.getString("end")).plusDays(1).atStartOfDay()));
        ResultSet res = stmt.executeQuery();

        Map<String, Integer> counts = new HashMap<>();
        while(res.next()) {
            counts.put(res.getTimestamp("hour_joined").toLocalDateTime().toString(), res.getInt("login_count"));
        }

        JSONObject ret = new JSONObject();
        ret.put("first_login_counts", counts);
        res.close();
        stmt.close();
        //System.out.println("Handler: done, returning.");
        return ret;
    }

}
