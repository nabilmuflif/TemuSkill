package com.example.temuskill.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
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

        // Ambil ID dari Intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            Toast.makeText(this, "ID Pesanan tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initViews();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadOrderData();
    }

    private void initViews() {
        tvClientName = findViewById(R.id.tv_client_name);
        tvServiceName = findViewById(R.id.tv_service_name);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderAddress = findViewById(R.id.tv_order_address);
        tvOrderNotes = findViewById(R.id.tv_order_notes);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        ivClientPhoto = findViewById(R.id.iv_client_photo);

        // Inisialisasi Tombol (Pastikan ID sama dengan XML)
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
                            setupButtons(); // Update tombol setelah data dimuat
                        }
                    } else {
                        Toast.makeText(this, "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void displayData() {
        tvServiceName.setText(currentOrder.getServiceName());
        tvOrderDate.setText(currentOrder.getJadwalKerja());
        tvTotalIncome.setText(PriceFormatter.formatPrice(currentOrder.getTotalBiaya()));

        String lokasi = currentOrder.getLokasi();
        tvOrderAddress.setText(lokasi != null && !lokasi.isEmpty() ? lokasi : "Lokasi Pelanggan");
        tvOrderNotes.setText(currentOrder.getCatatan() != null ? currentOrder.getCatatan() : "-");

        loadClientInfo(currentOrder.getClientId());
    }

    private void loadClientInfo(String clientId) {
        if (clientId == null) return;
        db.collection("users").document(clientId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User client = doc.toObject(User.class);
                if (client != null) {
                    tvClientName.setText(client.getNamaLengkap());
                    // Load Foto
                    if (client.getFotoProfilUrl() != null && !client.getFotoProfilUrl().isEmpty()) {
                        if (client.getFotoProfilUrl().startsWith("http")) {
                            Glide.with(this).load(client.getFotoProfilUrl()).placeholder(R.drawable.profile).into(ivClientPhoto);
                        } else {
                            try {
                                int resId = getResources().getIdentifier(client.getFotoProfilUrl(), "drawable", getPackageName());
                                if (resId != 0) Glide.with(this).load(resId).into(ivClientPhoto);
                            } catch (Exception e) {}
                        }
                    }
                }
            }
        });
    }

    // === LOGIKA TOMBOL ===
    private void setupButtons() {
        if (currentOrder == null) return;

        String status = currentOrder.getStatusPesanan() != null ? currentOrder.getStatusPesanan().toLowerCase() : "pending";
        Log.d("ORDER_STATUS", "Status Pesanan: " + status);

        // Default State: Main Button Visible, Secondary Gone
        btnMainAction.setVisibility(View.VISIBLE);
        btnMainAction.setEnabled(true);
        btnSecondaryAction.setVisibility(View.GONE);

        // Jika status "menunggu" atau "pending" -> TAMPILKAN KEDUA TOMBOL
        if (status.equals("menunggu") || status.equals("pending")) {
            // Tombol TERIMA (Biru)
            btnMainAction.setText("Terima Pesanan");
            btnMainAction.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue));
            btnMainAction.setOnClickListener(v -> updateStatus("aktif"));

            // Tombol TOLAK (Merah/Pink) -> Munculkan!
            btnSecondaryAction.setVisibility(View.VISIBLE);
            btnSecondaryAction.setText("Tolak Pesanan");
            btnSecondaryAction.setOnClickListener(v -> showCancelDialog());
        }
        // Jika status AKTIF/DIKERJAKAN -> TAMPILKAN "SELESAIKAN"
        else if (status.equals("aktif") || status.equals("confirmed") ||
                status.equals("dalam pengerjaan") || status.equals("in_progress") ||
                status.equals("dalam perjalanan") || status.equals("otw")) {

            btnMainAction.setText("Selesaikan Pekerjaan");
            btnMainAction.setBackgroundColor(ContextCompat.getColor(this, R.color.green_success)); // Hijau
            btnMainAction.setOnClickListener(v -> updateStatus("selesai"));

            // Sembunyikan tombol tolak saat sudah dikerjakan
            btnSecondaryAction.setVisibility(View.GONE);
        }
        // Jika SELESAI atau BATAL -> Matikan Tombol
        else {
            btnMainAction.setText(status.equalsIgnoreCase("dibatalkan") ? "Pesanan Dibatalkan" : "Pesanan Selesai");
            btnMainAction.setEnabled(false);
            btnMainAction.setBackgroundColor(Color.GRAY);
            btnSecondaryAction.setVisibility(View.GONE);
        }

        // Chat Button
        btnChatClient.setOnClickListener(v -> {
            // ... (Kode Intent Chat Anda)
        });
    }

    // === DIALOG BATALKAN PESANAN ===
    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_order, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Sesuaikan ID dengan layout dialog_cancel_order.xml Anda
        // Saya pakai ID umum: btn_yes / btn_no. Ganti jika beda.
        Button btnYes = view.findViewById(R.id.btn_yes); // Atau btn_confirm
        Button btnNo = view.findViewById(R.id.btn_no);   // Atau btn_cancel


        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                updateStatus("dibatalkan");
                dialog.dismiss();
            });
        }
        if (btnNo != null) {
            btnNo.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void updateStatus(String newStatus) {
        db.collection("orders").document(orderId).update("statusPesanan", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status diperbarui!", Toast.LENGTH_SHORT).show();
                    loadOrderData(); // Reload UI otomatis
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal update", Toast.LENGTH_SHORT).show());
    }
}