package com.nova.factoryerp.dao.interfaces;

import com.nova.factoryerp.models.AuditLog;
import java.sql.SQLException;
import java.util.List;

public interface AuditLogDAO {
    void log(AuditLog entry) throws SQLException;
    List<AuditLog> findRecent(int limit) throws SQLException;
    List<AuditLog> findByUser(int userId) throws SQLException;
}
