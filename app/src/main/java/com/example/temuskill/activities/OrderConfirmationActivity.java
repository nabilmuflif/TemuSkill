package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import de.hdodenhof.circleimageview.CircleImageView;

public class OrderConfirmationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String talentId;
    private int duration;
    private User talentData;
    private double totalPrice;

    // Views
    private TextView tvProviderName, tvProviderCategory, tvService, tvDuration, tvTotal;
    private CircleImageView ivProviderPhoto;
    private ImageView ivHeaderBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        talentId = getIntent().getStringExtra("TALENT_ID");
        duration = getIntent().getIntExtra("DURATION", 1);

        initViews();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_confirm).setOnClickListener(v -> createOrder());

        loadTalentData();
        loadTalentGallery(); // Memuat Banner dari Galeri
    }

    private void initViews() {
        tvProviderName = findViewById(R.id.tv_provider_name);
        tvProviderCategory = findViewById(R.id.tv_provider_category);
        tvService = findViewById(R.id.tv_service_name_confirm);
        tvDuration = findViewById(R.id.tv_duration);
        tvTotal = findViewById(R.id.tv_total_price);
        ivProviderPhoto = findViewById(R.id.iv_provider_photo);
    }

    private void loadTalentData() {
        if (talentId == null) return;

        db.collection("users").document(talentId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                talentData = doc.toObject(User.class);

                if (talentData != null) {
                    tvProviderName.setText(talentData.getNamaLengkap());

                    // Set Kategori/Keahlian
                    String skill = talentData.getKeahlian() != null ? talentData.getKeahlian() : "Jasa Umum";
                    tvProviderCategory.setText("Spesialis " + skill);
                    tvService.setText(skill);

                    // Set Durasi
                    String satuan = talentData.getSatuanTarif() != null ? talentData.getSatuanTarif() : "Jam";
                    tvDuration.setText(duration + " " + satuan);

                    // Hitung Harga
                    totalPrice = talentData.getTarif() * duration;
                    tvTotal.setText(PriceFormatter.formatPrice(totalPrice));

                    // LOAD FOTO PROFIL
                    String profileUrl = talentData.getFotoProfilUrl();
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        if (profileUrl.startsWith("http")) {
                            Glide.with(this).load(profileUrl).placeholder(R.drawable.profile).into(ivProviderPhoto);
                        } else {
                            int resId = getResources().getIdentifier(profileUrl, "drawable", getPackageName());
                            if (resId != 0) Glide.with(this).load(resId).into(ivProviderPhoto);
                        }
                    }
                }
            }
        });
    }

    // FUNGSI BARU: Ambil gambar pertama dari koleksi Gallery provider untuk Banner
    private void loadTalentGallery() {
        if (talentId == null) return;

        // Asumsi nama koleksi galeri adalah "gallery" di dalam dokumen user
        db.collection("users").document(talentId).collection("gallery")
                .limit(1) // Ambil 1 saja buat header
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            // Cek field imageUrl (sesuaikan dengan database Anda)
                            String imageUrl = doc.getString("imageUrl");
                            if (imageUrl == null) imageUrl = doc.getString("url"); // Coba field lain

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .centerCrop()
                                        .placeholder(R.drawable.populer1) // Gambar default saat loading
                                        .into(ivHeaderBanner);
                            }
                        }
                    } else {
                        // Jika tidak punya galeri, tetap pakai gambar default (sudah di XML)
                    }
                });
    }

    private void createOrder() {
        if (talentData == null) {
            Toast.makeText(this, "Data Talent belum siap...", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUserId = sessionManager.getUserId();
        String skill = talentData.getKeahlian() != null ? talentData.getKeahlian() : "Jasa Umum";

        Order order = new Order(myUserId, talentId, "Hari ini", totalPrice);
        order.setServiceName(skill);
        order.setServiceId(talentId);
        order.setProviderName(talentData.getNamaLengkap());
        order.setProviderId(talentId);
        order.setStatusPesanan(Constants.ORDER_STATUS_PENDING);

        db.collection("orders").add(order)
                .addOnSuccessListener(doc -> {
                    String orderId = doc.getId();
                    db.collection("orders").document(orderId).update("orderId", orderId);

                    // Notifikasi
                    NotificationUtils.sendNotification(talentId, "Pesanan Baru!", "Anda mendapat pesanan baru.");

                    // Ke Pembayaran
                    Intent intent = new Intent(this, PaymentQrisActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}