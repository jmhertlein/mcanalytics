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

import net.jmhertlein.mcanalytics.api.auth.AuthenticationMethod;
import org.json.JSONObject;

/**
 * Request to authenticate the client to the server.
 *
 * The request can try to either use a password, or try logging in with a certificate to do
 * password-less login.
 *
 * @author joshua
 */
public class AuthenticationRequest extends Request<Boolean> {
    private final AuthenticationMethod m;
    private final String password, username;

    public AuthenticationRequest(AuthenticationMethod m, String username, String password) {
        this.m = m;
        this.password = password;
        this.username = username;
    }

    @Override
    public Boolean processResponse(JSONObject response) {
        return response.getString("status").equals("OK");
    }

    @Override
    public String toJSON() {
        JSONObject o = new JSONObject();

        o.put("type", RequestType.AUTHENTICATION.toString());
        o.put("id", getRequestId());
        o.put("method", m.name());
        o.put("username", username);
        if(m == AuthenticationMethod.PASSWORD)
            o.put("password", password);

        return o.toString();
    }
}
