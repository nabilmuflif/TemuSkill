package com.example.temuskill.models;

public class ChatPreview {
    private String orderId;
    private String partnerName;
    private String lastMessage;
    private String time;
    private String partnerId; // TAMBAHAN: ID Lawan Bicara

    public ChatPreview(String orderId, String partnerName, String lastMessage, String time, String partnerId) {
        this.orderId = orderId;
        this.partnerName = partnerName;
        this.lastMessage = lastMessage;
        this.time = time;
        this.partnerId = partnerId;
    }

    public String getOrderId() { return orderId; }
    public String getPartnerName() { return partnerName; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public String getPartnerId() { return partnerId; } // Getter Baru
}