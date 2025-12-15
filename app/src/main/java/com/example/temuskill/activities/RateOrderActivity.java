package com.example.temuskill.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.Constants; // Import Constants
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RateOrderActivity extends AppCompatActivity {

    private ImageView ivService;
    private TextView tvProviderName, tvCategory, tvOrderDate, tvWorkDate, tvTime;
    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmit;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String orderId, talentId;
    private String currentProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_order);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        orderId = getIntent().getStringExtra("ORDER_ID");
        talentId = getIntent().getStringExtra("SERVICE_ID");

        initViews();
        loadOrderData();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void initViews() {
        ivService = findViewById(R.id.iv_service_image);
        tvProviderName = findViewById(R.id.tv_provider_name);
        tvCategory = findViewById(R.id.tv_category);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvWorkDate = findViewById(R.id.tv_work_date);
        tvTime = findViewById(R.id.tv_time);
        ratingBar = findViewById(R.id.rating_bar);
        etReview = findViewById(R.id.et_review);
        btnSubmit = findViewById(R.id.btn_submit);
    }

    private void loadOrderData() {
        if(orderId == null) return;
        db.collection("orders").document(orderId).get().addOnSuccessListener(doc -> {
            if(doc.exists()) {
                Order order = doc.toObject(Order.class);
                if(order != null) {
                    currentProviderId = order.getProviderId();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    String dateOrder = sdf.format(new Date(order.getCreatedAt()));

                    tvProviderName.setText(order.getProviderName());
                    tvOrderDate.setText("Tanggal Pemesanan : " + dateOrder);
                    tvWorkDate.setText("Jadwal : " + order.getJadwalKerja());
                    tvTime.setText("Durasi : Sesuai pesanan");

                    loadTalentDetails(order.getProviderId());
                }
            }
        });
    }

    private void loadTalentDetails(String tId) {
        db.collection("users").document(tId).get().addOnSuccessListener(doc -> {
            if(doc.exists()) {
                User talent = doc.toObject(User.class);
                if(talent != null) {
                    String skill = talent.getKeahlian() != null ? talent.getKeahlian() : "Jasa Umum";
                    tvCategory.setText(skill);
                    if(talent.getFotoProfilUrl() != null && !talent.getFotoProfilUrl().isEmpty()) {
                        if (talent.getFotoProfilUrl().startsWith("http")) {
                            Glide.with(this).load(talent.getFotoProfilUrl()).placeholder(R.drawable.populer1).into(ivService);
                        } else {
                            int resId = getResources().getIdentifier(talent.getFotoProfilUrl(), "drawable", getPackageName());
                            if(resId!=0) Glide.with(this).load(resId).into(ivService);
                        }
                    }
                }
            }
        });
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = etReview.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Mohon beri bintang minimal 1", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Mengirim...");

        Map<String, Object> review = new HashMap<>();
        review.put("orderId", orderId);
        review.put("serviceId", talentId);
        review.put("userId", sessionManager.getUserId());
        review.put("userName", sessionManager.getUserName());
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", System.currentTimeMillis());
        review.put("providerId", currentProviderId);

        db.collection("reviews").add(review).addOnSuccessListener(doc -> {

            // [PERBAIKAN UTAMA] Update Status menjadi 'reviewed'
            db.collection("orders").document(orderId)
                    .update("statusPesanan", Constants.ORDER_STATUS_REVIEWED)
                    .addOnSuccessListener(aVoid -> {
                        showSuccessDialog();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }).addOnFailureListener(e -> {
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "Gagal kirim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_rating_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            setResult(RESULT_OK); // Kirim sinyal sukses
            finish();
        }, 2000);
    }
}