package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.temuskill.utils.Constants;
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
                            setupButtons();
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
                            @SuppressWarnings("DiscouragedApi")
                            int resId = getResources().getIdentifier(client.getFotoProfilUrl(), "drawable", getPackageName());
                            if(resId != 0) Glide.with(this).load(resId).into(ivClientPhoto);
                        }
                    }
                }
            }
        });
    }

    // === REVISI: Menggunakan SWITCH-CASE ===
    private void setupButtons() {
        String status = currentOrder.getStatusPesanan();
        btnSecondaryAction.setVisibility(View.GONE);

        switch (status) {
            case Constants.ORDER_STATUS_PENDING:
                btnMainAction.setText("Terima Pesanan");
                btnMainAction.setEnabled(true);
                btnMainAction.setOnClickListener(v -> updateStatus(Constants.ORDER_STATUS_CONFIRMED));

                btnSecondaryAction.setVisibility(View.VISIBLE);
                btnSecondaryAction.setText("Tolak Pesanan");
                btnSecondaryAction.setOnClickListener(v -> updateStatus(Constants.ORDER_STATUS_CANCELLED));
                break;

            case Constants.ORDER_STATUS_CONFIRMED:
            case Constants.ORDER_STATUS_IN_PROGRESS:
                btnMainAction.setText("Selesaikan Pekerjaan");
                btnMainAction.setEnabled(true);
                btnMainAction.setBackgroundColor(ContextCompat.getColor(this, R.color.green_success));
                btnMainAction.setOnClickListener(v -> updateStatus(Constants.ORDER_STATUS_COMPLETED));
                break;

            case Constants.ORDER_STATUS_COMPLETED:
            case Constants.ORDER_STATUS_REVIEWED:
                btnMainAction.setText("Pesanan Selesai");
                btnMainAction.setEnabled(false);
                btnMainAction.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                break;

            case Constants.ORDER_STATUS_CANCELLED:
                btnMainAction.setText("Pesanan Dibatalkan");
                btnMainAction.setEnabled(false);
                btnMainAction.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                break;
        }

        btnChatClient.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getClientId() != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("NAVIGATE_TO", "CHAT_FRAGMENT");
                intent.putExtra("ORDER_ID", orderId);
                intent.putExtra("TARGET_USER_ID", currentOrder.getClientId());
                intent.putExtra("TARGET_USER_NAME", tvClientName.getText().toString());

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Data Client belum siap", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(String newStatus) {
        db.collection("orders").document(orderId).update("statusPesanan", newStatus)
                .addOnSuccessListener(aVoid -> {
                    String msg = "";
                    if (newStatus.equals(Constants.ORDER_STATUS_CONFIRMED)) msg = "Pesanan Diterima!";
                    else if (newStatus.equals(Constants.ORDER_STATUS_COMPLETED)) msg = "Pekerjaan Selesai!";
                    else if (newStatus.equals(Constants.ORDER_STATUS_CANCELLED)) msg = "Pesanan Ditolak.";

                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    loadOrderData();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal update status", Toast.LENGTH_SHORT).show());
    }
}