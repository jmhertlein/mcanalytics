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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.auth.AuthenticationMethod;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class AuthenticationRequestHandler extends RequestHandler {

    public AuthenticationRequestHandler(DataSource ds, StatementProvider stmts, RequestDispatcher d, JSONObject req) {
        super(ds, stmts, d, req);
    }

    @Override
    public boolean needsAuth() {
        return false;
    }

    @Override
    public JSONObject handle(Connection conn, StatementProvider stmts, JSONObject request, ClientMonitor c) throws Exception {
        JSONObject resp = new JSONObject();
        boolean success = false;

        AuthenticationMethod m = AuthenticationMethod.valueOf(request.getString("method"));
        String username = request.getString("username");

        if(m == AuthenticationMethod.PASSWORD) {
            success = authenticateWithPassword(request, conn, stmts, username);
        } else if(m == AuthenticationMethod.TRUST) {
            success = Stream.of(c.getSocket().getSession().getPeerCertificates())
                    .map(crt -> SSLUtil.getCNs((X509Certificate) crt))
                    .anyMatch(names -> names.contains(username));
        } else {
            throw new Exception("Invalid authentication method.");
        }

        if(!success) {
            throw new AuthenticationException();
        }
        return resp;
    }

    private boolean authenticateWithPassword(JSONObject request, Connection conn, StatementProvider stmts, String username) throws SQLException, NoSuchProviderException, JSONException, UnsupportedEncodingException, NoSuchAlgorithmException, AuthenticationException {
        byte[] password = request.getString("password").getBytes("UTF-8");
        byte[] storedHash;
        byte[] salt;
        try(PreparedStatement getHashSalt = conn.prepareStatement(stmts.get(SQLString.GET_HASHSALT_FOR_USER))) {
            getHashSalt.setString(1, username);
            try(ResultSet res = getHashSalt.executeQuery()) {
                if(!res.first())
                    return false;
                storedHash = res.getBytes("password_hash");
                salt = res.getBytes("salt");
            }
        }

        byte[] computedHash = SSLUtil.hash(password, salt);

        return Arrays.equals(storedHash, computedHash);
    }
}
