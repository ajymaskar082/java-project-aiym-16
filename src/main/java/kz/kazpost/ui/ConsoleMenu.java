package kz.kazpost.ui;

import kz.kazpost.exception.ParcelNotFoundException;
import kz.kazpost.model.*;
import kz.kazpost.repository.CrudDao;
import kz.kazpost.service.AsyncReportService;
import kz.kazpost.service.ParcelService;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class ConsoleMenu {

    private final ParcelService parcelService;
    private final AsyncReportService reportService;
    private final CrudDao<Parcel> dao;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleMenu(ParcelService parcelService,
                       AsyncReportService reportService,
                       CrudDao<Parcel> dao) {
        this.parcelService = parcelService;
        this.reportService = reportService;
        this.dao = dao;
    }

    public void run() {
        boolean running = true;

        System.out.println("\nҚАЗПОШТА — СӘЛЕМДЕМЕЛЕРДІ БАҚЫЛАУ ЖҮЙЕСІ");

        while (running) {
            printMenu();
            System.out.print("Таңдау: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> registerParcel();
                    case "2" -> searchParcel();
                    case "3" -> showAllParcels();
                    case "4" -> updateStatus();
                    case "5" -> showHistory();
                    case "6" -> showStatistics();
                    case "7" -> exportCsv();
                    case "8" -> showOopDemo();
                    case "0" -> {
                        System.out.println("Бағдарлама аяқталды.");
                        running = false;
                    }
                    default -> System.out.println("Қате таңдау!");
                }
            } catch (Exception e) {
                System.out.println("Қате: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("""
                \n---------- НЕГІЗГІ МӘЗІР ----------
                1. Сәлемдемені тіркеу
                2. Трек-нөмір арқылы іздеу
                3. Барлық сәлемдемелерді көру
                4. Сәлемдеме статусын жаңарту
                5. Статус тарихын қарау
                6. Статистика
                7. CSV есебін фондық ағынмен экспорттау
                8. OOP қағидаларын демонстрациялау
                0. Шығу
                --------------------------------
                """);
    }

    private void registerParcel() throws SQLException {
        System.out.println("\n--- СӘЛЕМДЕМЕНІ ТІРКЕУ ---");

        System.out.print("Жіберушінің аты-жөні: ");
        String senderName = scanner.nextLine();

        System.out.print("Жіберушінің телефоны: ");
        String senderPhone = scanner.nextLine();

        System.out.print("Жіберушінің мекенжайы: ");
        String senderAddress = scanner.nextLine();

        System.out.print("Алушының аты-жөні: ");
        String recipientName = scanner.nextLine();

        System.out.print("Алушының телефоны: ");
        String recipientPhone = scanner.nextLine();

        System.out.print("Алушының мекенжайы: ");
        String recipientAddress = scanner.nextLine();

        System.out.print("Пошта бөлімшесінің ID: ");
        int officeId = Integer.parseInt(scanner.nextLine());

        System.out.print("Пошта бөлімшесінің атауы: ");
        String officeName = scanner.nextLine();

        System.out.print("Пошта бөлімшесінің мекенжайы: ");
        String officeAddress = scanner.nextLine();

        System.out.print("Сәлемдеме салмағы (кг): ");
        double weight = Double.parseDouble(scanner.nextLine());

        Sender sender = new Sender(
                senderName, senderPhone, senderAddress
        );

        Recipient recipient = new Recipient(
                recipientName, recipientPhone, recipientAddress
        );

        PostOffice office = new PostOffice(
                officeId, officeName, officeAddress
        );

        Parcel parcel = new Parcel(
                sender, recipient, office, weight
        );

        parcelService.registerParcel(parcel);

        System.out.println("Сәлемдеме сәтті тіркелді!");
        System.out.println("Трек-нөмір: " + parcel.getTrackingNumber());
    }

    private void searchParcel() throws SQLException {
        System.out.print("Трек-нөмірді енгізіңіз: ");
        String number = scanner.nextLine();

        Parcel parcel = parcelService.findParcel(number)
                .orElseThrow(() -> new ParcelNotFoundException(number));

        printParcel(parcel);
    }

    private void showAllParcels() throws SQLException {
        var parcels = parcelService.getAllParcels();

        if (parcels.isEmpty()) {
            System.out.println("Сәлемдемелер әлі тіркелмеген.");
            return;
        }

        parcels.forEach(this::printParcel);
    }

    private void updateStatus() throws SQLException {
        System.out.print("Трек-нөмір: ");
        String number = scanner.nextLine();

        System.out.println("\nСтатустар:");
        DeliveryStatus[] statuses = DeliveryStatus.values();

        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". "
                    + statuses[i].getDisplayName());
        }

        System.out.print("Жаңа статус нөмірі: ");
        int index = Integer.parseInt(scanner.nextLine()) - 1;

        if (index < 0 || index >= statuses.length) {
            throw new IllegalArgumentException("Статус нөмірі қате");
        }

        parcelService.updateStatus(number, statuses[index]);
        System.out.println("Сәлемдеме статусы жаңартылды.");
    }

    private void showHistory() throws SQLException {
        System.out.print("Трек-нөмір: ");
        String number = scanner.nextLine();

        Parcel parcel = parcelService.findParcel(number)
                .orElseThrow(() -> new ParcelNotFoundException(number));

        System.out.println("\n--- СТАТУС ТАРИХЫ ---");

        parcel.getTrackingHistory().forEach(event ->
                System.out.println(
                        event.getEventTime() + " | "
                        + event.getStatus().getDisplayName() + " | "
                        + event.getDescription()
                )
        );
    }

    private void showStatistics() throws SQLException {
        Map<DeliveryStatus, Long> statistics =
                parcelService.getStatistics();

        System.out.println("\n--- СТАТИСТИКА ---");

        long total = statistics.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        System.out.println("Барлық сәлемдемелер саны: " + total);

        for (DeliveryStatus status : DeliveryStatus.values()) {
            System.out.println(
                    status.getDisplayName() + ": "
                    + statistics.getOrDefault(status, 0L)
            );
        }
    }

    private void exportCsv() throws SQLException {
        var parcels = parcelService.getAllParcels();

        CompletableFuture<Path> future = reportService.exportAsync(
                parcels, Path.of("reports", "parcels.csv")
        );

        System.out.println(
                "CSV экспорты фондық ағынға жіберілді."
        );

        future.whenComplete((path, error) -> {
            if (error != null) {
                System.out.println("CSV экспорт қатесі: "
                        + error.getMessage());
            } else {
                System.out.println("CSV есебі дайын: "
                        + path.toAbsolutePath());
            }
        });
    }

    private void showOopDemo() {
        System.out.println("\n--- OOP ДЕМОНСТРАЦИЯСЫ ---");

        Person sender = new Sender(
                "Айжан Қанат", "+77000000001", "Алматы"
        );

        Person recipient = new Recipient(
                "Аружан Сәрсен", "+77000000002", "Астана"
        );

        System.out.println("Sender рөлі: " + sender.getRole());
        System.out.println("Recipient рөлі: " + recipient.getRole());

        System.out.println(
                "Sender Person класынан мұра алған: "
                + (sender instanceof Person)
        );

        System.out.println(
                "Recipient Person класынан мұра алған: "
                + (recipient instanceof Person)
        );

        System.out.println(
                "DAO интерфейсінің нақты іске асырылуы: "
                + dao.getClass().getSimpleName()
        );

        System.out.println("Enum статустары:");
        for (DeliveryStatus status : DeliveryStatus.values()) {
            System.out.println("- " + status.getDisplayName());
        }
    }

    private void printParcel(Parcel parcel) {
        System.out.println("\n------------------------------");
        System.out.println("Трек-нөмір: " + parcel.getTrackingNumber());
        System.out.println("Жіберуші: "
                + parcel.getSender().getFullName());
        System.out.println("Алушы: "
                + parcel.getRecipient().getFullName());
        System.out.println("Бөлімше: "
                + parcel.getPostOffice().getName());
        System.out.println("Салмақ: " + parcel.getWeight() + " кг");
        System.out.println("Статус: "
                + parcel.getStatus().getDisplayName());
        System.out.println("------------------------------");
    }
}
