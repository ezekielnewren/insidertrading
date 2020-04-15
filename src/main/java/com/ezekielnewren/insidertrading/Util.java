package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.Authenticator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.*;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Contains static helper methods.
 */
public class Util {

    /**
     * Constant boolean variable used logging/debugging.
     */
    public static final boolean DEBUG;

    /**
     * Cryptographically secure pseudo-random number generator.
     * @see java.security.SecureRandom
     */
    static final SecureRandom random;

    static {
        boolean ASSERT_ON = false;
        assert(ASSERT_ON=true);
        boolean envDebug = "true".equals(System.getenv("debug"));

        DEBUG = ASSERT_ON || envDebug;

        Security.addProvider(new BouncyCastleProvider());

        SecureRandom strong;
        try {strong = SecureRandom.getInstanceStrong();} catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(strong.generateSeed(16));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a cryptographically strong, default psuedo-random number generator
     * @return random number
     * @see java.security.SecureRandom
     */
    public static SecureRandom getRandom() {
        return random;
    }

    /**
     * Retrieves random bytes.
     * @param amount the number of bytes you would like.
     * @return randomized bytes.
     */
    public static byte[] generateRandom(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be greater than 0");
        byte[] b = new byte[amount];
        random.nextBytes(b);
        return b;
    }

    /**
     * Generate a new byte array using a pseudo-random byte array.
     * @param amount the number of bytes you would like.
     * @return new byte array with the randomized bytes
     * @see com.yubico.webauthn.data.ByteArray
     */
    public static ByteArray generateRandomByteArray(int amount) {
        return new ByteArray(generateRandom(amount));
    }

    /**
     * Generates an ObjectId using a pseudo-random byte array.
     * @return new {@code MongoDB} ObjectId.
     * @see org.bson.types.ObjectId
     */
    public static ObjectId generateRandomObjectId() {
        return new ObjectId(generateRandom(12));
    }

    /**
     *
     * @param rp
     * @param startAssertionOptions
     * @param challenge
     * @return
     */
    public static AssertionRequest startAssertion(RelyingParty rp, StartAssertionOptions startAssertionOptions, ByteArray challenge) {
        PublicKeyCredentialRequestOptions.PublicKeyCredentialRequestOptionsBuilder pkcro = PublicKeyCredentialRequestOptions.builder()
                .challenge(challenge)
                .rpId(rp.getIdentity().getId())
                .allowCredentials(
                        startAssertionOptions.getUsername().map(un ->
                                new ArrayList<>(rp.getCredentialRepository().getCredentialIdsForUsername(un)))
                )
                .extensions(
                        startAssertionOptions.getExtensions()
                                .toBuilder()
                                .appid(rp.getAppId())
                                .build()
                )
                .timeout(startAssertionOptions.getTimeout())
                ;

        startAssertionOptions.getUserVerification().ifPresent(pkcro::userVerification);

        return AssertionRequest.builder()
                .publicKeyCredentialRequestOptions(
                        pkcro.build()
                )
                .username(startAssertionOptions.getUsername())
                .build();
    }

    /**
     *
     * @param auth
     * @param userHandle
     * @return
     */
    public static RegisteredCredential getRegisteredCredential(Authenticator auth, ByteArray userHandle) {
        if (auth == null) return null;
        return RegisteredCredential.builder()
                .credentialId(auth.getCredentialId())
                    .userHandle(userHandle)
            .publicKeyCose(auth.getPublicKeyCose())
            .signatureCount(auth.getSignatureCount())
            .build();
    }

    /**
     *
     * @param pkc
     * @return
     */
    public static PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> copyPublicKeyCredential(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc) {
        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> other = PublicKeyCredential.<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>builder()
                .id(pkc.getId())
                .response(pkc.getResponse())
                .clientExtensionResults(pkc.getClientExtensionResults())
                .build();
        return other;
    }

    /**
     *
     * @param coll
     * @param pred
     * @param <T>
     * @return
     */
    public static <T> Optional<T> findOne(Collection<T> coll, Predicate<T> pred) {
        return coll.stream().filter(pred).findFirst();
    }

    /**
     *
     * @param node
     * @return
     */
    public static Object asPOJO(JsonNode node) {
        switch (node.getNodeType()) {
            case NULL:
                return null;
            case BOOLEAN:
                return node.asBoolean();
            case NUMBER:
                return node.asLong();
            case STRING:
                return node.asText();
            case POJO:
                return ((POJONode) node).getPojo();
            default:
                throw new RuntimeException("must be a basic json type");
        }
    }

    public static long getRegistrationTimeout() {
        return 1000*60;
    }

    public static long getAssertionTimeout() {
        return getRegistrationTimeout();
    }

    public static X509Certificate createX509CertificateFromEncoded(ByteArray binary) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509", "BC")
                    .generateCertificate(new ByteArrayInputStream(binary.getBytes()));
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static X509Certificate createX509CertificateFromPem(ByteArray input) {
        return createX509CertificateFromEncoded(pemDecode(input));
    }

    public static String createPemFromX509Certificate(X509Certificate cert) {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter jpw = new JcaPEMWriter(sw)) {
            jpw.writeObject(cert);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public static ByteArray pemDecode(ByteArray input) {
        Reader r = new InputStreamReader(new ByteArrayInputStream(input.getBytes()));
        try (PemReader pr = new PemReader(r)) {
            return new ByteArray(pr.readPemObject().getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String pemEncode(ByteArray input) {
        StringWriter sw = new StringWriter();
        try (PemWriter pw = new PemWriter(sw)) {
            pw.writeObject(new MiscPEMGenerator(input.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public static boolean verify(X509Certificate subject, X509Certificate issuer) {
        try {
            subject.verify(issuer.getPublicKey(), Security.getProvider("BC"));
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            return false;
        }
        return true;
    }

    @SneakyThrows
    public static ByteArray download(URL url) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = url.openStream()) {
            IOUtils.copy(is, baos);
        }
        return new ByteArray(baos.toByteArray());
    }

    public static ByteArray doSHA256Digest(ByteArray input) {
        SHA256Digest d = new SHA256Digest();
        byte[] hashCalc = new byte[d.getDigestSize()];
        d.update(input.getBytes(), 0, input.size());
        d.doFinal(hashCalc, 0);
        return new ByteArray(hashCalc);
    }

}
