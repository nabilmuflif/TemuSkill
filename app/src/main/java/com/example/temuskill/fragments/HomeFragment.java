package com.example.temuskill.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide
import com.example.temuskill.R;
import com.example.temuskill.activities.NotificationActivity;
import com.example.temuskill.adapters.CategoryAdapter;
import com.example.temuskill.adapters.PopularAdapter;
import com.example.temuskill.adapters.TalentAdapter;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import de.hdodenhof.circleimageview.CircleImageView; // Import CircleImageView
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvTalents, rvPopular, rvCategories;
    private ProgressBar progressBar;
    private TalentAdapter talentAdapter;
    private CategoryAdapter categoryAdapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    private List<User> allTalents = new ArrayList<>();
    private EditText etSearch;
    private TextView sapaan, tvGreetingName;
    private View btnNotification;
    private CircleImageView ivProfile; // Tambahan Variabel Foto Profil

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());

        initViews(view);
        setupPopularBanner();
        setupCategories();

        // Load Data
        loadTalentsFromCloud();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data user (Foto & Nama) setiap kali fragment dibuka/resume
        loadUserProfile();
    }

    private void initViews(View view) {
        rvPopular = view.findViewById(R.id.rv_popular);
        rvTalents = view.findViewById(R.id.rv_services);
        rvCategories = view.findViewById(R.id.rv_categories);

        progressBar = view.findViewById(R.id.progress_bar);
        etSearch = view.findViewById(R.id.et_search);
        sapaan = view.findViewById(R.id.sapaan);
        tvGreetingName = view.findViewById(R.id.tv_greeting_name);
        btnNotification = view.findViewById(R.id.btn_notification);

        // Inisialisasi ImageView Profil
        ivProfile = view.findViewById(R.id.iv_profile);

        sapaan.setText("Temukan talent terbaik\ndi sekitarmu");
    }

    private void setupListeners() {
        etSearch.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificationActivity.class));
        });
    }

    // METHOD BARU: Load Data Profil dari Firestore
    private void loadUserProfile() {
        String uid = sessionManager.getUserId();
        if (uid == null) return;

        // Set nama sementara dari Session (biar cepat)
        tvGreetingName.setText("Hello, " + sessionManager.getUserName() + "!");

        // Ambil data "Fresh" dari Firestore (terutama Foto Profil)
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return; // Cek fragment masih nempel

                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("nama_lengkap");
                        String photoUrl = documentSnapshot.getString("foto_profil");

                        // Update nama jika ada perubahan
                        if (name != null) {
                            tvGreetingName.setText("Hello, " + name + "!");
                        }

                        // Update Foto Profil dengan Glide
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.profile) // Gambar default jika loading
                                    .error(R.drawable.profile) // Gambar default jika error
                                    .into(ivProfile);
                        }
                    }
                });
    }

    private void setupPopularBanner() {
        List<PopularAdapter.BannerItem> banners = new ArrayList<>();
        banners.add(new PopularAdapter.BannerItem("Teknisi Handal", "Ahli perbaikan AC & Listrik.", "Top Rated", "#111D5E"));
        banners.add(new PopularAdapter.BannerItem("Guru Privat", "Tingkatkan nilai akademik.", "New", "#111D5E"));
        PopularAdapter adapter = new PopularAdapter(banners);
        rvPopular.setAdapter(adapter);
    }

    private void setupCategories() {
        List<String> categoryList = Arrays.asList(
                "Semua",
                "Servis AC",
                "Guru Privat",
                "Jasa Jahit",
                "Montir",
                "Kebersihan",
                "Kecantikan",
                "Tukang Bangunan"
        );

        categoryAdapter = new CategoryAdapter(getContext(), categoryList, (category, position) -> {
            if (position == 0) {
                talentAdapter.updateData(allTalents);
            } else {
                filterTalentByCategory(category);
            }
        });

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void filterTalentByCategory(String category) {
        List<String> keywords = new ArrayList<>();
        switch (category) {
            case "Servis AC":
                keywords.add("AC"); keywords.add("Pendingin"); keywords.add("Elektronik");
                break;
            case "Guru Privat":
                keywords.add("Guru"); keywords.add("Les"); keywords.add("Privat"); keywords.add("Inggris");
                break;
            case "Jasa Jahit":
                keywords.add("Jahit"); keywords.add("Kain"); keywords.add("Permak");
                break;
            case "Montir":
                keywords.add("Montir"); keywords.add("Bengkel"); keywords.add("Motor"); keywords.add("Mobil");
                break;
            case "Kebersihan":
                keywords.add("Bersih"); keywords.add("Cuci"); keywords.add("Sapu");
                break;
            case "Kecantikan":
                keywords.add("Makeup"); keywords.add("Rias"); keywords.add("Salon");
                break;
            default:
                keywords.add(category);
                break;
        }

        List<User> filtered = new ArrayList<>();
        for (User u : allTalents) {
            String skill = u.getKeahlian();
            if (skill != null) {
                for (String kw : keywords) {
                    if (skill.toLowerCase().contains(kw.toLowerCase())) {
                        filtered.add(u);
                        break;
                    }
                }
            }
        }
        talentAdapter.updateData(filtered);
    }

    private void loadTalentsFromCloud() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .whereEqualTo("role", "penyedia_jasa")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    allTalents.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        User user = doc.toObject(User.class);
                        user.setUserId(doc.getId());
                        if(user.getIsActive() == 1) {
                            allTalents.add(user);
                        }
                    }

                    talentAdapter = new TalentAdapter(allTalents, getContext());
                    rvTalents.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    rvTalents.setAdapter(talentAdapter);
                })
                .addOnFailureListener(e -> {
                    if(progressBar!=null) progressBar.setVisibility(View.GONE);
                });
    }
}