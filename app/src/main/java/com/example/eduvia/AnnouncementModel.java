package com.example.eduvia;

public class AnnouncementModel {
    private String id;
    private String title;
    private String message;
    private String startDate;
    private String expiryDate;
    private String startTime;
    private String expiryTime;

    public AnnouncementModel(String id, String title, String message,
                             String startDate, String expiryDate, String startTime,
                             String expiryTime) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.startTime = startTime;
        this.expiryTime = expiryTime;
    }

    public AnnouncementModel() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }


    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getExpiryTime() { return expiryTime; }
    public void setExpiryTime(String expiryTime) { this.expiryTime = expiryTime; }

}
