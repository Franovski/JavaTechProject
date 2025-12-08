package com.csis231.api.Section;

import com.csis231.api.Event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    // Check if a section with a given name already exists for a specific event
    boolean existsByNameAndEvent(String name, Event event);

    // Get all sections belonging to a specific event
    List<Section> findByEvent(Event event);
}
