package com.example.demo.api;

import com.example.demo.model.Event;
import com.example.demo.security.TokenStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.Map;

/**
 * API client for interacting with the Event endpoints.
 *
 * <p>This service provides full CRUD operations, event state updates (cancel/complete),
 * capacity modifications, and filtered retrieval. All requests automatically include
 * the Authorization token stored in {@link TokenStore}. All responses are parsed and
 * the "data" field is extracted into strongly typed models.
 *
 * <p>Base URL: {@code /events}
 */
public class EventApi {

    private static final String BASE_URL = "http://localhost:8080/events";
    private final HttpClient client;
    private final ObjectMapper mapper;

    /**
     * Initializes the Event API client with a default {@link HttpClient}
     * and {@link ObjectMapper}.
     */
    public EventApi() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Builds an authorized request with JSON headers and the current token.
     *
     * @param uri the endpoint URL
     * @return configured {@link HttpRequest.Builder}
     */
    private HttpRequest.Builder authorizedRequest(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + TokenStore.get());
    }

    // =========================================================
    //                       CRUD OPERATIONS
    // =========================================================

    /**
     * Retrieves all events.
     *
     * @return a list of {@link Event}
     * @throws IOException if parsing or server error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<Event> list() throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> body = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(body.get("data"), new TypeReference<List<Event>>() {});
        }

        throw new IOException("Failed to load events: " + res.body());
    }

    /**
     * Retrieves a specific event by ID.
     *
     * @param id the event ID
     * @return the {@link Event} object
     * @throws IOException if the event does not exist or parsing fails
     * @throws InterruptedException if the request is interrupted
     */
    public Event get(Long id) throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/" + id).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> body = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(body.get("data"), Event.class);
        }

        throw new IOException("Event not found: " + res.body());
    }

    /**
     * Convenience method to fetch only the event name by ID.
     *
     * @param id the event ID
     * @return event name or null if unavailable
     */
    public String getEventNameById(Long id) {
        try {
            Event event = get(id);
            return event != null ? event.getName() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Creates a new event.
     *
     * @param event the event payload
     * @return the created {@link Event}
     * @throws IOException if creation fails
     * @throws InterruptedException if interrupted
     */
    public Event create(Event event) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(event);

        HttpRequest req = authorizedRequest(BASE_URL)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 201) {
            Map<String, Object> response = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(response.get("data"), Event.class);
        }

        throw new IOException("Failed to create event: " + res.body());
    }

    /**
     * Updates an existing event.
     *
     * @param id the event ID
     * @param event the updated event payload
     * @return the updated {@link Event}
     * @throws IOException if server rejects update
     * @throws InterruptedException if interrupted
     */
    public Event update(Long id, Event event) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(event);

        HttpRequest req = authorizedRequest(BASE_URL + "/" + id)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> response = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(response.get("data"), Event.class);
        }

        throw new IOException("Failed to update event: " + res.body());
    }

    /**
     * Deletes an event by ID.
     *
     * @param id the event ID
     * @throws IOException if deletion fails
     * @throws InterruptedException if interrupted
     */
    public void delete(Long id) throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/" + id).DELETE().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new IOException("Failed to delete event: " + res.body());
        }
    }

    // =========================================================
    //                   STATUS & CAPACITY UPDATES
    // =========================================================

    /**
     * Cancels an event.
     *
     * @param id the event ID
     * @return updated {@link Event} after cancellation
     * @throws IOException if the operation fails
     * @throws InterruptedException if interrupted
     */
    public Event cancel(Long id) throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/" + id + "/cancel")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> response = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(response.get("data"), Event.class);
        }

        throw new IOException("Failed to cancel event: " + res.body());
    }

    /**
     * Marks an event as completed.
     *
     * @param id the event ID
     * @return updated {@link Event}
     * @throws IOException if updating fails
     * @throws InterruptedException if interrupted
     */
    public Event complete(Long id) throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/" + id + "/complete")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> response = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(response.get("data"), Event.class);
        }

        throw new IOException("Failed to complete event: " + res.body());
    }

    /**
     * Updates an event's capacity.
     *
     * @param id the event ID
     * @param capacity the new capacity value
     * @return updated {@link Event}
     * @throws IOException if update fails
     * @throws InterruptedException if interrupted
     */
    public Event updateCapacity(Long id, int capacity) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(Map.of("capacity", capacity));

        HttpRequest req = authorizedRequest(BASE_URL + "/" + id + "/capacity")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> response = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(response.get("data"), Event.class);
        }

        throw new IOException("Failed to update capacity: " + res.body());
    }

    // =========================================================
    //                         FILTERS
    // =========================================================

    /**
     * Retrieves events by category ID.
     *
     * @param categoryId the category ID
     * @return list of events
     * @throws IOException if loading fails
     * @throws InterruptedException if interrupted
     */
    public List<Event> getByCategory(Long categoryId) throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/category/" + categoryId).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> body = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(body.get("data"), new TypeReference<List<Event>>() {});
        }

        throw new IOException("Failed to load events by category: " + res.body());
    }

    /**
     * Retrieves events filtered by status.
     *
     * @param status status string (e.g. "ACTIVE", "CANCELED")
     * @return list of events
     */
    public List<Event> getByStatus(String status) throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/status/" + status).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> body = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(body.get("data"), new TypeReference<List<Event>>() {});
        }

        throw new IOException("Failed to load events by status: " + res.body());
    }

    /**
     * Retrieves events by exact date.
     *
     * @param date format: dd-MM-yyyy
     * @return list of events occurring on that date
     */
    public List<Event> getByDate(String date) throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/date/" + date).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> body = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(body.get("data"), new TypeReference<List<Event>>() {});
        }

        throw new IOException("Failed to load events by date: " + res.body());
    }

    /**
     * Retrieves events between two dates.
     *
     * @param start start date (dd-MM-yyyy)
     * @param end end date (dd-MM-yyyy)
     * @return list of events
     */
    public List<Event> getBetween(String start, String end) throws IOException, InterruptedException {
        String uri = BASE_URL + "/between?start=" + start + "&end=" + end;

        HttpRequest req = authorizedRequest(uri).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> body = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(body.get("data"), new TypeReference<List<Event>>() {});
        }

        throw new IOException("Failed to load events between dates: " + res.body());
    }

    /**
     * Retrieves all upcoming events.
     *
     * @return list of upcoming events
     */
    public List<Event> getUpcoming() throws IOException, InterruptedException {
        HttpRequest req = authorizedRequest(BASE_URL + "/upcoming").GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            Map<String, Object> body = mapper.readValue(res.body(), new TypeReference<>() {});
            return mapper.convertValue(body.get("data"), new TypeReference<List<Event>>() {});
        }

        throw new IOException("Failed to load upcoming events: " + res.body());
    }
}
