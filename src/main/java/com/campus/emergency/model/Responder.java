
package com.campus.emergency.model;

public class Responder {

    private int id;
    private int userId;
    private String name;
    private String responderType;
    private int locationId;
    private boolean available;

    public Responder(int id, int userId, String name,
                     String responderType, int locationId,
                     boolean available) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.responderType = responderType;
        this.locationId = locationId;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getResponderType() {
        return responderType;
    }

    public int getLocationId() {
        return locationId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
