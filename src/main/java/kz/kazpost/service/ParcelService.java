package kz.kazpost.service;

import kz.kazpost.exception.ParcelNotFoundException;
import kz.kazpost.model.DeliveryStatus;
import kz.kazpost.model.Parcel;
import kz.kazpost.repository.CrudDao;
import kz.kazpost.util.TrackingNumberGenerator;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ParcelService {

    private final CrudDao<Parcel> dao;
    private final TrackingNumberGenerator trackingNumberGenerator;

    public ParcelService(CrudDao<Parcel> dao,
                         TrackingNumberGenerator trackingNumberGenerator) {
        if (dao == null || trackingNumberGenerator == null) {
            throw new IllegalArgumentException(
                    "DAO және генератор міндетті"
            );
        }

        this.dao = dao;
        this.trackingNumberGenerator = trackingNumberGenerator;
    }

    // 1. Жаңа сәлемдемені тіркеу
    public Parcel registerParcel(Parcel parcel) throws SQLException {
        if (parcel == null) {
            throw new IllegalArgumentException(
                    "Сәлемдеме бос болмауы керек"
            );
        }

        String trackingNumber = trackingNumberGenerator.generate();
        parcel.setTrackingNumber(trackingNumber);

        dao.save(parcel);

        return parcel;
    }

    // 2. Трек-нөмір арқылы іздеу
    public Optional<Parcel> findParcel(String trackingNumber)
            throws SQLException {
        validateTrackingNumber(trackingNumber);
        return dao.findById(trackingNumber.trim());
    }

    // 3. Сәлемдеме статусын жаңарту
    public void updateStatus(String trackingNumber,
                             DeliveryStatus newStatus)
            throws SQLException {
        validateTrackingNumber(trackingNumber);

        Parcel parcel = dao.findById(trackingNumber.trim())
                .orElseThrow(() ->
                        new ParcelNotFoundException(trackingNumber));

        parcel.updateStatus(
                newStatus,
                "Статус жаңартылды: " + newStatus.getDisplayName()
        );

        dao.update(parcel);
    }

    // 4. Барлық сәлемдемелер тізімі — List
    public List<Parcel> getAllParcels() throws SQLException {
        return dao.findAll();
    }

    // 5. Статустар бойынша есеп — Map және Stream API
    public Map<DeliveryStatus, Long> getStatistics()
            throws SQLException {
        return dao.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Parcel::getStatus,
                        Collectors.counting()
                ));
    }

    // 6. Бірегей трек-нөмірлер жиыны — Set және Stream API
    public Set<String> getUniqueTrackingNumbers()
            throws SQLException {
        return dao.findAll()
                .stream()
                .map(Parcel::getTrackingNumber)
                .filter(number -> number != null && !number.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // 7. Салмағы бойынша сүзу — Stream filter
    public List<Parcel> findParcelsByMinimumWeight(double minimumWeight)
            throws SQLException {
        if (!Double.isFinite(minimumWeight) || minimumWeight < 0) {
            throw new IllegalArgumentException(
                    "Салмақ теріс болмауы керек"
            );
        }

        return dao.findAll()
                .stream()
                .filter(parcel -> parcel.getWeight() >= minimumWeight)
                .toList();
    }

    private void validateTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Трек-нөмір бос болмауы керек"
            );
        }
    }
}
