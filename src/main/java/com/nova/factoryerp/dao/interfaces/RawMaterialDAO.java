package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.RawMaterial;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RawMaterialDAO {
    List<RawMaterial> findAll() throws SQLException;
    List<RawMaterial> search(String keyword, String category, String status) throws SQLException;
    Optional<RawMaterial> findById(int id) throws SQLException;
    Optional<RawMaterial> findByCode(String code) throws SQLException;
    List<String> findAllCategories() throws SQLException;
    void save(RawMaterial m) throws SQLException;
    void update(RawMaterial m) throws SQLException;
    void delete(int id) throws SQLException;
    void updateStock(int id, BigDecimal newStock) throws SQLException;
    List<RawMaterial> findLowStock() throws SQLException;
}
