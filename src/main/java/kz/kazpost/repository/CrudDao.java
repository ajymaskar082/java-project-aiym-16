package kz.kazpost.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CrudDao<T> {
    void save(T entity) throws SQLException;

    Optional<T> findById(String id) throws SQLException;

    List<T> findAll() throws SQLException;

    void update(T entity) throws SQLException;
}
