package com.csis231.api.Event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    // ✅ GET all events
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        Map<String, Object> response = new HashMap<>();

        if (events.isEmpty()) {
            response.put("message", "No events found");
            response.put("data", Collections.emptyList());
            response.put("count", 0);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("message", "Events retrieved successfully");
        response.put("count", events.size());
        response.put("data", events);
        return ResponseEntity.ok(response);
    }

    // ✅ GET event by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEventById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Event> eventOpt = eventService.getEventById(id);

        if (eventOpt.isEmpty()) {
            response.put("error", "Event not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("message", "Event retrieved successfully");
        response.put("data", eventOpt.get());
        return ResponseEntity.ok(response);
    }

    // ✅ GET events by category ID
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getEventsByCategory(@PathVariable Long categoryId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Event> events = eventService.getEventsByCategory(categoryId);

            if (events.isEmpty()) {
                response.put("message", "No events found for this category");
                response.put("data", Collections.emptyList());
                response.put("count", 0);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("message", "Events retrieved successfully");
            response.put("count", events.size());
            response.put("data", events);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ✅ GET events by status
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getEventsByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        Event.EventStatus eventStatus;

        try {
            eventStatus = Event.EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            response.put("error", "Invalid status. Valid statuses: " + Arrays.toString(Event.EventStatus.values()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        List<Event> events = eventService.getEventsByStatus(eventStatus);
        if (events.isEmpty()) {
            response.put("message", "No events found with status: " + eventStatus);
            response.put("data", Collections.emptyList());
            response.put("count", 0);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("message", "Events retrieved successfully");
        response.put("count", events.size());
        response.put("data", events);
        return ResponseEntity.ok(response);
    }

    // ✅ GET events by date
    @GetMapping("/date/{date}")
    public ResponseEntity<Map<String, Object>> getEventsByDate(@PathVariable String date) {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate localDate = LocalDate.parse(date, dateFormatter);
            List<Event> events = eventService.getEventsByDate(localDate);

            if (events.isEmpty()) {
                response.put("message", "No events found on date: " + date);
                response.put("data", Collections.emptyList());
                response.put("count", 0);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("message", "Events retrieved successfully");
            response.put("count", events.size());
            response.put("data", events);
            return ResponseEntity.ok(response);

        } catch (DateTimeParseException e) {
            response.put("error", "Invalid date format. Use dd-MM-yyyy");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // GET events between two dates
    @GetMapping("/between")
    public ResponseEntity<Map<String, Object>> getEventsBetween(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {

        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate startDate = LocalDate.parse(start, dateFormatter);
            LocalDate endDate = LocalDate.parse(end, dateFormatter);

            List<Event> events = eventService.getEventsBetweenDatesWithStatus(startDate, endDate);

            if (events.isEmpty()) {
                response.put("message", "No events found in the given date range");
                response.put("data", Collections.emptyList());
                response.put("count", 0);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("message", "Events retrieved successfully");
            response.put("count", events.size());
            response.put("data", events);
            return ResponseEntity.ok(response);

        } catch (DateTimeParseException e) {
            response.put("error", "Invalid date format. Use dd-MM-yyyy");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ✅ GET upcoming events
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingEvents() {
        List<Event> events = eventService.getUpcomingEvents();
        Map<String, Object> response = new HashMap<>();

        if (events.isEmpty()) {
            response.put("message", "No upcoming events found");
            response.put("data", Collections.emptyList());
            response.put("count", 0);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("message", "Upcoming events retrieved successfully");
        response.put("count", events.size());
        response.put("data", events);
        return ResponseEntity.ok(response);
    }

    // ✅ CREATE event
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Event event) {
        Map<String, Object> response = new HashMap<>();

        try {
            Event savedEvent = eventService.createEvent(event);
            response.put("message", "Event created successfully");
            response.put("data", savedEvent);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to create event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ UPDATE event
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEvent(@PathVariable Long id, @RequestBody Event updatedEvent) {
        Map<String, Object> response = new HashMap<>();

        try {
            Event savedEvent = eventService.updateEvent(id, updatedEvent);
            response.put("message", "Event updated successfully");
            response.put("data", savedEvent);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to update event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ DELETE event
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEvent(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            eventService.deleteEvent(id);
            response.put("message", "Event deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to delete event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ CANCEL event
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelEvent(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Event cancelledEvent = eventService.cancelEvent(id);
            response.put("message", "Event cancelled successfully");
            response.put("data", cancelledEvent);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to cancel event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ COMPLETE event
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeEvent(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Event completedEvent = eventService.completeEvent(id);
            response.put("message", "Event completed successfully");
            response.put("data", completedEvent);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to complete event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ UPDATE event capacity
    @PatchMapping("/{id}/capacity")
    public ResponseEntity<Map<String, Object>> updateCapacity(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer capacity = request.get("capacity");
            if (capacity == null) {
                response.put("error", "Capacity value is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Event updatedEvent = eventService.updateCapacity(id, capacity);
            response.put("message", "Event capacity updated successfully");
            response.put("data", updatedEvent);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to update capacity: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}