package com.example.demo.api;


import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;


public final class ClientProps {
    private static final String PATH = "/com/example/demo/client.properties";
    private static Properties PROPS;


    private ClientProps() {}


    private static synchronized void loadOnce() throws Exception {
        if (PROPS != null) return;
        PROPS = new Properties();
        try (InputStream in = ClientProps.class.getResourceAsStream(PATH)) {
            if (in == null) throw new IllegalStateException("client.properties not found at " + PATH);
            PROPS.load(in);
        }
    }


    public static String get(String key) throws Exception {
        loadOnce();
        return PROPS.getProperty(key);
    }


    public static String getOr(String key, String def) {
        try { String v = get(key); return (v == null || v.isBlank()) ? def : v; }
        catch (Exception e) { return def; }
    }


    public static String require(String key) throws Exception {
        String v = get(key);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing property: " + key);
        return v;
    }
}