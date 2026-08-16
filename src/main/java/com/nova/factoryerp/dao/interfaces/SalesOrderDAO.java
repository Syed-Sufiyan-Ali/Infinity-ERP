package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.SalesOrder;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SalesOrderDAO {

    List<SalesOrder> findAll() throws SQLException;

    List<SalesOrder> search(
            String keyword,
            String status
    ) throws SQLException;

    Optional<SalesOrder> findById(long id) throws SQLException;

    Optional<SalesOrder> findByOrderNumber(
            String orderNumber
    ) throws SQLException;

    long save(SalesOrder order) throws SQLException;

    void update(SalesOrder order) throws SQLException;

    void cancel(long id) throws SQLException;
}