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

import java.io.BufferedReader;
import java.io.EOFException;
import net.jmhertlein.mcanalytics.api.request.Request;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class APISocket {
    private final Map<Long, FutureRequest<?>> requests;
    private long nextID;
    private final PrintWriter out;
    private final BufferedReader in;
    private final ExecutorService workers;

    public APISocket(PrintWriter out, BufferedReader in) {
        nextID = 0;
        requests = new ConcurrentHashMap<>();
        this.out = out;
        this.in = in;
        workers = Executors.newCachedThreadPool();
    }

    public <T> FutureRequest<T> submit(Request<T> d) throws IOException {
        FutureRequest<T> r = new FutureRequest<>(d);
        r.setRequestID(nextID);
        requests.put(nextID, r);
        nextID++;

        out.println(r.toJSON());
        out.flush();

        return r;
    }

    public void startListener() {
        workers.submit(() -> listen());
    }

    public void listen() {
        for(;;) {
            JSONObject read;
            try {
                String readStr = in.readLine();
                if(readStr == null) {
                    System.out.println("READ: exiting due to suspected socket closure.");
                    return;
                }
                read = new JSONObject(readStr);
            } catch(SocketException | EOFException ex) {
                System.out.println("Socket closed (" + ex.getLocalizedMessage() + ").");
                return;
            } catch(IOException ex) {
                Logger.getLogger(APISocket.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }

            if(read.has("response_to")) {
                long id = read.getLong("response_to");
                FutureRequest<?> r = requests.remove(id);
                r.setResponse(read);
                workers.submit(r);
            }
        }
    }

    public void shutdown() {
        workers.shutdownNow();
        out.close();
        try {
            in.close();
        } catch(IOException ex) {
            Logger.getLogger(APISocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
