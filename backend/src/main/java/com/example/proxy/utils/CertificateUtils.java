package com.example.proxy.utils;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Date;

@RequiredArgsConstructor
public class CertificateUtils {

    public static X509Certificate dynamicCertificateGenerator(KeyPair dynamicKeyPair, KeyPair rootKeyPair, String commonName, X509Certificate rootCert) throws Exception {

        JcaX509v3CertificateBuilder certBuilder = getJcaX509v3CertificateBuilder(rootCert, commonName, dynamicKeyPair);

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(rootKeyPair.getPrivate());
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        GeneralNames san = new GeneralNames(new GeneralName(GeneralName.dNSName, commonName));
        certBuilder.addExtension(Extension.subjectAlternativeName, false, san);

        X509CertificateHolder certificateHolder = certBuilder.build(contentSigner);

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certificateHolder);
    }

    private static JcaX509v3CertificateBuilder getJcaX509v3CertificateBuilder(X509Certificate rootCert, String commonName, KeyPair dynamicKeyPair) {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date endDate = new Date(now + (365 * 24 * 60 * 60 * 1000L));
        String subjectDN = "CN="+commonName+", OU=Proxy, O=YourOrganization, L=YourCity, ST=YourState, C=YourCountry";

        return new JcaX509v3CertificateBuilder(
                rootCert,
                BigInteger.valueOf(now),
                startDate,
                endDate,
                new X500Name(subjectDN),
                dynamicKeyPair.getPublic()
        );
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }
}
