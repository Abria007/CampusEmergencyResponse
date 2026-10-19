
package com.campus.emergency.service;
import com.campus.emergency.dao.ResponderDAO;
import com.campus.emergency.model.Responder;
import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.model.Incident;
import com.campus.emergency.dao.AssignmentDAO;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IncidentService {

    private final IncidentDAO incidentDAO;

    private static final Set<String> VALID_CATEGORIES =
            new HashSet<>(Arrays.asList(
                    "FIRE",
                    "MEDICAL",
                    "ELECTRICAL",
                    "SECURITY",
                    "WATER"
            ));

    public IncidentService() {
        incidentDAO = new IncidentDAO();
    }

    public boolean reportIncident(int reportedBy, String category,
                                  String description, int locationId)
            throws SQLException {

        if (reportedBy <= 0) {
            throw new IllegalArgumentException("Invalid reporter ID.");
        }

        if (category == null ||
                !VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException(
                    "Invalid incident category."
            );
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Incident description cannot be empty."
            );
        }

        if (locationId <= 0) {
            throw new IllegalArgumentException("Invalid location ID.");
        }

        String priority = determinePriority(category);

        Incident incident = new Incident(
                0,
                reportedBy,
                category,
                description.trim(),
                locationId,
                priority,
                "REPORTED",
                null,
                null
        );

        return incidentDAO.createIncident(incident);
    }

    private String determinePriority(String category) {
        switch (category) {
            case "FIRE":
            case "MEDICAL":
                return "CRITICAL";

            case "ELECTRICAL":
            case "SECURITY":
                return "HIGH";

            case "WATER":
                return "MEDIUM";

            default:
                throw new IllegalArgumentException(
                        "Cannot determine priority for this category."
                );
        }
    }


    public boolean assignResponder(int incidentId, int responderId)
            throws SQLException {

        if (incidentId <= 0 || responderId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid incident ID or responder ID."
            );
        }

        Incident incident = incidentDAO.findById(incidentId);

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Incident not found."
            );
        }

        if (!"REPORTED".equals(incident.getStatus())) {
            throw new IllegalArgumentException(
                    "Only reported incidents can be assigned."
            );
        }

        ResponderDAO responderDAO = new ResponderDAO();
        Responder responder = responderDAO.findById(responderId);

        if (responder == null) {
            throw new IllegalArgumentException(
                    "Responder not found."
            );
        }

        if (!responder.isAvailable()) {
            throw new IllegalArgumentException(
                    "Responder is not available."
            );
        }

        AssignmentDAO assignmentDAO = new AssignmentDAO();

        return assignmentDAO.assignResponderAtomically(
                incidentId, responderId);
    }

}
