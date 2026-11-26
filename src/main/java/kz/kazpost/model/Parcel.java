package kz.kazpost.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parcel {
    private long parcelId;
    private String trackingNumber;
    private final Sender sender;
    private final Recipient recipient;
    private final PostOffice postOffice;
    private final double weight;
    private DeliveryStatus status;
    private final List<TrackingEvent> trackingHistory;

    public Parcel(Sender sender, Recipient recipient,
                  PostOffice postOffice, double weight) {
        if (sender == null || recipient == null || postOffice == null) {
            throw new IllegalArgumentException("Жіберуші, алушы және бөлімше міндетті");
        }
        if (!Double.isFinite(weight) || weight <= 0) {
            throw new IllegalArgumentException("Салмақ оң сан болуы керек");
        }

        this.sender = sender;
        this.recipient = recipient;
        this.postOffice = postOffice;
        this.weight = weight;
        this.status = DeliveryStatus.REGISTERED;
        this.trackingHistory = new ArrayList<>();

        trackingHistory.add(
                new TrackingEvent(status, "Сәлемдеме жүйеде тіркелді")
        );
    }

    public void setParcelId(long parcelId) {
        if (parcelId <= 0) {
            throw new IllegalArgumentException("Parcel ID оң болуы керек");
        }
        this.parcelId = parcelId;
    }

    public void setTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Трек-нөмір бос болмауы керек");
        }
        if (this.trackingNumber != null) {
            throw new IllegalStateException("Трек-нөмір бұрын тағайындалған");
        }
        this.trackingNumber = trackingNumber.trim();
    }

    public void updateStatus(DeliveryStatus newStatus, String description) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Статусты өзгертуге рұқсат жоқ: "
                            + status + " -> " + newStatus
            );
        }

        TrackingEvent event = new TrackingEvent(newStatus, description);
        this.status = newStatus;
        trackingHistory.add(event);
    }

    public void restoreTrackingHistory(List<TrackingEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Статус тарихы бос болмауы керек");
        }

        trackingHistory.clear();
        DeliveryStatus previous = null;

        for (TrackingEvent event : events) {
            if (event == null) {
                throw new IllegalArgumentException("Оқиға null болмауы керек");
            }

            if (previous == null && event.getStatus() != DeliveryStatus.REGISTERED) {
                throw new IllegalArgumentException("Тарих REGISTERED статусынан басталуы керек");
            }

            if (previous != null && !previous.canTransitionTo(event.getStatus())) {
                throw new IllegalArgumentException("Статус тарихында рұқсат етілмеген ауысу бар");
            }

            trackingHistory.add(event);
            previous = event.getStatus();
        }

        status = previous;
    }

    public long getParcelId() {
        return parcelId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Sender getSender() {
        return sender;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public PostOffice getPostOffice() {
        return postOffice;
    }

    public double getWeight() {
        return weight;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public List<TrackingEvent> getTrackingHistory() {
        return Collections.unmodifiableList(
                new ArrayList<>(trackingHistory)
        );
    }
}
