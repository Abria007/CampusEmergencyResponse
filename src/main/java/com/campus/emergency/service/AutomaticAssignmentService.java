
package com.campus.emergency.service;

import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.dao.LocationDAO;
import com.campus.emergency.dao.ResponderDAO;
import com.campus.emergency.model.Incident;
import com.campus.emergency.model.Location;
import com.campus.emergency.model.Responder;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.PriorityQueue;

public class AutomaticAssignmentService {

    private final IncidentDAO incidentDAO;
    private final LocationDAO locationDAO;
    private final ResponderDAO responderDAO;
    private final IncidentService incidentService;

    public AutomaticAssignmentService() {
        this.incidentDAO = new IncidentDAO();
        this.locationDAO = new LocationDAO();
        this.responderDAO = new ResponderDAO();
        this.incidentService = new IncidentService();
    }

    public boolean assignNearestResponder(int incidentId)
            throws SQLException {

        Incident incident = incidentDAO.findById(incidentId);

        if (incident == null
                || !"REPORTED".equalsIgnoreCase(incident.getStatus())) {
            return false;
        }

        Location incidentLocation =
                locationDAO.findById(incident.getLocationId());

        if (!hasValidCoordinates(incidentLocation)) {
            return false;
        }

        PriorityQueue<ResponderCandidate> candidates =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                ResponderCandidate::getDistanceKm));

        for (Responder responder : responderDAO.findAllAvailable()) {

            if (!responder.canHandleCategory(incident.getCategory())) {
                continue;
            }

            Location responderLocation =
                    locationDAO.findById(responder.getLocationId());

            if (!hasValidCoordinates(responderLocation)) {
                continue;
            }

            double distance = DistanceCalculator.calculateDistance(
                    incidentLocation.getLatitude(),
                    incidentLocation.getLongitude(),
                    responderLocation.getLatitude(),
                    responderLocation.getLongitude()
            );

            candidates.offer(
                    new ResponderCandidate(responder, distance));
        }

        while (!candidates.isEmpty()) {

            ResponderCandidate candidate = candidates.poll();

            boolean assigned = incidentService.assignResponder(
                    incidentId,
                    candidate.getResponder().getId()
            );

            if (assigned) {
                return true;
            }
        }

        return false;
    }

    private boolean hasValidCoordinates(Location location) {

        if (location == null) {
            return false;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)) {
            return false;
        }

        if (latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            return false;
        }

        // Treat (0, 0) as missing campus coordinates.
        return latitude != 0.0 || longitude != 0.0;
    }

    private static class ResponderCandidate {

        private final Responder responder;
        private final double distanceKm;

        public ResponderCandidate(
                Responder responder, double distanceKm) {
            this.responder = responder;
            this.distanceKm = distanceKm;
        }

        public Responder getResponder() {
            return responder;
        }

        public double getDistanceKm() {
            return distanceKm;
        }
    }
}
