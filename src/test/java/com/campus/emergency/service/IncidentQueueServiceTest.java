
package com.campus.emergency.service;

import com.campus.emergency.dao.IncidentDAO;
import com.campus.emergency.model.Incident;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncidentQueueServiceTest {

    static class FakeIncidentDAO extends IncidentDAO {

        private final List<Incident> incidents;

        FakeIncidentDAO(List<Incident> incidents) {
            this.incidents = incidents;
        }

        @Override
        public List<Incident> findReportedIncidents()
                throws SQLException {
            return incidents;
        }
    }

    @Test
    void testIncidentsAreReturnedInPriorityOrder() throws SQLException {

        Incident medium = new Incident(
                1, 1, "WATER", "Water leakage",
                1, "MEDIUM", "REPORTED", null, null
        );

        Incident critical = new Incident(
                2, 1, "FIRE", "Fire emergency",
                1, "CRITICAL", "REPORTED", null, null
        );

        Incident high = new Incident(
                3, 1, "SECURITY", "Security issue",
                1, "HIGH", "REPORTED", null, null
        );

        IncidentDAO fakeDAO = new FakeIncidentDAO(
                Arrays.asList(medium, critical, high)
        );

        IncidentQueueService service =
                new IncidentQueueService(fakeDAO);

        IncidentPriorityQueue queue =
                service.loadReportedIncidents();

        assertEquals(critical, queue.getNextIncident());
        assertEquals(high, queue.getNextIncident());
        assertEquals(medium, queue.getNextIncident());
        assertTrue(queue.isEmpty());
    }

    @Test
    void testEmptyIncidentListReturnsEmptyQueue()
            throws SQLException {

        IncidentDAO fakeDAO = new FakeIncidentDAO(
                Collections.emptyList()
        );

        IncidentQueueService service =
                new IncidentQueueService(fakeDAO);

        IncidentPriorityQueue queue =
                service.loadReportedIncidents();

        assertTrue(queue.isEmpty());
        assertEquals(0, service.getReportedIncidentCount());
        assertNull(service.getNextIncident());
    }

    @Test
    void testNextIncidentReturnsHighestPriority()
            throws SQLException {

        Incident high = new Incident(
                2, 1, "SECURITY", "Security issue",
                1, "HIGH", "REPORTED", null, null
        );

        Incident critical = new Incident(
                1, 1, "FIRE", "Fire emergency",
                1, "CRITICAL", "REPORTED", null, null
        );

        IncidentDAO fakeDAO = new FakeIncidentDAO(
                Arrays.asList(high, critical)
        );

        IncidentQueueService service =
                new IncidentQueueService(fakeDAO);

        assertEquals(critical, service.getNextIncident());
    }
}
