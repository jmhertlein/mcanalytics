package net.jmhertlein.mcanalytics.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import net.jmhertlein.mcanalytics.console.gui.LoginPane;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;

public class MCAConsoleApplication extends Application {
    private static final Path DATA_PATH = Paths.get(System.getProperty("user.home"), ".local", "share", "mcanalytics-console"),
            TM_PATH = DATA_PATH.resolve("trust.jks"),
            CONFIG_PATH = DATA_PATH.resolve("config.json");

    private APISocket sock;
    private KeyStore trust;
    private JSONObject config;

    @Override
    public void start(Stage stage) throws Exception {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        DATA_PATH.toFile().mkdirs();

        setupTrust();
        loadConfig();

        stage.setTitle("MCAnalytics Login");
        stage.setScene(new Scene(new LoginPane(this, config, trust)));
        stage.show();
    }

    private void setupTrust() {
        trust = SSLUtil.newKeyStore();

        File f = TM_PATH.toFile();
        if(f.exists()) {
            try(FileInputStream fis = new FileInputStream(f)) {
                trust.load(fis, new char[0]);
            } catch(IOException | NoSuchAlgorithmException | CertificateException ex) {
                System.err.println("Error loading trust source: " + ex.getLocalizedMessage());
            }
        } else {
            System.out.println("No trust.jks, generating new...");
            KeyPair pair = SSLUtil.newECDSAKeyPair();
            X509Certificate cert = SSLUtil.newSelfSignedCertificate(pair, SSLUtil.newX500Name(System.getProperty("user.name"), "<Servername>", "MCAnalytics Users"), false);
            try {
                trust.setCertificateEntry("client-public-selfsigned", cert);
                trust.setKeyEntry("client-private", pair.getPrivate(), new char[0], new Certificate[]{cert});
            } catch(KeyStoreException ex) {
                System.err.println("Error initializing keystore: " + ex.getLocalizedMessage());
            }
        }

        try {
            Enumeration<String> aliases = trust.aliases();
            while(aliases.hasMoreElements()) {
                String nextElement = aliases.nextElement();

                if(trust.isKeyEntry(nextElement)) {
                    Stream.of(trust.getCertificateChain(nextElement)).map(c -> c.toString()).forEach(s -> System.out.println("-------\n" + s + "\n--------"));
                }
            }
        } catch(KeyStoreException ex) {
            Logger.getLogger(MCAConsoleApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public APISocket getSock() {
        return sock;
    }

    public void setAPISocket(APISocket sock) {
        this.sock = sock;
    }

    private void loadConfig() {
        if(CONFIG_PATH.toFile().exists()) {
            try {
                config = new JSONObject(FileUtils.readFileToString(CONFIG_PATH.toFile()));
                System.out.println("Loaded config.");
            } catch(IOException ex) {
                Logger.getLogger(MCAConsoleApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                CONFIG_PATH.toFile().createNewFile();
            } catch(IOException ex) {
                Logger.getLogger(MCAConsoleApplication.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error creating empty config.");
            }

            config = new JSONObject();
            config.put("hosts", Collections.EMPTY_LIST);
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as
     * fallback in case the application can not be launched through deployment artifacts, e.g., in
     * IDEs with limited FX support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        FileUtils.write(CONFIG_PATH.toFile(), config.toString(2));

        if(sock != null) {
            sock.shutdown();
            System.out.println("Closed API socket.");
        }

        if(!TM_PATH.toFile().exists()) {
            TM_PATH.toFile().createNewFile();
        }

        try(FileOutputStream fos = new FileOutputStream(TM_PATH.toFile())) {
            trust.store(fos, new char[0]);
            System.out.println("Saved store to disk.");
        }
    }

}
