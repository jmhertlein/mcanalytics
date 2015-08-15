package net.jmhertlein.mcanalytics.console;

import java.io.File;
import java.io.FileInputStream;
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
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.jmhertlein.mcanalytics.api.APISocket;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MCAConsoleApplication extends Application {
    private static final Path DATA_PATH = Paths.get(System.getProperty("user.home"), ".local", "share", "mcanalytics-console"),
            TM_PATH = DATA_PATH.resolve("trust.jks");

    private APISocket sock;
    private KeyStore trust;

    @Override
    public void start(Stage stage) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        DATA_PATH.toFile().mkdirs();

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

        stage.setTitle("MCAnalytics Login");
        stage.setScene(new Scene(new LoginPane(this, trust)));
        stage.show();
    }

    public APISocket getSock() {
        return sock;
    }

    public void setAPISocket(APISocket sock) {
        this.sock = sock;
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
        } catch(IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
            ex.printStackTrace(System.err);
        }
    }

}
