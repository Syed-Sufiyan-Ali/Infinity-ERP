package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.Customer;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {

    List<Customer> search(String keyword, String status) throws SQLException;

    Optional<Customer> findById(int id) throws SQLException;

    Optional<Customer> findByCode(String customerCode) throws SQLException;

    void save(Customer customer) throws SQLException;

    void update(Customer customer) throws SQLException;

    void deactivate(int id) throws SQLException;
}