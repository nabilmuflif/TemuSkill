package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.Constants; // Pastikan Constants sudah ada
import com.example.temuskill.utils.NotificationUtils; // Pastikan NotificationUtils sudah ada
import com.example.temuskill.utils.PriceFormatter;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderConfirmationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String talentId;
    private int duration;
    private User talentData;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Ambil data dari Intent
        talentId = getIntent().getStringExtra("TALENT_ID");
        duration = getIntent().getIntExtra("DURATION", 1);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_confirm).setOnClickListener(v -> createOrder());

        loadTalentData();
    }

    private void loadTalentData() {
        if (talentId == null) return;

        db.collection("users").document(talentId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                talentData = doc.toObject(User.class);

                if (talentData != null) {
                    TextView tvProvider = findViewById(R.id.tv_provider_name);
                    TextView tvService = findViewById(R.id.tv_service_name_confirm);
                    TextView tvDuration = findViewById(R.id.tv_duration);
                    TextView tvTotal = findViewById(R.id.tv_total_price);

                    tvProvider.setText(talentData.getNamaLengkap());

                    // Nama Layanan diambil dari keahlian talent
                    String skill = talentData.getKeahlian() != null ? talentData.getKeahlian() : "Jasa Umum";
                    tvService.setText(skill);

                    String satuan = talentData.getSatuanTarif() != null ? talentData.getSatuanTarif() : "Jam";
                    tvDuration.setText(duration + " " + satuan);

                    // Hitung Total Biaya
                    totalPrice = talentData.getTarif() * duration;
                    tvTotal.setText(PriceFormatter.formatPrice(totalPrice));
                }
            }
        });
    }

    private void createOrder() {
        if (talentData == null) {
            Toast.makeText(this, "Data Talent belum dimuat.", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUserId = sessionManager.getUserId();
        String skill = talentData.getKeahlian() != null ? talentData.getKeahlian() : "Jasa Umum";

        // 1. Buat Objek Order
        Order order = new Order(
                myUserId,
                talentId,
                "Hari ini (Proses Cepat)",
                totalPrice
        );

        order.setServiceName(skill);
        order.setServiceId(talentId); // ID Service disamakan dengan ID Talent untuk simplifikasi
        order.setProviderName(talentData.getNamaLengkap());
        order.setProviderId(talentId);

        // PENTING: Gunakan Constants agar status konsisten (pending/confirmed/completed)
        order.setStatusPesanan(Constants.ORDER_STATUS_PENDING);

        // 2. Simpan ke Firestore
        db.collection("orders").add(order)
                .addOnSuccessListener(doc -> {
                    String orderId = doc.getId();
                    // Update ID dokumen ke dalam field orderId
                    db.collection("orders").document(orderId).update("orderId", orderId);

                    // 3. KIRIM NOTIFIKASI KE TALENT
                    // "Ada pesanan baru masuk!"
                    String title = "Pesanan Baru Masuk 🔔";
                    String message = "Halo " + talentData.getNamaLengkap() + ", Anda menerima pesanan baru untuk jasa " + skill + ". Segera konfirmasi!";
                    NotificationUtils.sendNotification(talentId, title, message);

                    // 4. Pindah ke Halaman Pembayaran
                    Intent intent = new Intent(this, PaymentQrisActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal membuat pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}