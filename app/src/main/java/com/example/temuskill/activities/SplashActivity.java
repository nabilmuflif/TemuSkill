package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.utils.FirebaseSeeder;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Seed data dummy (bisa dikomentari jika sudah rilis)
        FirebaseSeeder.seedData(this);

        handler = new Handler();
        runnable = () -> {
            Intent intent = new Intent(SplashActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
        };

        handler.postDelayed(runnable, SPLASH_DELAY);
    }

    // FIX: Hapus callback saat activity dihancurkan agar tidak crash
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}