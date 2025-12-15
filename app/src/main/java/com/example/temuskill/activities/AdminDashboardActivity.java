package com.example.temuskill.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.temuskill.R;
import com.example.temuskill.fragments.DashboardFragment;
import com.example.temuskill.fragments.ReportsFragment;
import com.example.temuskill.fragments.ServicesFragment;
import com.example.temuskill.fragments.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        bottomNav = findViewById(R.id.admin_bottom_nav);

        // Load Default Fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (id == R.id.nav_users) {
                selectedFragment = new UserFragment();
            } else if (id == R.id.nav_services) {
                selectedFragment = new ServicesFragment();
            } else if (id == R.id.nav_reports) {
                selectedFragment = new ReportsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_admin, fragment)
                .commit();
    }
}