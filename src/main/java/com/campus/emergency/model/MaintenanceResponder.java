
package com.campus.emergency.model;

public class MaintenanceResponder extends Responder {

    public MaintenanceResponder(int id, int userId, String name,
                                int locationId, boolean available) {
        super(id, userId, name, "MAINTENANCE", locationId, available);
    }

    @Override
    public boolean canHandleCategory(String category) {
        return "ELECTRICAL".equalsIgnoreCase(category)
                || "WATER".equalsIgnoreCase(category);
    }
}
