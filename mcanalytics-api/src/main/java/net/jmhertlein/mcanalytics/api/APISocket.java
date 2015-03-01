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

import java.io.EOFException;
import net.jmhertlein.mcanalytics.api.request.Request;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mcanalytics.api.request.PastOnlinePlayerCountRequest;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class APISocket {
    private final Map<Long, FutureRequest<?>> requests;
    private long nextID;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final ExecutorService workers;

    public APISocket(ObjectOutputStream out, ObjectInputStream in) {
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

        System.out.println("CLIENT: Writing...");
        out.writeUTF(r.toJSON());
        out.flush();
        System.out.println("CLIENT: Written!");

        return r;
    }

    public void startListener() {
        workers.submit(() -> listen());
    }

    public void listen() {
        for(;;) {
            JSONObject read;
            try {
                read = new JSONObject(in.readUTF());
                System.out.println("CLIENT: APISocket read some input: " + read.toString());
            } catch(SocketException | EOFException ex) {
                System.out.println("Socket closed (" + ex.getLocalizedMessage() + ").");
                return;
            } catch(IOException ex) {
                Logger.getLogger(APISocket.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }

            if(read.has("response_to")) {
                System.out.println("was a response");
                long id = read.getLong("response_to");
                FutureRequest<?> r = requests.remove(id);
                System.out.print("Got r...");
                r.setResponse(read);
                System.out.println("Set response, started running async.");
                workers.submit(r);
            }
        }
    }

    public static void main(String... args) throws IOException {
        Socket s = new Socket("localhost", 35555);

        ObjectOutputStream out;
        ObjectInputStream in;

        try {
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        } catch(IOException ex) {
            ex.printStackTrace();
            return;
        }

        APISocket api = new APISocket(out, in);

        api.startListener();

        FutureRequest<LinkedHashMap<LocalDateTime, Integer>> result = api.submit(new PastOnlinePlayerCountRequest(LocalDateTime.now().minusDays(10), LocalDateTime.now().plusDays(10)));

        try {
            System.out.println("RESULT IS: ");
            Map<LocalDateTime, Integer> m = result.get();
            for(Map.Entry<LocalDateTime, Integer> e : m.entrySet()) {
                System.out.println(e.getKey().toString() + " | " + e.getValue());
            }
        } catch(InterruptedException | ExecutionException ex) {
            Logger.getLogger(APISocket.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("DONE");
    }
}
