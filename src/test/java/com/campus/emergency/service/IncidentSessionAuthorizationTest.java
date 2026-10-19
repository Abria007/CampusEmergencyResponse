
package com.campus.emergency.service;

import com.campus.emergency.dao.UserDAO;
import com.campus.emergency.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentSessionAuthorizationTest {

    private AuthService authService;
    private IncidentService incidentService;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        incidentService = new IncidentService();
        userDAO = new UserDAO();
    }

    @Test
    void reportingWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.reportIncident(
                        null, "FIRE", "Fire in laboratory", 1));
    }

    @Test
    void assigningWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.assignResponder(null, 1, 1));
    }

    @Test
    void startingIncidentWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.startIncident(null, 1));
    }

    @Test
    void resolvingIncidentWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.resolveIncident(
                        null, 1, "Issue resolved"));
    }

    @Test
    void reporterCannotAssignIncident() throws SQLException {
        String username = "reporter_" + System.nanoTime();
        String password = "TestPass123";

        assertNotNull(authService.register(username, password));

        AuthenticatedSession session =
                authService.loginSession(username, password);

        assertNotNull(session);

        assertThrows(SecurityException.class, () ->
                incidentService.assignResponder(session, 1, 1));
    }

    @Test
    void reporterCannotStartIncident() throws SQLException {
        String username = "reporter_" + System.nanoTime();
        String password = "TestPass123";

        assertNotNull(authService.register(username, password));

        AuthenticatedSession session =
                authService.loginSession(username, password);

        assertNotNull(session);

        assertThrows(SecurityException.class, () ->
                incidentService.startIncident(session, 1));
    }

    @Test
    void reporterCannotResolveIncident() throws SQLException {
        String username = "reporter_" + System.nanoTime();
        String password = "TestPass123";

        assertNotNull(authService.register(username, password));

        AuthenticatedSession session =
                authService.loginSession(username, password);

        assertNotNull(session);

        assertThrows(SecurityException.class, () ->
                incidentService.resolveIncident(
                        session, 1, "Issue resolved"));
    }
}
