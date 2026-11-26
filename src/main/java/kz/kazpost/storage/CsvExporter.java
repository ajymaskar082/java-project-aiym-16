package kz.kazpost.storage;

import kz.kazpost.model.Parcel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvExporter {

    public void export(List<Parcel> parcels, Path path) throws IOException {
        if (parcels == null || path == null) {
            throw new IllegalArgumentException(
                    "Сәлемдемелер тізімі мен файл жолы міндетті"
            );
        }

        Path absolutePath = path.toAbsolutePath();
        Path parent = absolutePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                absolutePath, StandardCharsets.UTF_8)) {

            writer.write(
                    "tracking_number,sender,recipient,post_office,"
                            + "weight_kg,status,events_count"
            );
            writer.newLine();

            for (Parcel parcel : parcels) {
                String[] values = {
                        parcel.getTrackingNumber(),
                        parcel.getSender().getFullName(),
                        parcel.getRecipient().getFullName(),
                        parcel.getPostOffice().getName(),
                        String.valueOf(parcel.getWeight()),
                        parcel.getStatus().name(),
                        String.valueOf(parcel.getTrackingHistory().size())
                };

                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        writer.write(",");
                    }
                    writer.write(escape(values[i]));
                }

                writer.newLine();
            }
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"")
                || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }
}
