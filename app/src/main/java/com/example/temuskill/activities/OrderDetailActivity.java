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
import com.example.temuskill.utils.PriceFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    // UI Components
    private TextView tvPickupAddress, tvDriverName, tvDriverRating, tvPlateNumber, tvPaymentMethod, tvTotalPrice;
    private CircleImageView ivDriverPhoto;
    private ImageView ivHeaderGallery; // Variabel untuk Gambar Header
    private Button btnCall, btnChat, btnCancelOrder;

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
        setupListeners();
        loadOrderData();
    }

    private void initViews() {
        // Text Views
        tvPickupAddress = findViewById(R.id.tv_pickup_address);
        tvDriverName = findViewById(R.id.tv_driver_name);
        tvDriverRating = findViewById(R.id.tv_driver_rating);
        tvPlateNumber = findViewById(R.id.tv_plate_number);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvTotalPrice = findViewById(R.id.tv_total_price);

        // Images
        ivDriverPhoto = findViewById(R.id.iv_driver_photo);
        ivHeaderGallery = findViewById(R.id.iv_header_gallery);

        // Buttons
        btnCall = findViewById(R.id.btn_call_driver);
        btnChat = findViewById(R.id.btn_chat_driver);
        btnCancelOrder = findViewById(R.id.btn_cancel_order_final);

        // Header Back Button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // 1. Batalkan Pesanan
        btnCancelOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, CancelOrderActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            startActivity(intent);
        });

        // 2. Chat Mitra
        btnChat.setOnClickListener(v -> {
            Toast.makeText(this, "Membuka Chat...", Toast.LENGTH_SHORT).show();
        });

        // 3. Lihat Profil Mitra
        View.OnClickListener profileListener = v -> {
            if(currentOrder != null) {
                Intent intent = new Intent(this, TalentDetailActivity.class);
                intent.putExtra("TALENT_ID", currentOrder.getProviderId());
                startActivity(intent);
            }
        };
        findViewById(R.id.btn_view_profile).setOnClickListener(profileListener);
        ivDriverPhoto.setOnClickListener(profileListener);
    }

    private void loadOrderData() {
        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOrder = documentSnapshot.toObject(Order.class);
                        if (currentOrder != null) {
                            displayData();
                            setupButtonVisibility();
                        }
                    }
                });
    }

    private void displayData() {
        // Isi data Order ke UI
        tvPickupAddress.setText("Lokasi Pengerjaan Sesuai Pesanan");
        tvDriverName.setText(currentOrder.getProviderName());
        tvPlateNumber.setText(currentOrder.getServiceName());

        tvTotalPrice.setText(PriceFormatter.formatPrice(currentOrder.getTotalBiaya()));
        tvPaymentMethod.setText("Tunai");

        // Load data Provider
        loadProviderInfo(currentOrder.getProviderId());
    }

    private void loadProviderInfo(String providerId) {
        db.collection("users").document(providerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User provider = doc.toObject(User.class);
                if (provider != null) {

                    // === 1. LOAD FOTO PROFIL (FIX LOGIC DUMMY vs REAL) ===
                    String photoUrl = provider.getFotoProfilUrl();

                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        if (photoUrl.startsWith("http") || photoUrl.startsWith("android.resource")) {
                            // Jika URL Internet atau URI Android -> Load Langsung
                            Glide.with(this).load(photoUrl)
                                    .placeholder(R.drawable.profile)
                                    .into(ivDriverPhoto);
                        } else {
                            // Jika Nama Resource (Data Dummy, misal "talent_1") -> Cari ID Drawable
                            int resId = getResources().getIdentifier(photoUrl, "drawable", getPackageName());
                            if (resId != 0) {
                                Glide.with(this).load(resId)
                                        .placeholder(R.drawable.profile)
                                        .into(ivDriverPhoto);
                            } else {
                                ivDriverPhoto.setImageResource(R.drawable.profile);
                            }
                        }
                    } else {
                        ivDriverPhoto.setImageResource(R.drawable.profile);
                    }

                    // === 2. LOAD FOTO HEADER DARI GALERI ===
                    List<String> galeri = provider.getGaleriUrl();
                    if (galeri != null && !galeri.isEmpty()) {
                        String headerImage = galeri.get(0); // Ambil foto pertama
                        Glide.with(this).load(headerImage)
                                .centerCrop()
                                .placeholder(R.color.grey_bg)
                                .into(ivHeaderGallery);
                    } else {
                        // Fallback ke foto profil jika galeri kosong
                        if (photoUrl != null && !photoUrl.isEmpty() && !photoUrl.startsWith("http")) {
                            // Handle dummy profile pic for header too
                            int resId = getResources().getIdentifier(photoUrl, "drawable", getPackageName());
                            if(resId != 0) Glide.with(this).load(resId).centerCrop().into(ivHeaderGallery);
                        } else {
                            Glide.with(this).load(photoUrl).centerCrop().placeholder(R.color.grey_bg).into(ivHeaderGallery);
                        }
                    }

                    // Rating
                    tvDriverRating.setText("5.0");
                }
            }
        });
    }

    private void setupButtonVisibility() {
        String status = currentOrder.getStatusPesanan();

        if (Constants.ORDER_STATUS_PENDING.equals(status)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(orderId != null) loadOrderData();
    }
}