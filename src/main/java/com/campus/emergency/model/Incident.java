
package com.campus.emergency.model;

public class Incident {

    private int id;
    private int reportedBy;
    private String category;
    private String description;
    private int locationId;
    private String priority;
    private String status;
    private Integer assignedResponderId;
    private String resolutionNotes;

    public Incident(int id, int reportedBy, String category,
                    String description, int locationId,
                    String priority, String status,
                    Integer assignedResponderId,
                    String resolutionNotes) {

        this.id = id;
        this.reportedBy = reportedBy;
        this.category = category;
        this.description = description;
        this.locationId = locationId;
        this.priority = priority;
        this.status = status;
        this.assignedResponderId = assignedResponderId;
        this.resolutionNotes = resolutionNotes;
    }

    public int getId() {
        return id;
    }

    public int getReportedBy() {
        return reportedBy;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public Integer getAssignedResponderId() {
        return assignedResponderId;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }
}
