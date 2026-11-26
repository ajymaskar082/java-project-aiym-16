package kz.kazpost.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class TrackingNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {
        String date = LocalDate.now().format(DATE_FORMAT);

        String uniquePart = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .replace("-", "")
                .toUpperCase(Locale.ROOT);

        return "KZ" + date + uniquePart;
    }
}
