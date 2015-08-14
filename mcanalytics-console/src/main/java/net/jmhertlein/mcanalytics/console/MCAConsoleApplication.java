package net.jmhertlein.mcanalytics.console;

import net.jmhertlein.mcanalytics.console.gui.LoginPane;
import java.security.Security;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.jmhertlein.mcanalytics.api.APISocket;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MCAConsoleApplication extends Application {
    private APISocket sock;

    @Override
    public void start(Stage stage) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        stage.setTitle("MCAnalytics Login");
        stage.setScene(new Scene(new LoginPane(this)));
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
    }

}
