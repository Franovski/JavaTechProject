package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Section {

    private Long id;
    private String name;
    private int rowCount;
    private int seatCount;
    private SectionStatus status;
    private Event event; // nested event
    private Long eventId; // support backend sending flat eventId
    private String eventName; // support backend sending flat eventName

    public enum SectionStatus {
        ACTIVE, INACTIVE, CLOSED
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }

    public SectionStatus getStatus() { return status; }
    public void setStatus(SectionStatus status) { this.status = status; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Long getEventId() { return eventId; }
    @JsonProperty("eventId")
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventName() {
        if (event != null && event.getName() != null) {
            return event.getName();
        }
        return eventName != null ? eventName : "";
    }

    @JsonProperty("eventName")
    public void setEventName(String eventName) { this.eventName = eventName; }

    // ===== Convenience for TableView =====
    @JsonIgnore
    public String displayEventName() {
        return getEventName();
    }
}
