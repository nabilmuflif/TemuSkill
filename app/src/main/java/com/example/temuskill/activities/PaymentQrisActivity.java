package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    private CountDownTimer countDownTimer;

    // Timer 5 menit = 5 * 60 * 1000 milliseconds
    private static final long TIMER_DURATION = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_qris);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("ORDER_ID");

        btnDone = findViewById(R.id.btn_done_payment);
        tvTimer = findViewById(R.id.tv_timer);

        btnDone.setOnClickListener(v -> processPayment());

        // Mulai timer 5 menit
        startPaymentTimer();
    }

    private void startPaymentTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Konversi milliseconds ke menit dan detik
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                // Format: "05 menit : 00 detik"
                String timeText = String.format("%02d menit : %02d detik", minutes, seconds);
                tvTimer.setText(timeText);

                // Ubah warna text jika waktu < 1 menit (warning)
                if (minutes == 0) {
                    tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFinish() {
                // Waktu habis
                tvTimer.setText("00 menit : 00 detik");
                tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                Toast.makeText(PaymentQrisActivity.this,
                        "Waktu pembayaran habis!", Toast.LENGTH_LONG).show();

                // Disable tombol pembayaran
                btnDone.setEnabled(false);
                btnDone.setText("Waktu Habis");
                btnDone.setBackgroundTintList(
                        getResources().getColorStateList(android.R.color.darker_gray, null)
                );

                // Optional: Batalkan order atau kembali ke halaman sebelumnya
                // cancelOrder();

                // Atau kembali ke halaman sebelumnya setelah 2 detik
                new android.os.Handler().postDelayed(() -> {
                    finish();
                }, 2000);
            }
        }.start();
    }

    private void processPayment() {
        if (orderId == null) {
            Toast.makeText(this, "Order ID Error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stop timer saat pembayaran diproses
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnDone.setEnabled(false);
        btnDone.setText("Memproses...");

        // Update status di database menjadi 'confirmed'
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

                    // Restart timer jika gagal
                    startPaymentTimer();
                });
    }

    // Optional: Method untuk cancel order jika waktu habis
    private void cancelOrder() {
        if (orderId != null) {
            db.collection("orders").document(orderId)
                    .update("statusPesanan", "cancelled")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Pesanan dibatalkan karena waktu habis",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Pastikan timer dibersihkan saat activity ditutup
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Konfirmasi sebelum keluar (opsional)
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onBackPressed();
    }
}