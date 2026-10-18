
package com.campus.emergency.service;

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
}
