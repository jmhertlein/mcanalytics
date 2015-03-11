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
 *
 * @author joshua
 */
public class AuthenticationRequest extends Request<Boolean> {
    private AuthenticationMethod m;
    private String password, username;

    public AuthenticationRequest(AuthenticationMethod m, String username, String password) {
        this.m = m;
        this.password = password;
        this.username = username;
    }

    @Override
    public Boolean call() throws Exception {
        return response.getString("status").equals("OK");
    }

    @Override
    public String toJSON() {
        JSONObject o = new JSONObject();

        o.put("type", RequestType.AUTHENTICATION_REQUEST.toString());
        o.put("id", requestId);
        o.put("method", m.name());
        o.put("username", username);
        if(m == AuthenticationMethod.PASSWORD)
            o.put("password", password);

        return o.toString();
    }
}
