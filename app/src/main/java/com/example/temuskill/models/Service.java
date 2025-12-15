package com.example.temuskill.models;

public class Service {
    private String serviceId;
    private String providerId;
    private String category;    // Contoh: "Les & Bimbingan"
    private String details;     // Contoh: "Bahasa Mandarin, Inggris"
    private int orderCount;     // Dummy counter

    public Service() {}

    public Service(String providerId, String category, String details) {
        this.providerId = providerId;
        this.category = category;
        this.details = details;
        this.orderCount = 0;
    }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
}