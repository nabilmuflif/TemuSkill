package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // 1. Tambahan Import ini
import androidx.fragment.app.Fragment;

import com.example.temuskill.R;
import com.example.temuskill.fragments.ChatListFragment;
import com.example.temuskill.fragments.HomeFragment;
import com.example.temuskill.fragments.OrderFragment;
import com.example.temuskill.fragments.ProfileFragment;
import com.example.temuskill.fragments.ProviderHomeFragment;
import com.example.temuskill.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 2. PAKSA MODE TERANG (Light Mode)
        // Kode ini harus diletakkan paling atas, sebelum super.onCreate atau setContentView
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // 3. Cek Status Login
        // Jika belum login, lempar kembali ke LoginActivity
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        bottomNav = findViewById(R.id.bottom_navigation);

        // 4. Set Fragment Awal saat aplikasi dibuka
        if (savedInstanceState == null) {
            loadInitialFragment();
        }

        // 5. Setup Listener Menu Bawah
        setupBottomNavigation();
    }

    private void loadInitialFragment() {
        // Cek Role User
        boolean isProvider = sessionManager.isPenyediaJasa();

        Fragment initialFragment;
        if (isProvider) {
            // Jika Provider -> Buka Dashboard Provider
            initialFragment = new ProviderHomeFragment();
        } else {
            // Jika Pencari -> Buka Home Biasa
            initialFragment = new HomeFragment();
        }

        // Muat Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, initialFragment)
                .commit();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Ambil role user lagi untuk memastikan navigasi benar
            boolean isProvider = sessionManager.isPenyediaJasa();

            if (itemId == R.id.homeBtn) {
                // LOGIKA UTAMA: Beda Home beda Role
                if (isProvider) {
                    selectedFragment = new ProviderHomeFragment();
                } else {
                    selectedFragment = new HomeFragment();
                }

            } else if (itemId == R.id.pesananBtn) {
                // Halaman Order
                selectedFragment = new OrderFragment();

            } else if (itemId == R.id.chatBtn) {
                // Halaman Chat
                selectedFragment = new ChatListFragment();

            } else if (itemId == R.id.akunBtn) {
                // Halaman Profil
                selectedFragment = new ProfileFragment();
            }

            // Ganti Fragment jika ada yang dipilih
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}