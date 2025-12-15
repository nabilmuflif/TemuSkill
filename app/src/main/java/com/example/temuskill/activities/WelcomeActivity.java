package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnDaftar = findViewById(R.id.btn_register_welcome);
        Button btnMasuk = findViewById(R.id.btn_login_welcome);

        // DAFTAR -> Pilih Role Dulu -> Baru Register
        btnDaftar.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
        });

        // MASUK -> Langsung Login (Karena user sudah punya akun & role)
        btnMasuk.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}