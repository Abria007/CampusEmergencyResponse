
package com.campus.emergency.service;

import com.campus.emergency.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationServiceTest {

    private final AuthorizationService authorizationService =
            new AuthorizationService();

    @Test
    void activeReporterCanAccessReporterOperations() {
        User reporter = new User(
                1, "test_reporter", "hashedPassword",
                "REPORTER", true);

        assertDoesNotThrow(() ->
                authorizationService.requireRole(
                        reporter, "REPORTER"));
    }

    @Test
    void activeResponderCanAccessResponderOperations() {
        User responder = new User(
                2, "test_responder", "hashedPassword",
                "RESPONDER", true);

        assertDoesNotThrow(() ->
                authorizationService.requireRole(
                        responder, "RESPONDER"));
    }

    @Test
    void wrongRoleIsRejected() {
        User reporter = new User(
                1, "test_reporter", "hashedPassword",
                "REPORTER", true);

        assertThrows(SecurityException.class, () ->
                authorizationService.requireRole(
                        reporter, "ADMIN"));
    }

    @Test
    void inactiveUserIsRejected() {
        User inactiveUser = new User(
                3, "inactive_user", "hashedPassword",
                "ADMIN", false);

        assertThrows(SecurityException.class, () ->
                authorizationService.requireRole(
                        inactiveUser, "ADMIN"));
    }

    @Test
    void nullUserIsRejected() {
        assertThrows(SecurityException.class, () ->
                authorizationService.requireRole(
                        null, "ADMIN"));
    }
}
