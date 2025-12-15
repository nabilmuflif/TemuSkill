package com.example.temuskill.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.temuskill.R;
import com.example.temuskill.adapters.EditGalleryAdapter;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail, etBio, etKeahlian, etTarif, etSatuan;
    private LinearLayout layoutProviderFields;
    private ImageView ivProfile, btnBack, btnChangePhoto;
    private Button btnSave, btnAddGallery;
    private ProgressBar progressBar;
    private RecyclerView rvGallery;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    // Profile Pic
    private Uri profileImageUri;
    private String currentProfileUrl = "";
    // Gallery
    private List<Object> galleryList = new ArrayList<>();
    private EditGalleryAdapter galleryAdapter;

    // Launcher Foto Profil
    private final ActivityResultLauncher<String> pickProfileImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivProfile);
                }
            }
    );

    // Launcher Foto Galeri
    private final ActivityResultLauncher<String> pickGalleryImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    galleryList.add(uri);
                    galleryAdapter.notifyDataSetChanged();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initCloudinary();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupGalleryAdapter();
        setupViewsByRole();
        loadUserData();

        btnBack.setOnClickListener(v -> finish());
        btnChangePhoto.setOnClickListener(v -> pickProfileImage.launch("image/*"));
        btnAddGallery.setOnClickListener(v -> pickGalleryImage.launch("image/*"));
        btnSave.setOnClickListener(v -> startSavingProcess());
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dfrdpp4c0");
            config.put("api_key", "146351521487582");
            config.put("api_secret", "_rKm0ByWMMqKewCkjjfi_-dJ_sU");
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Sudah terinisialisasi
        }
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etBio = findViewById(R.id.et_bio);
        etKeahlian = findViewById(R.id.et_keahlian);
        etTarif = findViewById(R.id.et_tarif);
        etSatuan = findViewById(R.id.et_satuan);
        layoutProviderFields = findViewById(R.id.layout_provider_fields);
        ivProfile = findViewById(R.id.iv_profile);
        btnBack = findViewById(R.id.btn_back);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnSave = findViewById(R.id.btn_save);
        btnAddGallery = findViewById(R.id.btn_add_gallery);
        progressBar = findViewById(R.id.progress_bar);
        rvGallery = findViewById(R.id.rv_gallery_edit);
    }

    private void setupGalleryAdapter() {
        galleryAdapter = new EditGalleryAdapter(galleryList, position -> {
            galleryList.remove(position);
            galleryAdapter.notifyItemRemoved(position);
        });
        rvGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvGallery.setAdapter(galleryAdapter);
    }

    private void setupViewsByRole() {
        if (!sessionManager.isPenyediaJasa()) {
            layoutProviderFields.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        String uid = sessionManager.getUserId();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if(user != null) {
                    etName.setText(user.getNamaLengkap());
                    etPhone.setText(user.getNomorTelepon());
                    etEmail.setText(user.getEmail());

                    if (user.getBio() != null) etBio.setText(user.getBio());
                    if (user.getKeahlian() != null) etKeahlian.setText(user.getKeahlian());

                    if (user.getTarif() > 0) etTarif.setText(String.valueOf((long)user.getTarif()));
                    if (user.getSatuanTarif() != null) etSatuan.setText(user.getSatuanTarif());

                    if (user.getFotoProfilUrl() != null && !user.getFotoProfilUrl().isEmpty()) {
                        currentProfileUrl = user.getFotoProfilUrl();
                        Glide.with(this).load(currentProfileUrl).placeholder(R.drawable.profile).circleCrop().into(ivProfile);
                    }

                    if (user.getGaleriUrl() != null) {
                        galleryList.addAll(user.getGaleriUrl());
                        galleryAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void startSavingProcess() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) { etName.setError("Nama wajib diisi"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        // 1. Upload Foto Profil (Jika ada baru)
        if (profileImageUri != null) {
            uploadProfileImage(name);
        } else {
            // Lanjut ke upload galeri
            processGalleryUploads(name, currentProfileUrl);
        }
    }

    private void uploadProfileImage(String name) {
        MediaManager.get().upload(profileImageUri).callback(new UploadCallback() {
            @Override public void onStart(String requestId) {}
            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
            @Override public void onSuccess(String requestId, Map resultData) {
                if (isFinishing()) return; // Cek activity
                String newUrl = (String) resultData.get("secure_url");
                processGalleryUploads(name, newUrl);
            }
            @Override public void onError(String requestId, ErrorInfo error) {
                if (isFinishing()) return;
                handleError("Gagal upload foto profil");
            }
            @Override public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }

    private void processGalleryUploads(String name, String finalProfileUrl) {
        List<String> finalGalleryUrls = new ArrayList<>();
        List<Uri> newImagesToUpload = new ArrayList<>();

        for (Object item : galleryList) {
            if (item instanceof String) {
                finalGalleryUrls.add((String) item);
            } else if (item instanceof Uri) {
                newImagesToUpload.add((Uri) item);
            }
        }

        if (newImagesToUpload.isEmpty()) {
            updateFirestore(name, finalProfileUrl, finalGalleryUrls);
        } else {
            uploadGalleryRecursive(newImagesToUpload, 0, finalGalleryUrls, name, finalProfileUrl);
        }
    }

    private void uploadGalleryRecursive(List<Uri> uris, int index, List<String> resultUrls, String name, String profileUrl) {
        if (isFinishing()) return; // Safety check

        if (index >= uris.size()) {
            updateFirestore(name, profileUrl, resultUrls);
            return;
        }

        MediaManager.get().upload(uris.get(index)).callback(new UploadCallback() {
            @Override public void onStart(String requestId) {}
            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
            @Override public void onSuccess(String requestId, Map resultData) {
                resultUrls.add((String) resultData.get("secure_url"));
                uploadGalleryRecursive(uris, index + 1, resultUrls, name, profileUrl);
            }
            @Override public void onError(String requestId, ErrorInfo error) {
                if (!isFinishing()) {
                    Toast.makeText(EditProfileActivity.this, "Gagal upload 1 foto galeri", Toast.LENGTH_SHORT).show();
                    uploadGalleryRecursive(uris, index + 1, resultUrls, name, profileUrl);
                }
            }
            @Override public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }

    private void updateFirestore(String name, String profileUrl, List<String> galleryUrls) {
        String uid = sessionManager.getUserId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("nama_lengkap", name);
        updates.put("nomor_telepon", etPhone.getText().toString().trim());

        if (sessionManager.isPenyediaJasa()) {
            updates.put("bio", etBio.getText().toString().trim());
            updates.put("keahlian", etKeahlian.getText().toString().trim());
            String tarifStr = etTarif.getText().toString().trim();
            updates.put("tarif", tarifStr.isEmpty() ? 0 : Double.parseDouble(tarifStr));
            updates.put("satuanTarif", etSatuan.getText().toString().trim());
            updates.put("galeriUrl", galleryUrls);
        }

        if (profileUrl != null && !profileUrl.isEmpty()) {
            updates.put("foto_profil", profileUrl);
        }

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    // FIX: Pastikan activity masih ada sebelum update UI
                    if (isFinishing() || isDestroyed()) return;

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    sessionManager.createLoginSession(uid, name, etEmail.getText().toString(), sessionManager.getUserRole());
                    finish();
                })
                .addOnFailureListener(e -> handleError("Gagal update database: " + e.getMessage()));
    }

    private void handleError(String msg) {
        runOnUiThread(() -> {
            if (isFinishing()) return;
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            btnSave.setText("Simpan Perubahan");
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }
}