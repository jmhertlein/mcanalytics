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
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
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

    public static PKCS10CertificationRequest newCertificateRequest(X500Name principal, KeyPair p) {
        try {
            PKCS10CertificationRequestBuilder b = new JcaPKCS10CertificationRequestBuilder(principal, p.getPublic());
            ContentSigner s = new JcaContentSignerBuilder(SIGNING_ALGORITHM).setProvider("BC").build(p.getPrivate());
            return b.build(s);
        } catch(OperatorCreationException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Creates a new self-signed X509 certificate
     *
     * @param pair the public/private keypair- the pubkey will be added to the cert and the private
     * key will be used to sign the certificate
     * @param subject the distinguished name of the subject
     * @param isAuthority true to make the cert a CA cert, false otherwise
     * @return
     */
    public static X509Certificate newSelfSignedCertificate(KeyPair pair, X500Name subject, boolean isAuthority) {
        X509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(
                subject,
                BigInteger.probablePrime(128, new SecureRandom()),
                Date.from(Instant.now().minusSeconds(1)),
                Date.from(LocalDateTime.now().plusYears(3).toInstant(ZoneOffset.UTC)),
                subject,
                pair.getPublic());
        try {
            b.addExtension(Extension.basicConstraints, true, new BasicConstraints(isAuthority));
        } catch(CertIOException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            X509CertificateHolder bcCert = b.build(new JcaContentSignerBuilder(SIGNING_ALGORITHM).setProvider("BC").build(pair.getPrivate()));
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(bcCert);
        } catch(CertificateException | OperatorCreationException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Given a certificate signing request, produce a signed certificate.
     *
     * @param caKey
     * @param caCert
     * @param r
     * @param makeAuthority
     * @return
     */
    public static X509Certificate fulfillCertRequest(PrivateKey caKey, X509Certificate caCert, PKCS10CertificationRequest r, boolean makeAuthority) {
        X509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(
                new X500Name(caCert.getSubjectDN().getName()), // the order of O,OU,CN returned is very important
                BigInteger.probablePrime(128, new SecureRandom()),
                Date.from(Instant.now().minusSeconds(1)),
                Date.from(LocalDateTime.now().plusYears(3).toInstant(ZoneOffset.UTC)),
                r.getSubject(),
                getPublicKeyFromInfo(r.getSubjectPublicKeyInfo()));

        try {
            b.addExtension(Extension.basicConstraints, true, new BasicConstraints(makeAuthority));
        } catch(CertIOException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

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
            ex.printStackTrace(System.err);
            ret = null;
        }

        return ret;
    }

    /**
     * Builds an SSLConect that trusts the trust material in the KeyStore
     *
     * @param trustMaterial
     * @return
     */
    public static SSLContext buildContext(KeyStore trustMaterial) {
        SSLContext ctx;
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustMaterial);

            KeyManagerFactory keyMgr = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyMgr.init(trustMaterial, new char[0]);

            ctx = SSLContext.getInstance("TLS");
            ctx.init(keyMgr.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch(KeyStoreException | UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            ctx = null;
        }

        return ctx;
    }

    /**
     * Same as buildContext(), but wraps all X509TrustManagers in a SavableTrustManager to provide
     * UntrustedCertificateExceptions so that when a client connects to a server it does not trust,
     * the program can recover the key and ask the user if they wish to trust it.
     *
     * @param trustMaterial
     * @return
     */
    public static SSLContext buildClientContext(KeyStore trustMaterial) {
        SSLContext ctx;
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustMaterial);
            ctx = SSLContext.getInstance("TLS");
            //key manager factory go!
            KeyManagerFactory keyMgr = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyMgr.init(trustMaterial, new char[0]);

            TrustManager[] trustManagers = tmf.getTrustManagers();

            for(int i = 0; i < trustManagers.length; i++) {
                if(trustManagers[i] instanceof X509TrustManager) {
                    System.out.println("Wrapped a trust manager.");
                    trustManagers[i] = new SavableTrustManager((X509TrustManager) trustManagers[i]);
                }
            }

            ctx.init(keyMgr.getKeyManagers(), trustManagers, null);
        } catch(KeyStoreException | UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            ctx = null;
        }

        return ctx;
    }

    public static KeyStore newKeyStore() {
        KeyStore store;
        try {
            store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(null, null);
            return store;
        } catch(KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public static PublicKey getPublicKeyFromInfo(SubjectPublicKeyInfo o) {
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

    public static Set<String> getNames(ASN1ObjectIdentifier type, X500Name name) {
        return Stream.of(name.getRDNs(type))
                .flatMap(n -> Stream.of(n.getTypesAndValues()))
                .map(n -> IETFUtils.valueToString(n.getValue()))
                .collect(Collectors.toSet());
    }

    public static X500Name newX500Name(String commonName, String orgName, String ouName) {
        X500NameBuilder b = new X500NameBuilder(BCStyle.INSTANCE);
        b.addRDN(BCStyle.CN, commonName);
        b.addRDN(BCStyle.OU, ouName);
        b.addRDN(BCStyle.O, orgName);

        return b.build();
    }

    public static X509Certificate certFromBase64(String base64) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decodeBase64(base64)));
        } catch(CertificateException ex) {
            Logger.getLogger(SSLUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Set<String> getNames(ASN1ObjectIdentifier type, X509Certificate cert) {
        Set<String> names = new HashSet<>();
        X500Name x500name;
        try {
            x500name = new JcaX509CertificateHolder(cert).getSubject();
        } catch(CertificateEncodingException cee) {
            return names;
        }

        return getNames(type, x500name);
    }

    public static Set<String> getCNs(X509Certificate cert) {
        return getNames(BCStyle.CN, cert);
    }

    public static Set<String> getCNs(X500Name name) {
        return getNames(BCStyle.CN, name);
    }

    public static Set<String> getOrgNames(X509Certificate cert) {
        return getNames(BCStyle.O, cert);
    }

    /**
     * Combines the given password with the given salt, and hashes it with 10000 passes of SHA-512,
     * re-combining the hash from the previous iteration with the password and salt to produce input
     * for the current iteration.
     *
     * @param pass
     * @param salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static byte[] hash(byte[] pass, byte[] salt) throws NoSuchAlgorithmException, NoSuchProviderException {
        final int PASSES = 10000;
        MessageDigest mda = MessageDigest.getInstance("SHA-512", "BC");
        mda.update(salt);
        mda.update(pass);
        byte[] hash = mda.digest();

        for(int i = 0; i < PASSES; i++) {
            mda.update(hash);
            mda.update(salt);
            mda.update(pass);
            hash = mda.digest();
        }

        return hash;
    }

    /**
     * Generates a new 64-bit salt with the default CSPRNG
     *
     * @return a 64-bit cryptographic salt
     */
    public static byte[] newSalt() {
        SecureRandom gen = new SecureRandom();
        byte[] salt = new byte[8];
        gen.nextBytes(salt);
        return salt;
    }
}
