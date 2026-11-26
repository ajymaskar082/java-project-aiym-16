package kz.kazpost.service;

import kz.kazpost.model.Parcel;
import kz.kazpost.storage.CsvExporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncReportService implements AutoCloseable {

    private final CsvExporter csvExporter;
    private final ExecutorService executor;

    public AsyncReportService(CsvExporter csvExporter) {
        if (csvExporter == null) {
            throw new IllegalArgumentException(
                    "CSV экспорттаушы бос болмауы керек"
            );
        }

        this.csvExporter = csvExporter;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "kazpost-report-worker");
            thread.setDaemon(false);
            return thread;
        });
    }

    public CompletableFuture<Path> exportAsync(
            List<Parcel> parcels, Path path) {

        if (parcels == null || path == null) {
            throw new IllegalArgumentException(
                    "Сәлемдемелер тізімі мен файл жолы міндетті"
            );
        }

        List<Parcel> snapshot = List.copyOf(parcels);

        return CompletableFuture.supplyAsync(() -> {
            try {
                csvExporter.export(snapshot, path);
                return path;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    @Override
    public void close() {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
