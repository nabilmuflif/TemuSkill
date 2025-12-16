package com.example.temuskill.utils;

import com.example.temuskill.models.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationUtils {

    public static void sendNotification(String targetUserId, String title, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Membuat objek notifikasi baru
        Notification notif = new Notification(targetUserId, title, message);

        // Simpan ke Firestore
        db.collection("notifications")
                .add(notif)
                .addOnSuccessListener(documentReference -> {
                    // Berhasil dikirim (opsional: bisa tambah log)
                })
                .addOnFailureListener(e -> {
                    // Gagal dikirim
                });
    }
}