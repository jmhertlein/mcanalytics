/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class UniqueLoginsPerDayRequestHandler extends RequestHandler {

    public UniqueLoginsPerDayRequestHandler(DataSource ds, StatementProvider stmts, RequestDispatcher d, JSONObject req) {
        super(ds, stmts, d, req);
    }

    @Override
    public JSONObject handle(Connection conn, StatementProvider stmts, JSONObject request, ClientMonitor client) throws Exception {
        PreparedStatement stmt = conn.prepareStatement(stmts.get(SQLString.GET_UNIQUE_LOGINS));

        stmt.clearParameters();
        stmt.setTimestamp(1, Timestamp.valueOf(LocalDate.parse(request.getString("start")).atStartOfDay()));
        stmt.setTimestamp(2, Timestamp.valueOf(LocalDate.parse(request.getString("end")).plusDays(1).atStartOfDay()));
        ResultSet res = stmt.executeQuery();

        Map<String, Integer> counts = new HashMap<>();
        while(res.next()) {
           counts.put(res.getTimestamp("login_day").toLocalDateTime().toLocalDate().toString(), res.getInt("login_count"));
        }

        JSONObject ret = new JSONObject();
        ret.put("logins", counts);
        res.close();
        stmt.close();
        return ret;
    }
    
}
