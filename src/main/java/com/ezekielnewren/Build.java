package com.ezekielnewren;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class Build {

    /**
     *
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
     * @return
     */
    public static ImmutableMap<String, String> info() {
        return internal;
    }

    /**
     * @param key
     * @return
     */
    public static String get(String key) {
        return info().get(key);
    }

}
