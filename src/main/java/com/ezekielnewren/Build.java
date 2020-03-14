package com.ezekielnewren;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Class that contains methods to create a {@code ImmutableMap}
 * and retrieve values from it.
 */
public class Build {

    /**
     * Create a {@code ImmutableMap}.
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
     * Method to return a created {@code ImmutableMap}.
     * @return {@code ImmutableMap} with values.
     * @see com.google.common.collect.ImmutableMap
     */
    public static ImmutableMap<String, String> info() {
        return internal;
    }

    /**
     * Method to get the key from the {@link ImmutableMap}.
     * @param key the key of key value pair in a hash map.
     * @return key as a {@code String}.
     * @see java.lang.String
     */
    public static String get(String key) {
        return info().get(key);
    }

}
