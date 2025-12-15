package com.example.temuskill.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddCategoryAdminActivity extends AppCompatActivity {

    private EditText etName, etUnit, etDesc;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category_admin);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_cat_name);
        etUnit = findViewById(R.id.et_cat_unit);
        etDesc = findViewById(R.id.et_cat_desc);
        btnSave = findViewById(R.id.btn_save_category);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {
        String name = etName.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama kategori wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("unit", unit);
        category.put("description", desc);
        // ID Dokumen dibuat otomatis oleh Firebase

        db.collection("categories").add(category)
                .addOnSuccessListener(doc -> {
                    // Update ID di dalam dokumen agar mudah diambil
                    db.collection("categories").document(doc.getId()).update("categoryId", doc.getId());

                    Toast.makeText(this, "Kategori Berhasil Ditambahkan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Simpan Kategori");
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}