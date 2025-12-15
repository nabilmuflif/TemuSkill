package com.example.temuskill.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.adapters.NotificationAdapter; // Import Adapter Baru
import com.example.temuskill.models.Notification;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter; // Menggunakan class adapter terpisah
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Tombol Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();
    }

    private void loadNotifications() {
        String myUid = sessionManager.getUserId();

        // Ambil notifikasi dari Firestore berdasarkan ID User yang sedang login
        db.collection("notifications")
                .whereEqualTo("userId", myUid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Notification notif = doc.toObject(Notification.class);
                        list.add(notif);
                    }

                    // Setup Adapter
                    adapter = new NotificationAdapter(list);
                    rvNotifications.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Handle error jika perlu (misal log atau toast)
                });
    }
}