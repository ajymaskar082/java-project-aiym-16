package kz.kazpost.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParcelTest {

    private Parcel createParcel(double weight) {
        return new Parcel(
                new Sender("Айжан", "+77010000001", "Алматы"),
                new Recipient("Аружан", "+77020000002", "Астана"),
                new PostOffice(1, "Алматы бөлімшесі", "Алматы"),
                weight
        );
    }

    @Test
    void parcelStartsWithRegisteredStatus() {
        Parcel parcel = createParcel(2.5);
        assertEquals(DeliveryStatus.REGISTERED, parcel.getStatus());
        assertEquals(1, parcel.getTrackingHistory().size());
    }

    @Test
    void parcelCanChangeStatusToAccepted() {
        Parcel parcel = createParcel(2.5);
        parcel.updateStatus(DeliveryStatus.ACCEPTED, "Қабылданды");

        assertEquals(DeliveryStatus.ACCEPTED, parcel.getStatus());
        assertEquals(2, parcel.getTrackingHistory().size());
    }

    @Test
    void parcelRejectsInvalidStatusTransition() {
        Parcel parcel = createParcel(2.5);

        assertThrows(IllegalStateException.class,
                () -> parcel.updateStatus(
                        DeliveryStatus.DELIVERED, "Жеткізілді"));
    }

    @Test
    void parcelRejectsInvalidWeight() {
        assertThrows(IllegalArgumentException.class,
                () -> createParcel(-1));

        assertThrows(IllegalArgumentException.class,
                () -> createParcel(0));
    }

    @Test
    void trackingHistoryCannotBeModifiedExternally() {
        Parcel parcel = createParcel(2.5);

        assertThrows(UnsupportedOperationException.class,
                () -> parcel.getTrackingHistory().clear());
    }
}
