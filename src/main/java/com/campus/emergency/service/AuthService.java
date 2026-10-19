
package com.campus.emergency.service;

import com.campus.emergency.dao.UserDAO;
import com.campus.emergency.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    // Public registration: users can register only as REPORTER.
    public boolean register(String username, String password)
            throws SQLException {

        return register(username, password, "REPORTER");
    }

    // Keep the existing method signature for compatibility,
    // but prevent public registration with privileged roles.
    public boolean register(
            String username,
            String password,
            String role
    ) throws SQLException {

        if (role == null ||
                !"REPORTER".equalsIgnoreCase(role.trim())) {
            throw new IllegalArgumentException(
                    "Public registration is allowed only for REPORTER accounts."
            );
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Username cannot be empty."
            );
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        username = username.trim();

        if (userDAO.usernameExists(username)) {
            return false;
        }

        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        User user = new User(
                0,
                username,
                passwordHash,
                "REPORTER",
                true
        );

        return userDAO.createUser(user);
    }

    public User login(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());

        if (user == null || !user.isActive()) {
            return null;
        }

        boolean passwordMatches = BCrypt.checkpw(
                password, user.getPasswordHash());

        if (passwordMatches) {
            return user;
        }

        return null;
    }

    public AuthenticatedSession loginSession(
            String username, String password) throws SQLException {

        User user = login(username, password);

        if (user == null) {
            return null;
        }

        return new AuthenticatedSession(user);
    }
}
