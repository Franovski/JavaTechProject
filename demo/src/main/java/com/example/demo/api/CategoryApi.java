package com.example.demo.api;

import com.example.demo.model.Category;
import com.example.demo.security.TokenStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Handles all HTTP communication related to Category APIs.
 * Provides methods to list, create, update, and delete categories
 * through the backend REST service.
 *
 * Uses Java 11 HttpClient with Bearer Token authentication.
 */
public class CategoryApi {

    private static final String BASE_URL = "http://localhost:8080/categories";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== GET ====================

    /**
     * Fetch all categories from the backend.
     *
     * Sends a GET request to /categories and extracts the "data" JSON array.
     *
     * @return List of Category objects
     * @throws IOException if serialization or communication fails
     * @throws InterruptedException if the request is interrupted
     */
    public List<Category> list() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + TokenStore.get())
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        // Extract "data" node from JSON
        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("data");

        return mapper.readValue(data.traverse(), new TypeReference<>() {});
    }

    // ==================== CREATE ====================

    /**
     * Create a new category.
     *
     * Sends a POST request to /categories with a JSON body containing
     * the category name.
     *
     * @param name Name of the category to create
     * @return The created Category object
     * @throws IOException if serialization or communication fails
     * @throws InterruptedException if the request is interrupted
     */
    public Category create(String name) throws IOException, InterruptedException {
        Category body = new Category();
        body.setName(name);

        String json = mapper.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + TokenStore.get())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        // Extract "data" node
        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("data");

        return mapper.readValue(data.traverse(), Category.class);
    }

    // ==================== UPDATE ====================

    /**
     * Update an existing category by ID.
     *
     * Sends a PUT request to /categories/{id} with the updated name.
     *
     * @param id Category ID
     * @param name Updated category name
     * @return Updated Category object
     * @throws IOException if serialization or communication fails
     * @throws InterruptedException if the request is interrupted
     */
    public Category update(Long id, String name) throws IOException, InterruptedException {
        Category body = new Category();
        body.setName(name);

        String json = mapper.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + TokenStore.get())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        // Extract "data" node
        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("data");

        return mapper.readValue(data.traverse(), Category.class);
    }

    // ==================== DELETE ====================

    /**
     * Delete a category by ID.
     *
     * Sends a DELETE request to /categories/{id}.
     * No response body is expected.
     *
     * @param id Category ID
     * @throws IOException if communication fails
     * @throws InterruptedException if the request is interrupted
     */
    public void delete(Long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + TokenStore.get())
                .DELETE()
                .build();

        httpClient.send(req, HttpResponse.BodyHandlers.discarding());
    }
}
