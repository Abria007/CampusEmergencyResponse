
package com.campus.emergency.service;

import com.campus.emergency.database.DatabaseConnection;
import com.campus.emergency.model.User;
import com.campus.emergency.model.Incident;
import com.campus.emergency.model.Location;
import com.campus.emergency.model.Responder;
import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.dao.ResponderDAO;
import com.campus.emergency.dao.UserDAO;
import com.campus.emergency.service.IncidentService;
import com.campus.emergency.service.AutomaticAssignmentService;
import com.campus.emergency.service.AuthService;

import org.mindrot.jbcrypt.BCrypt;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AutomaticAssignmentServiceTest {

    @Test
    void assignsNearestEligibleResponder() throws SQLException {

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();
        ResponderDAO responderDAO = new ResponderDAO();
        IncidentDAO incidentDAO = new IncidentDAO();
        IncidentService incidentService = new IncidentService();
        AutomaticAssignmentService automaticService =
                new AutomaticAssignmentService();

        String reporterUsername = "auto_reporter_" + suffix;
        String nearUsername = "auto_near_" + suffix;
        String farUsername = "auto_far_" + suffix;

        assertTrue(authService.register(
                reporterUsername, "Password123!", "REPORTER"));

        User reporter = userDAO.findByUsername(reporterUsername);

        User nearUser = createResponderUser(
                userDAO, nearUsername, "Password123!");

        User farUser = createResponderUser(
                userDAO, farUsername, "Password123!");

        assertNotNull(reporter);
        assertNotNull(nearUser);
        assertNotNull(farUser);

        int incidentLocationId = insertLocation(
                "Incident_" + suffix, 11.0168, 76.9558);

        int nearLocationId = insertLocation(
                "Near_" + suffix, 11.0170, 76.9560);

        int farLocationId = insertLocation(
                "Far_" + suffix, 11.0300, 76.9700);

        int nearResponderId = insertResponder(
                nearUser.getId(), "Near Responder", "SECURITY",
                nearLocationId);

        insertResponder(
                farUser.getId(), "Far Responder", "SECURITY",
                farLocationId);

        assertTrue(incidentService.reportIncident(
                reporter.getId(), "SECURITY",
                "Automatic assignment test", incidentLocationId));

        Incident incident = null;

        for (Incident candidate : incidentDAO.findAll()) {
            if (candidate.getReportedBy() == reporter.getId()
                    && "Automatic assignment test".equals(
                    candidate.getDescription())) {
                incident = candidate;
                break;
            }
        }

        assertNotNull(incident, "The reported incident should exist");
        assertTrue(
                automaticService.assignNearestResponder(incident.getId()));

        Incident assignedIncident = incidentDAO.findById(incident.getId());

        assertNotNull(assignedIncident);
        assertEquals("ASSIGNED", assignedIncident.getStatus());
        assertEquals(
                nearResponderId,
                assignedIncident.getAssignedResponderId());

        Responder nearResponder = responderDAO.findById(nearResponderId);
        assertNotNull(nearResponder);
        assertFalse(nearResponder.isAvailable());
    }

    private int insertLocation(
            String building, double latitude, double longitude)
            throws SQLException {

        String sql = "INSERT INTO locations " +
                "(building, floor, room, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, building);
            statement.setString(2, "1");
            statement.setString(3, "101");
            statement.setDouble(4, latitude);
            statement.setDouble(5, longitude);
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM locations WHERE building = ?")) {

            statement.setString(1, building);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getInt("id");
            }
        }
    }

    private int insertResponder(
            int userId, String name, String responderType,
            int locationId) throws SQLException {

        String sql = "INSERT INTO responders " +
                "(user_id, name, responder_type, location_id, is_available) " +
                "VALUES (?, ?, ?, ?, 1)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, name);
            statement.setString(3, responderType);
            statement.setInt(4, locationId);
            statement.executeUpdate();
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM responders WHERE user_id = ?")) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getInt("id");
            }
        }
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
