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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.request.RequestType;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import net.jmhertlein.mcanalytics.plugin.daemon.ClientMonitor;
import org.json.JSONObject;

/**
 *
 * @author joshua
 */
public class RequestDispatcher {
    private final ExecutorService workers;
    private final ConcurrentLinkedQueue<JSONObject> writeQueue;
    private final DataSource connections;
    private final StatementProvider stmts;
    private final ClientMonitor client;

    public RequestDispatcher(ClientMonitor owner, DataSource connections, StatementProvider stmts, ExecutorService workers) {
        this.client = owner;
        this.workers = workers;
        writeQueue = new ConcurrentLinkedQueue<>();
        this.connections = connections;
        this.stmts = stmts;
    }

    public void submitJob(JSONObject job) {
        System.out.println("Dispatcher is passing job to workers...");
        workers.submit(getHandlerForRequestJSON(job));
    }

    public void queueResponse(JSONObject o) {
        System.out.println("DISPATCHER: queueing response.");
        writeQueue.add(o);
        synchronized(writeQueue) {
            System.out.println("DISPATCHER: notifying writer.");
            writeQueue.notifyAll();
        }
    }

    public ConcurrentLinkedQueue<JSONObject> getWriteQueue() {
        return writeQueue;
    }

    public ClientMonitor getClient() {
        return client;
    }

    private Runnable getHandlerForRequestJSON(JSONObject job) {
        RequestType type = RequestType.valueOf(job.getString("type"));
        Runnable ret;
        switch(type) {
            case ONLINE_PLAYER_COUNT:
                ret = new OnlinePlayerCountRequestHandler(connections, stmts, this, job);
                break;
            case PAST_ONLINE_PLAYER_COUNT:
                ret = new PastOnlinePlayerCountRequestHandler(connections, stmts, this, job);
                System.out.println("Job is a PastOnlinePlayerCountRequest");
                break;
            case AUTHENTICATION_REQUEST:
                ret = new AuthenticationRequestHandler(connections, stmts, this, job);
                break;
            case PASSWORD_RESET_REQUEST:
                ret = new PasswordResetRequestHandler(connections, stmts, this, job);
                break;
            default:
                ret = null;
                System.out.println("INVALID JOB");
        }

        return ret;
    }
}