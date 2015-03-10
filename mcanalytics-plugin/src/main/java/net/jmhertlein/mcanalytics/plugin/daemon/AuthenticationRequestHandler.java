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

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.auth.AuthenticationMethod;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
    public JSONObject handle(Connection conn, StatementProvider stmts, JSONObject request, ClientMonitor c) throws Exception {
        JSONObject resp = new JSONObject();
        boolean success = false;

        AuthenticationMethod m = AuthenticationMethod.valueOf(request.getString("method"));
        String username = request.getString("username");

        if(m == AuthenticationMethod.PASSWORD) {
            success = authenticateWithPassword(request, conn, stmts, username);
        } else if(m == AuthenticationMethod.TRUST) {
            success = Stream.of(c.getSocket().getSession().getPeerCertificates())
                    .map(crt -> getCNs((X509Certificate) crt))
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

        byte[] computedHash = hash(password, salt);

        return Arrays.equals(storedHash, computedHash);
    }

    /**
     * Gets the common names of the subject of an X509Certificate
     *
     * based on:
     * https://stackoverflow.com/questions/2914521/how-to-extract-cn-from-x509certificate-in-java
     *
     * Also note CN is indeed a multi-valued attribute:
     * https://tools.ietf.org/html/rfc4519#section-2.3
     *
     * I'm pretty sure the outer loop will return only one RDN, but the inner loop can return many.
     *
     * @param cert
     * @return a list of all CNs, or an empty list if the certificate's encoding is invalid
     */
    private static Set<String> getCNs(X509Certificate cert) {
        Set<String> names = new HashSet<>();
        X500Name x500name;
        try {
            x500name = new JcaX509CertificateHolder(cert).getSubject();
        } catch(CertificateEncodingException cee) {
            return names;
        }

        for(RDN rdn : x500name.getRDNs(BCStyle.CN)) {
            for(AttributeTypeAndValue atv : rdn.getTypesAndValues()) {
                names.add(IETFUtils.valueToString(atv.getValue()));
            }
        }

        return names;
    }

    private static byte[] hash(byte[] pass, byte[] salt) throws NoSuchAlgorithmException, NoSuchProviderException {
        final int PASSES = 10000;
        MessageDigest mda = MessageDigest.getInstance("SHA-512", "BC");
        mda.update(salt);
        mda.update(pass);
        byte[] hash = mda.digest();

        for(int i = 0; i < PASSES; i++) {
            mda.update(hash);
            mda.update(salt);
            mda.update(pass);
            hash = mda.digest();
        }

        return hash;
    }

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        Random gen = new SecureRandom();
        byte[] salt = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x01, 0x02, 0x03};
        byte[] pass;
        try {
            pass = "hello world this is my pass".getBytes("UTF-8");
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(AuthenticationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        try {
            byte[] hash = hash(pass, salt);
            System.out.println(Base64.encode(hash));
        } catch(NoSuchAlgorithmException ex) {
            Logger.getLogger(AuthenticationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch(NoSuchProviderException ex) {
            Logger.getLogger(AuthenticationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
