package com.example.temuskill.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CancelOrderActivity extends AppCompatActivity {

    private RadioGroup rgCancelReason;
    private Button btnSubmitCancel;
    private ImageView btnBack;
    private FirebaseFirestore db;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_order);

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        rgCancelReason = findViewById(R.id.rg_cancel_reason);
        btnSubmitCancel = findViewById(R.id.btn_submit_cancel);
        btnBack = findViewById(R.id.btn_back_cancel);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSubmitCancel.setOnClickListener(v -> {
            int selectedId = rgCancelReason.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, R.string.msg_select_reason, Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = findViewById(selectedId);
            String reason = selectedRadioButton.getText().toString();
            processCancellation(reason);
        });
    }

    private void processCancellation(String reason) {
        btnSubmitCancel.setEnabled(false);
        btnSubmitCancel.setText("Memproses...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("statusPesanan", "cancelled");
        updates.put("cancellationReason", reason);
        // updates.put("cancelledBy", "client"); // Opsional: Siapa yang membatalkan

        db.collection("orders").document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.msg_cancel_success, Toast.LENGTH_SHORT).show();
                    // Kembali ke Home atau halaman sebelumnya
                    finish(); // Atau arahkan ke MainActivity dengan flag CLEAR_TOP
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.msg_cancel_failed, Toast.LENGTH_SHORT).show();
                    btnSubmitCancel.setEnabled(true);
                    btnSubmitCancel.setText(R.string.btn_submit_cancel);
                });
    }
}