package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Deklarasi Variabel UI
    private EditText etNama, etEmail, etPassword, etConfirmPassword, etPhone;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;

    // Firebase & Data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole = "pencari_jasa"; // Default role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ambil Role dari halaman sebelumnya (RoleSelection)
        if (getIntent().hasExtra("SELECTED_ROLE")) {
            selectedRole = getIntent().getStringExtra("SELECTED_ROLE");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        // ID ini HARUS sama persis dengan yang ada di activity_register.xml
        etNama = findViewById(R.id.et_nama);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tvLoginLink); // Pastikan ID di XML adalah tvLoginLink
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        // Jika user klik "Masuk disini", pindah ke Login
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("SELECTED_ROLE", selectedRole); // Bawa role biar konsisten
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Bersihkan nomor telepon (Hanya ambil angkanya)
        String phoneRaw = etPhone.getText().toString().trim();
        String phoneClean = phoneRaw.replaceAll("[^0-9]", "");

        // Validasi Input
        if (nama.isEmpty()) { etNama.setError("Nama wajib diisi"); return; }
        if (!ValidationUtils.isValidEmail(email)) { etEmail.setError("Email tidak valid"); return; }
        if (phoneClean.length() < 10) { etPhone.setError("Nomor HP minimal 10 digit"); return; }
        if (password.length() < 6) { etPassword.setError("Password minimal 6 karakter"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Password tidak cocok"); return; }

        // Tampilkan Loading
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Proses Register Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Simpan detail user ke Firestore
                            saveUserToFirestore(firebaseUser.getUid(), nama, email, phoneClean, selectedRole);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Gagal Daftar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String nama, String email, String phone, String role) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("nama_lengkap", nama);
        userMap.put("email", email);
        userMap.put("nomor_telepon", phone);
        userMap.put("role", role);
        userMap.put("is_active", 1);
        userMap.put("created_at", System.currentTimeMillis());

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_SHORT).show();

                    // Logout dulu agar sistem memaksa login ulang di halaman Login
                    mAuth.signOut();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}