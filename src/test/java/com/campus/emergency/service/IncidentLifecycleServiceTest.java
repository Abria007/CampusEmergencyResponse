
package com.campus.emergency.service;

import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.model.Incident;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class IncidentLifecycleServiceTest {

    static class FakeIncidentDAO extends IncidentDAO {

        private final Incident incident;
        boolean started;
        boolean resolved;
        String savedNotes;

        FakeIncidentDAO(Incident incident) {
            this.incident = incident;
        }

        @Override
        public Incident findById(int incidentId) {
            if (incident != null &&
                    incident.getId() == incidentId) {
                return incident;
            }

            return null;
        }

        @Override
        public boolean startIncident(int incidentId)
                throws SQLException {
            started = true;
            return true;
        }

        @Override
        public boolean resolveIncident(
                int incidentId,
                String resolutionNotes
        ) throws SQLException {
            resolved = true;
            savedNotes = resolutionNotes;
            return true;
        }
    }

    private Incident createIncident(String status) {
        return new Incident(
                1,
                1,
                "FIRE",
                "Fire emergency",
                1,
                "CRITICAL",
                status,
                2,
                null
        );
    }

    @Test
    void testAssignedIncidentCanBeStarted()
            throws SQLException {

        FakeIncidentDAO dao =
                new FakeIncidentDAO(createIncident("ASSIGNED"));

        IncidentService service = new IncidentService(dao);

        assertTrue(service.startIncident(1));
        assertTrue(dao.started);
    }

    @Test
    void testInProgressIncidentCanBeResolved()
            throws SQLException {

        FakeIncidentDAO dao =
                new FakeIncidentDAO(createIncident("IN_PROGRESS"));

        IncidentService service = new IncidentService(dao);

        assertTrue(service.resolveIncident(
                1, "Fire extinguished and area secured."
        ));

        assertTrue(dao.resolved);
        assertEquals(
                "Fire extinguished and area secured.",
                dao.savedNotes
        );
    }

    @Test
    void testReportedIncidentCannotBeStarted() {

        FakeIncidentDAO dao =
                new FakeIncidentDAO(createIncident("REPORTED"));

        IncidentService service = new IncidentService(dao);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.startIncident(1)
        );

        assertFalse(dao.started);
    }

    @Test
    void testEmptyResolutionNotesAreRejected() {

        FakeIncidentDAO dao =
                new FakeIncidentDAO(createIncident("IN_PROGRESS"));

        IncidentService service = new IncidentService(dao);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveIncident(1, "   ")
        );

        assertFalse(dao.resolved);
    }
}
