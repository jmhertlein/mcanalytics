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

import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class OnlinePlayerCountRequest implements Request<Integer> {
    private final Parameters p;
    private JSONObject response;

    public OnlinePlayerCountRequest() {
        p = new Parameters();
    }

    @Override
    public String toJSON() {
        return p.toJSON().toString();
    }

    @Override
    public void setResponse(JSONObject json) {
        response = json;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("call() was called in OnlinePlayerCountRequest");
        return response.getInt("count");
    }

    @Override
    public void setRequestID(long requestID) {
        p.setRequestID(requestID);
    }

    public static class Parameters {
        private long requestID;

        public long getRequestID() {
            return requestID;
        }

        public void setRequestID(long requestID) {
            this.requestID = requestID;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            ret.put("id", requestID);
            ret.put("type", RequestType.ONLINE_PLAYER_COUNT);
            return ret;
        }

        public static Parameters fromJSON(JSONObject o) {
            Parameters p = new Parameters();
            p.requestID = o.getLong("id");
            return p;
        }
    }

}
