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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class PastOnlinePlayerCountRequest implements Request<LinkedHashMap<LocalDateTime, Integer>> {
    private final Parameters p;
    private JSONObject response;

    public PastOnlinePlayerCountRequest(LocalDateTime start, LocalDateTime end) {
        p = new Parameters(start, end);
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
    public LinkedHashMap<LocalDateTime, Integer> call() throws Exception {
        JSONObject counts = response.getJSONObject("counts");
        LinkedHashMap<LocalDateTime, Integer> ret = new LinkedHashMap<>();

        for(String s : counts.keySet()) {
            ret.put(LocalDateTime.parse(s), counts.getInt(s));
        }

        return ret;
    }

    @Override
    public void setRequestID(long requestID) {
        p.setRequestID(requestID);
    }

    public static class Parameters {
        private long requestID;
        private final LocalDateTime start, end;

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        private Parameters(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        public long getRequestID() {
            return requestID;
        }

        public void setRequestID(long requestID) {
            this.requestID = requestID;
        }

        public JSONObject toJSON() {
            JSONObject ret = new JSONObject();
            ret.put("id", requestID);
            ret.put("type", RequestType.PAST_ONLINE_PLAYER_COUNT);
            ret.put("start", start.toString());
            ret.put("end", end.toString());
            return ret;
        }

        public static Parameters fromJSON(JSONObject o) {
            long requestID = o.getLong("id");
            LocalDateTime start = LocalDateTime.parse(o.getString("start"));
            LocalDateTime end = LocalDateTime.parse(o.getString("end"));
            Parameters p = new Parameters(start, end);
            p.requestID = requestID;
            return p;
        }
    }
}
