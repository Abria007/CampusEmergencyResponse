
package com.campus.emergency.service;

import com.campus.emergency.dao.UserDAO;
import com.campus.emergency.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthService {

    private final UserDAO userDAO;

    private static final Set<String> VALID_ROLES =
            new HashSet<>(Arrays.asList(
                    "ADMIN",
                    "RESPONDER",
                    "REPORTER"
            ));

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public boolean register(String username, String password, String role)
            throws SQLException {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        if (role == null || !VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid user role.");
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
                role,
                true
        );

        return userDAO.createUser(user);
    }

    public User login(String username, String password)
            throws SQLException {

        if (username == null || password == null) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());

        if (user == null || !user.isActive()) {
            return null;
        }

        boolean passwordMatches = BCrypt.checkpw(
                password,
                user.getPasswordHash()
        );

        if (passwordMatches) {
            return user;
        }

        return null;
    }
}
