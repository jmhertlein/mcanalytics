/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mcanalytics.api.request;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class UniqueLoginsPerDayRequest extends Request<Map<LocalDate, Integer>> {
    private final LocalDate start, end;

    public UniqueLoginsPerDayRequest(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }
    
    @Override
    public Map<LocalDate, Integer> processResponse(JSONObject response) {
        JSONObject counts = response.getJSONObject("logins");
        LinkedHashMap<LocalDate, Integer> ret = new LinkedHashMap<>();
        for(String s : counts.keySet()) {
            ret.put(LocalDate.parse(s), counts.getInt(s));
        }

        return ret;
    }

    @Override
    public String toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("id", getRequestId());
        ret.put("type", RequestType.UNIQUE_LOGINS_PER_DAY);
        ret.put("start", start.toString());
        ret.put("end", end.toString());
        return ret.toString();
    }
    
}
