
package com.campus.emergency.model;

public class SecurityResponder extends Responder {

    public SecurityResponder(int id, int userId, String name,
                             int locationId, boolean available) {
        super(id, userId, name, "SECURITY", locationId, available);
    }

    @Override
    public boolean canHandleCategory(String category) {
        return "SECURITY".equalsIgnoreCase(category);
    }
}
