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
package net.jmhertlein.mcanalytics.plugin.daemon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.request.PastOnlinePlayerCountRequest.Parameters;
import net.jmhertlein.mcanalytics.plugin.Statements;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class PastOnlinePlayerCountRequestHandler extends RequestHandler {
    private final DataSource connections;

    public PastOnlinePlayerCountRequestHandler(DataSource connections, RequestDispatcher d, JSONObject req) {
        super(d, req);
        this.connections = connections;
    }

    @Override
    public JSONObject handle(JSONObject req) throws SQLException {
        Connection conn = connections.getConnection();
        PreparedStatement stmt = conn.prepareStatement(Statements.GET_HOURLY_PLAYER_COUNTS.toString());

        Parameters args = Parameters.fromJSON(req);

        stmt.clearParameters();
        stmt.setTimestamp(1, Timestamp.valueOf(args.getStart()));
        stmt.setTimestamp(2, Timestamp.valueOf(args.getEnd()));
        ResultSet res = stmt.executeQuery();

        Map<String, Integer> counts = new HashMap<>();
        while(res.next()) {
            counts.put(res.getTimestamp("instant").toLocalDateTime().toString(), res.getInt("count"));
        }

        JSONObject ret = new JSONObject();
        ret.put("counts", counts);
        res.close();
        stmt.close();
        conn.close();

        return ret;
    }
}
