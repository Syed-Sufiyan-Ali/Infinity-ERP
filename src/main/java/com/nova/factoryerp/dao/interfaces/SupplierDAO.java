package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.Supplier;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SupplierDAO {
    List<Supplier> findAll() throws SQLException;
    List<Supplier> search(String keyword) throws SQLException;
    Optional<Supplier> findById(int id) throws SQLException;
    void save(Supplier s) throws SQLException;
    void update(Supplier s) throws SQLException;
    void delete(int id) throws SQLException;
}
