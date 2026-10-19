
package com.campus.emergency.model;

public class MedicalResponder extends Responder {

    public MedicalResponder(int id, int userId, String name,
                            int locationId, boolean available) {
        super(id, userId, name, "MEDICAL", locationId, available);
    }

    @Override
    public boolean canHandleCategory(String category) {
        return "MEDICAL".equalsIgnoreCase(category);
    }
}
