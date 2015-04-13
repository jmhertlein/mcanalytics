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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class NewPlayerLoginsRequest extends Request<LinkedHashMap<LocalDateTime, Integer>> {
    private final LocalDate start, end;

    public NewPlayerLoginsRequest(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("id", getRequestId());
        ret.put("type", RequestType.NEW_PLAYER_LOGINS);
        ret.put("start", start.toString());
        ret.put("end", end.toString());
        return ret.toString();
    }

    @Override
    public LinkedHashMap<LocalDateTime, Integer> processResponse(JSONObject response) {
        JSONObject counts = response.getJSONObject("first_login_counts");
        LinkedHashMap<LocalDateTime, Integer> ret = new LinkedHashMap<>();

        for(String s : counts.keySet()) {
            ret.put(LocalDateTime.parse(s), counts.getInt(s));
        }

        return ret;
    }
}
