package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.SalesOrderItem;

import java.sql.SQLException;
import java.util.List;

public interface SalesOrderItemDAO {

    List<SalesOrderItem> findByOrderId(
            long salesOrderId
    ) throws SQLException;

    long save(SalesOrderItem item) throws SQLException;

    void deleteByOrderId(long salesOrderId) throws SQLException;
}