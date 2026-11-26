package kz.kazpost.storage;

import kz.kazpost.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    @TempDir
    Path tempDir;

    private Parcel createParcel() {
        Parcel parcel = new Parcel(
                new Sender("Аскар, Айым", "+77010000001", "Алматы"),
                new Recipient("Аружан", "+77020000002", "Астана"),
                new PostOffice(1, "Алматы бөлімшесі", "Алматы"),
                2.5
        );
        parcel.setTrackingNumber("KZTEST0001");
        return parcel;
    }

    @Test
    void exportCreatesCsvWithParcelData() throws Exception {
        Path file = tempDir.resolve("parcels.csv");

        new CsvExporter().export(List.of(createParcel()), file);

        String content = Files.readString(file);

        assertTrue(content.contains("tracking_number,sender,recipient"));
        assertTrue(content.contains("KZTEST0001"));
        assertTrue(content.contains("\"Аскар, Айым\""));
        assertTrue(content.contains("2.5"));
    }

    @Test
    void exportCreatesHeaderForEmptyList() throws Exception {
        Path file = tempDir.resolve("empty.csv");

        new CsvExporter().export(List.of(), file);

        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("tracking_number"));
    }

    @Test
    void exportCreatesMissingParentDirectories() throws Exception {
        Path file = tempDir.resolve("nested/reports/parcels.csv");

        new CsvExporter().export(List.of(createParcel()), file);

        assertTrue(Files.exists(file));
    }
}
