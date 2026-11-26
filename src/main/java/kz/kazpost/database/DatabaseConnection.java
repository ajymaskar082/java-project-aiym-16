package kz.kazpost.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private final Path databasePath;

    public DatabaseConnection() {
        this(Path.of("data", "kazpost.db"));
    }

    public DatabaseConnection(Path databasePath) {
        if (databasePath == null) {
            throw new IllegalArgumentException(
                    "Деректер қорының жолы бос болмауы керек"
            );
        }
        this.databasePath = databasePath;
    }

    public Connection getConnection() throws SQLException {
        try {
            Path parent = databasePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new SQLException(
                    "Деректер қорының бумасын жасау мүмкін болмады", e
            );
        }

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databasePath.toAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    public void initializeSchema() throws SQLException {
        String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS senders (
                sender_id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                phone TEXT NOT NULL,
                address TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS recipients (
                recipient_id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                phone TEXT NOT NULL,
                address TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS post_offices (
                office_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                address TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS parcels (
                parcel_id INTEGER PRIMARY KEY AUTOINCREMENT,
                tracking_number TEXT NOT NULL UNIQUE,
                sender_id INTEGER NOT NULL,
                recipient_id INTEGER NOT NULL,
                office_id INTEGER NOT NULL,
                weight REAL NOT NULL CHECK (weight > 0),
                status TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (sender_id) REFERENCES senders(sender_id),
                FOREIGN KEY (recipient_id) REFERENCES recipients(recipient_id),
                FOREIGN KEY (office_id) REFERENCES post_offices(office_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS tracking_events (
                event_id INTEGER PRIMARY KEY AUTOINCREMENT,
                parcel_id INTEGER NOT NULL,
                status TEXT NOT NULL,
                description TEXT NOT NULL,
                event_time TEXT NOT NULL,
                FOREIGN KEY (parcel_id) REFERENCES parcels(parcel_id)
                    ON DELETE CASCADE
            )
            """
        };

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            for (String table : tables) {
                statement.execute(table);
            }
        }
    }

    public Path getDatabasePath() {
        return databasePath;
    }
}
