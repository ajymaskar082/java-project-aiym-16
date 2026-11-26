package kz.kazpost.repository;

import kz.kazpost.database.DatabaseConnection;
import kz.kazpost.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SqliteParcelDaoTest {

    @TempDir
    Path tempDir;

    private SqliteParcelDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection database =
                new DatabaseConnection(tempDir.resolve("test.db"));
        database.initializeSchema();
        dao = new SqliteParcelDao(database);
    }

    private Parcel createParcel(String trackingNumber) {
        Parcel parcel = new Parcel(
                new Sender("Айжан", "+77010000001", "Алматы"),
                new Recipient("Аружан", "+77020000002", "Астана"),
                new PostOffice(1, "Алматы бөлімшесі", "Алматы"),
                2.5
        );
        parcel.setTrackingNumber(trackingNumber);
        return parcel;
    }

    @Test
    void saveAndFindByTrackingNumber() throws SQLException {
        Parcel parcel = createParcel("KZTEST0001");
        dao.save(parcel);

        Optional<Parcel> result = dao.findById("KZTEST0001");

        assertTrue(result.isPresent());
        assertEquals("Айжан", result.get().getSender().getFullName());
        assertEquals(DeliveryStatus.REGISTERED, result.get().getStatus());
        assertEquals(1, result.get().getTrackingHistory().size());
    }

    @Test
    void findAllReturnsSavedParcels() throws SQLException {
        dao.save(createParcel("KZTEST0001"));
        dao.save(createParcel("KZTEST0002"));

        List<Parcel> parcels = dao.findAll();

        assertEquals(2, parcels.size());
    }

    @Test
    void updatePersistsStatusAndHistory() throws SQLException {
        Parcel parcel = createParcel("KZTEST0003");
        dao.save(parcel);

        parcel.updateStatus(DeliveryStatus.ACCEPTED, "Қабылданды");
        dao.update(parcel);

        Parcel restored = dao.findById("KZTEST0003").orElseThrow();

        assertEquals(DeliveryStatus.ACCEPTED, restored.getStatus());
        assertEquals(2, restored.getTrackingHistory().size());
    }
}
