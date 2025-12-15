package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User; // Pakai User
import com.example.temuskill.utils.PriceFormatter;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderConfirmationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String talentId; // Ganti serviceId jadi talentId
    private int duration;
    private User talentData; // Ganti Service jadi User
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Ambil TALENT_ID, bukan SERVICE_ID
        talentId = getIntent().getStringExtra("TALENT_ID");

        // Durasi default 1 jam jika tidak ada
        duration = getIntent().getIntExtra("DURATION", 1);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_confirm).setOnClickListener(v -> createOrder());

        loadTalentData();
    }

    private void loadTalentData() {
        if(talentId == null) return;

        // Ambil data dari koleksi USERS
        db.collection("users").document(talentId).get().addOnSuccessListener(doc -> {
            if(doc.exists()) {
                talentData = doc.toObject(User.class);

                if(talentData != null) {
                    TextView tvProvider = findViewById(R.id.tv_provider_name);
                    TextView tvService = findViewById(R.id.tv_service_name_confirm);
                    TextView tvDuration = findViewById(R.id.tv_duration);
                    TextView tvTotal = findViewById(R.id.tv_total_price);

                    tvProvider.setText(talentData.getNamaLengkap());

                    // Nama Layanan = Keahlian Utama Talent
                    String skill = talentData.getKeahlian() != null ? talentData.getKeahlian() : "Jasa Umum";
                    tvService.setText(skill);

                    tvDuration.setText(duration + " " + (talentData.getSatuanTarif() != null ? talentData.getSatuanTarif() : "Jam"));

                    // Hitung Total
                    totalPrice = talentData.getTarif() * duration;
                    tvTotal.setText(PriceFormatter.formatPrice(totalPrice));
                }
            }
        });
    }

    private void createOrder() {
        if(talentData == null) return;

        // Buat Order Baru
        Order order = new Order(
                sessionManager.getUserId(),
                talentId, // Simpan ID Talent sebagai 'serviceId' atau 'providerId'
                "Hari ini (Proses Cepat)",
                totalPrice
        );

        // Isi detail order
        String skill = talentData.getKeahlian() != null ? talentData.getKeahlian() : "Jasa Umum";
        order.setServiceName(skill);
        order.setServiceId(talentId); // Service ID diisi ID Talent

        order.setProviderName(talentData.getNamaLengkap());
        order.setProviderId(talentId); // Provider ID = Talent ID

        order.setStatusPesanan("pending");

        db.collection("orders").add(order)
                .addOnSuccessListener(doc -> {
                    String orderId = doc.getId();
                    db.collection("orders").document(orderId).update("orderId", orderId);

                    Intent intent = new Intent(this, PaymentQrisActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show());
    }
}