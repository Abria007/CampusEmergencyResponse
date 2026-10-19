
package com.campus.emergency.service;

import com.campus.emergency.model.Incident;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IncidentPriorityQueueTest {

    @Test
    void testCriticalIncidentComesFirst() {

        IncidentPriorityQueue queue = new IncidentPriorityQueue();

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

        queue.addIncident(medium);
        queue.addIncident(critical);
        queue.addIncident(high);

        assertEquals(critical, queue.getNextIncident());
        assertEquals(high, queue.getNextIncident());
        assertEquals(medium, queue.getNextIncident());

        assertTrue(queue.isEmpty());
    }

    @Test
    void testSamePriorityUsesSmallerIncidentIdFirst() {

        IncidentPriorityQueue queue = new IncidentPriorityQueue();

        Incident second = new Incident(
                2, 1, "SECURITY", "Second incident",
                1, "HIGH", "REPORTED", null, null
        );

        Incident first = new Incident(
                1, 1, "SECURITY", "First incident",
                1, "HIGH", "REPORTED", null, null
        );

        queue.addIncident(second);
        queue.addIncident(first);

        assertEquals(first, queue.getNextIncident());
        assertEquals(second, queue.getNextIncident());
    }

    @Test
    void testPeekDoesNotRemoveIncident() {

        IncidentPriorityQueue queue = new IncidentPriorityQueue();

        Incident incident = new Incident(
                1, 1, "FIRE", "Fire emergency",
                1, "CRITICAL", "REPORTED", null, null
        );

        queue.addIncident(incident);

        assertEquals(incident, queue.peekNextIncident());
        assertEquals(1, queue.size());

        assertEquals(incident, queue.getNextIncident());
        assertTrue(queue.isEmpty());
    }

    @Test
    void testNullIncidentIsRejected() {

        IncidentPriorityQueue queue = new IncidentPriorityQueue();

        assertThrows(
                IllegalArgumentException.class,
                () -> queue.addIncident(null)
        );
    }

    @Test
    void testQueueSize() {

        IncidentPriorityQueue queue = new IncidentPriorityQueue();

        assertEquals(0, queue.size());

        queue.addIncident(new Incident(
                1, 1, "FIRE", "Fire",
                1, "CRITICAL", "REPORTED", null, null
        ));

        queue.addIncident(new Incident(
                2, 1, "WATER", "Water leakage",
                1, "MEDIUM", "REPORTED", null, null
        ));

        assertEquals(2, queue.size());
    }
}
