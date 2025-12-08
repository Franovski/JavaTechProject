package com.csis231.api.Event;

import com.csis231.api.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Find all events belonging to a specific category
    List<Event> findByCategory(Category category);

    // Find all events by status (e.g., ACTIVE, CANCELLED, COMPLETED)
    List<Event> findByStatus(Event.EventStatus status);

    // Find all events occurring on a specific date
    List<Event> findByDate(LocalDate date);

    // Find all upcoming events (after a given date)
    List<Event> findByDateAfter(LocalDate date);

    // Check if an event with the same name already exists on the same date and time
    boolean existsByNameAndDateAndTime(String name, LocalDate date, java.time.LocalTime time);

    // Find all events between two dates (inclusive), ordered by date then time
    List<Event> findByDateBetweenOrderByDateAscTimeAsc(LocalDate startDate, LocalDate endDate);


}
