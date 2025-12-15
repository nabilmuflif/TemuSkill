package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.models.Service;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.PriceFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SelectServiceActivity extends AppCompatActivity {

    private TextView tvName, tvKeahlian, tvTotal;
    private ImageView ivHeader;
    private ChipGroup chipGroupServices;
    private RadioGroup rgDuration;
    private Button btnPesan;

    private FirebaseFirestore db;
    private String talentId;

    // DATA PENTING
    private User currentTalent; // Menyimpan data talent (termasuk harga)
    private Service selectedService; // Menyimpan jasa yang dipilih (nama)
    private int duration = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_service);

        talentId = getIntent().getStringExtra("TALENT_ID");
        if (talentId == null) { finish(); return; }

        db = FirebaseFirestore.getInstance();
        initViews();

        loadTalentInfo();
        loadServices();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_talent_name);
        tvKeahlian = findViewById(R.id.tv_keahlian);
        tvTotal = findViewById(R.id.tv_total_price);
        ivHeader = findViewById(R.id.iv_header);
        chipGroupServices = findViewById(R.id.chip_group_services);
        rgDuration = findViewById(R.id.rg_duration);
        btnPesan = findViewById(R.id.btn_pesan);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Logic Durasi
        rgDuration.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_1_jam) duration = 1;
            else if (checkedId == R.id.rb_2_jam) duration = 2;
            else if (checkedId == R.id.rb_3_jam) duration = 3;
            calculateTotal();
        });

        // Logic Pesan
        btnPesan.setOnClickListener(v -> {
            if (selectedService == null) {
                Toast.makeText(this, "Pilih layanan dulu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentTalent == null) {
                Toast.makeText(this, "Memuat data talent...", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, OrderConfirmationActivity.class);
            intent.putExtra("TALENT_ID", talentId);

            // Kirim Nama Layanan (Dari Service yang dipilih)
            String fullServiceName = selectedService.getCategory();
            if (selectedService.getDetails() != null && !selectedService.getDetails().isEmpty()) {
                fullServiceName += " (" + selectedService.getDetails() + ")";
            }
            intent.putExtra("SERVICE_NAME", fullServiceName);

            // Kirim Harga Total (Harga dari Talent * Durasi)
            double total = currentTalent.getTarif() * duration;
            intent.putExtra("SERVICE_PRICE", total);

            startActivity(intent);
        });
    }

    private void loadTalentInfo() {
        db.collection("users").document(talentId).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                // SIMPAN DATA TALENT KE VARIABEL GLOBAL
                currentTalent = doc.toObject(User.class);

                if(currentTalent != null) {
                    tvName.setText(currentTalent.getNamaLengkap());
                    tvKeahlian.setText(currentTalent.getKeahlian());

                    // Load Foto Profil
                    String imgUrl = currentTalent.getFotoProfilUrl();
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        if (imgUrl.startsWith("http")) {
                            Glide.with(this).load(imgUrl).into(ivHeader);
                        } else {
                            int resId = getResources().getIdentifier(imgUrl, "drawable", getPackageName());
                            if (resId != 0) Glide.with(this).load(resId).into(ivHeader);
                        }
                    }

                    // Update total awal (kalau user sudah pilih chip duluan sebelum data talent masuk)
                    calculateTotal();
                }
            }
        });
    }

    private void loadServices() {
        db.collection("services").whereEqualTo("providerId", talentId).get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Service s = doc.toObject(Service.class);
                        s.setServiceId(doc.getId());
                        addChip(s);
                    }
                });
    }

    private void addChip(Service service) {
        Chip chip = new Chip(this);
        // Tampilkan Kategori sebagai nama di Chip
        chip.setText(service.getCategory());
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(R.color.selector_chip_bg);
        chip.setTextColor(getColor(R.color.black));

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedService = service;
                calculateTotal();
            }
        });
        chipGroupServices.addView(chip);
    }

    private void calculateTotal() {
        // Pastikan layanan dipilih DAN data talent sudah termuat (untuk ambil harga)
        if (selectedService != null && currentTalent != null) {
            double total = currentTalent.getTarif() * duration;
            tvTotal.setText(PriceFormatter.formatPrice(total));
        } else if (currentTalent != null) {
            // Jika belum pilih layanan tapi talent ada, tampilkan harga dasar
            tvTotal.setText(PriceFormatter.formatPrice(currentTalent.getTarif()) + " / " + currentTalent.getSatuanTarif());
        }
    }
}