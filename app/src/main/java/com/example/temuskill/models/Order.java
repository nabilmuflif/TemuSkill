package com.example.temuskill.models;

import com.google.firebase.firestore.PropertyName;

public class Order {
    private String orderId;
    private String clientId;
    private String serviceId;
    private String jadwalKerja;
    private double totalBiaya;
    private String statusPesanan;
    private String catatan;
    private long createdAt;

    private String serviceName;
    private String providerName;
    private String providerId;

    // Variabel Boolean Object (Default null safe)
    private Boolean isReviewed = false;

    public Order() {}

    public Order(String clientId, String serviceId, String jadwalKerja, double totalBiaya) {
        this.clientId = clientId;
        this.serviceId = serviceId;
        this.jadwalKerja = jadwalKerja;
        this.totalBiaya = totalBiaya;
        this.statusPesanan = "pending";
        this.createdAt = System.currentTimeMillis();
        this.isReviewed = false;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getJadwalKerja() { return jadwalKerja; }
    public void setJadwalKerja(String jadwalKerja) { this.jadwalKerja = jadwalKerja; }

    public double getTotalBiaya() { return totalBiaya; }
    public void setTotalBiaya(double totalBiaya) { this.totalBiaya = totalBiaya; }

    public String getStatusPesanan() { return statusPesanan; }
    public void setStatusPesanan(String statusPesanan) { this.statusPesanan = statusPesanan; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    // [PENTING] Mapping field isReviewed agar terbaca Firebase
    @PropertyName("isReviewed")
    public boolean getIsReviewed() {
        return isReviewed != null && isReviewed;
    }

    @PropertyName("isReviewed")
    public void setIsReviewed(boolean isReviewed) {
        this.isReviewed = isReviewed;
    }

    // Helper untuk logika Java
    public boolean isReviewed() {
        return getIsReviewed();
    }

    public void setReviewed(boolean reviewed) {
        this.isReviewed = reviewed;
    }
}