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
package net.jmhertlein.mcanalytics.plugin.daemon;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class ClientMonitor implements Runnable {
    private final Socket client;
    private final ExecutorService workers;
    private final RequestDispatcher dispatcher;

    public ClientMonitor(DataSource connections, ExecutorService workers, Socket client) {
        this.client = client;
        this.workers = workers;
        dispatcher = new RequestDispatcher(connections, workers);
    }

    @Override
    public void run() {
        ObjectOutputStream out;
        ObjectInputStream in;
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
        } catch(IOException ex) {
            Logger.getLogger(ClientMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        workers.submit(() -> read(in));
        workers.submit(() -> write(out));
    }

    private void write(ObjectOutputStream out) {
        ConcurrentLinkedQueue<JSONObject> queue = dispatcher.getWriteQueue();
        for(;;) {
            if(queue.isEmpty()) {
                synchronized(queue) {
                    try {
                        queue.wait();
                    } catch(InterruptedException ex) {
                    }
                }
            } else {
                try {
                    out.writeUTF(queue.remove().toString());
                    out.flush();
                } catch(IOException ex) {
                    Logger.getLogger(ClientMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void read(ObjectInputStream in) {
        for(;;) {
            JSONObject o;
            try {
                String s = in.readUTF();
                o = new JSONObject(s);
            } catch(SocketException | EOFException se) {
                return;
            } catch(IOException ex) {
                Logger.getLogger(ClientMonitor.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            } catch(Throwable t) {
                t.printStackTrace();
                System.out.println(t.getLocalizedMessage());
                continue;
            }
            dispatcher.submitJob(o);
        }
    }

}
