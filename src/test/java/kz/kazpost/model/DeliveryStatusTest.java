package kz.kazpost.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryStatusTest {

    @Test
    void allowsValidStatusTransitions() {
        assertTrue(DeliveryStatus.REGISTERED.canTransitionTo(
                DeliveryStatus.ACCEPTED));
        assertTrue(DeliveryStatus.REGISTERED.canTransitionTo(
                DeliveryStatus.LOST));

        assertTrue(DeliveryStatus.ACCEPTED.canTransitionTo(
                DeliveryStatus.IN_TRANSIT));
        assertTrue(DeliveryStatus.ACCEPTED.canTransitionTo(
                DeliveryStatus.LOST));

        assertTrue(DeliveryStatus.IN_TRANSIT.canTransitionTo(
                DeliveryStatus.ARRIVED_AT_OFFICE));
        assertTrue(DeliveryStatus.IN_TRANSIT.canTransitionTo(
                DeliveryStatus.LOST));

        assertTrue(DeliveryStatus.ARRIVED_AT_OFFICE.canTransitionTo(
                DeliveryStatus.IN_TRANSIT));
        assertTrue(DeliveryStatus.ARRIVED_AT_OFFICE.canTransitionTo(
                DeliveryStatus.DELIVERED));
        assertTrue(DeliveryStatus.ARRIVED_AT_OFFICE.canTransitionTo(
                DeliveryStatus.LOST));
    }

    @Test
    void rejectsInvalidStatusTransitions() {
        assertFalse(DeliveryStatus.REGISTERED.canTransitionTo(
                DeliveryStatus.IN_TRANSIT));
        assertFalse(DeliveryStatus.REGISTERED.canTransitionTo(
                DeliveryStatus.DELIVERED));

        assertFalse(DeliveryStatus.ACCEPTED.canTransitionTo(
                DeliveryStatus.REGISTERED));
        assertFalse(DeliveryStatus.ACCEPTED.canTransitionTo(
                DeliveryStatus.DELIVERED));

        assertFalse(DeliveryStatus.IN_TRANSIT.canTransitionTo(
                DeliveryStatus.ACCEPTED));
        assertFalse(DeliveryStatus.IN_TRANSIT.canTransitionTo(
                DeliveryStatus.DELIVERED));

        assertFalse(DeliveryStatus.ARRIVED_AT_OFFICE.canTransitionTo(
                DeliveryStatus.ACCEPTED));
    }

    @Test
    void deliveredAndLostAreTerminalStatuses() {
        for (DeliveryStatus status : DeliveryStatus.values()) {
            assertFalse(DeliveryStatus.DELIVERED.canTransitionTo(status));
            assertFalse(DeliveryStatus.LOST.canTransitionTo(status));
        }
    }

    @Test
    void rejectsNullAndRepeatedStatus() {
        for (DeliveryStatus status : DeliveryStatus.values()) {
            assertFalse(status.canTransitionTo(status));
            assertFalse(status.canTransitionTo(null));
        }
    }
}
