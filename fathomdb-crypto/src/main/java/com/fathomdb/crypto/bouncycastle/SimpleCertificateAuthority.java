package com.fathomdb.crypto.bouncycastle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class SimpleCertificateAuthority {
    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";
    private static final long ONE_DAY = 24L * 60L * 60L * 1000L;

    public X509Certificate[] caCertificate;
    public PrivateKey caPrivateKey;

    private static Certificate signCertificate(X500Name signer, PrivateKey signerPrivateKey, X500Name subject,
            SubjectPublicKeyInfo subjectPublicKeyInfo) {
        try {
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(SIGNATURE_ALGORITHM);
            AlgorithmIdentifier digestAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

            long days = 3650;

            long now = System.currentTimeMillis();
            Date notBefore = new Date(now - ONE_DAY);
            Date notAfter = new Date(notBefore.getTime() + (days * ONE_DAY));

            BigInteger serialNumber;

            synchronized (SimpleCertificateAuthority.class) {
                long nextSerialNumber = System.currentTimeMillis();
                serialNumber = BigInteger.valueOf(nextSerialNumber);
            }

            X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(signer, serialNumber, notBefore,
                    notAfter, subject, subjectPublicKeyInfo);

            // {
            // boolean isCritical = false;
            // certificateBuilder.addExtension(X509Extensions.SubjectKeyIdentifier,
            // isCritical,
            // csr.getSubjectPublicKeyInfo());
            // }

            AsymmetricKeyParameter caPrivateKeyParameters = PrivateKeyFactory.createKey(signerPrivateKey.getEncoded());
            ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digestAlgId)
                    .build(caPrivateKeyParameters);

            X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
            Certificate certificate = certificateHolder.toASN1Structure();

            return certificate;
        } catch (OperatorCreationException e) {
            throw new IllegalArgumentException("Error signing certificate", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error signing certificate", e);
        }
    }

    public X509Certificate signCsr(String csr) {
        try {
            PKCS10CertificationRequest csrHolder = parseCsr(csr);
            return signCsr(csrHolder);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading CSR", e);
        }
    }

    private static PKCS10CertificationRequest parseCsr(String csr) throws IOException {
        PemReader reader = new PemReader(new StringReader(csr));
        PemObject pemObject = reader.readPemObject();
        reader.close();

        PKCS10CertificationRequest csrHolder = new PKCS10CertificationRequest(pemObject.getContent());
        return csrHolder;
    }

    public X509Certificate signCsr(PKCS10CertificationRequest csr) {
        SubjectPublicKeyInfo subjectPublicKeyInfo = csr.getSubjectPublicKeyInfo();
        X500Name subject = csr.getSubject();
        Certificate certificate = signCertificate(
                BouncyCastleHelpers.toX500Name(caCertificate[0].getSubjectX500Principal()), caPrivateKey, subject,
                subjectPublicKeyInfo);
        return toX509(certificate);
    }

    private static X509Certificate toX509(Certificate certificate) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificate
                    .getEncoded()));
            return cert;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting certificate", e);
        } catch (CertificateException e) {
            throw new IllegalArgumentException("Error converting certificate", e);
        }
    }

    public static X509Certificate signAsCa(X500Principal subject, PublicKey subjectPublicKey, X500Principal issuer,
            PrivateKey issuerPrivateKey) {
        Certificate certificate = signCertificate(BouncyCastleHelpers.toX500Name(issuer), issuerPrivateKey,
                BouncyCastleHelpers.toX500Name(subject), BouncyCastleHelpers.toSubjectPublicKeyInfo(subjectPublicKey));
        return toX509(certificate);
    }

    public static X509Certificate selfSign(X500Principal subject, KeyPair keyPair) {
        X500Principal issuer = subject;
        Certificate certificate = signCertificate(BouncyCastleHelpers.toX500Name(issuer), keyPair.getPrivate(),
                BouncyCastleHelpers.toX500Name(subject),
                BouncyCastleHelpers.toSubjectPublicKeyInfo(keyPair.getPublic()));
        return toX509(certificate);
    }

    public static X509Certificate selfSign(String csr, KeyPair keyPair) {
        try {
            PKCS10CertificationRequest csrHolder = parseCsr(csr);

            SubjectPublicKeyInfo subjectPublicKeyInfo = csrHolder.getSubjectPublicKeyInfo();
            X500Name subject = csrHolder.getSubject();

            // Self sign
            X500Name issuer = subject;
            PrivateKey issuerPrivateKey = keyPair.getPrivate();

            Certificate certificate = signCertificate(issuer, issuerPrivateKey, subject, subjectPublicKeyInfo);
            return toX509(certificate);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading CSR", e);
        }
    }
}
