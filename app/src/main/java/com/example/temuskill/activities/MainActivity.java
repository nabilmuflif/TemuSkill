package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.temuskill.R;
import com.example.temuskill.fragments.ChatFragment;
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadInitialFragment();
            // Cek apakah ada navigasi dari Activity lain (misal OrderDetail)
            handleIncomingIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("NAVIGATE_TO")) {
            String destination = intent.getStringExtra("NAVIGATE_TO");

            if ("CHAT_FRAGMENT".equals(destination)) {
                String targetId = intent.getStringExtra("TARGET_USER_ID");
                String targetName = intent.getStringExtra("TARGET_USER_NAME");

                // TANGKAP ORDER ID (Revisi Penting)
                String orderId = intent.getStringExtra("ORDER_ID");

                openChatFragment(targetId, targetName, orderId);
            }
        }
    }

    private void openChatFragment(String targetId, String targetName, String orderId) {
        // Set UI Bottom Nav ke Chat
        bottomNav.setSelectedItemId(R.id.chatBtn);

        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();

        // Masukkan data ke Bundle untuk dikirim ke ChatFragment
        args.putString("TARGET_USER_ID", targetId);
        args.putString("TARGET_USER_NAME", targetName);
        args.putString("ORDER_ID", orderId); // Data Order ID

        chatFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadInitialFragment() {
        boolean isProvider = sessionManager.isPenyediaJasa();
        Fragment initialFragment;

        if (isProvider) {
            initialFragment = new ProviderHomeFragment();
        } else {
            initialFragment = new HomeFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, initialFragment)
                .commit();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            boolean isProvider = sessionManager.isPenyediaJasa();

            if (itemId == R.id.homeBtn) {
                if (isProvider) selectedFragment = new ProviderHomeFragment();
                else selectedFragment = new HomeFragment();
            } else if (itemId == R.id.pesananBtn) {
                selectedFragment = new OrderFragment();
            } else if (itemId == R.id.chatBtn) {
                selectedFragment = new ChatListFragment();
            } else if (itemId == R.id.akunBtn) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}