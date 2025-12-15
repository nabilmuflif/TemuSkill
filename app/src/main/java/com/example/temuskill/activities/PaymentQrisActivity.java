package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentQrisActivity extends AppCompatActivity {

    private String orderId;
    private FirebaseFirestore db;
    private Button btnDone;
    private TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_qris);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("ORDER_ID");

        btnDone = findViewById(R.id.btn_done_payment);
        tvTimer = findViewById(R.id.tv_timer);

        btnDone.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        if (orderId == null) {
            Toast.makeText(this, "Order ID Error", Toast.LENGTH_SHORT).show();
            return;
        }

        btnDone.setEnabled(false);
        btnDone.setText("Memproses...");

        // PERBAIKAN: Update status di database menjadi 'confirmed'
        db.collection("orders").document(orderId)
                .update("statusPesanan", Constants.ORDER_STATUS_CONFIRMED)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pembayaran Dikonfirmasi!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, PaymentSuccessActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnDone.setEnabled(true);
                    btnDone.setText("Saya Sudah Bayar");
                    Toast.makeText(this, "Gagal konfirmasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}