
package com.campus.emergency.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponderFactoryTest {

    @Test
    void testSecurityResponderCreation() {
        Responder responder = ResponderFactory.createResponder(
                1, 101, "Security Test", "SECURITY", 1, true);

        assertInstanceOf(SecurityResponder.class, responder);
        assertTrue(responder.canHandleCategory("SECURITY"));
        assertFalse(responder.canHandleCategory("FIRE"));
    }

    @Test
    void testMedicalResponderCreation() {
        Responder responder = ResponderFactory.createResponder(
                2, 102, "Medical Test", "MEDICAL", 1, true);

        assertInstanceOf(MedicalResponder.class, responder);
        assertTrue(responder.canHandleCategory("MEDICAL"));
        assertFalse(responder.canHandleCategory("SECURITY"));
    }

    @Test
    void testFireSafetyResponderCreation() {
        Responder responder = ResponderFactory.createResponder(
                3, 103, "Fire Test", "FIRE_SAFETY", 1, true);

        assertInstanceOf(FireSafetyResponder.class, responder);
        assertTrue(responder.canHandleCategory("FIRE"));
        assertFalse(responder.canHandleCategory("WATER"));
    }

    @Test
    void testMaintenanceResponderCreation() {
        Responder responder = ResponderFactory.createResponder(
                4, 104, "Maintenance Test", "MAINTENANCE", 1, true);

        assertInstanceOf(MaintenanceResponder.class, responder);
        assertTrue(responder.canHandleCategory("ELECTRICAL"));
        assertTrue(responder.canHandleCategory("WATER"));
        assertFalse(responder.canHandleCategory("FIRE"));
    }

    @Test
    void testUnknownResponderTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                ResponderFactory.createResponder(
                        5, 105, "Unknown Test", "UNKNOWN", 1, true));
    }
}
