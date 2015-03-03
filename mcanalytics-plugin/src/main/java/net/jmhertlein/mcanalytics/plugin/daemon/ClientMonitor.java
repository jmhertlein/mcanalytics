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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.bukkit.Bukkit;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class ClientMonitor implements Runnable {
    private final Socket client;
    private final Semaphore shutdownGuard;
    private volatile boolean shutdown;
    private final ExecutorService workers;
    private final RequestDispatcher dispatcher;
    private PrintWriter out;
    private BufferedReader in;

    public ClientMonitor(DataSource connections, StatementProvider stmts, ExecutorService workers, Socket client) {
        this.client = client;
        this.workers = workers;
        dispatcher = new RequestDispatcher(connections, stmts, workers);
        shutdown = false;
        shutdownGuard = new Semaphore(1);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(client.getOutputStream());
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch(IOException ex) {
            Logger.getLogger(ClientMonitor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        workers.submit(() -> read(in));
        workers.submit(() -> write(out));
    }

    private void write(PrintWriter out) {
        ConcurrentLinkedQueue<JSONObject> queue = dispatcher.getWriteQueue();
        while(!shutdown) {
            if(queue.isEmpty()) {
                synchronized(queue) {
                    try {
                        queue.wait();
                    } catch(InterruptedException ex) {
                    }
                }
            } else {
                out.println(queue.remove().toString());
                out.flush();
            }
        }

        System.out.println("CL-WRITE: Exiting, turning off the lights...");
        try {
            close();
        } catch(IOException ex) {
            Logger.getLogger(ClientMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void read(BufferedReader in) {
        while(!shutdown) {
            JSONObject o;
            try {
                String s = in.readLine();
                if(s == null) {
                    System.out.println("CL-READ: Exited due to suspected socket disconnect, turning off the lights....");
                    close();
                    return;
                }
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

    public void close() throws IOException {
        try {
            shutdownGuard.acquire();
        } catch(InterruptedException ex) {
            Logger.getLogger(ClientMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(!shutdown) {
            shutdown = true;
            out.close();
            in.close();
            client.close();
            ConcurrentLinkedQueue<JSONObject> writeQueue = this.dispatcher.getWriteQueue();
            synchronized(writeQueue) {
                writeQueue.notifyAll();
            }
            System.out.println("Client: All closed!");
        }
        shutdownGuard.release();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            System.out.println("Client got gc'd!");
        } finally {
            super.finalize();
        }
    }

}
