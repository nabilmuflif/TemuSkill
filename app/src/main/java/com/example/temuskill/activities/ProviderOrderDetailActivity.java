package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.Constants;
import com.example.temuskill.utils.NotificationUtils;
import com.example.temuskill.utils.PriceFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProviderOrderDetailActivity extends AppCompatActivity {

    private TextView tvClientName, tvServiceName, tvOrderDate, tvOrderAddress, tvOrderNotes, tvTotalIncome;
    private CircleImageView ivClientPhoto;
    private Button btnMainAction, btnSecondaryAction;
    private ImageView btnChatClient;

    private FirebaseFirestore db;
    private String orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_order_detail);

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) { finish(); return; }

        db = FirebaseFirestore.getInstance();
        initViews();
        loadOrderData();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvClientName = findViewById(R.id.tv_client_name);
        tvServiceName = findViewById(R.id.tv_service_name);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderAddress = findViewById(R.id.tv_order_address);
        tvOrderNotes = findViewById(R.id.tv_order_notes);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        ivClientPhoto = findViewById(R.id.iv_client_photo);
        btnMainAction = findViewById(R.id.btn_main_action);
        btnSecondaryAction = findViewById(R.id.btn_secondary_action);
        btnChatClient = findViewById(R.id.btn_chat_client);
    }

    private void loadOrderData() {
        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOrder = documentSnapshot.toObject(Order.class);
                        if (currentOrder != null) {
                            displayData();
                            setupButtons(); // Update tampilan tombol
                        }
                    }
                });
    }

    private void displayData() {
        tvServiceName.setText(currentOrder.getServiceName());
        tvOrderDate.setText(currentOrder.getJadwalKerja());
        tvTotalIncome.setText(PriceFormatter.formatPrice(currentOrder.getTotalBiaya()));
        tvOrderAddress.setText("Lokasi Pelanggan (Lihat Peta)");
        tvOrderNotes.setText(currentOrder.getCatatan() != null ? currentOrder.getCatatan() : "-");

        loadClientInfo(currentOrder.getClientId());
    }

    private void loadClientInfo(String clientId) {
        db.collection("users").document(clientId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User client = doc.toObject(User.class);
                if (client != null) {
                    tvClientName.setText(client.getNamaLengkap());
                    if (client.getFotoProfilUrl() != null && !client.getFotoProfilUrl().isEmpty()) {
                        if(client.getFotoProfilUrl().startsWith("http")){
                            Glide.with(this).load(client.getFotoProfilUrl()).placeholder(R.drawable.profile).into(ivClientPhoto);
                        } else {
                            int resId = getResources().getIdentifier(client.getFotoProfilUrl(), "drawable", getPackageName());
                            if(resId != 0) Glide.with(this).load(resId).into(ivClientPhoto);
                        }
                    }
                }
            }
        });
    }

    // --- BAGIAN UTAMA YANG DIPERBAIKI ---
    private void setupButtons() {
        String status = currentOrder.getStatusPesanan();
        if (status == null) status = "";

        // Default: Sembunyikan tombol tolak
        btnSecondaryAction.setVisibility(View.GONE);
        btnMainAction.setEnabled(true);

        // LOGIKA TOMBOL BERDASARKAN STATUS
        if (status.equals(Constants.ORDER_STATUS_PENDING)) {
            // Status: Pending -> Tombol "Terima" & "Tolak"
            btnMainAction.setText("Terima Pesanan");
            btnMainAction.setBackgroundColor(getColor(R.color.primary_blue)); // Warna Biru
            btnMainAction.setOnClickListener(v -> updateStatus(Constants.ORDER_STATUS_CONFIRMED));

            btnSecondaryAction.setVisibility(View.VISIBLE);
            btnSecondaryAction.setText("Tolak Pesanan");
            btnSecondaryAction.setOnClickListener(v -> updateStatus(Constants.ORDER_STATUS_CANCELLED));

        } else if (status.equals(Constants.ORDER_STATUS_CONFIRMED)) {
            // Status: Confirmed -> Tombol "Selesaikan Pekerjaan"
            btnMainAction.setText("Selesaikan Pekerjaan");
            btnMainAction.setBackgroundColor(getColor(R.color.green_success)); // Warna Hijau
            btnMainAction.setOnClickListener(v -> updateStatus(Constants.ORDER_STATUS_COMPLETED));

        } else if (status.equals(Constants.ORDER_STATUS_COMPLETED)) {
            // Status: Completed -> Tombol "Pesanan Selesai" (Mati)
            btnMainAction.setText("Pesanan Selesai");
            btnMainAction.setEnabled(false);
            btnMainAction.setBackgroundColor(getColor(android.R.color.darker_gray)); // Warna Abu-abu

        } else if (status.equals(Constants.ORDER_STATUS_CANCELLED)) {
            // Status: Cancelled -> Tombol "Dibatalkan"
            btnMainAction.setText("Pesanan Dibatalkan");
            btnMainAction.setEnabled(false);
            btnMainAction.setBackgroundColor(getColor(R.color.red_error)); // Warna Merah
        } else {
            // Fallback jika status tidak dikenali
            btnMainAction.setText("Status: " + status);
        }

        btnChatClient.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Chat segera hadir...", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateStatus(String newStatus) {
        db.collection("orders").document(orderId).update("statusPesanan", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status Berhasil Diperbarui!", Toast.LENGTH_SHORT).show();

                    // Kirim Notifikasi ke Client
                    if (currentOrder != null) {
                        String clientId = currentOrder.getClientId();
                        String title = "Update Pesanan 📦";
                        String msg = "Status pesanan jasa Anda kini: " + getStatusLabel(newStatus);
                        NotificationUtils.sendNotification(clientId, title, msg);
                    }

                    // Refresh halaman agar tombol berubah otomatis
                    loadOrderData();
                });
    }

    private String getStatusLabel(String status) {
        if (status.equals(Constants.ORDER_STATUS_CONFIRMED)) return "Diterima & Sedang Dikerjakan";
        if (status.equals(Constants.ORDER_STATUS_COMPLETED)) return "Selesai";
        if (status.equals(Constants.ORDER_STATUS_CANCELLED)) return "Dibatalkan";
        return status;
    }
}