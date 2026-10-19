
package com.campus.emergency.service;
import java.sql.SQLException;
import com.campus.emergency.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    void testSuccessfulRegistrationAndLogin() throws SQLException {
        String username = "test_" + UUID.randomUUID();

        boolean registered = authService.register(
                username, "SecurePass123!", "REPORTER"
        );

        assertTrue(registered);

        User user = authService.login(username, "SecurePass123!");

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals("REPORTER", user.getRole());
    }

    @Test
    void failedLoginDoesNotCreateAuthenticatedSession() throws SQLException {
        AuthService authService = new AuthService();

        AuthenticatedSession session = authService.loginSession(
                "nonexistent_user_12345", "WrongPassword123!");

        assertNull(session);
    }

    @Test
    void testIncorrectPasswordIsRejected() throws SQLException {
        String username = "test_" + UUID.randomUUID();

        authService.register(
                username, "SecurePass123!", "REPORTER"
        );

        User user = authService.login(username, "WrongPassword123!");

        assertNull(user);
    }

    @Test
    void testDuplicateUsernameIsRejected() throws SQLException {
        String username = "test_" + UUID.randomUUID();

        boolean firstRegistration = authService.register(
                username, "SecurePass123!", "REPORTER"
        );

        boolean secondRegistration = authService.register(
                username, "AnotherPass123!", "REPORTER"
        );

        assertTrue(firstRegistration);
        assertFalse(secondRegistration);
    }

    @Test
    void successfulLoginCreatesAuthenticatedSession() throws SQLException {
        String username = "session_test_" +
                java.util.UUID.randomUUID().toString().substring(0, 8);

        AuthService authService = new AuthService();

        assertTrue(authService.register(
                username, "Password123!", "REPORTER"));

        AuthenticatedSession session = authService.loginSession(
                username, "Password123!");

        assertNotNull(session);
        assertEquals(username, session.getUsername());
        assertEquals("REPORTER", session.getRole());
        assertTrue(session.isActive());
    }
}
