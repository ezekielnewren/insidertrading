package com.ezekielnewren.insidertrading;

import com.yubico.webauthn.data.ByteArray;
import org.bson.types.ObjectId;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Class Util contains static helper methods.
 */
public class Util {

    /**
     * Constant boolean variable used logging/debugging.
     */
    public static final boolean DEBUG;

    /**
     * Cryptographically strong random number generator.
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
     * Method to retrieve random bytes.
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
     *
     * @param amount the number of bytes you would like.
     * @return new byte array with the randomized bytes
     */
    public static ByteArray generateRandomByteArray(int amount) {
        return new ByteArray(generateRandom(amount));
    }

    /**
     *
     * @return new mongo ObjectId
     */
    public static ObjectId generateRandomObjectId() {
        return new ObjectId(generateRandom(12));
    }


}
