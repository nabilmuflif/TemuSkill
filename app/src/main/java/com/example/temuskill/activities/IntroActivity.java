package com.example.temuskill.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.temuskill.R;
import com.example.temuskill.adapters.OnboardingAdapter;
import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView btnSecondary, tvIndicator, btnBackFinal;
    private ImageButton btnNextCircle;
    private Button btnStartBig;
    private RelativeLayout layoutNav;
    private List<OnboardingAdapter.OnboardingItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.viewPager);
        btnSecondary = findViewById(R.id.btn_secondary); // Tombol Lewati/Kembali
        tvIndicator = findViewById(R.id.tv_indicator);
        btnNextCircle = findViewById(R.id.btn_next_circle);
        btnStartBig = findViewById(R.id.btn_start_big); // Tombol Mulai
        btnBackFinal = findViewById(R.id.btn_back_final);
        layoutNav = findViewById(R.id.layout_nav);

        setupItems();
        viewPager.setAdapter(new OnboardingAdapter(items));

        // Tombol Next (Panah Bulat)
        btnNextCircle.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < items.size()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        // Tombol Kiri (Lewati / Kembali)
        btnSecondary.setOnClickListener(v -> handleSecondaryButton());

        // Tombol Kembali di slide terakhir
        btnBackFinal.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() - 1));

        // Tombol Mulai Besar -> Ke Role Selection
        btnStartBig.setOnClickListener(v -> navigateTo(RoleSelectionActivity.class));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
            }
        });
    }

    private void setupItems() {
        items = new ArrayList<>();
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.img_intro_1,
                "Selamat Datang di\nTemuSkill!",
                "Temu, cari, dan tawarkan keahlianmu. Akses mudah untuk semua jenis jasa."));
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.img_intro_2,
                "Butuh jasa cepat dan\nterpercaya?",
                "Cari penyedia jasa terdekat sesuai kebutuhanmu."));
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.img_intro_3,
                "Ubah keahlian jadi\npenghasilan!",
                "Daftarkan skill kamu, buat profil, dan mulai dapat pelanggan baru."));
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.img_intro_4,
                "Siapapun bisa\nbergabung",
                "Aplikasi ini dirancang mudah diakses. Semua orang punya tempat untuk tumbuh."));
    }

    private void updateUI(int position) {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            dots.append(i == position ? "● " : "• ");
        }
        tvIndicator.setText(dots.toString().trim());

        if (position == 0) {
            btnSecondary.setText("Lewati"); // Jika diklik -> Ke WelcomeActivity
            layoutNav.setVisibility(View.VISIBLE);
            btnStartBig.setVisibility(View.GONE);
            btnBackFinal.setVisibility(View.GONE);
        } else if (position == items.size() - 1) {
            // Slide Terakhir -> Muncul tombol Mulai
            layoutNav.setVisibility(View.GONE);
            btnStartBig.setVisibility(View.VISIBLE);
            btnBackFinal.setVisibility(View.VISIBLE);
        } else {
            btnSecondary.setText("Kembali");
            layoutNav.setVisibility(View.VISIBLE);
            btnStartBig.setVisibility(View.GONE);
            btnBackFinal.setVisibility(View.GONE);
        }
    }

    private void handleSecondaryButton() {
        int current = viewPager.getCurrentItem();
        if (current == 0) {
            // Jika di halaman 1 ("Lewati") -> Arahkan ke WelcomeActivity
            navigateTo(WelcomeActivity.class);
        } else {
            // Jika di halaman lain ("Kembali") -> Mundur satu slide
            viewPager.setCurrentItem(current - 1);
        }
    }

    // Helper Method untuk Navigasi
    private void navigateTo(Class<?> targetActivity) {
        // Simpan status bahwa user sudah melihat intro
        SharedPreferences pref = getSharedPreferences("TemuSkillPref", MODE_PRIVATE);
        pref.edit().putBoolean("isIntroShown", true).apply();

        // Pindah Activity sesuai target
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        finish();
    }
}