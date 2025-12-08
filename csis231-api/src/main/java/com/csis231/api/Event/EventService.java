package com.csis231.api.Event;

import com.csis231.api.Section.SectionService;
import com.csis231.api.category.Category;
import com.csis231.api.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final SectionService sectionService;

    /**
     * Retrieve all events from the database
     * @return List of all events
     */
    public List<Event> getAllEvents() {
        log.info("Fetching all events");
        return eventRepository.findAll();
    }

    /**
     * Retrieve a single event by its ID
     * @param id Event ID
     * @return Optional containing the event if found
     */
    public Optional<Event> getEventById(Long id) {
        log.info("Fetching event with ID: {}", id);
        return eventRepository.findById(id);
    }

    /**
     * Retrieve all events belonging to a specific category
     * @param categoryId Category ID
     * @return List of events in the category
     * @throws IllegalArgumentException if category doesn't exist
     */
    public List<Event> getEventsByCategory(Long categoryId) {
        log.info("Fetching events for category ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        return eventRepository.findByCategory(category);
    }

    /**
     * Retrieve all events with a specific status
     * @param status Event status
     * @return List of events with the given status
     */
    public List<Event> getEventsByStatus(Event.EventStatus status) {
        log.info("Fetching events with status: {}", status);
        return eventRepository.findByStatus(status);
    }

    /**
     * Retrieve all events occurring on a specific date
     * @param date The date to search for
     * @return List of events on that date
     */
    public List<Event> getEventsByDate(LocalDate date) {
        log.info("Fetching events for date: {}", date);
        return eventRepository.findByDate(date);
    }

    /**
     * Retrieve all upcoming events (after today)
     * @return List of upcoming events
     */
    public List<Event> getUpcomingEvents() {
        LocalDate today = LocalDate.now();
        log.info("Fetching upcoming events after: {}", today);
        return eventRepository.findByDateAfter(today);
    }

    /**
     * Retrieve all upcoming events after a specific date
     * @param date The date to search from
     * @return List of events after the given date
     */
    public List<Event> getUpcomingEventsAfter(LocalDate date) {
        log.info("Fetching events after date: {}", date);
        return eventRepository.findByDateAfter(date);
    }

    /**
     * Retrieve all events between two dates (inclusive), sorted by date and time,
     * with statuses auto-updated based on the current date.
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return List of events in the range with updated statuses
     */
    public List<Event> getEventsBetweenDatesWithStatus(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching events between {} and {} with auto-updated status", startDate, endDate);

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates must not be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        List<Event> events = eventRepository.findByDateBetweenOrderByDateAscTimeAsc(startDate, endDate);

        // Update status for each event based on date
        for (Event event : events) {
            updateEventStatusBasedOnDate(event);
        }

        return events;
    }



    /**
     * Create a new event with comprehensive validation
     * @param event Event to create
     * @return The saved event
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Event createEvent(Event event) {
        log.info("Creating new event: {}", event.getName());

        // Validate required fields
        validateEventRequiredFields(event);

        // Validate category exists
        Category category = validateAndGetCategory(event.getCategory().getId());
        event.setCategory(category);

        // Validate business rules
        validateEventBusinessRules(event);

        // Check for duplicates
        if (eventRepository.existsByNameAndDateAndTime(event.getName(), event.getDate(), event.getTime())) {
            throw new IllegalArgumentException("An event with this name, date, and time already exists");
        }

        // Set default status if not provided
        if (event.getStatus() == null) {
            event.setStatus(Event.EventStatus.ACTIVE);
        }

        // Auto-update status based on date
        updateEventStatusBasedOnDate(event);

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());

        return savedEvent;
    }

    /**
     * Update an existing event
     * @param id Event ID to update
     * @param updatedEvent Event data to update with
     * @return The updated event
     * @throws IllegalArgumentException if event not found or validation fails
     */
    @Transactional
    public Event updateEvent(Long id, Event updatedEvent) {
        log.info("Updating event with ID: {}", id);

        // Check if event exists
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));

        // Validate required fields
        validateEventRequiredFields(updatedEvent);

        // Validate category exists
        Category category = validateAndGetCategory(updatedEvent.getCategory().getId());

        // Validate business rules
        validateEventBusinessRules(updatedEvent);

        // Check for duplicates (excluding current event)
        if (isDuplicateEvent(existingEvent, updatedEvent)) {
            throw new IllegalArgumentException("An event with this name, date, and time already exists");
        }

        // Update fields
        existingEvent.setName(updatedEvent.getName());
        existingEvent.setDate(updatedEvent.getDate());
        existingEvent.setTime(updatedEvent.getTime());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setCapacity(updatedEvent.getCapacity());
        existingEvent.setStatus(updatedEvent.getStatus());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setImage(updatedEvent.getImage());
        existingEvent.setCategory(category);

        // Auto-update status based on date
        updateEventStatusBasedOnDate(existingEvent);

        Event savedEvent = eventRepository.save(existingEvent);
        log.info("Event updated successfully with ID: {}", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Delete an event by ID
     * @param id Event ID to delete
     * @throws IllegalArgumentException if event not found
     */
    @Transactional
    public void deleteEvent(Long id) {
        log.info("Deleting event with ID: {}", id);

        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event not found with ID: " + id);
        }

        eventRepository.deleteById(id);
        log.info("Event deleted successfully with ID: {}", id);
    }

    /**
     * Check if an event exists by ID
     * @param id Event ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }

    /**
     * Cancel an event (change status to CANCELLED)
     * @param id Event ID
     * @return The cancelled event
     * @throws IllegalArgumentException if event not found
     */
    @Transactional
    public Event cancelEvent(Long id) {
        log.info("Cancelling event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));

        if (event.getStatus() == Event.EventStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed event");
        }

        event.setStatus(Event.EventStatus.CANCELLED);
        Event savedEvent = eventRepository.save(event);

        log.info("Event cancelled successfully with ID: {}", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Mark an event as completed
     * @param id Event ID
     * @return The completed event
     * @throws IllegalArgumentException if event not found
     */
    @Transactional
    public Event completeEvent(Long id) {
        log.info("Completing event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));

        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled event");
        }

        event.setStatus(Event.EventStatus.COMPLETED);
        Event savedEvent = eventRepository.save(event);

        log.info("Event completed successfully with ID: {}", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Update capacity for an event
     * @param id Event ID
     * @param newCapacity New capacity value
     * @return The updated event
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Event updateCapacity(Long id, int newCapacity) {
        log.info("Updating capacity for event ID: {} to {}", id, newCapacity);

        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));

        event.setCapacity(newCapacity);
        return eventRepository.save(event);
    }

    // ==================== Private Validation Methods ====================

    /**
     * Validate all required fields are present
     */
    private void validateEventRequiredFields(Event event) {
        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name is required");
        }

        if (event.getDate() == null) {
            throw new IllegalArgumentException("Event date is required");
        }

        if (event.getTime() == null) {
            throw new IllegalArgumentException("Event time is required");
        }

        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Event location is required");
        }

        if (event.getCategory() == null || event.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    /**
     * Validate business rules (capacity, date constraints, etc.)
     */
    private void validateEventBusinessRules(Event event) {
        // Validate capacity
        if (event.getCapacity() <= 0) {
            throw new IllegalArgumentException("Event capacity must be greater than 0");
        }

        // Validate name length
        if (event.getName().length() > 100) {
            throw new IllegalArgumentException("Event name cannot exceed 100 characters");
        }

        // Validate location length
        if (event.getLocation().length() > 150) {
            throw new IllegalArgumentException("Event location cannot exceed 150 characters");
        }
    }

    /**
     * Validate category exists and return it
     */
    private Category validateAndGetCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));
    }

    /**
     * Check if updated event would be a duplicate
     */
    private boolean isDuplicateEvent(Event existing, Event updated) {
        // If name, date, or time changed, check for duplicates
        boolean hasChanged = !existing.getName().equalsIgnoreCase(updated.getName())
                || !existing.getDate().equals(updated.getDate())
                || !existing.getTime().equals(updated.getTime());

        return hasChanged && eventRepository.existsByNameAndDateAndTime(
                updated.getName(),
                updated.getDate(),
                updated.getTime()
        );
    }

    /**
     * Automatically update event status based on date
     * UPCOMING: Future date
     * ACTIVE: Today's date
     * COMPLETED: Past date (if not already cancelled)
     */
    private void updateEventStatusBasedOnDate(Event event) {
        LocalDate today = LocalDate.now();

        // Don't override if manually set to CANCELLED
        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            return;
        }

        if (event.getDate().isAfter(today)) {
            event.setStatus(Event.EventStatus.UPCOMING);
        } else if (event.getDate().isBefore(today)) {
            // Automatically set past events to COMPLETED
            event.setStatus(Event.EventStatus.COMPLETED);
            log.info("Event date {} is in the past. Status automatically set to COMPLETED", event.getDate());
        } else {
            // Today's event - keep as ACTIVE or UPCOMING
            if (event.getStatus() != Event.EventStatus.ACTIVE) {
                event.setStatus(Event.EventStatus.ACTIVE);
            }
        }
    }
}