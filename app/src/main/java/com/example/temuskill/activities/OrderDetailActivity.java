package com.example.temuskill.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.Constants;
import com.example.temuskill.utils.NotificationUtils; // PENTING
import com.example.temuskill.utils.PriceFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvProviderName, tvServiceName, tvOrderDate, tvOrderAddress, tvTotalIncome;
    private CircleImageView ivProviderPhoto;
    private Button btnCancelOrder, btnRateOrder; // Tambahkan btnCancelOrder

    private FirebaseFirestore db;
    private String orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) { finish(); return; }

        db = FirebaseFirestore.getInstance();

        initViews();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadOrderData();
    }

    private void initViews() {
        // Pastikan ID di XML sesuai
        tvProviderName = findViewById(R.id.tv_provider_name);
        tvServiceName = findViewById(R.id.tv_service_name);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderAddress = findViewById(R.id.tv_order_address);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        ivProviderPhoto = findViewById(R.id.iv_provider_photo);

        // Inisialisasi Tombol
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        // Jika ada tombol lain (misal btn_rate untuk review), inisialisasi juga
    }

    private void loadOrderData() {
        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOrder = documentSnapshot.toObject(Order.class);
                        if (currentOrder != null) {
                            displayData();
                            setupButtons(); // PENTING: Panggil fungsi setup tombol
                        }
                    }
                });
    }

    private void displayData() {
        tvServiceName.setText(currentOrder.getServiceName());
        tvOrderDate.setText(currentOrder.getJadwalKerja());
        tvTotalIncome.setText(PriceFormatter.formatPrice(currentOrder.getTotalBiaya()));

        // Load Data Provider (Talent)
        loadProviderInfo(currentOrder.getProviderId());
    }

    private void loadProviderInfo(String providerId) {
        db.collection("users").document(providerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User provider = doc.toObject(User.class);
                if (provider != null) {
                    tvProviderName.setText(provider.getNamaLengkap());
                    if (provider.getFotoProfilUrl() != null) {
                        if(provider.getFotoProfilUrl().startsWith("http")) {
                            Glide.with(this).load(provider.getFotoProfilUrl()).into(ivProviderPhoto);
                        } else {
                            int resId = getResources().getIdentifier(provider.getFotoProfilUrl(), "drawable", getPackageName());
                            if(resId!=0) Glide.with(this).load(resId).into(ivProviderPhoto);
                        }
                    }
                }
            }
        });
    }

    // --- BAGIAN UTAMA LOGIKA TOMBOL ---
    private void setupButtons() {
        String status = currentOrder.getStatusPesanan();

        // Default: Sembunyikan semua tombol dulu
        btnCancelOrder.setVisibility(View.GONE);

        // LOGIKA: Tombol Batal HANYA muncul jika status "pending"
        if (Constants.ORDER_STATUS_PENDING.equals(status)) {

            btnCancelOrder.setVisibility(View.VISIBLE);
            btnCancelOrder.setOnClickListener(v -> cancelOrder());

        }
        // Tambahkan logika lain jika perlu (misal tombol Review jika status completed)
    }

    private void cancelOrder() {
        // Update status di Firestore menjadi 'cancelled'
        db.collection("orders").document(orderId).update("statusPesanan", Constants.ORDER_STATUS_CANCELLED)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pesanan berhasil dibatalkan", Toast.LENGTH_SHORT).show();

                    // KIRIM NOTIFIKASI KE TALENT (PROVIDER)
                    if (currentOrder != null) {
                        String talentId = currentOrder.getProviderId();
                        String title = "Pesanan Dibatalkan ❌";
                        String msg = "Pelanggan membatalkan pesanan untuk jasa: " + currentOrder.getServiceName();
                        NotificationUtils.sendNotification(talentId, title, msg);
                    }

                    loadOrderData(); // Refresh halaman
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal membatalkan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}