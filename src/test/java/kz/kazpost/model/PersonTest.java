package kz.kazpost.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    void senderHasCorrectRole() {
        Person sender = new Sender("Айжан", "+77010000001", "Алматы");
        assertEquals("Жіберуші", sender.getRole());
    }

    @Test
    void recipientHasCorrectRole() {
        Person recipient = new Recipient("Аружан", "+77020000002", "Астана");
        assertEquals("Алушы", recipient.getRole());
    }

    @Test
    void personFieldsAreTrimmedAndAccessibleThroughGetters() {
        Person person = new Sender("  Айжан  ", " +77010000001 ", " Алматы ");
        assertEquals("Айжан", person.getFullName());
        assertEquals("+77010000001", person.getPhone());
        assertEquals("Алматы", person.getAddress());
    }
}
