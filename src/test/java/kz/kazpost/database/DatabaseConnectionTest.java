package kz.kazpost.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsNullDatabasePath() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConnection(null)
        );
    }

    @Test
    void createsDatabaseAndParentDirectories() throws Exception {
        Path databasePath = tempDir
                .resolve("nested")
                .resolve("kazpost.db");

        DatabaseConnection database =
                new DatabaseConnection(databasePath);

        try (Connection connection = database.getConnection()) {
            assertTrue(connection.isValid(2));
        }

        assertTrue(Files.exists(databasePath));
    }

    @Test
    void initializesAllRequiredTables() throws Exception {
        DatabaseConnection database =
                new DatabaseConnection(tempDir.resolve("schema.db"));

        database.initializeSchema();

        String sql = """
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type = 'table'
                  AND name IN (
                    'senders',
                    'recipients',
                    'post_offices',
                    'parcels',
                    'tracking_events'
                  )
                """;

        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            assertTrue(result.next());
            assertEquals(5, result.getInt(1));
        }
    }

    @Test
    void enforcesForeignKeys() throws Exception {
        DatabaseConnection database =
                new DatabaseConnection(tempDir.resolve("foreign-keys.db"));

        database.initializeSchema();

        String sql = """
                INSERT INTO parcels (
                    tracking_number,
                    sender_id,
                    recipient_id,
                    office_id,
                    weight,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "KZ-INVALID-001");
            statement.setLong(2, 999);
            statement.setLong(3, 999);
            statement.setLong(4, 999);
            statement.setDouble(5, 2.5);
            statement.setString(6, "REGISTERED");
            statement.setString(7, "2026-11-27T10:00:00");

            assertThrows(
                    SQLException.class,
                    statement::executeUpdate
            );
        }
    }
}
