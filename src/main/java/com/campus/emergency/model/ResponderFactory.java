
package com.campus.emergency.model;

public class ResponderFactory {

    public static Responder createResponder(
            int id,
            int userId,
            String name,
            String responderType,
            int locationId,
            boolean available) {

        if (responderType == null) {
            throw new IllegalArgumentException(
                    "Responder type cannot be null");
        }

        switch (responderType.toUpperCase()) {

            case "SECURITY":
                return new SecurityResponder(
                        id, userId, name, locationId, available);

            case "MEDICAL":
                return new MedicalResponder(
                        id, userId, name, locationId, available);

            case "FIRE_SAFETY":
                return new FireSafetyResponder(
                        id, userId, name, locationId, available);

            case "MAINTENANCE":
                return new MaintenanceResponder(
                        id, userId, name, locationId, available);

            default:
                throw new IllegalArgumentException(
                        "Unknown responder type: " + responderType);
        }
    }
}
