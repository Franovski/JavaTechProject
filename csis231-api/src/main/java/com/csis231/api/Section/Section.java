package com.csis231.api.Section;

import com.csis231.api.Event.Event;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "sections",
        uniqueConstraints = @UniqueConstraint(columnNames = { "section_name", "event_id" })
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long id;

    @Column(name = "section_name", nullable = false, length = 100)
    private String name;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(name = "seat_count", nullable = false)
    private int seatCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_status", nullable = false, length = 20)
    @Builder.Default
    private SectionStatus status = SectionStatus.ACTIVE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Event event;


    // Enum for section status
    public enum SectionStatus {
        ACTIVE,
        INACTIVE,
        CLOSED
    }
}
