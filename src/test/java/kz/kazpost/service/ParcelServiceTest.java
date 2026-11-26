package kz.kazpost.service;

import kz.kazpost.exception.ParcelNotFoundException;
import kz.kazpost.model.*;
import kz.kazpost.repository.CrudDao;
import kz.kazpost.util.TrackingNumberGenerator;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParcelServiceTest {

    private static class MemoryDao implements CrudDao<Parcel> {
        private final Map<String, Parcel> parcels = new LinkedHashMap<>();

        @Override
        public void save(Parcel parcel) {
            parcels.put(parcel.getTrackingNumber(), parcel);
        }

        @Override
        public Optional<Parcel> findById(String id) {
            return Optional.ofNullable(parcels.get(id));
        }

        @Override
        public List<Parcel> findAll() {
            return new ArrayList<>(parcels.values());
        }

        @Override
        public void update(Parcel parcel) {
            parcels.put(parcel.getTrackingNumber(), parcel);
        }
    }

    private ParcelService createService(MemoryDao dao) {
        return new ParcelService(dao, new TrackingNumberGenerator());
    }

    private Parcel createParcel(double weight) {
        return new Parcel(
                new Sender("Айжан", "+77010000001", "Алматы"),
                new Recipient("Аружан", "+77020000002", "Астана"),
                new PostOffice(1, "Алматы бөлімшесі", "Алматы"),
                weight
        );
    }

    @Test
    void registerParcelGeneratesTrackingNumber() throws SQLException {
        MemoryDao dao = new MemoryDao();
        ParcelService service = createService(dao);

        Parcel parcel = service.registerParcel(createParcel(2.5));

        assertNotNull(parcel.getTrackingNumber());
        assertTrue(parcel.getTrackingNumber().startsWith("KZ"));
        assertTrue(dao.findById(parcel.getTrackingNumber()).isPresent());
    }

    @Test
    void findParcelReturnsRegisteredParcel() throws SQLException {
        ParcelService service = createService(new MemoryDao());
        Parcel saved = service.registerParcel(createParcel(2.5));

        Optional<Parcel> result = service.findParcel(saved.getTrackingNumber());

        assertTrue(result.isPresent());
        assertEquals(saved.getTrackingNumber(),
                result.get().getTrackingNumber());
    }

    @Test
    void updateStatusChangesParcelStatus() throws SQLException {
        ParcelService service = createService(new MemoryDao());
        Parcel saved = service.registerParcel(createParcel(2.5));

        service.updateStatus(
                saved.getTrackingNumber(), DeliveryStatus.ACCEPTED);

        Parcel updated = service.findParcel(saved.getTrackingNumber())
                .orElseThrow();

        assertEquals(DeliveryStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    void updateStatusThrowsWhenParcelDoesNotExist() {
        ParcelService service = createService(new MemoryDao());

        assertThrows(ParcelNotFoundException.class,
                () -> service.updateStatus("KZ-NOT-FOUND",
                        DeliveryStatus.ACCEPTED));
    }

    @Test
    void statisticsAndWeightFilterWork() throws SQLException {
        ParcelService service = createService(new MemoryDao());

        service.registerParcel(createParcel(1.0));
        service.registerParcel(createParcel(3.0));

        assertEquals(2, service.getAllParcels().size());
        assertEquals(2, service.getStatistics().get(DeliveryStatus.REGISTERED));
        assertEquals(1, service.findParcelsByMinimumWeight(2.0).size());
        assertEquals(2, service.getUniqueTrackingNumbers().size());
    }
}
