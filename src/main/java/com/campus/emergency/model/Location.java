
package com.campus.emergency.model;

public class Location {

    private int id;
    private String building;
    private String floor;
    private String room;
    private double latitude;
    private double longitude;

    public Location(int id, String building, String floor,
                    String room, double latitude, double longitude) {
        this.id = id;
        this.building = building;
        this.floor = floor;
        this.room = room;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public String getBuilding() {
        return building;
    }

    public String getFloor() {
        return floor;
    }

    public String getRoom() {
        return room;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getFullLocation() {
        return building + ", Floor " + floor + ", Room " + room;
    }
}
