package com.example.temuskill.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class VerificationActivity extends AppCompatActivity {

    private Button btnAjukan;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Tombol Back
        findViewById(R.id.btn_back_container).setOnClickListener(v -> finish());

        // Tombol Ajukan -> Langsung Proses
        btnAjukan = findViewById(R.id.btn_ajukan);
        btnAjukan.setOnClickListener(v -> submitDirectVerification());
    }

    private void submitDirectVerification() {
        String uid = sessionManager.getUserId();
        if (uid == null) return;

        btnAjukan.setEnabled(false);
        btnAjukan.setText("Memproses...");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        data.put("statusVerifikasi", "pending");
        data.put("requestDate", System.currentTimeMillis());

        db.collection("provider_profiles").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    btnAjukan.setEnabled(true);
                    btnAjukan.setText("Ajukan Verifikasi");
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        // Tampilkan dialog full screen agar terlihat seperti pindah halaman
        Dialog dialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_verification_sent);

        // Tombol Oke -> Tutup Dialog & Activity
        Button btnOke = dialog.findViewById(R.id.btn_oke);
        btnOke.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Kembali ke Profil
        });

        dialog.show();
    }
}