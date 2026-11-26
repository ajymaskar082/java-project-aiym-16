package kz.kazpost.repository;

import kz.kazpost.database.DatabaseConnection;
import kz.kazpost.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteParcelDao extends Repository<Parcel> {

    public SqliteParcelDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public void save(Parcel parcel) throws SQLException {
        if (parcel == null || parcel.getTrackingNumber() == null) {
            throw new IllegalArgumentException("Тіркелген сәлемдеме және трек-нөмір міндетті");
        }

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int senderId = insertPerson(connection, "senders",
                        parcel.getSender());
                int recipientId = insertPerson(connection, "recipients",
                        parcel.getRecipient());

                savePostOffice(connection, parcel.getPostOffice());

                String sql = """
                    INSERT INTO parcels
                    (tracking_number, sender_id, recipient_id, office_id,
                     weight, status, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {
                    statement.setString(1, parcel.getTrackingNumber());
                    statement.setInt(2, senderId);
                    statement.setInt(3, recipientId);
                    statement.setInt(4, parcel.getPostOffice().getOfficeId());
                    statement.setDouble(5, parcel.getWeight());
                    statement.setString(6, parcel.getStatus().name());
                    statement.setString(7, LocalDateTime.now().toString());
                    statement.executeUpdate();
                }

                long parcelId = getLastInsertId(connection);
                parcel.setParcelId(parcelId);

                insertEvents(connection, parcelId, parcel.getTrackingHistory());

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private int insertPerson(Connection connection, String table,
                             Person person) throws SQLException {
        String sql = "INSERT INTO " + table
                + " (full_name, phone, address) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, person.getFullName());
            statement.setString(2, person.getPhone());
            statement.setString(3, person.getAddress());
            statement.executeUpdate();
        }

        return Math.toIntExact(getLastInsertId(connection));
    }

    private void savePostOffice(Connection connection, PostOffice office)
            throws SQLException {
        String sql = """
            INSERT INTO post_offices (office_id, name, address)
            VALUES (?, ?, ?)
            ON CONFLICT(office_id) DO UPDATE SET
                name = excluded.name,
                address = excluded.address
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, office.getOfficeId());
            statement.setString(2, office.getName());
            statement.setString(3, office.getAddress());
            statement.executeUpdate();
        }
    }

    private long getLastInsertId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT last_insert_rowid()")) {
            if (result.next()) {
                return result.getLong(1);
            }
            throw new SQLException("Жазба ID алынбады");
        }
    }

    private void insertEvents(Connection connection, long parcelId,
                              List<TrackingEvent> events) throws SQLException {
        String sql = """
            INSERT INTO tracking_events
            (parcel_id, status, description, event_time)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (TrackingEvent event : events) {
                statement.setLong(1, parcelId);
                statement.setString(2, event.getStatus().name());
                statement.setString(3, event.getDescription());
                statement.setString(4, event.getEventTime().toString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public Optional<Parcel> findById(String trackingNumber)
            throws SQLException {
        String sql = """
            SELECT p.parcel_id, p.tracking_number, p.weight,
                   s.full_name AS sender_name, s.phone AS sender_phone,
                   s.address AS sender_address,
                   r.full_name AS recipient_name, r.phone AS recipient_phone,
                   r.address AS recipient_address,
                   o.office_id, o.name AS office_name,
                   o.address AS office_address
            FROM parcels p
            JOIN senders s ON p.sender_id = s.sender_id
            JOIN recipients r ON p.recipient_id = r.recipient_id
            JOIN post_offices o ON p.office_id = o.office_id
            WHERE p.tracking_number = ?
            """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trackingNumber);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Sender sender = new Sender(
                        rs.getString("sender_name"),
                        rs.getString("sender_phone"),
                        rs.getString("sender_address")
                );

                Recipient recipient = new Recipient(
                        rs.getString("recipient_name"),
                        rs.getString("recipient_phone"),
                        rs.getString("recipient_address")
                );

                PostOffice office = new PostOffice(
                        rs.getInt("office_id"),
                        rs.getString("office_name"),
                        rs.getString("office_address")
                );

                Parcel parcel = new Parcel(
                        sender, recipient, office, rs.getDouble("weight")
                );

                parcel.setParcelId(rs.getLong("parcel_id"));
                parcel.setTrackingNumber(rs.getString("tracking_number"));

                List<TrackingEvent> events =
                        loadEvents(connection, parcel.getParcelId());

                if (!events.isEmpty()) {
                    parcel.restoreTrackingHistory(events);
                }

                return Optional.of(parcel);
            }
        }
    }

    private List<TrackingEvent> loadEvents(Connection connection, long parcelId)
            throws SQLException {
        String sql = """
            SELECT event_id, status, description, event_time
            FROM tracking_events
            WHERE parcel_id = ?
            ORDER BY event_id
            """;

        List<TrackingEvent> events = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parcelId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    events.add(new TrackingEvent(
                            rs.getLong("event_id"),
                            DeliveryStatus.valueOf(rs.getString("status")),
                            rs.getString("description"),
                            LocalDateTime.parse(rs.getString("event_time"))
                    ));
                }
            }
        }

        return events;
    }

    @Override
    public List<Parcel> findAll() throws SQLException {
        List<String> trackingNumbers = new ArrayList<>();
        List<Parcel> parcels = new ArrayList<>();

        String sql = "SELECT tracking_number FROM parcels ORDER BY parcel_id";

        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                trackingNumbers.add(rs.getString("tracking_number"));
            }
        }

        for (String number : trackingNumbers) {
            findById(number).ifPresent(parcels::add);
        }

        return parcels;
    }

    @Override
    public void update(Parcel parcel) throws SQLException {
        if (parcel == null || parcel.getParcelId() <= 0) {
            throw new IllegalArgumentException("Сақталған сәлемдеме міндетті");
        }

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String sql = """
                    UPDATE parcels SET status = ?
                    WHERE parcel_id = ?
                    """;

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, parcel.getStatus().name());
                    statement.setLong(2, parcel.getParcelId());

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("Сәлемдеме деректер қорынан табылмады");
                    }
                }

                long savedCount;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM tracking_events WHERE parcel_id = ?")) {
                    statement.setLong(1, parcel.getParcelId());
                    try (ResultSet rs = statement.executeQuery()) {
                        rs.next();
                        savedCount = rs.getLong(1);
                    }
                }

                List<TrackingEvent> history = parcel.getTrackingHistory();

                if (history.size() < savedCount) {
                    throw new SQLException("Статус тарихы деректер қорымен сәйкес емес");
                }

                insertEvents(connection, parcel.getParcelId(),
                        history.subList(Math.toIntExact(savedCount), history.size()));

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }
}
