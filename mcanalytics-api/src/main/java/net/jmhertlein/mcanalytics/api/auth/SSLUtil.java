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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

/**
 *
 * @author joshua
 */
public class SSLUtil {
    private static final String SIGNING_ALGORITHM = "SHA512withECDSA";
    private KeyStore clientTrust;

    public static PKCS10CertificationRequest newCertificateRequest(String principal, KeyPair p) {
        try {
            PKCS10CertificationRequestBuilder b = new JcaPKCS10CertificationRequestBuilder(getName(principal), p.getPublic());
            ContentSigner s = new JcaContentSignerBuilder(SIGNING_ALGORITHM).setProvider("BC").build(p.getPrivate());
            return b.build(s);
        } catch(OperatorCreationException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static X509Certificate newSelfSignedCertificate(KeyPair pair, String subject) {
        X509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(
                getName(subject),
                BigInteger.ZERO,
                Date.from(Instant.now()),
                Date.from(LocalDateTime.now().plusYears(3).toInstant(ZoneOffset.UTC)),
                getName(subject),
                pair.getPublic());

        try {
            X509CertificateHolder bcCert = b.build(new JcaContentSignerBuilder(SIGNING_ALGORITHM).setProvider("BC").build(pair.getPrivate()));
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(bcCert);
        } catch(CertificateException | OperatorCreationException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static X509Certificate fulfillCertRequest(PrivateKey caKey, X509Certificate caCert, PKCS10CertificationRequest r) {
        X509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(
                new X500Name(caCert.getSubjectX500Principal().getName()),
                BigInteger.ZERO,
                Date.from(Instant.now()),
                Date.from(LocalDateTime.now().plusYears(3).toInstant(ZoneOffset.UTC)),
                r.getSubject(),
                getPublicKeyFromInfo(r.getSubjectPublicKeyInfo()));
        try {
            ContentSigner signer = new JcaContentSignerBuilder(SIGNING_ALGORITHM).setProvider("BC").build(caKey);
            X509CertificateHolder build = b.build(signer);
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(build);
        } catch(OperatorCreationException | CertificateException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static KeyPair newECDSAKeyPair() {
        return newECDSAKeyPair("secp521r1", "BC");
    }

    /**
     * Generates a new Elliptic Curve Digital Signature Algorithm (ECDSA) public/private key pair.
     *
     * System's default SecureRandom is used
     *
     * @param curveName the name of a pre-defined elliptic curve (e.g. secp521r1)
     * @param provider the JCE provider to use
     * @return a new ECDSA key pair
     */
    public static KeyPair newECDSAKeyPair(String curveName, String provider) {
        KeyPair ret;
        try {
            ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(curveName);
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", provider);
            g.initialize(ecGenSpec, new SecureRandom());
            ret = g.generateKeyPair();
        } catch(NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            ret = null;
        }

        return ret;
    }

    private static SSLContext buildContext(KeyStore trustMaterial) {
        SSLContext ctx;
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustMaterial);
            ctx = SSLContext.getInstance("TLS");
            //key manager factory go!
            KeyManagerFactory keyMgr = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyMgr.init(trustMaterial, new char[0]);

            ctx.init(keyMgr.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch(KeyStoreException | UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            ctx = null;
        }

        return ctx;
    }

    private static KeyStore getNewKeyStore() {
        KeyStore store;
        try {
            store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(null, new char[0]);
            return store;
        } catch(KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    private static PublicKey getPublicKeyFromInfo(SubjectPublicKeyInfo o) {
        try {
            byte[] bytes = o.getEncoded("X509");
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bytes));
        } catch(NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static X500Name getName(String cn) {
        X500NameBuilder b = new X500NameBuilder(BCStyle.INSTANCE);
        b.addRDN(BCStyle.CN, cn);

        return b.build();
    }

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        System.out.println("Generating CA pair");
        KeyPair caPair = newECDSAKeyPair();
        System.out.println("Generating self-signed CA cert.");
        X509Certificate caCert = newSelfSignedCertificate(caPair, "joshCA");
        System.out.println("Generating client pair");
        KeyPair clPair = newECDSAKeyPair();
        System.out.println("Generating CSR");
        PKCS10CertificationRequest clReq = newCertificateRequest("test-client", clPair);
        System.out.println("Signing requested cert");
        X509Certificate clCert = fulfillCertRequest(caPair.getPrivate(), caCert, clReq);
        System.out.println("Yay done");

        Runnable server = () -> {
            KeyStore serverStore = getNewKeyStore();
            try {
                serverStore.setCertificateEntry("testCA", caCert);
                serverStore.setKeyEntry("caPrivateKey", caPair.getPrivate(), new char[0], new Certificate[]{caCert});
            } catch(KeyStoreException ex) {
                Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            SSLContext ctx = buildContext(serverStore);
            ServerSocket serverSock;
            try {
                serverSock = ctx.getServerSocketFactory().createServerSocket(50000);
                System.out.println("Listening...");
                Socket client = serverSock.accept();
                System.out.println("Got client.");
                PrintWriter out = new PrintWriter(new GZIPOutputStream(client.getOutputStream()));
                //BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(client.getInputStream())));

                out.println("Hello, world!");
                out.flush();
                System.out.println("SERVER: Wrote.");
                out.close();
                //in.close();
                client.close();
            } catch(IOException ex) {
                Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        };

        Thread t = new Thread(server);
        t.start();

        try {
            System.out.println("Waiting for 1 sec...");
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        KeyStore clientStore = getNewKeyStore();
        try {
            clientStore.setCertificateEntry("caCert", caCert);
        } catch(KeyStoreException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        SSLContext ctx = buildContext(clientStore);
        try {
            Socket sock = ctx.getSocketFactory().createSocket("localhost", 50000);
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(sock.getInputStream())));
            System.out.println(in.readLine());
            in.close();
            sock.close();
        } catch(IOException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
