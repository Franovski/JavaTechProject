package com.example.demo.api;

import com.example.demo.model.User;
import com.example.demo.security.TokenStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * API client for user management.  Wraps calls to the backend's
 * /users endpoints and converts JSON payloads into {@link User} objects.
 * Uses the JWT from {@link TokenStore} for authentication.
 */
public class UserApi {
    // Points to the new specific user controller path.
    private static final String BASE_URL = "http://localhost:8080/users";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper   = new ObjectMapper();

    /** Returns all users. */
    public List<User> list() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + TokenStore.get())
                .GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), new TypeReference<>() {});
    }

    /**
     * Creates a new user.  All fields on the provided {@link User}
     * (except id) will be sent.  The password should be the raw password,
     * which the backend will hash before persisting.
     */
    public User create(User user) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(user);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + TokenStore.get())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), User.class);
    }

    /**
     * Updates an existing user.  The user’s ID must be non-null.  Only non-null
     * fields on the provided {@link User} will be updated server-side.
     */
    public User update(User user) throws IOException, InterruptedException {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User id must not be null for update");
        }
        String json = mapper.writeValueAsString(user);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + user.getId()))
                .header("Authorization", "Bearer " + TokenStore.get())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), User.class);
    }

    /** Deletes a user by ID. */
    public void delete(Long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + TokenStore.get())
                .DELETE().build();
        httpClient.send(req, HttpResponse.BodyHandlers.discarding());
    }
}