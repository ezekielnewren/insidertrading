package com.ezekielnewren;

import com.google.common.collect.ImmutableMap;
import com.yubico.webauthn.data.ByteArray;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * Class that contains methods to create a {@code ImmutableMap} and retrieve values from it.
 */
public class Build {

    /**
     * Constant variable for an {@code ImmutableMap}.
     * @see com.google.common.collect.ImmutableMap
     */
    static ImmutableMap<String, String> internal;

    static {
        Properties p = new Properties();
        try (InputStream is = Build.class.getClassLoader().getResourceAsStream("build.info")) {
            p.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<Object, Object> item: p.entrySet()) {
            builder.put(item.getKey().toString(), item.getValue().toString());
        }
        internal = builder.build();
    }


    /**
     * Return a created {@code ImmutableMap}.
     * @return {@code ImmutableMap} with values.
     * @see com.google.common.collect.ImmutableMap
     */
    public static ImmutableMap<String, String> info() {
        return internal;
    }

    /**
     * Get the key from the {@link ImmutableMap}.
     * @param key the key of key value pair in a hash map.
     * @return key as a {@code String}.
     * @see java.lang.String
     */
    public static String get(String key) {
        return info().get(key);
    }

    public static InputStream getResource(String fileName) {
        return Build.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static ByteArray getResourceAsByteArray(String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = getResource(fileName)) {
            IOUtils.copy(is, baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ByteArray(baos.toByteArray());
    }

    public static String getResourceAsStringUTF8(String fileName) {
        return new String(getResourceAsByteArray(fileName).getBytes(), StandardCharsets.UTF_8);
    }

}
