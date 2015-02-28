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
package net.jmhertlein.mcanalytics.api.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.FutureRequest;
import net.jmhertlein.mcanalytics.api.Request;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joshua
 */
public class RequestsTest {

    public RequestsTest() {
    }

    @Test
    public void testRequest() throws IOException {
        Runnable serverThread = () -> {
            System.out.println("Server thread is going!");
            ServerSocket listen;

            try {
                listen = new ServerSocket(11223);
            } catch(IOException ex) {
                Logger.getLogger(RequestsTest.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }

            Socket server;
            ObjectOutputStream out;
            ObjectInputStream in;

            try {
                System.out.println("Server running accept()...");
                server = listen.accept();
                System.out.println("Server got client.");
                out = new ObjectOutputStream(server.getOutputStream());
                in = new ObjectInputStream(server.getInputStream());
                System.out.println("Server listening!");
                for(;;) {
                    System.out.println("SERVER: " + in.readUTF());
                    out.writeUTF("{\"response_to\": 0,\"msg\": \"cash money\"}");
                    out.flush();
                    System.out.println("SERVER: Responded to client.");
                }
            } catch(IOException ex) {
                Logger.getLogger(RequestsTest.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        };

        Thread t = new Thread(serverThread);
        t.start();

        Socket sock;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        boolean running = true;
        do {
            try {
                sock = new Socket("localhost", 11223);
                out = new ObjectOutputStream(sock.getOutputStream());
                in = new ObjectInputStream(sock.getInputStream());
            } catch(IOException ex) {
                ex.printStackTrace();
                running = false;
            }
        } while(!running);

        APISocket s = new APISocket(out, in);
        s.startListener();

        FutureRequest<Boolean> f = s.submit(new Request<Boolean>() {
            private JSONObject resp;

            @Override
            public String toJSON() {
                return "msg: \"hello world\"";
            }

            @Override
            public void setResponse(JSONObject json) {
                System.out.println("CLIENT: Setting resonse to " + json.toString());
                resp = json;
            }

            @Override
            public Boolean call() throws Exception {
                System.out.println("CLIENT: Server says this: " + resp.getString("msg"));
                return true;
            }
        });

        System.out.println("Time to wait!");
        boolean ran = false;
        try {
            ran = f.get();
        } catch(InterruptedException ex) {
            Logger.getLogger(RequestsTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch(ExecutionException ex) {
            Logger.getLogger(RequestsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        Assert.assertTrue(ran);
    }
}
