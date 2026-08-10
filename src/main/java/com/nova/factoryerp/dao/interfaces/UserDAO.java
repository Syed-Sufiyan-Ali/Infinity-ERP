package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findById(int id) throws SQLException;
    List<User> findAll() throws SQLException;
    void save(User user) throws SQLException;
    void update(User user) throws SQLException;
    void updateLastLogin(int userId) throws SQLException;
    void delete(int id) throws SQLException;
}
