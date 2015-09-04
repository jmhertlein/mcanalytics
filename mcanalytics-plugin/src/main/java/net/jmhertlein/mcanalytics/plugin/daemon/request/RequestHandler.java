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
package net.jmhertlein.mcanalytics.plugin.daemon.request;

import java.sql.Connection;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import net.jmhertlein.mcanalytics.plugin.daemon.ClientMonitor;
import net.jmhertlein.mcanalytics.plugin.daemon.NotAuthenticatedException;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public abstract class RequestHandler implements Runnable {
    private final RequestDispatcher dispatcher;
    private final JSONObject req;
    private final DataSource ds;
    private final StatementProvider stmts;

    public RequestHandler(DataSource ds, StatementProvider stmts, RequestDispatcher d, JSONObject req) {
        this.dispatcher = d;
        this.req = req;
        this.ds = ds;
        this.stmts = stmts;
    }

    private long getResponseID() {
        return req.getLong("id");
    }

    /**
     * Generates the response JSON object for the request this object handles
     *
     * A note to implementations: as a general rule, any object you create, *you* are responsible
     * for destroying/closing/cleaning up. Any object passed in, you do *not* need to clean up or
     * close- it will be closed by the caller.
     *
     * Following from this, you do not need to close the Connection object this method is passed.
     * You *DO* need to close any Statements and ResultSets that you create!
     *
     * @param conn a connection to the database.
     * @param stmts SQL statements for the DBMS in use
     * @param request the JSON object received from the client
     * @param client the client's monitor object
     * @return the JSON object to be returned to the client
     * @throws Exception
     */
    public abstract JSONObject handle(Connection conn, StatementProvider stmts, JSONObject request, ClientMonitor client) throws Exception;

    public boolean needsAuth() {
        return true;
    }

    @Override
    public final void run() {
        JSONObject o;
        try(Connection conn = ds.getConnection()) {
            //System.out.println("Handler method being called!");
            checkAuthentication();
            o = handle(conn, stmts, req, dispatcher.getClient());
            //System.out.println("Handler method returned.");
            o.put("status", "OK");
        } catch(Exception e) {
            e.printStackTrace(System.err);
            o = new JSONObject();
            o.put("status", "ERROR");
            o.put("status_msg", e.getLocalizedMessage());
        }

        o.put("response_to", getResponseID());
        //System.out.println("Queueing the response from handler.");
        dispatcher.queueResponse(o);
    }

    private void checkAuthentication() throws NotAuthenticatedException {
        if(needsAuth() && !dispatcher.getClient().isAuthenticated())
            throw new NotAuthenticatedException();
    }

}
