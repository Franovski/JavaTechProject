package com.csis231.api.Section;

import com.csis231.api.Event.Event;
import com.csis231.api.Event.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SectionService {

    private final SectionRepository sectionRepository;
    private final EventRepository eventRepository;

    // ==================== GET ====================

    /**
     * Retrieve all sections
     * @return List of all sections
     */
    public List<Section> getAllSections() {
        log.info("Fetching all sections");
        return sectionRepository.findAll();
    }

    /**
     * Retrieve a section by ID
     * @param id Section ID
     * @return Optional containing the section if found
     */
    public Optional<Section> getSectionById(Long id) {
        log.info("Fetching section with ID: {}", id);
        return sectionRepository.findById(id);
    }

    /**
     * Retrieve all sections belonging to a specific event
     * @param eventId Event ID
     * @return List of sections linked to the given event
     * @throws IllegalArgumentException if event not found
     */
    public List<Section> getSectionsByEvent(Long eventId) {
        log.info("Fetching sections for event ID: {}", eventId);
        Event event = validateAndGetEvent(eventId);
        return sectionRepository.findByEvent(event);
    }

    /**
     * Retrieve all sections with a given status
     * @param status Section status
     * @return List of sections with the given status
     */
    public List<Section> getSectionsByStatus(Section.SectionStatus status) {
        log.info("Fetching sections with status: {}", status);
        return sectionRepository.findAll().stream()
                .filter(s -> s.getStatus() == status)
                .toList();
    }

    // ==================== CREATE ====================

    /**
     * Create a new section with validation
     * @param section Section object to create
     * @return The created section
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Section createSection(Section section) {
        log.info("Creating new section: {}", section.getName());

        // Validate required fields
        validateSectionRequiredFields(section);

        // Validate event exists
        Event event = validateAndGetEvent(section.getEvent().getId());

        // Validate business rules
        validateBusinessRules(section);

        // Check for duplicates
        if (sectionRepository.existsByNameAndEvent(section.getName(), event)) {
            throw new IllegalArgumentException("Section with this name already exists for this event");
        }

        // Set event reference
        section.setEvent(event);

        // Set default status
        if (section.getStatus() == null) {
            section.setStatus(Section.SectionStatus.ACTIVE);
        }

        Section saved = sectionRepository.save(section);
        log.info("Section created successfully with ID: {}", saved.getId());
        return saved;
    }

    // ==================== UPDATE ====================

    /**
     * Update an existing section
     * @param id Section ID to update
     * @param updatedSection Section data used for update
     * @return Updated section object
     * @throws IllegalArgumentException if validation fails or section not found
     */
    @Transactional
    public Section updateSection(Long id, Section updatedSection) {
        log.info("Updating section with ID: {}", id);

        // Fetch existing section
        Section existing = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + id));

        // Validate event
        Event event = validateAndGetEvent(updatedSection.getEvent().getId());

        // Check duplicate name for same event
        if (!existing.getName().equalsIgnoreCase(updatedSection.getName()) &&
                sectionRepository.existsByNameAndEvent(updatedSection.getName(), event)) {
            throw new IllegalArgumentException("Section with this name already exists for this event");
        }

        // Validate required fields
        validateSectionRequiredFields(updatedSection);

        // Validate business rules
        validateBusinessRules(updatedSection);

        // Update fields
        existing.setName(updatedSection.getName());
        existing.setRowCount(updatedSection.getRowCount());
        existing.setSeatCount(updatedSection.getSeatCount());

        // Cannot activate section unless parent event is ACTIVE
        if (existing.getStatus() == Section.SectionStatus.INACTIVE &&
                updatedSection.getStatus() == Section.SectionStatus.ACTIVE &&
                event.getStatus() != Event.EventStatus.ACTIVE) {
            throw new IllegalStateException("Cannot activate section because parent event is not ACTIVE");
        }

        existing.setStatus(updatedSection.getStatus());
        existing.setEvent(event);

        Section saved = sectionRepository.save(existing);
        log.info("Section updated successfully with ID: {}", saved.getId());
        return saved;
    }

    // ==================== DELETE ====================

    /**
     * Delete a section by ID
     * @param id Section ID
     * @throws IllegalArgumentException if section not found
     */
    @Transactional
    public void deleteSection(Long id) {
        log.info("Deleting section with ID: {}", id);

        if (!sectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Section not found with ID: " + id);
        }

        sectionRepository.deleteById(id);
        log.info("Section deleted successfully with ID: {}", id);
    }

    // ==================== PRIVATE VALIDATION METHODS ====================

    /**
     * Validate all required fields for a section
     * @param section Section to validate
     */
    private void validateSectionRequiredFields(Section section) {
        if (section.getName() == null || section.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Section name is required");
        }
        if (section.getRowCount() <= 0) {
            throw new IllegalArgumentException("Row count is required and must be > 0");
        }
        if (section.getSeatCount() <= 0) {
            throw new IllegalArgumentException("Seat count is required and must be > 0");
        }
        if (section.getEvent() == null || section.getEvent().getId() == null) {
            throw new IllegalArgumentException("Event is required for section");
        }
    }

    /**
     * Validate business rules (seat limits, name length, event status)
     * @param section Section to validate
     */
    private void validateBusinessRules(Section section) {
        if (section.getRowCount() <= 0) {
            throw new IllegalArgumentException("Row count must be > 0");
        }
        if (section.getSeatCount() <= 0) {
            throw new IllegalArgumentException("Seat count must be > 0");
        }
        if (section.getName().length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 chars");
        }

        int totalSeats = section.getRowCount() * section.getSeatCount();
        if (totalSeats > 10000) {
            throw new IllegalArgumentException("Total seats cannot exceed 10,000");
        }

        Event event = validateAndGetEvent(section.getEvent().getId());

        // Cannot modify sections for cancelled or completed events
        if (event.getStatus() == Event.EventStatus.CANCELLED ||
                event.getStatus() == Event.EventStatus.COMPLETED) {
            throw new IllegalStateException("Cannot create/update sections for cancelled or completed events");
        }
    }

    /**
     * Validate event exists
     * @param eventId Event ID
     * @return Event object
     * @throws IllegalArgumentException if event not found
     */
    private Event validateAndGetEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
    }
}
