package com.example.demo.security;


import java.util.prefs.Preferences;


public final class TokenStore {
    private static final String KEY = "jwt";
    private static final Preferences PREF = Preferences.userRoot().node("com.example.demo");
    private static String cached;


    private TokenStore() {}


    public static void set(String token) {
        cached = token;
        try { PREF.put(KEY, token == null ? "" : token); } catch (Exception ignored) {}
    }


    public static String get() {
        if (cached != null && !cached.isBlank()) return cached;
        String v = PREF.get(KEY, "");
        cached = v;
        return v;
    }


    public static boolean hasToken() { return get() != null && !get().isBlank(); }


    public static void clear() { set(""); }
}