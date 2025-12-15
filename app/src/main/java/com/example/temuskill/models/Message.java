package com.example.temuskill.models;

import com.google.firebase.Timestamp; // Pastikan import ini benar

public class Message {
    private String senderId;
    private String receiverId;
    private String text;
    private Timestamp timestamp; // Menggunakan format waktu Firebase

    // Constructor Kosong (Wajib untuk Firestore)
    public Message() {}

    public Message(String senderId, String receiverId, String text) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = Timestamp.now();
    }

    // === GETTER & SETTER ===
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}