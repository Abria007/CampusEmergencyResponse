
package com.campus.emergency.service;

import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.model.Incident;

import java.sql.SQLException;
import java.util.List;

public class IncidentQueueService {

    private final IncidentDAO incidentDAO;

    public IncidentQueueService() {
        this.incidentDAO = new IncidentDAO();
    }

    public IncidentQueueService(IncidentDAO incidentDAO) {
        if (incidentDAO == null) {
            throw new IllegalArgumentException(
                    "IncidentDAO cannot be null."
            );
        }

        this.incidentDAO = incidentDAO;
    }

    // Load all reported incidents into the priority queue.
    public IncidentPriorityQueue loadReportedIncidents()
            throws SQLException {

        List<Incident> incidents =
                incidentDAO.findReportedIncidents();

        IncidentPriorityQueue queue =
                new IncidentPriorityQueue();

        for (Incident incident : incidents) {
            queue.addIncident(incident);
        }

        return queue;
    }

    // Return the next incident without removing it.
    public Incident getNextIncident() throws SQLException {

        IncidentPriorityQueue queue =
                loadReportedIncidents();

        return queue.peekNextIncident();
    }

    // Return the number of reported incidents.
    public int getReportedIncidentCount() throws SQLException {

        return incidentDAO.findReportedIncidents().size();
    }
}
