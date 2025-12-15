package com.example.temuskill.models;

public class Notification {
    private String title;
    private String message;
    private long timestamp;
    private String userId; // Penerima Notifikasi

    public Notification() {} // Constructor kosong Firebase

    public Notification(String userId, String title, String message) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
}