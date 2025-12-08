package com.csis231.api.Ticket;

import com.csis231.api.Event.Event;
import com.csis231.api.Section.Section;
import com.csis231.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find all tickets for a specific event
    List<Ticket> findByEvent(Event event);

    // Find all tickets for a specific section
    List<Ticket> findBySection(Section section);

    // Find all tickets belonging to a specific user
    List<Ticket> findByUser(User user);

    // Find all tickets by status (e.g., VALID, EXPIRED, USED)
    List<Ticket> findByStatus(Ticket.TicketStatus status);
}
