package kz.kazpost.model;

import java.time.LocalDateTime;

public class TrackingEvent {
    private final long eventId;
    private final DeliveryStatus status;
    private final String description;
    private final LocalDateTime eventTime;

    public TrackingEvent(long eventId, DeliveryStatus status,
                         String description, LocalDateTime eventTime) {
        if (status == null || eventTime == null) {
            throw new IllegalArgumentException("Статус пен уақыт міндетті");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Оқиға сипаттамасы бос болмауы керек");
        }

        this.eventId = eventId;
        this.status = status;
        this.description = description.trim();
        this.eventTime = eventTime;
    }

    public TrackingEvent(DeliveryStatus status, String description) {
        this(0, status, description, LocalDateTime.now());
    }

    public long getEventId() {
        return eventId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}
