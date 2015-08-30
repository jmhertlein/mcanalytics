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
package net.jmhertlein.mcanalytics.api.auth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author joshua
 */
public class SSLUtilTest {

    public SSLUtilTest() {
        Security.addProvider(new BouncyCastleProvider());
        System.out.println("Added BC provider");
    }

    @Test
    public void trustTest() throws KeyStoreException, IOException {
        KeyPair server = SSLUtil.newECDSAKeyPair();
        KeyPair client = SSLUtil.newECDSAKeyPair();

        KeyStore clientStore = SSLUtil.newKeyStore();
        KeyStore serverStore = SSLUtil.newKeyStore();

        X509Certificate serverCert = SSLUtil.newSelfSignedCertificate(server, SSLUtil.newX500Name("server", "test", "java"), true);

        PKCS10CertificationRequest csr = SSLUtil.newCertificateRequest(SSLUtil.newX500Name("client", "test", "java"), client);
        X509Certificate clientCert = SSLUtil.fulfillCertRequest(server.getPrivate(), serverCert, csr, false);

        System.out.println("=================================");
        System.out.println(clientCert.toString());
        System.out.println("=================================");
        System.out.println(serverCert.toString());
        System.out.println("=================================");

        serverStore.setCertificateEntry("server-cert", serverCert);
        serverStore.setKeyEntry("server-private", server.getPrivate(), new char[0], new Certificate[]{serverCert});

        clientStore.setCertificateEntry("server-cert", serverCert);
        clientStore.setKeyEntry("client-private", client.getPrivate(), new char[0], new Certificate[]{clientCert, serverCert});

        try {
            clientCert.verify(server.getPublic());
            clientCert.verify(serverCert.getPublicKey());
            serverCert.verify(server.getPublic());
            serverCert.verify(serverCert.getPublicKey());
            System.out.println("verified!");

            clientCert.checkValidity();
            serverCert.checkValidity();
            System.out.println("Both valid!");
        } catch(CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
            Logger.getLogger(SSLUtilTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }

        SSLContext serverCtx = SSLUtil.buildContext(serverStore);
        SSLContext clientCtx = SSLUtil.buildContext(clientStore);

        SSLServerSocket sslServer = (SSLServerSocket) serverCtx.getServerSocketFactory().createServerSocket();
        sslServer.setWantClientAuth(true);
        sslServer.setUseClientMode(false);

        SSLSocket sslClient = (SSLSocket) clientCtx.getSocketFactory().createSocket();
        sslClient.setWantClientAuth(true);

        sslServer.bind(new InetSocketAddress(33333));

        Thread th = new Thread() {

            @Override
            public void run() {
                try {
                    SSLSocket cl = (SSLSocket) sslServer.accept();
                    System.out.println("Got client");
                    System.out.println("Client identified with:");
                    for(Certificate c : cl.getSession().getPeerCertificates()) {
                        System.out.println("----------------------------");
                        System.out.println(c.toString());
                        System.out.println("----------------------------");
                    }
                    assertTrue(cl.getSession().getPeerCertificates().length > 0);
                    System.out.println("Closing...");
                    cl.close();
                } catch(IOException ex) {
                    System.out.println("Got here.");
                    ex.printStackTrace(System.err);
                    Logger.getLogger(SSLUtilTest.class.getName()).log(Level.SEVERE, null, ex);
                    fail();
                }
            }
        };

        th.start();

        sslClient.connect(new InetSocketAddress("localhost", 33333));
        sslClient.startHandshake();
        
        try {
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            Logger.getLogger(SSLUtilTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void serializationTest() throws CertificateEncodingException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyPair pair = SSLUtil.newECDSAKeyPair();
        X509Certificate cert = SSLUtil.newSelfSignedCertificate(pair, SSLUtil.newX500Name("Tom Riddle", "Hogwarts", "British Magical Schools"), true);
        
        KeyStore store = SSLUtil.newKeyStore();
        store.setCertificateEntry("cert", cert);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        store.store(out, new char[0]);
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        
        KeyStore loaded = SSLUtil.newKeyStore();
        loaded.load(in, new char[0]);
        
        X509Certificate loadedCert = (X509Certificate) loaded.getCertificate("cert");
        
        System.out.println("----------------OLD------------");
        System.out.println(cert.toString());
        System.out.println("----------------NEW-----------");
        System.out.println(loadedCert.toString());
        System.out.println("----------------------------");
        
        assertEquals(cert, loadedCert);
        
    }
}
