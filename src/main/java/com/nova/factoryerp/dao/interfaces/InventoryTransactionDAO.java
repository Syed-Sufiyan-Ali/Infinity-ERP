package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.InventoryTransaction;

import java.sql.SQLException;
import java.util.List;

public interface InventoryTransactionDAO {

    void save(InventoryTransaction tx) throws SQLException;

    List<InventoryTransaction> findAll() throws SQLException;

    List<InventoryTransaction> search(
            String keyword,
            String type,
            String itemType,
            String fromDate,
            String toDate
    ) throws SQLException;

    /**
     * Returns all inventory transactions linked to a specific material lot.
     */
    List<InventoryTransaction> findByLotId(int lotId) throws SQLException;
}