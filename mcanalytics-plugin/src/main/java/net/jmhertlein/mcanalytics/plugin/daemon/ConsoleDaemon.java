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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;

/**
 *
 * @author joshua
 */
public class ConsoleDaemon {
    private static final int PORT = 35555;
    private final ExecutorService workers;
    private SSLServerSocket s;
    private final DataSource connections;
    private final StatementProvider stmts;
    private final KeyStore trustMaterial;

    public ConsoleDaemon(KeyStore trustMaterial, DataSource connections, StatementProvider stmts) {
        workers = Executors.newCachedThreadPool();
        this.connections = connections;
        this.stmts = stmts;
        this.trustMaterial = trustMaterial;
    }

    public void startListening() {
        workers.submit(() -> listen());
    }

    public void listen() {
        SSLContext ctx = SSLUtil.buildContext(trustMaterial);
        try {
            s = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket();
            s.setWantClientAuth(true);
            s.setUseClientMode(false);
            s.bind(new InetSocketAddress(PORT));
        } catch(IOException ex) {
            Logger.getLogger(ConsoleDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }

        for(;;) {
            SSLSocket client;
            try {
                client = (SSLSocket) s.accept();
                client.startHandshake();
            } catch(SSLHandshakeException sslhe) {
                System.err.println("Client dropped: " + sslhe.getLocalizedMessage());
                continue;
            } catch(SocketException ex) {
                System.out.println("Server port listen socket closed: " + ex.getLocalizedMessage());
                return;
            } catch(IOException ex) {
                Logger.getLogger(ConsoleDaemon.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            workers.submit(new ClientMonitor(connections, stmts, workers, client));
        }
    }

    public void shutdown() {
        workers.shutdownNow();
        try {
            s.close();
        } catch(IOException ex) {
            Logger.getLogger(ConsoleDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            System.out.println("Daemon going for shutdown, waiting for workers to die...");
            workers.awaitTermination(30, TimeUnit.SECONDS);
            System.out.println("Timed out or stopped.");
        } catch(InterruptedException ex) {
            Logger.getLogger(ConsoleDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
