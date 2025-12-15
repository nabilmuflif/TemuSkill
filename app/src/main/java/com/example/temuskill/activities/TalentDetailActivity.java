package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.adapters.GalleryAdapter;
import com.example.temuskill.adapters.ReviewAdapter;
import com.example.temuskill.models.Review;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.PriceFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TalentDetailActivity extends AppCompatActivity {

    private TextView tvName, tvSkills, tvBio, tvPrice, tvRatingHeader, tvEmptyReview, lblAboutName;
    private ImageView ivHeader;
    private Button btnContact;

    private TextView txtTentang, txtGaleri, txtUlasan;
    private View indTentang, indGaleri, indUlasan;
    private View layoutTentang, layoutUlasan;
    private RecyclerView rvGaleri, rvUlasan;

    private FirebaseFirestore db;
    private String talentId;
    private User currentTalent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talent_detail);

        talentId = getIntent().getStringExtra("TALENT_ID");
        if (talentId == null) { finish(); return; }

        db = FirebaseFirestore.getInstance();
        initViews();
        setupTabs();

        loadTalentData();
        loadReviews();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_talent_name);
        tvSkills = findViewById(R.id.tv_skills);
        tvBio = findViewById(R.id.tv_bio);
        tvPrice = findViewById(R.id.tv_price);
        tvRatingHeader = findViewById(R.id.tv_rating_header);
        ivHeader = findViewById(R.id.iv_header);
        btnContact = findViewById(R.id.btn_contact);
        lblAboutName = findViewById(R.id.lbl_about_name);

        txtTentang = findViewById(R.id.txt_tentang);
        txtGaleri = findViewById(R.id.txt_galeri);
        txtUlasan = findViewById(R.id.txt_ulasan);
        indTentang = findViewById(R.id.indicator_tentang);
        indGaleri = findViewById(R.id.indicator_galeri);
        indUlasan = findViewById(R.id.indicator_ulasan);

        layoutTentang = findViewById(R.id.layout_tentang);
        layoutUlasan = findViewById(R.id.layout_ulasan);
        rvGaleri = findViewById(R.id.rv_galeri);
        rvUlasan = findViewById(R.id.rv_ulasan_detail);
        tvEmptyReview = findViewById(R.id.tv_empty_review);

        rvGaleri.setLayoutManager(new GridLayoutManager(this, 2));
        rvUlasan.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnContact.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectServiceActivity.class);
            intent.putExtra("TALENT_ID", talentId);
            startActivity(intent);
        });
    }

    private void setupTabs() {
        findViewById(R.id.tab_tentang).setOnClickListener(v -> showTab(0));
        findViewById(R.id.tab_galeri).setOnClickListener(v -> showTab(1));
        findViewById(R.id.tab_ulasan).setOnClickListener(v -> showTab(2));
        showTab(0);
    }

    private void showTab(int index) {
        txtTentang.setSelected(false); indTentang.setVisibility(View.INVISIBLE);
        txtGaleri.setSelected(false); indGaleri.setVisibility(View.INVISIBLE);
        txtUlasan.setSelected(false); indUlasan.setVisibility(View.INVISIBLE);

        layoutTentang.setVisibility(View.GONE);
        rvGaleri.setVisibility(View.GONE);
        layoutUlasan.setVisibility(View.GONE);

        if (index == 0) {
            txtTentang.setSelected(true); indTentang.setVisibility(View.VISIBLE);
            layoutTentang.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            txtGaleri.setSelected(true); indGaleri.setVisibility(View.VISIBLE);
            rvGaleri.setVisibility(View.VISIBLE);
        } else {
            txtUlasan.setSelected(true); indUlasan.setVisibility(View.VISIBLE);
            layoutUlasan.setVisibility(View.VISIBLE);
        }
    }

    private void loadTalentData() {
        db.collection("users").document(talentId).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                currentTalent = doc.toObject(User.class);
                if(currentTalent != null) {
                    tvName.setText(currentTalent.getNamaLengkap());
                    lblAboutName.setText("Tentang " + currentTalent.getNamaLengkap());
                    tvSkills.setText(currentTalent.getKeahlian() != null ? currentTalent.getKeahlian() : "Jasa Umum");
                    tvBio.setText(currentTalent.getBio() != null ? currentTalent.getBio() : "-");

                    if (currentTalent.getTarif() > 0) {
                        String satuan = currentTalent.getSatuanTarif() != null ? currentTalent.getSatuanTarif() : "";
                        tvPrice.setText(PriceFormatter.formatPrice(currentTalent.getTarif()) + " / " + satuan);
                    } else {
                        tvPrice.setText("Hubungi");
                    }

                    // --- LOAD GALERI ---
                    String imgUrl = "";
                    List<String> galeri = currentTalent.getGaleriUrl();

                    // Cek apakah User punya galeri
                    if (galeri != null && !galeri.isEmpty()) {
                        // Ambil foto pertama untuk header
                        imgUrl = galeri.get(0);
                        // Tampilkan semua foto di tab Galeri
                        rvGaleri.setAdapter(new GalleryAdapter(galeri));
                    } else {
                        // Fallback jika tidak ada galeri
                        imgUrl = currentTalent.getFotoProfilUrl();
                        List<String> dummy = new ArrayList<>();
                        // Pesan kosong jika tidak ada galeri
                        rvGaleri.setAdapter(new GalleryAdapter(dummy));
                    }

                    // Set Gambar Header
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        if (imgUrl.startsWith("http")) {
                            Glide.with(this).load(imgUrl).into(ivHeader);
                        } else {
                            // Cek resource lokal (jika dummy)
                            int resId = getResources().getIdentifier(imgUrl, "drawable", getPackageName());
                            if (resId != 0) Glide.with(this).load(resId).into(ivHeader);
                            else ivHeader.setImageResource(R.drawable.profile);
                        }
                    } else {
                        ivHeader.setImageResource(R.drawable.profile);
                    }
                }
            }
        });
    }

    private void loadReviews() {
        // PERBAIKAN: HAPUS .orderBy("timestamp") agar tidak butuh Index Firebase
        db.collection("reviews")
                .whereEqualTo("providerId", talentId)
                // .orderBy("timestamp", Query.Direction.DESCENDING) <--- HAPUS INI SEMENTARA
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Review> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Review.class));
                    }

                    if(list.isEmpty()) {
                        tvEmptyReview.setVisibility(View.VISIBLE);
                        rvUlasan.setVisibility(View.GONE);
                        tvRatingHeader.setText(" • ★ 0.0 (0)");
                    } else {
                        tvEmptyReview.setVisibility(View.GONE);
                        rvUlasan.setVisibility(View.VISIBLE);
                        rvUlasan.setAdapter(new ReviewAdapter(list));

                        // Hitung Rata-rata Rating
                        double totalRating = 0;
                        for (Review r : list) totalRating += r.getRating();
                        double avg = totalRating / list.size();

                        tvRatingHeader.setText(String.format(" • ★ %.1f (%d)", avg, list.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat ulasan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}