package com.example.temuskill.models;

public class Review {
    private String reviewId; // Jika pakai ID otomatis Firestore, ini opsional
    private String orderId;
    private String serviceId;
    private String userId; // ID Pemberi Review (Client)
    private String userName;
    private float rating; // float agar bisa 4.5
    private String comment;
    private long timestamp;

    // TAMBAHAN BARU
    private String providerId;

    public Review() {}

    // Getter & Setter
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; } // Hati-hati dengan nama field di Firestore (comment vs komentar)
    public void setComment(String comment) { this.comment = comment; }
    // Jika di firestore pakai "komentar", buat getter/setter getKomentar() juga

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
}