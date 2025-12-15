package com.example.temuskill.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDetailActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvInfoGender, tvInfoJoin, tvInfoOrders;
    private CircleImageView ivAvatar;
    private Button btnToggle, btnDelete;
    private FirebaseFirestore db;
    private String userId;
    private int currentStatus = 1; // Default Aktif

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("USER_ID");

        initViews();
        loadUserData();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnToggle.setOnClickListener(v -> showConfirmationDialog(currentStatus == 1 ? "nonaktifkan" : "aktifkan"));
        btnDelete.setOnClickListener(v -> showConfirmationDialog("hapus"));
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_detail_name);
        tvEmail = findViewById(R.id.tv_detail_email);
        tvInfoGender = findViewById(R.id.tv_info_gender);
        tvInfoJoin = findViewById(R.id.tv_info_join);
        tvInfoOrders = findViewById(R.id.tv_info_orders);
        ivAvatar = findViewById(R.id.iv_detail_avatar);
        btnToggle = findViewById(R.id.btn_toggle_status);
        btnDelete = findViewById(R.id.btn_delete_user);
    }

    private void loadUserData() {
        if(userId == null) return;
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // Ambil data manual agar lebih akurat
                String nama = doc.getString("nama_lengkap");
                String email = doc.getString("email");
                String role = doc.getString("role");
                Long createdAt = doc.getLong("created_at");

                // PERBAIKAN DISINI: Baca langsung field "is_active"
                Long statusLong = doc.getLong("is_active");
                if (statusLong != null) {
                    currentStatus = statusLong.intValue();
                } else {
                    currentStatus = 1; // Default jika tidak ada field
                }

                tvName.setText(nama);
                tvEmail.setText(email);

                if (createdAt != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    tvInfoJoin.setText("Bergabung sejak : " + sdf.format(new Date(createdAt)));
                }

                tvInfoGender.setText("Jenis Kelamin : Laki-Laki"); // Dummy karena belum ada di DB

                // Update UI Tombol sesuai status terbaru
                updateToggleButtonUI();

                // Hitung Order
                countUserOrders(userId, role);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void countUserOrders(String uid, String role) {
        if (role == null) return;
        String field = role.equals("penyedia_jasa") ? "providerId" : "clientId";
        db.collection("orders").whereEqualTo(field, uid).count().get(com.google.firebase.firestore.AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> {
                    tvInfoOrders.setText("Total Pesanan : " + snapshot.getCount());
                });
    }

    private void updateToggleButtonUI() {
        if (currentStatus == 1) {
            // Jika user AKTIF -> Tombol tawarkan NONAKTIFKAN
            btnToggle.setText("Nonaktifkan Pengguna");
            btnToggle.setTextColor(Color.parseColor("#555555"));
        } else {
            // Jika user NONAKTIF -> Tombol tawarkan AKTIFKAN
            btnToggle.setText("Aktifkan Pengguna");
            btnToggle.setTextColor(Color.parseColor("#4CAF50")); // Hijau
        }
    }

    private void showConfirmationDialog(String action) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_action);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        tvTitle.setText("Yakin ingin " + action + "\npengguna ini?");

        Button btnYes = dialog.findViewById(R.id.btn_yes);
        Button btnNo = dialog.findViewById(R.id.btn_no);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            if (action.equals("hapus")) {
                deleteUser();
            } else {
                toggleStatus();
            }
        });

        dialog.show();
    }

    private void toggleStatus() {
        // Logika Toggle: Jika 1 jadi 0, Jika 0 jadi 1
        int newStatus = (currentStatus == 1) ? 0 : 1;

        db.collection("users").document(userId).update("is_active", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Update variabel lokal segera
                    currentStatus = newStatus;
                    updateToggleButtonUI();

                    String msg = (newStatus == 1) ? "Akun diaktifkan kembali" : "Akun berhasil dinonaktifkan";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteUser() {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pengguna dihapus permanen", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}