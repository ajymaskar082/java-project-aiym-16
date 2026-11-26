package kz.kazpost.repository;

import kz.kazpost.database.DatabaseConnection;

public abstract class Repository<T> implements CrudDao<T> {

    protected final DatabaseConnection databaseConnection;

    protected Repository(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {
            throw new IllegalArgumentException(
                    "DatabaseConnection бос болмауы керек"
            );
        }
        this.databaseConnection = databaseConnection;
    }
}
