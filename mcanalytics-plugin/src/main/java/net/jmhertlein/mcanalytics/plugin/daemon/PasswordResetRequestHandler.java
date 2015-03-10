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
import java.util.Arrays;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.json.JSONObject;

public class PasswordResetRequestHandler extends RequestHandler {

    public PasswordResetRequestHandler(DataSource ds, StatementProvider stmts, RequestDispatcher d, JSONObject req) {
        super(ds, stmts, d, req);
    }

    @Override
    public JSONObject handle(Connection conn, StatementProvider stmts, JSONObject request, ClientMonitor client) throws Exception {
        //request params
        String username, oldPass, newPass;
        username = request.getString("username");
        oldPass = request.getString("old");
        newPass = request.getString("new");

        //convert params to bytes
        byte[] oldBytes = oldPass.getBytes("UTF-8"), newBytes = newPass.getBytes("UTF-8");
        byte[] oldHash, salt;

        //retrieve hash and salt from db
        try(PreparedStatement getHashSalt = conn.prepareStatement(stmts.get(SQLString.GET_HASHSALT_FOR_USER))) {
            getHashSalt.setString(1, username);
            try(ResultSet res = getHashSalt.executeQuery()) {
                if(!res.first()) {
                    throw new AuthenticationException();
                }
                oldHash = res.getBytes("password_hash");
                salt = res.getBytes("salt");
            }
        }

        byte[] oldHashComputed = SSLUtil.hash(oldBytes, salt);

        if(!Arrays.equals(oldHash, oldHashComputed)) {
            throw new AuthenticationException();
        }

        byte[] newHash = SSLUtil.hash(newBytes, salt);

        try(PreparedStatement updateHash = conn.prepareStatement(stmts.get(SQLString.UPDATE_PLAYER_PASSWORD))) {
            updateHash.setBytes(1, newHash);
            updateHash.setString(2, username);
            updateHash.executeUpdate();
        }

        return new JSONObject();
    }

}
