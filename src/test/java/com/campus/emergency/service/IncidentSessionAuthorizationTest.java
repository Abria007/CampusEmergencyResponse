
package com.campus.emergency.service;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentSessionAuthorizationTest {

    private final IncidentService incidentService = new IncidentService();

    @Test
    void reportingWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.reportIncident(
                        null, "FIRE", "Fire in laboratory", 1));
    }

    @Test
    void assigningWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.assignResponder(null, 1, 1));
    }

    @Test
    void startingIncidentWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.startIncident(null, 1));
    }

    @Test
    void resolvingIncidentWithoutSessionIsRejected() {
        assertThrows(SecurityException.class, () ->
                incidentService.resolveIncident(
                        null, 1, "Issue resolved"));
    }
}
