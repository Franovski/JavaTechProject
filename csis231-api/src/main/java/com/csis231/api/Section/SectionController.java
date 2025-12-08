package com.csis231.api.Section;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    // ==================== GET ====================
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSections() {
        List<Section> sections = sectionService.getAllSections();
        Map<String, Object> response = new HashMap<>();

        if (sections.isEmpty()) {
            response.put("message", "No sections found");
            response.put("data", Collections.emptyList());
            response.put("count", 0);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("message", "Sections retrieved successfully");
        response.put("count", sections.size());
        response.put("data", sections.stream().map(this::mapSection).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSectionById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Section section = sectionService.getSectionById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Section not found"));
            response.put("message", "Section retrieved successfully");
            response.put("data", mapSection(section));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Map<String, Object>> getSectionsByEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Section> sections = sectionService.getSectionsByEvent(eventId);
            if (sections.isEmpty()) {
                response.put("message", "No sections found for this event");
                response.put("data", Collections.emptyList());
                response.put("count", 0);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("message", "Sections retrieved successfully");
            response.put("count", sections.size());
            response.put("data", sections.stream().map(this::mapSection).collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getSectionsByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Section.SectionStatus sectionStatus = Section.SectionStatus.valueOf(status.toUpperCase());
            List<Section> sections = sectionService.getSectionsByStatus(sectionStatus);
            if (sections.isEmpty()) {
                response.put("message", "No sections found with status: " + sectionStatus);
                response.put("data", Collections.emptyList());
                response.put("count", 0);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("message", "Sections retrieved successfully");
            response.put("count", sections.size());
            response.put("data", sections.stream().map(this::mapSection).collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", "Invalid status. Valid values: " +
                    Arrays.toString(Section.SectionStatus.values()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== CREATE ====================
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSection(@RequestBody Section section) {
        Map<String, Object> response = new HashMap<>();
        try {
            Section saved = sectionService.createSection(section);
            response.put("message", "Section created successfully");
            response.put("data", mapSection(saved));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== UPDATE ====================
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSection(@PathVariable Long id, @RequestBody Section section) {
        Map<String, Object> response = new HashMap<>();
        try {
            Section saved = sectionService.updateSection(id, section);
            response.put("message", "Section updated successfully");
            response.put("data", mapSection(saved));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("error", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
        }
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSection(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            sectionService.deleteSection(id);
            response.put("message", "Section deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ==================== HELPER ====================
    private Map<String, Object> mapSection(Section s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("name", s.getName());
        map.put("rowCount", s.getRowCount());
        map.put("seatCount", s.getSeatCount());
        map.put("status", s.getStatus().name());
        map.put("eventId", s.getEvent().getId());
        map.put("eventName", s.getEvent().getName());
        return map;
    }
}
