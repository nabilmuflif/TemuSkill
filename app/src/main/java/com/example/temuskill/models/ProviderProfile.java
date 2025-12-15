package com.example.temuskill.models;

public class ProviderProfile {
    private String userId;
    private String statusVerifikasi; // pending, verified, rejected
    private long requestDate;

    public ProviderProfile() {}

    // Getters
    public String getUserId() { return userId; }
    public String getStatusVerifikasi() { return statusVerifikasi; }
    public long getRequestDate() { return requestDate; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setStatusVerifikasi(String statusVerifikasi) { this.statusVerifikasi = statusVerifikasi; }
    public void setRequestDate(long requestDate) { this.requestDate = requestDate; }
}