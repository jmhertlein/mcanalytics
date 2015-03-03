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
package net.jmhertlein.mcanalytics.plugin;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.plugin.daemon.ConsoleDaemon;
import net.jmhertlein.mcanalytics.plugin.listener.PlayerListener;
import net.jmhertlein.mcanalytics.plugin.listener.WritePlayerCountTask;
import net.jmhertlein.reflective.TreeCommandExecutor;
import net.jmhertlein.reflective.TreeTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.postgresql.ds.PGPoolingDataSource;

/**
 *
 * @author joshua
 */
public class MCAnalyticsPlugin extends JavaPlugin {
    private DataSource connections;
    private ScheduledExecutorService cron;
    private ConsoleDaemon d;
    private StatementProvider stmts;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        connectToDatabase();
        setupDatabase();
        setupListeners();
        setupTimedHooks();
        startConsoleDaemon();
        setupCommands();
    }

    @Override
    public void onDisable() {
        cron.shutdown();
        d.shutdown();
    }

    private void connectToDatabase() {
        String type = getConfig().getString("database.type"),
                host = getConfig().getString("database.host"),
                dbName = getConfig().getString("database.db_name"),
                user = getConfig().getString("database.user"),
                pass = getConfig().getString("database.pass");
        int port = getConfig().getInt("database.port");

        SQLBackend dbType = SQLBackend.parse(type);
        stmts = new StatementProvider("/db", dbType);

        try {
            switch(dbType) {
                case MYSQL:
                    Class.forName("com.mysql.jdbc.Driver");
                    MysqlConnectionPoolDataSource msqlpool = new MysqlConnectionPoolDataSource();
                    msqlpool.setDatabaseName(dbName);
                    msqlpool.setUser(user);
                    msqlpool.setPassword(pass);
                    msqlpool.setServerName(host);
                    msqlpool.setPort(port);

                    connections = msqlpool;
                    break;
                case POSTGRES:
                    Class.forName("org.postgresql.Driver");
                    PGPoolingDataSource pgpool = new PGPoolingDataSource();
                    pgpool.setDataSourceName("mcanalytics-pg-pool");
                    pgpool.setServerName(host);
                    pgpool.setPortNumber(port);
                    pgpool.setDatabaseName(dbName);
                    pgpool.setUser(user);
                    pgpool.setPassword(pass);
                    pgpool.setMaxConnections(10);

                    connections = pgpool;
                    break;
                default:
                    getLogger().severe("ERROR: unsupported database type in config.yml");
            }
        } catch(ClassNotFoundException ex) {
            getLogger().log(Level.SEVERE, "Could not find JDBC driver: {0}", ex.getLocalizedMessage());
        }
    }

    private void setupTimedHooks() {
        cron = Executors.newSingleThreadScheduledExecutor();
        cron.scheduleAtFixedRate(() -> {
            WritePlayerCountTask t = new WritePlayerCountTask(this, connections, stmts);
            t.gather();
            Bukkit.getScheduler().runTask(this, t);
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void setupDatabase() {
        try(Connection conn = connections.getConnection(); Statement s = conn.createStatement()) {
            s.execute(stmts.get(SQLString.CREATE_HOURLY_PLAYER_COUNT));
            s.execute(stmts.get(SQLString.CREATE_NEW_PLAYER_LOGIN));
        } catch(SQLException ex) {
            Logger.getLogger(MCAnalyticsPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void startConsoleDaemon() {
        d = new ConsoleDaemon(connections, stmts);
        d.startListening();
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this, connections, stmts), this);
    }

    private void setupCommands() {
        TreeCommandExecutor tree = new TreeCommandExecutor();
        tree.add(new AnalyticsCommandDefinition());
        getServer().getPluginCommand("mca").setExecutor(tree);
        getServer().getPluginCommand("mca").setTabCompleter(new TreeTabCompleter(tree));
    }

}
