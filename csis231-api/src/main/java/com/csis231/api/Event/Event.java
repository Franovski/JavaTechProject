package com.csis231.api.Event;

import com.csis231.api.category.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "events",
        uniqueConstraints = @UniqueConstraint(columnNames = { "name", "date", "time" })
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "date", nullable = false)
    @JsonFormat(pattern = "dd-MM-yyyy") // ✅ Fix for LocalDate
    private LocalDate date;

    @Column(name = "time", nullable = false)
    @JsonFormat(pattern = "HH:mm") // ✅ Fix for LocalTime
    private LocalTime time;

    @Column(name = "location", nullable = false, length = 150)
    private String location;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.ACTIVE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image", length = 255)
    private String image;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    // Enum for event status
    public enum EventStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED,
        UPCOMING
    }
}
