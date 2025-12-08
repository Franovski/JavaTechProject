package com.example.demo.api;

import com.example.demo.security.TokenStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ApiClient {
    private ApiClient() {}

    private static final int TIMEOUT_SEC = 60;
    private static final int RETRIES = 3;

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
            .build();

    private static String baseUrl() {
        String v = ClientProps.getOr("api.baseUrl", null);
        if (v == null) v = ClientProps.getOr("backend.baseUrl", null);
        if (v == null) v = ClientProps.getOr("baseUrl", "http://localhost:8080");
        return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
    }

    public static HttpResponse<String> get(String path) throws Exception {
        String url = baseUrl() + path;
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                .header("Accept", "application/json")
                .GET();
        if (TokenStore.hasToken()) b.header("Authorization", "Bearer " + TokenStore.get());
        return sendWithRetry(url, b.build());
    }

    public static HttpResponse<String> post(String path, String json) throws Exception {
        String url = baseUrl() + path;
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (TokenStore.hasToken()) b.header("Authorization", "Bearer " + TokenStore.get());
        return sendWithRetry(url, b.build());
    }

    private static HttpResponse<String> sendWithRetry(String url, HttpRequest req) throws Exception {
        Exception last = null;
        for (int i = 1; i <= RETRIES; i++) {
            try {
                return CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                last = e;
                // small backoff
                Thread.sleep(i * 200L);
            }
        }
        throw new RuntimeException("Cannot connect to " + url + " after " + RETRIES + " attempts → "
                + last.getClass().getSimpleName(), last);
    }
}
