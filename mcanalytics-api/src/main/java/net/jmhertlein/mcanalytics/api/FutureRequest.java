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
package net.jmhertlein.mcanalytics.api;

import net.jmhertlein.mcanalytics.api.request.Request;
import java.util.concurrent.FutureTask;
import org.json.JSONObject;

/**
 * FutureRequest is a wrapper around a Request that provides a way to wait for the request to be
 * fulfilled by the server. When a Request is submitted to the APISocket, it will wrap it in a
 * FutureRequest. Client code can then wait on this object with the get() method (like the usual
 * Future interface) which will block until the request is completed.
 *
 * @see net.jmhertlein.mcanalytics.api.APISocket
 * @see net.jmhertlein.mcanalytics.api.request.Request
 * @author joshua
 */
public class FutureRequest<T> extends FutureTask<T> {
    private long requestID;
    private final Request<T> request;

    public FutureRequest(Request<T> p) {
        super(p);
        this.request = p;
    }

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        request.setRequestID(requestID);
    }

    public String toJSON() {
        return request.toJSON();
    }

    public void setResponse(JSONObject o) {
        request.setResponse(o);
    }

}
