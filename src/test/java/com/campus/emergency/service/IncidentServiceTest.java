
package com.campus.emergency.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.dao.ResponderDAO;
import com.campus.emergency.dao.UserDAO;
import com.campus.emergency.database.DatabaseConnection;
import com.campus.emergency.model.Incident;
import com.campus.emergency.model.Responder;
import com.campus.emergency.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        // 1. Create separate reporter and responder accounts.
        authService.register(
                reporterUsername, "Reporter@123", "REPORTER"
        );

        authService.register(
                responderUsername, "Responder@123", "RESPONDER"
        );

        User reporter = userDAO.findByUsername(reporterUsername);
        User responderUser = userDAO.findByUsername(responderUsername);

        assertNotNull(reporter);
        assertNotNull(responderUser);

        // 2. Create a unique campus location.
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

        // 3. Add an available security responder.
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

        // 4. Report a fire incident.
        IncidentService incidentService = new IncidentService();

        assertTrue(incidentService.reportIncident(
                reporter.getId(),
                "FIRE",
                "Smoke detected near the laboratory",
                locationId
        ));

        // 5. Find the newly created incident.
        IncidentDAO incidentDAO = new IncidentDAO();
        Incident incident = null;

        for (Incident item : incidentDAO.findAll()) {
            if (item.getReportedBy() == reporter.getId()) {
                incident = item;
                break;
            }
        }

        assertNotNull(incident);
        assertEquals("CRITICAL", incident.getPriority());
        assertEquals("REPORTED", incident.getStatus());

        // 6. Assign the responder.
        assertTrue(incidentService.assignResponder(
                incident.getId(), responderId
        ));

        // 7. Verify the final incident status and assigned responder.
        Incident updatedIncident =
                incidentDAO.findById(incident.getId());

        assertNotNull(updatedIncident);
        assertEquals("ASSIGNED", updatedIncident.getStatus());
        assertEquals(
                responderId,
                updatedIncident.getAssignedResponderId()
        );

        // 8. Verify that the responder is now unavailable.
        ResponderDAO responderDAO = new ResponderDAO();
        Responder updatedResponder =
                responderDAO.findById(responderId);

        assertNotNull(updatedResponder);
        assertFalse(updatedResponder.isAvailable());
    }

}
