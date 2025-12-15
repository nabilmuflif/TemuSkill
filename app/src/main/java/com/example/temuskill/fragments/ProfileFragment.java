package com.example.temuskill.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.activities.AboutUsActivity;
import com.example.temuskill.activities.EditProfileActivity;
import com.example.temuskill.activities.IntroActivity;
import com.example.temuskill.activities.LanguageActivity;
import com.example.temuskill.activities.LoginActivity;
import com.example.temuskill.activities.MyReviewsActivity;
import com.example.temuskill.activities.MyServicesActivity; // Pastikan Activity ini ada
import com.example.temuskill.activities.VerificationActivity;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvVerifyStatus, tvBio;
    private View btnMyServices, btnMyReviews, dividerServices, dividerReviews;
    private CircleImageView ivProfile;

    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(getContext());
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupListeners(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        setupProviderFeatures();
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvBio = view.findViewById(R.id.tv_bio);
        ivProfile = view.findViewById(R.id.iv_profile_pic);
        tvVerifyStatus = view.findViewById(R.id.btn_verify_status);

        // Menu Provider
        btnMyServices = view.findViewById(R.id.btn_my_services);
        btnMyReviews = view.findViewById(R.id.btn_my_reviews);

        // Divider (Garis pemisah) untuk di-hide juga
        dividerServices = view.findViewById(R.id.divider_services);
        dividerReviews = view.findViewById(R.id.divider_reviews);
    }

    private void setupListeners(View view) {
        // Menu Umum
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
        view.findViewById(R.id.btn_language).setOnClickListener(v -> startActivity(new Intent(getContext(), LanguageActivity.class)));
        view.findViewById(R.id.btn_about).setOnClickListener(v -> startActivity(new Intent(getContext(), AboutUsActivity.class)));

        // Menu Provider (Cek null safety)
        if (btnMyServices != null) {
            btnMyServices.setOnClickListener(v -> startActivity(new Intent(getContext(), MyServicesActivity.class)));
        }

        if (btnMyReviews != null) {
            btnMyReviews.setOnClickListener(v -> startActivity(new Intent(getContext(), MyReviewsActivity.class)));
        }

        if (tvVerifyStatus != null) {
            tvVerifyStatus.setOnClickListener(v -> startActivity(new Intent(getContext(), VerificationActivity.class)));
        }

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            sessionManager.logout();

            if (getContext() != null) {
                SharedPreferences introPref = requireContext().getSharedPreferences("TemuSkillPref", Context.MODE_PRIVATE);
                introPref.edit().putBoolean("isIntroShown", false).apply();

                Intent intent = new Intent(getContext(), IntroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    // --- LOGIKA PENTING: SEMBUNYIKAN MENU JIKA BUKAN PROVIDER ---
    private void setupProviderFeatures() {
        boolean isProvider = sessionManager.isPenyediaJasa();

        int visibility = isProvider ? View.VISIBLE : View.GONE;

        if (tvVerifyStatus != null) tvVerifyStatus.setVisibility(visibility);

        if (btnMyServices != null) btnMyServices.setVisibility(visibility);
        if (dividerServices != null) dividerServices.setVisibility(visibility);

        if (btnMyReviews != null) btnMyReviews.setVisibility(visibility);
        if (dividerReviews != null) dividerReviews.setVisibility(visibility);

        // Bio juga bisa disembunyikan jika User
        if (tvBio != null) {
            if (!isProvider) {
                tvBio.setVisibility(View.GONE);
            }
        }

        if (isProvider) {
            checkVerificationStatus();
        }
    }

    private void loadUserProfile() {
        if (tvName != null) tvName.setText(sessionManager.getUserName());
        if (tvEmail != null) tvEmail.setText(sessionManager.getUserEmail());

        String uid = sessionManager.getUserId();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (!isAdded() || getContext() == null) return;

                if (doc.exists()) {
                    String photo = doc.getString("foto_profil");
                    if (photo != null && !photo.isEmpty() && ivProfile != null) {
                        Glide.with(this).load(photo).placeholder(R.drawable.profile).into(ivProfile);
                    }

                    String bio = doc.getString("bio");
                    if (tvBio != null) {
                        // Tampilkan Bio hanya jika ada isinya DAN user adalah Provider (atau sesuai kebijakan)
                        if (sessionManager.isPenyediaJasa() && bio != null && !bio.isEmpty()) {
                            tvBio.setText(bio);
                            tvBio.setVisibility(View.VISIBLE);
                        } else {
                            tvBio.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    private void checkVerificationStatus() {
        String uid = sessionManager.getUserId();
        if(uid == null) return;

        db.collection("provider_profiles").document(uid).get().addOnSuccessListener(doc -> {
            if (!isAdded() || tvVerifyStatus == null) return;

            if (doc.exists()) {
                String status = doc.getString("statusVerifikasi");
                if ("verified".equals(status)) {
                    tvVerifyStatus.setText("Akun Terverifikasi ✓");
                    tvVerifyStatus.setTextColor(getContext().getColor(android.R.color.holo_green_dark));
                    tvVerifyStatus.setOnClickListener(v -> Toast.makeText(getContext(), "Akun Anda sudah terverifikasi!", Toast.LENGTH_SHORT).show());
                } else if ("pending".equals(status)) {
                    tvVerifyStatus.setText("Menunggu Verifikasi ⧗");
                    tvVerifyStatus.setTextColor(getContext().getColor(android.R.color.holo_orange_dark));
                } else if ("rejected".equals(status)) {
                    tvVerifyStatus.setText("Verifikasi Ditolak. Ajukan Lagi >");
                    tvVerifyStatus.setTextColor(getContext().getColor(android.R.color.holo_red_dark));
                }
            } else {
                tvVerifyStatus.setText("Ajukan Verifikasi >");
                tvVerifyStatus.setTextColor(getContext().getColor(R.color.purple_500));
            }
        });
    }
}