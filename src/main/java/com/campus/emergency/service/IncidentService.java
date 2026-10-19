
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

    // Constructor used by the real application.
    public IncidentService() {
        this(new IncidentDAO());
    }

    // Constructor used by unit tests.
    public IncidentService(IncidentDAO incidentDAO) {
        if (incidentDAO == null) {
            throw new IllegalArgumentException(
                    "IncidentDAO cannot be null."
            );
        }

        this.incidentDAO = incidentDAO;
    }

    public boolean reportIncident(
            int reportedBy,
            String category,
            String description,
            int locationId
    ) throws SQLException {

        if (reportedBy <= 0) {
            throw new IllegalArgumentException(
                    "Invalid reporter ID."
            );
        }

        if (category == null ||
                !VALID_CATEGORIES.contains(category.trim().toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid incident category."
            );
        }

        category = category.trim().toUpperCase();

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Incident description cannot be empty."
            );
        }

        if (locationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid location ID."
            );
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

    public boolean assignResponder(
            int incidentId,
            int responderId
    ) throws SQLException {

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
                incidentId,
                responderId
        );
    }

    public boolean startIncident(int incidentId)
            throws SQLException {

        if (incidentId <= 0) {
            throw new IllegalArgumentException(
                    "Incident ID must be positive."
            );
        }

        Incident incident = incidentDAO.findById(incidentId);

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Incident not found."
            );
        }

        if (!"ASSIGNED".equals(incident.getStatus())) {
            throw new IllegalArgumentException(
                    "Only assigned incidents can be started."
            );
        }

        return incidentDAO.startIncident(incidentId);
    }

    public boolean resolveIncident(
            int incidentId,
            String resolutionNotes
    ) throws SQLException {

        if (incidentId <= 0) {
            throw new IllegalArgumentException(
                    "Incident ID must be positive."
            );
        }

        if (resolutionNotes == null ||
                resolutionNotes.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Resolution notes cannot be empty."
            );
        }

        Incident incident = incidentDAO.findById(incidentId);

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Incident not found."
            );
        }

        if (!"IN_PROGRESS".equals(incident.getStatus())) {
            throw new IllegalArgumentException(
                    "Only in-progress incidents can be resolved."
            );
        }

        return incidentDAO.resolveIncident(
                incidentId,
                resolutionNotes.trim()
        );
    }
}
