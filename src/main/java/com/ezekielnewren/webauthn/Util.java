package com.ezekielnewren.webauthn;

import com.yubico.webauthn.data.ByteArray;
import org.bson.types.ObjectId;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Util {

    public static final boolean DEBUG;

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

    public static byte[] generateRandom(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be greater than 0");
        byte[] b = new byte[amount];
        random.nextBytes(b);
        return b;
    }

    public static ByteArray generateRandomByteArray(int amount) {
        return new ByteArray(generateRandom(amount));
    }

    public static ObjectId generateRandomObjectId() {
        return new ObjectId(generateRandom(12));
    }


}
