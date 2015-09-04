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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.plugin.daemon.ConsoleDaemon;
import net.jmhertlein.mcanalytics.plugin.listener.PlayerListener;
import net.jmhertlein.reflective.TreeCommandExecutor;
import net.jmhertlein.reflective.TreeTabCompleter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.postgresql.ds.PGPoolingDataSource;

/**
 *
 * @author joshua
 */
public class MCAnalyticsPlugin extends JavaPlugin {
    public static final String SERVER_PRIVATE_KEY = "serverPrivateKey", SERVER_CERTIFICATE = "serverIdentity";
    private DataSource connections;
    private ConsoleDaemon d;
    private StatementProvider stmts;
    private KeyStore trustMaterial;

    @Override
    public void onEnable() {
        Security.insertProviderAt(new BouncyCastleProvider(), 1); // 1 is highest priority position
        saveDefaultConfig();

        try {
            connectToDatabase();
            setupDatabase();
        } catch(Exception ex) {
            getLogger().log(Level.SEVERE, "ERROR: COULD NOT INITIALIZE DATABASE: {0}", ex.getLocalizedMessage());
            getLogger().severe("=====================MCANALYTICS DISABLING======================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupListeners();
        loadTrustMaterial(new File(getDataFolder(), "trust.jks"));
        startConsoleDaemon();
        setupCommands();
    }

    @Override
    public void onDisable() {
        if(d != null)
            d.shutdown();
    }

    private void connectToDatabase() throws ClassNotFoundException {
        String type = getConfig().getString("database.type"),
                host = getConfig().getString("database.host"),
                dbName = getConfig().getString("database.db_name"),
                user = getConfig().getString("database.user"),
                pass = getConfig().getString("database.pass");
        int port = getConfig().getInt("database.port");

        SQLBackend dbType = SQLBackend.parse(type);
        stmts = new StatementProvider("/db", dbType);

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
                throw new IllegalStateException("unsupported database type in config.yml");
        }

    }

    private void setupDatabase() throws SQLException {
        try(Connection conn = connections.getConnection(); Statement s = conn.createStatement()) {
            s.execute(stmts.get(SQLString.CREATE_HOURLY_PLAYER_COUNT));
            s.execute(stmts.get(SQLString.CREATE_NEW_PLAYER_LOGIN));
            s.execute(stmts.get(SQLString.CREATE_PASSWORD_TABLE));
        }
    }

    private void startConsoleDaemon() {
        d = new ConsoleDaemon(trustMaterial, connections, stmts);
        d.startListening();
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private void setupCommands() {
        TreeCommandExecutor tree = new TreeCommandExecutor();
        tree.add(new AnalyticsCommandDefinition(this));
        getServer().getPluginCommand("mca").setExecutor(tree);
        getServer().getPluginCommand("mca").setTabCompleter(new TreeTabCompleter(tree));
    }

    private void loadTrustMaterial(File source) {
        trustMaterial = SSLUtil.newKeyStore();

        if(source.exists()) {
            try(FileInputStream fis = new FileInputStream(source)) {
                trustMaterial.load(fis, new char[0]);
                //getLogger().info("Loaded JKS.");
                //getLogger().info("Size: " + trustMaterial.size());
            } catch(IOException | NoSuchAlgorithmException | CertificateException ex) {
                Logger.getLogger(MCAnalyticsPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //getLogger().info("No keystore found, re-initializing.");
            populateWithTrustMaterial(trustMaterial);
            try(FileOutputStream fos = new FileOutputStream(source)) {
                trustMaterial.store(fos, new char[0]);
                //getLogger().info("Saved new store to disk.");
            } catch(IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
                Logger.getLogger(MCAnalyticsPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void populateWithTrustMaterial(KeyStore store) {
        getLogger().info("Generating new ECDSA keypair (this may take a moment)");
        KeyPair keys = SSLUtil.newECDSAKeyPair();
        Certificate cert = SSLUtil.newSelfSignedCertificate(keys, SSLUtil.newX500Name("MCAnalytics Server", getServer().getName(), "plugins"), true);

        try {
            store.setCertificateEntry(SERVER_CERTIFICATE, cert);
            store.setKeyEntry(SERVER_PRIVATE_KEY, keys.getPrivate(), new char[0], new Certificate[]{cert});
            getLogger().info("Successfully created new server identity.");
        } catch(KeyStoreException ex) {
            getLogger().log(Level.SEVERE, "Error creating server's identity: {0}", ex.getLocalizedMessage());
        }
    }

    public DataSource getConnectionPool() {
        return connections;
    }

    public StatementProvider getStmts() {
        return stmts;
    }

    public ConsoleDaemon getDaemon() {
        return d;
    }

}
