
package com.campus.emergency.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceCalculatorTest {

    @Test
    void testSameLocationHasZeroDistance() {
        double distance = DistanceCalculator.calculateDistance(
                11.0168, 76.9558,
                11.0168, 76.9558);

        assertEquals(0.0, distance, 0.000001);
    }

    @Test
    void testDistanceIsPositiveForDifferentLocations() {
        double distance = DistanceCalculator.calculateDistance(
                11.0168, 76.9558,
                11.0180, 76.9570);

        assertTrue(distance > 0);
    }

    @Test
    void testDistanceIsSymmetric() {
        double forward = DistanceCalculator.calculateDistance(
                11.0168, 76.9558,
                11.0180, 76.9570);

        double backward = DistanceCalculator.calculateDistance(
                11.0180, 76.9570,
                11.0168, 76.9558);

        assertEquals(forward, backward, 0.000001);
    }

    @Test
    void testDistanceBetweenIdenticalCoordinatesIsZero() {
        double distance = DistanceCalculator.calculateDistance(
                0.0, 0.0, 0.0, 0.0);

        assertEquals(0.0, distance, 0.000001);
    }
}
