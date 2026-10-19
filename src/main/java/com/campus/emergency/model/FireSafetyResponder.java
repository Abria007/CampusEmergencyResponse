
package com.campus.emergency.model;

public class FireSafetyResponder extends Responder {

    public FireSafetyResponder(int id, int userId, String name,
                               int locationId, boolean available) {
        super(id, userId, name, "FIRE_SAFETY", locationId, available);
    }

    @Override
    public boolean canHandleCategory(String category) {
        return "FIRE".equalsIgnoreCase(category);
    }
}
