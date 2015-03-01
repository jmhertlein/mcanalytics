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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class ConsoleDaemon {
    private static final int PORT = 35555;
    private final ExecutorService workers;
    private ServerSocket s;

    public ConsoleDaemon() {
        workers = Executors.newCachedThreadPool();
    }

    public void startListening() {
        workers.submit(() -> listen());
    }

    public void listen() {
        try {
            s = new ServerSocket(PORT);
        } catch(IOException ex) {
            Logger.getLogger(ConsoleDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }

        for(;;) {
            Socket client;
            System.out.println("Listening on port " + PORT);
            try {
                client = s.accept();
                System.out.println("Got client.");
            } catch(SocketException ex) {
                System.out.println("Server port listen socket closed: " + ex.getLocalizedMessage());
                return;
            } catch(IOException ex) {
                Logger.getLogger(ConsoleDaemon.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            workers.submit(new ClientMonitor(workers, client));
        }
    }

    public static void main(String... args) {
        ConsoleDaemon d = new ConsoleDaemon();

        d.startListening();
    }
}
