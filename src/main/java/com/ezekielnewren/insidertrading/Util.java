package com.ezekielnewren.insidertrading;

import com.ezekielnewren.insidertrading.data.Authenticator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.*;
import org.bson.types.ObjectId;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
     * Starts the assertion and builds the options for the public key credential request.
     * @param rp {@code RelyingParty} information.
     * @param startAssertionOptions contains the parameters for the public key credential request options (pkcro.
     * @param challenge challenge.
     * @return an {@code AssertionRequest} containing the pckro, and the username.
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
     * Used to get the registered credential id .
     * @param auth {@code Authenticator} data.
     * @param userHandle handle for authenticator.
     * @return {@code RegisterCredential} or null if the {@code auth} is null.
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
     * Used to create a copy of the public key credentials.
     * @param pkc original public key credential.
     * @return public key credential copy.
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
     * Used to find the first object in a collection that matches the specified parameters.
     * @param coll collection to be searched.
     * @param pred specified filter parameter.
     * @param <T> object type.
     * @return object that matched the given parameter.
     */
    public static <T> Optional<T> findOne(Collection<T> coll, Predicate<T> pred) {
        return coll.stream().filter(pred).findFirst();
    }

    /**
     * Used to specify the datatype when converting {@code JSON} to {@code POJO}
     * @param node {@code JSON} objects.
     * @return {@code JSON} as the correct {@code Java} datatype.
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

}
