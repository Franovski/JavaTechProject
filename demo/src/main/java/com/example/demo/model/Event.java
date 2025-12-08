package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    private Long id;
    private String name;
    private LocalDate date;
    private LocalTime time;
    private String location;
    private int capacity;
    private EventStatus status;
    private String description;
    private String image;
    private Category category; // ✅ match backend FK

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public enum EventStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED,
        UPCOMING
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // ===== Date Handling =====
    @JsonProperty("date")
    public String getDateString() {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    @JsonProperty("date")
    public void setDateString(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                this.date = LocalDate.parse(value, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                this.date = null;
            }
        } else {
            this.date = null;
        }
    }

    @JsonIgnore
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    // ===== Time Handling =====
    @JsonProperty("time")
    public String getTimeString() {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }

    @JsonProperty("time")
    public void setTimeString(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                this.time = LocalTime.parse(value, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                this.time = null;
            }
        } else {
            this.time = null;
        }
    }

    @JsonIgnore
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    // Convenience for TableView display
    @JsonIgnore
    public String getCategoryName() { return category != null ? category.getName() : ""; }

    @JsonIgnore
    public String getDateFormatted() { return date != null ? date.format(DATE_FORMATTER) : ""; }

    @JsonIgnore
    public String getTimeFormatted() { return time != null ? time.format(TIME_FORMATTER) : ""; }
}
