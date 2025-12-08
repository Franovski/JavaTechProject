package com.example.demo.api;

import com.example.demo.model.Section;
import com.example.demo.security.TokenStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

/**
 * Handles all HTTP communication related to Section APIs.
 * Provides methods to list, create, update, and delete sections
 * through the backend REST service.
 *
 * Uses Java 11 HttpClient with Bearer Token authentication.
 */
public class SectionApi {

    private static final String BASE_URL = "http://localhost:8080/sections";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== GET ====================

    /**
     * Fetch all sections from the backend.
     *
     * Sends a GET request to /sections and extracts the "data" JSON array.
     *
     * @return List of Section objects
     * @throws IOException if serialization or communication fails
     * @throws InterruptedException if the request is interrupted
     */
    public List<Section> list() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + TokenStore.get())
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("data");

        return mapper.readValue(data.traverse(), new TypeReference<>() {});
    }

    // ==================== GET BY ID ====================

    /**
     * Fetch a single section by ID.
     *
     * Sends a GET request to /sections/{id}.
     *
     * @param id Section ID
     * @return Section object
     * @throws IOException if serialization or communication fails
     * @throws InterruptedException if interrupted
     */
    public Section get(Long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + TokenStore.get())
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("data");

        return mapper.readValue(data.traverse(), Section.class);
    }

    // ==================== CREATE ====================

    /**
     * Create a new section.
     *
     * Sends a POST request to /sections with a JSON body.
     *
     * @param section Section to be created
     * @return Created Section object
     * @throws IOException if serialization or communication fails
     * @throws InterruptedException if interrupted
     */
    public Section create(Section section) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(section);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + TokenStore.get())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("data");

        return mapper.readValue(data.traverse(), Section.class);
    }

    // ==================== UPDATE ====================

    /**
     * Update a section by ID.
     *
     * Sends a PUT request to /sections/{id} with updated fields.
     *
     * @param id Section ID
     * @param section Updated section data
     * @return Updated Section object
     * @throws IOException if serialization or communication fails
     * @throws InterruptedException if interrupted
     */
    public Section update(Long id, Section section) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(section);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + TokenStore.get())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("data");

        return mapper.readValue(data.traverse(), Section.class);
    }

    // ==================== DELETE ====================

    /**
     * Delete a section by ID.
     *
     * Sends a DELETE request to /sections/{id}.
     * No response body is expected.
     *
     * @param id Section ID
     * @throws IOException if communication fails
     * @throws InterruptedException if interrupted
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
