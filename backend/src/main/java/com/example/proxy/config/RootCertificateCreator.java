package com.example.proxy.config;

import lombok.Getter;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

@Configuration
@Getter
public class RootCertificateCreator {

    private final KeyPair rootKeyPair;
    private final X509Certificate rootCertificate;
    private final String rootFingerPrint;

    public RootCertificateCreator() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            this.rootKeyPair = keyPairGenerator.generateKeyPair();
            this.rootCertificate = generateRootCertificate(rootKeyPair);
            this.rootFingerPrint = SPKICalculator();
        } catch (Exception ex) {
            throw new RuntimeException("Root Certification Creation Error: " + ex.getMessage());
        }
    }

    private X509Certificate generateRootCertificate(KeyPair keyPair) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        JcaX509v3CertificateBuilder certBuilder = getJcaX509v3CertificateBuilder(keyPair, now, startDate);

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        X509CertificateHolder certificateHolder = certBuilder.build(contentSigner);
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certificateHolder);
    }

    private JcaX509v3CertificateBuilder getJcaX509v3CertificateBuilder(KeyPair keyPair, long now, Date startDate) {
        Date endDate = new Date(now + (10 * 365 * 24 * 60 * 60 * 1000L));

        String issuerDN = "CN=RootCA, OU=Proxy, O=YourOrganization, L=YourCity, ST=YourState, C=YourCountry";

        return new JcaX509v3CertificateBuilder(
                new X500Name(issuerDN),
                BigInteger.valueOf(now),
                startDate,
                endDate,
                new X500Name(issuerDN),
                keyPair.getPublic()
        );
    }

    private String SPKICalculator() throws NoSuchAlgorithmException {
        byte[] publicKeyEncoded = rootCertificate.getPublicKey().getEncoded();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] spkiHash = digest.digest(publicKeyEncoded);
        return Base64.getEncoder().encodeToString(spkiHash);
    }
}
