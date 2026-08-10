package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.MaterialLot;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MaterialLotDAO {
    List<MaterialLot> findAll() throws SQLException;
    List<MaterialLot> search(String keyword, Integer materialId, String status) throws SQLException;
    List<MaterialLot> findByMaterial(int materialId) throws SQLException;
    List<MaterialLot> findAvailableByMaterial(int materialId) throws SQLException;
    Optional<MaterialLot> findById(int id) throws SQLException;
    void save(MaterialLot lot) throws SQLException;
    void update(MaterialLot lot) throws SQLException;
    void updateRemainingQty(int id, BigDecimal qty) throws SQLException;
    void updateStatus(int id, String status) throws SQLException;
}
