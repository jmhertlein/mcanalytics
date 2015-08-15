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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.auth.AuthenticationMethod;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import net.jmhertlein.mcanalytics.plugin.daemon.AuthenticationException;
import net.jmhertlein.mcanalytics.plugin.daemon.ClientMonitor;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class AuthenticationRequestHandler extends RequestHandler {
    private final PrivateKey serverKey;
    private final X509Certificate serverCert;

    public AuthenticationRequestHandler(PrivateKey serverKey, X509Certificate serverCert, DataSource ds, StatementProvider stmts, RequestDispatcher d, JSONObject req) {
        super(ds, stmts, d, req);

        this.serverKey = serverKey;
        this.serverCert = serverCert;
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

        System.out.println("Auth method is " + m.name());

        if(m == AuthenticationMethod.PASSWORD) {
            success = authenticateWithPassword(request, conn, stmts, username);
            if(success && request.has("remember") && request.getBoolean("remember")) {
                success &= signClientKey(request, resp);
            }
        } else if(m == AuthenticationMethod.TRUST) {
            success = SSLUtil.getCNs((X509Certificate) c.getSocket().getSession().getPeerCertificates()[0]).contains(username);
            System.out.println("Username is " + username + " and CNs in received certs are:");
            SSLUtil.getCNs((X509Certificate) c.getSocket().getSession().getPeerCertificates()[0]).stream().forEach(s -> System.out.println(s));
        } else {
            throw new Exception("Invalid authentication method.");
        }

        if(success) {
            c.setUsername(username);
            c.setAuthenticated(true);
        } else {
            c.setUsername(null);
            c.setAuthenticated(false);
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
                if(!res.next())
                    return false;
                storedHash = Base64.decodeBase64(res.getString("password_hash"));
                salt = Base64.decodeBase64(res.getString("salt"));
            }
        }

        byte[] computedHash = SSLUtil.hash(password, salt);

        return Arrays.equals(storedHash, computedHash);
    }

    private boolean signClientKey(JSONObject request, JSONObject response) {
        String username = request.getString("username");
        PKCS10CertificationRequest csr;
        try {
            csr = new PKCS10CertificationRequest(Base64.decodeBase64(request.getString("csr")));
        } catch(IOException ex) {
            return false;
        }

        Set<String> names = SSLUtil.getCNs(csr.getSubject());
        if(names.size() == 1 && names.contains(username)) {
            X509Certificate clientCert = SSLUtil.fulfillCertRequest(serverKey, serverCert, csr, false);
            try {
                response.put("cert", Base64.encodeBase64String(clientCert.getEncoded()));
                response.put("ca", Base64.encodeBase64String(serverCert.getEncoded()));
                return true;
            } catch(CertificateEncodingException ex) {
                Logger.getLogger(AuthenticationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else {
            return false;
        }
    }
}
