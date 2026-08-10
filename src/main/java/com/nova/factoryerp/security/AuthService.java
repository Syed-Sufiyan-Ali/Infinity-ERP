package com.nova.factoryerp.security;

import com.nova.factoryerp.dao.impl.AuditLogDAOImpl;
import com.nova.factoryerp.dao.impl.UserDAOImpl;
import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.dao.interfaces.UserDAO;
import com.nova.factoryerp.models.AuditLog;
import com.nova.factoryerp.models.User;
import com.nova.factoryerp.utils.PasswordUtil;
import com.nova.factoryerp.utils.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO = new UserDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();

    public enum LoginResult {
        SUCCESS, INVALID_CREDENTIALS, ACCOUNT_DISABLED, DATABASE_ERROR
    }

    public LoginResult login(String username, String password) {
        try {
            Optional<User> opt = userDAO.findByUsername(username);
            if (opt.isEmpty()) {
                log.warn("Login failed — username not found: {}", username);
                return LoginResult.INVALID_CREDENTIALS;
            }
            User user = opt.get();
            if (!user.isActive()) {
                log.warn("Login failed — account disabled: {}", username);
                return LoginResult.ACCOUNT_DISABLED;
            }
            if (!PasswordUtil.verify(password, user.getPassword())) {
                log.warn("Login failed — wrong password: {}", username);
                return LoginResult.INVALID_CREDENTIALS;
            }
            // Success
            userDAO.updateLastLogin(user.getId());
            SessionManager.getInstance().setCurrentUser(user);
            auditDAO.log(new AuditLog(user.getId(), user.getUsername(),
                "LOGIN", "AUTH", "User logged in successfully"));
            log.info("User logged in: {} [{}]", username, user.getRoleName());
            return LoginResult.SUCCESS;

        } catch (SQLException e) {
            log.error("Database error during login", e);
            return LoginResult.DATABASE_ERROR;
        }
    }

    public void logout() {
        SessionManager sm = SessionManager.getInstance();
        if (sm.isLoggedIn()) {
            try {
                auditDAO.log(new AuditLog(sm.getCurrentUserId(), sm.getCurrentUsername(),
                    "LOGOUT", "AUTH", "User logged out"));
            } catch (SQLException ignored) {}
            log.info("User logged out: {}", sm.getCurrentUsername());
        }
        sm.logout();
    }

    /**
     * Utility: generate a BCrypt hash for seeding.
     * Used to set the correct seed passwords.
     */
    public static String generateHash(String plain) {
        return PasswordUtil.hash(plain);
    }
}
