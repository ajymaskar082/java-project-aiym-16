package kz.kazpost;

import kz.kazpost.database.DatabaseConnection;
import kz.kazpost.model.Parcel;
import kz.kazpost.repository.CrudDao;
import kz.kazpost.repository.SqliteParcelDao;
import kz.kazpost.service.AsyncReportService;
import kz.kazpost.service.ParcelService;
import kz.kazpost.storage.CsvExporter;
import kz.kazpost.ui.ConsoleMenu;
import kz.kazpost.util.TrackingNumberGenerator;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseConnection database = new DatabaseConnection();
            database.initializeSchema();

            CrudDao<Parcel> dao = new SqliteParcelDao(database);

            ParcelService parcelService = new ParcelService(
                    dao,
                    new TrackingNumberGenerator()
            );

            try (AsyncReportService reportService =
                         new AsyncReportService(new CsvExporter())) {

                ConsoleMenu menu = new ConsoleMenu(
                        parcelService,
                        reportService,
                        dao
                );

                menu.run();
            }

        } catch (Exception e) {
            System.err.println(
                    "Бағдарламаны іске қосу қатесі: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
