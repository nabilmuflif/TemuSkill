package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String preSelectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        preSelectedRole = getIntent().getStringExtra("SELECTED_ROLE");

        if (sessionManager.isLoggedIn()) {
            checkRoleAndRedirect(sessionManager.getUserRole());
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RoleSelectionActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        fetchUserData(uid);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Login Gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserData(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (documentSnapshot.exists()) {
                        String nama = documentSnapshot.getString("nama_lengkap");
                        String role = documentSnapshot.getString("role");

                        Long isActiveLong = documentSnapshot.getLong("is_active");
                        int isActive = (isActiveLong != null) ? isActiveLong.intValue() : 1;

                        if (isActive == 0) {
                            Toast.makeText(this, "Akun Anda telah dinonaktifkan Admin. Hubungi CS.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            return;
                        }

                        sessionManager.createLoginSession(uid, nama, mAuth.getCurrentUser().getEmail(), role);
                        Toast.makeText(this, "Selamat datang, " + nama, Toast.LENGTH_SHORT).show();
                        checkRoleAndRedirect(role);
                    } else {
                        Toast.makeText(this, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Gagal ambil data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkRoleAndRedirect(String role) {
        Intent intent;
        if (role != null && "admin".equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}