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
package net.jmhertlein.mcanalytics.api.request;

import org.apache.commons.codec.binary.Base64;
import java.io.IOException;
import java.security.cert.X509Certificate;
import net.jmhertlein.mcanalytics.api.auth.AuthenticationMethod;
import net.jmhertlein.mcanalytics.api.auth.AuthenticationResult;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.json.JSONObject;

/**
 * Request to authenticate the client to the server.
 *
 * The request can try to either use a password, or try logging in with a certificate to do
 * password-less login.
 *
 * @author joshua
 */
public class AuthenticationRequest extends Request<AuthenticationResult> {
    private final AuthenticationMethod m;
    private final String password, username;
    private final boolean requestRemember;
    private final PKCS10CertificationRequest csr;

    public AuthenticationRequest(String username, String password, PKCS10CertificationRequest csr) {
        this.m = AuthenticationMethod.PASSWORD;
        this.password = password;
        this.username = username;
        this.requestRemember = true;
        this.csr = csr;
    }

    public AuthenticationRequest(String username, String password) {
        this.m = AuthenticationMethod.PASSWORD;
        this.password = password;
        this.username = username;
        this.requestRemember = false;
        this.csr = null;
    }

    public AuthenticationRequest(String username) {
        this.username = username;
        this.m = AuthenticationMethod.TRUST;
        this.password = null;
        this.requestRemember = false;
        this.csr = null;
    }

    @Override
    public AuthenticationResult processResponse(JSONObject response) {
        Boolean success = response.getString("status").equals("OK");

        X509Certificate cert, ca;
        if(response.has("cert")) {
            cert = SSLUtil.certFromBase64(response.getString("cert"));
        } else {
            cert = null;
        }

        if(response.has("ca")) {
            ca = SSLUtil.certFromBase64(response.getString("ca"));
        } else {
            ca = null;
        }

        return new AuthenticationResult(cert, ca, success);
    }

    @Override
    public String toJSON() {
        JSONObject o = new JSONObject();

        o.put("type", RequestType.AUTHENTICATION.toString());
        o.put("id", getRequestId());
        o.put("method", m.name());
        o.put("username", username);
        if(m == AuthenticationMethod.PASSWORD) {
            o.put("password", password);
            if(requestRemember) {
                o.put("remember", true);
                try {
                    o.put("csr", Base64.encodeBase64String(csr.getEncoded()));
                } catch(IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return o.toString();
    }
}
