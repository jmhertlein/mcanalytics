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

import java.util.concurrent.Callable;
import org.json.JSONObject;

/**
 * Request represents a request that the client might make of the server.
 *
 * This represents a base class that handles getting request IDs and calling the processResponse
 * method when the server's response to it comes back.
 *
 * The main methods of interest to implementations are:
 *
 * toJSON()- should return a JSON string that the server will know how to reply to.
 *
 * processResponse(JSONObject)- the callback method that will be passed the server's parsed JSON
 * response string. This allows client-side processing to be done to transform the JSON input into
 * more pleasant Java data structures
 *
 * @author joshua
 * @param <T> The type of the Java object that this request will return
 */
public abstract class Request<T> implements Callable<T> {
    private JSONObject response;
    private long requestId;

    public void setResponse(JSONObject json) {
        response = json;
    }

    @Override
    public final T call() {
        return processResponse(response);
    }

    /**
     * Performs client-side post-processing/transformation of the returned JSON data
     *
     * @param response the server's response to what toString() generated
     * @return the Java object that this Request is contracted to return
     */
    public abstract T processResponse(JSONObject response);

    /**
     *
     * @return the raw JSON string representing this request plus any parameters, to send to the
     * server
     */
    public abstract String toJSON();

    public long getRequestId() {
        return requestId;
    }

    public void setRequestID(long requestID) {
        this.requestId = requestID;
    }
}
