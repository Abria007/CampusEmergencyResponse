
package com.campus.emergency.service;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.dao.ResponderDAO;
import com.campus.emergency.dao.UserDAO;
import com.campus.emergency.database.DatabaseConnection;
import com.campus.emergency.model.Incident;
import com.campus.emergency.model.Responder;
import com.campus.emergency.model.User;
import com.campus.emergency.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

class IncidentServiceTest {

    private final IncidentService incidentService =
            new IncidentService();

    @Test
    void testInvalidReporterIdIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.reportIncident(
                        0,
                        "FIRE",
                        "Smoke detected",
                        1
                )
        );
    }

    @Test
    void testInvalidCategoryIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.reportIncident(
                        1,
                        "INVALID",
                        "Something happened",
                        1
                )
        );
    }

    @Test
    void testEmptyDescriptionIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.reportIncident(
                        1,
                        "FIRE",
                        "   ",
                        1
                )
        );
    }

    @Test
    void testInvalidLocationIdIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.reportIncident(
                        1,
                        "FIRE",
                        "Smoke detected",
                        0
                )
        );
    }

    @Test
    void testReportAndAssignIncident() throws Exception {

        String uniqueId = UUID.randomUUID().toString()
                .substring(0, 8);

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();

        String reporterUsername = "reporter_" + uniqueId;
        String responderUsername = "responder_" + uniqueId;

        assertTrue(authService.register(
                reporterUsername, "Reporter@123", "REPORTER"
        ));

        User reporter = userDAO.findByUsername(reporterUsername);

        User responderUser = createResponderUser(
                userDAO, responderUsername, "Responder@123");

        assertNotNull(reporter);
        assertNotNull(responderUser);

        int locationId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO locations (building, floor, room) " +
                             "VALUES (?, ?, ?)")) {

            statement.setString(1, "Test Building " + uniqueId);
            statement.setString(2, "1");
            statement.setString(3, "101");
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM locations WHERE building = ?")) {

            statement.setString(1, "Test Building " + uniqueId);

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                locationId = result.getInt("id");
            }
        }

        int responderId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO responders " +
                             "(user_id, name, responder_type, " +
                             "location_id, is_available) " +
                             "VALUES (?, ?, ?, ?, 1)")) {

            statement.setInt(1, responderUser.getId());
            statement.setString(2, "Test Responder " + uniqueId);
            statement.setString(3, "SECURITY");
            statement.setInt(4, locationId);
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM responders WHERE user_id = ?")) {

            statement.setInt(1, responderUser.getId());

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                responderId = result.getInt("id");
            }
        }

        assertTrue(incidentService.reportIncident(
                reporter.getId(),
                "FIRE",
                "Smoke detected near the laboratory",
                locationId
        ));

        IncidentDAO incidentDAO = new IncidentDAO();
        Incident incident = null;

        for (Incident item : incidentDAO.findAll()) {
            if (item.getReportedBy() == reporter.getId()
                    && ("Smoke detected near the laboratory")
                    .equals(item.getDescription())) {
                incident = item;
                break;
            }
        }

        assertNotNull(incident);
        assertEquals("CRITICAL", incident.getPriority());
        assertEquals("REPORTED", incident.getStatus());

        assertTrue(incidentService.assignResponder(
                incident.getId(), responderId
        ));

        Incident updatedIncident =
                incidentDAO.findById(incident.getId());

        assertNotNull(updatedIncident);
        assertEquals("ASSIGNED", updatedIncident.getStatus());
        assertEquals(
                responderId,
                updatedIncident.getAssignedResponderId()
        );

        ResponderDAO responderDAO = new ResponderDAO();
        Responder updatedResponder =
                responderDAO.findById(responderId);

        assertNotNull(updatedResponder);
        assertFalse(updatedResponder.isAvailable());
    }

    @Test
    void testFailedAssignmentDoesNotMakeResponderUnavailable()
            throws SQLException {

        String suffix = UUID.randomUUID().toString()
                .substring(0, 8);

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();
        ResponderDAO responderDAO = new ResponderDAO();

        String reporterUsername = "rollback_reporter_" + suffix;
        String responderUsername = "rollback_responder_" + suffix;

        assertTrue(authService.register(
                reporterUsername, "Password123!", "REPORTER"
        ));

        User reporter = userDAO.findByUsername(reporterUsername);

        User responderUser = createResponderUser(
                userDAO, responderUsername, "Password123!");

        assertNotNull(reporter);
        assertNotNull(responderUser);

        int locationId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO locations " +
                             "(building, floor, room, latitude, longitude) " +
                             "VALUES (?, ?, ?, ?, ?)")) {

            statement.setString(1, "Rollback_" + suffix);
            statement.setString(2, "1");
            statement.setString(3, "101");
            statement.setDouble(4, 11.0168);
            statement.setDouble(5, 76.9558);
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM locations WHERE building = ?")) {

            statement.setString(1, "Rollback_" + suffix);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                locationId = resultSet.getInt("id");
            }
        }

        int responderId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO responders " +
                             "(user_id, name, responder_type, " +
                             "location_id, is_available) " +
                             "VALUES (?, ?, ?, ?, 1)")) {

            statement.setInt(1, responderUser.getId());
            statement.setString(2, "Rollback Responder " + suffix);
            statement.setString(3, "SECURITY");
            statement.setInt(4, locationId);
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM responders WHERE user_id = ?")) {

            statement.setInt(1, responderUser.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                responderId = resultSet.getInt("id");
            }
        }

        assertTrue(incidentService.reportIncident(
                reporter.getId(),
                "SECURITY",
                "First incident " + suffix,
                locationId
        ));

        assertTrue(incidentService.reportIncident(
                reporter.getId(),
                "SECURITY",
                "Second incident " + suffix,
                locationId
        ));

        IncidentDAO incidentDAO = new IncidentDAO();

        Incident firstIncident = null;
        Incident secondIncident = null;

        for (Incident item : incidentDAO.findAll()) {
            if (item.getReportedBy() == reporter.getId()) {
                if (("First incident " + suffix)
                        .equals(item.getDescription())) {
                    firstIncident = item;
                } else if (("Second incident " + suffix)
                        .equals(item.getDescription())) {
                    secondIncident = item;
                }
            }
        }

        assertNotNull(firstIncident);
        assertNotNull(secondIncident);

        final Incident first = firstIncident;
        final Incident second = secondIncident;

        assertTrue(incidentService.assignResponder(
                first.getId(), responderId
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.assignResponder(
                        second.getId(), responderId
                )
        );

        Responder savedResponder =
                responderDAO.findById(responderId);

        assertNotNull(savedResponder);
        assertFalse(savedResponder.isAvailable());

        Incident savedSecondIncident =
                incidentDAO.findById(secondIncident.getId());

        assertNotNull(savedSecondIncident);
        assertEquals("REPORTED", savedSecondIncident.getStatus());
        assertNull(savedSecondIncident.getAssignedResponderId());
    }

    @Test
    void responderCannotStartAnotherRespondersIncident() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();
        IncidentDAO incidentDAO = new IncidentDAO();

        String reporterUsername = "owner_reporter_" + suffix;
        String responderOneUsername = "owner_one_" + suffix;
        String responderTwoUsername = "owner_two_" + suffix;
        String password = "TestPass123!";

        assertTrue(authService.register(reporterUsername, password));

        User reporter = userDAO.findByUsername(reporterUsername);
        User responderOneUser = createResponderUser(
                userDAO, responderOneUsername, password);
        User responderTwoUser = createResponderUser(
                userDAO, responderTwoUsername, password);

        assertNotNull(reporter);
        assertNotNull(responderOneUser);
        assertNotNull(responderTwoUser);

        AuthenticatedSession responderTwoSession =
                authService.loginSession(responderTwoUsername, password);

        assertNotNull(responderTwoSession);

        int locationId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO locations (building, floor, room) VALUES (?, ?, ?)")) {
            statement.setString(1, "Ownership Test " + suffix);
            statement.setString(2, "1");
            statement.setString(3, "101");
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM locations WHERE building = ?")) {
            statement.setString(1, "Ownership Test " + suffix);

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                locationId = result.getInt("id");
            }
        }

        int responderOneId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO responders " +
                             "(user_id, name, responder_type, location_id, is_available) " +
                             "VALUES (?, ?, ?, ?, 1)")) {
            statement.setInt(1, responderOneUser.getId());
            statement.setString(2, "Responder One " + suffix);
            statement.setString(3, "SECURITY");
            statement.setInt(4, locationId);
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM responders WHERE user_id = ?")) {
            statement.setInt(1, responderOneUser.getId());

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                responderOneId = result.getInt("id");
            }
        }

        assertTrue(incidentService.reportIncident(
                reporter.getId(),
                "SECURITY",
                "Ownership test incident " + suffix,
                locationId
        ));

        Incident incident = null;

        for (Incident item : incidentDAO.findAll()) {
            if (item.getReportedBy() == reporter.getId()
                    && item.getDescription().equals(
                    "Ownership test incident " + suffix)) {
                incident = item;
                break;
            }
        }

        assertNotNull(incident);

        assertTrue(incidentService.assignResponder(
                incident.getId(), responderOneId));

        final int incidentId = incident.getId();

        assertThrows(SecurityException.class, () ->
                incidentService.startIncident(
                        responderTwoSession, incidentId));

        Incident unchanged = incidentDAO.findById(incidentId);

        assertNotNull(unchanged);
        assertEquals("ASSIGNED", unchanged.getStatus());
        assertEquals(
                responderOneId,
                unchanged.getAssignedResponderId());
    }

    @Test
    void responderCannotResolveAnotherRespondersIncident() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String password = "TestPass123!";

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();
        IncidentDAO incidentDAO = new IncidentDAO();

        String reporterUsername = "resolve_reporter_" + suffix;
        String ownerUsername = "resolve_owner_" + suffix;
        String otherUsername = "resolve_other_" + suffix;

        assertTrue(authService.register(reporterUsername, password));

        User reporter = userDAO.findByUsername(reporterUsername);
        User ownerUser = createResponderUser(
                userDAO, ownerUsername, password);
        createResponderUser(userDAO, otherUsername, password);

        assertNotNull(reporter);
        assertNotNull(ownerUser);

        AuthenticatedSession otherSession =
                authService.loginSession(otherUsername, password);

        assertNotNull(otherSession);

        int locationId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO locations (building, floor, room) VALUES (?, ?, ?)")) {
            statement.setString(1, "Resolve Test " + suffix);
            statement.setString(2, "1");
            statement.setString(3, "102");
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM locations WHERE building = ?")) {
            statement.setString(1, "Resolve Test " + suffix);

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                locationId = result.getInt("id");
            }
        }

        int responderId;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO responders " +
                             "(user_id, name, responder_type, location_id, is_available) " +
                             "VALUES (?, ?, ?, ?, 1)")) {
            statement.setInt(1, ownerUser.getId());
            statement.setString(2, "Resolve Owner " + suffix);
            statement.setString(3, "SECURITY");
            statement.setInt(4, locationId);
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM responders WHERE user_id = ?")) {
            statement.setInt(1, ownerUser.getId());

            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                responderId = result.getInt("id");
            }
        }

        String description = "Resolve ownership test " + suffix;

        assertTrue(incidentService.reportIncident(
                reporter.getId(), "SECURITY", description, locationId));

        Incident incident = null;

        for (Incident item : incidentDAO.findAll()) {
            if (item.getReportedBy() == reporter.getId()
                    && description.equals(item.getDescription())) {
                incident = item;
                break;
            }
        }

        assertNotNull(incident);
        assertTrue(incidentService.assignResponder(
                incident.getId(), responderId));
        assertTrue(incidentService.startIncident(incident.getId()));

        int incidentId = incident.getId();

        assertThrows(SecurityException.class, () ->
                incidentService.resolveIncident(
                        otherSession, incidentId, "Resolved by unauthorized responder"));

        Incident unchanged = incidentDAO.findById(incidentId);

        assertNotNull(unchanged);
        assertEquals("IN_PROGRESS", unchanged.getStatus());
        assertEquals(responderId, unchanged.getAssignedResponderId());
    }



    private User createResponderUser(
            UserDAO userDAO, String username, String password)
            throws SQLException {

        String passwordHash = BCrypt.hashpw(
                password, BCrypt.gensalt(12));

        User responder = new User(
                0, username, passwordHash, "RESPONDER", true);

        assertTrue(userDAO.createUser(responder));

        return userDAO.findByUsername(username);
    }
}
