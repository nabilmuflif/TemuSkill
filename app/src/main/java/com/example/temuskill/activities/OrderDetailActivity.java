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

    private TextView tvPickupAddress, tvDriverName, tvDriverRating, tvPlateNumber, tvPaymentMethod, tvTotalPrice;
    private CircleImageView ivDriverPhoto;
    private ImageView ivHeaderGallery;
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
        tvPickupAddress = findViewById(R.id.tv_pickup_address);
        tvDriverName = findViewById(R.id.tv_driver_name);
        tvDriverRating = findViewById(R.id.tv_driver_rating);
        tvPlateNumber = findViewById(R.id.tv_plate_number);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvTotalPrice = findViewById(R.id.tv_total_price);

        ivDriverPhoto = findViewById(R.id.iv_driver_photo);
        ivHeaderGallery = findViewById(R.id.iv_header_gallery);
        
        btnChat = findViewById(R.id.btn_chat_driver);
        btnCancelOrder = findViewById(R.id.btn_cancel_order_final);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // 1. Batalkan Pesanan
        btnCancelOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, CancelOrderActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            startActivity(intent);
        });

        // 2. Chat Mitra (REVISI PENTING: Kirim ORDER_ID ke MainActivity)
        btnChat.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getProviderId() != null) {
                Intent intent = new Intent(this, MainActivity.class);

                // Beri sinyal ke MainActivity
                intent.putExtra("NAVIGATE_TO", "CHAT_FRAGMENT");

                // KIRIM ORDER ID AGAR CHAT BISA LOAD PESAN
                intent.putExtra("ORDER_ID", orderId);

                // Kirim Data Mitra (agar Header Nama & Foto instan muncul)
                intent.putExtra("TARGET_USER_ID", currentOrder.getProviderId());
                intent.putExtra("TARGET_USER_NAME", currentOrder.getProviderName());

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Data belum siap", Toast.LENGTH_SHORT).show();
            }
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
        tvPickupAddress.setText("Lokasi Pengerjaan Sesuai Pesanan");
        tvDriverName.setText(currentOrder.getProviderName());
        tvPlateNumber.setText(currentOrder.getServiceName());
        tvTotalPrice.setText(PriceFormatter.formatPrice(currentOrder.getTotalBiaya()));
        tvPaymentMethod.setText("Tunai");

        loadProviderInfo(currentOrder.getProviderId());
    }

    private void loadProviderInfo(String providerId) {
        db.collection("users").document(providerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User provider = doc.toObject(User.class);
                if (provider != null) {
                    String photoUrl = provider.getFotoProfilUrl();
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        if (photoUrl.startsWith("http") || photoUrl.startsWith("android.resource")) {
                            Glide.with(this).load(photoUrl).placeholder(R.drawable.profile).into(ivDriverPhoto);
                        } else {
                            int resId = getResources().getIdentifier(photoUrl, "drawable", getPackageName());
                            if (resId != 0) Glide.with(this).load(resId).placeholder(R.drawable.profile).into(ivDriverPhoto);
                            else ivDriverPhoto.setImageResource(R.drawable.profile);
                        }
                    } else {
                        ivDriverPhoto.setImageResource(R.drawable.profile);
                    }

                    List<String> galeri = provider.getGaleriUrl();
                    if (galeri != null && !galeri.isEmpty()) {
                        Glide.with(this).load(galeri.get(0)).centerCrop().placeholder(R.color.grey_bg).into(ivHeaderGallery);
                    } else {
                        if (photoUrl != null && !photoUrl.isEmpty() && !photoUrl.startsWith("http")) {
                            int resId = getResources().getIdentifier(photoUrl, "drawable", getPackageName());
                            if(resId != 0) Glide.with(this).load(resId).centerCrop().into(ivHeaderGallery);
                        } else {
                            Glide.with(this).load(photoUrl).centerCrop().placeholder(R.color.grey_bg).into(ivHeaderGallery);
                        }
                    }
                    tvDriverRating.setText("5.0");
                }
            }
        });
    }

    private void setupButtonVisibility() {
        String status = currentOrder.getStatusPesanan();

        // 1. Tombol Batalkan (Pending/Confirmed -> Muncul)
        if (Constants.ORDER_STATUS_PENDING.equals(status) || Constants.ORDER_STATUS_CONFIRMED.equals(status)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }

        // 2. Tombol Chat (Selesai/Dibatalkan -> Nonaktif & Ganti Teks)
        if (Constants.ORDER_STATUS_COMPLETED.equals(status) || Constants.ORDER_STATUS_REVIEWED.equals(status)) {
            btnChat.setText("Pesanan Telah Selesai");
            btnChat.setEnabled(false);
        } else if (Constants.ORDER_STATUS_CANCELLED.equals(status)) {
            btnChat.setText("Pesanan Telah Dibatalkan");
            btnChat.setEnabled(false);
        } else {
            btnChat.setText("Chat Mitra");
            btnChat.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(orderId != null) loadOrderData();
    }
}