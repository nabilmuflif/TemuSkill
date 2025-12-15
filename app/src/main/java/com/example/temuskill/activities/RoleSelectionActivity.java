package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.temuskill.R;
import com.example.temuskill.adapters.RoleAdapter;
import java.util.ArrayList;
import java.util.List;

public class RoleSelectionActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton btnLeft, btnRight;
    private List<RoleAdapter.RoleItem> roleItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        viewPager = findViewById(R.id.viewPagerRole);
        btnLeft = findViewById(R.id.btn_arrow_left);
        btnRight = findViewById(R.id.btn_arrow_right);

        setupRoles();

        RoleAdapter adapter = new RoleAdapter(roleItems, roleKey -> {
            // PERBAIKAN: Setelah pilih role, lanjut ke Register (Daftar)
            goToRegister(roleKey);
        });
        viewPager.setAdapter(adapter);

        btnLeft.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) viewPager.setCurrentItem(current - 1);
        });

        btnRight.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < roleItems.size() - 1) viewPager.setCurrentItem(current + 1);
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateArrows(position);
            }
        });
    }

    private void setupRoles() {
        roleItems = new ArrayList<>();
        roleItems.add(new RoleAdapter.RoleItem(
                R.drawable.ic_role_seeker_illustration,
                "Pencari Jasa",
                "Temu, cari, dan pesan jasa terpercaya untuk membantu kebutuhan sehari-harimu.",
                "Pilih Sebagai Pencari",
                "pencari_jasa"
        ));
        roleItems.add(new RoleAdapter.RoleItem(
                R.drawable.ic_role_provider_illustration,
                "Penyedia Jasa",
                "Daftarkan keahlianmu dan mulai mendapatkan pelanggan dari sekitar.",
                "Pilih Sebagai Penyedia",
                "penyedia_jasa"
        ));
    }

    private void updateArrows(int position) {
        if (position == 0) {
            btnLeft.setVisibility(View.INVISIBLE);
            btnRight.setVisibility(View.VISIBLE);
        } else if (position == roleItems.size() - 1) {
            btnLeft.setVisibility(View.VISIBLE);
            btnRight.setVisibility(View.INVISIBLE);
        } else {
            btnLeft.setVisibility(View.VISIBLE);
            btnRight.setVisibility(View.VISIBLE);
        }
    }

    private void goToRegister(String role) {
        // Buka halaman Register dan bawa data Role
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("SELECTED_ROLE", role);
        startActivity(intent);
        finish(); // Tutup halaman pemilihan role
    }
}