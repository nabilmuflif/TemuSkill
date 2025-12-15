package com.example.temuskill.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.temuskill.R;
import com.example.temuskill.models.Category;
import com.example.temuskill.models.Service;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AddServiceActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText etServiceName, etDetails;
    private Button btnSave;

    private FirebaseFirestore db;
    private SessionManager sessionManager;

    // List untuk menampung data kategori dari Firebase
    private List<Category> categoryList = new ArrayList<>();
    private ArrayAdapter<Category> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        loadCategories(); // Muat data kategori ke Spinner

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveService());
    }

    private void initViews() {
        spinnerCategory = findViewById(R.id.spinner_category);
        etServiceName = findViewById(R.id.et_service_name);
        etDetails = findViewById(R.id.et_details);
        btnSave = findViewById(R.id.btn_save);
    }

    private void loadCategories() {
        // Ambil data Kategori dari Firestore (yang dibuat Admin/Seeder)
        db.collection("categories").get()
                .addOnSuccessListener(snapshots -> {
                    categoryList.clear();

                    if (snapshots.isEmpty()) {
                        // Fallback jika database kosong
                        Category dummy = new Category("Umum");
                        dummy.setCategoryId("gen_01");
                        categoryList.add(dummy);
                    } else {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Category cat = doc.toObject(Category.class);
                            cat.setCategoryId(doc.getId());
                            categoryList.add(cat);
                        }
                    }

                    // Set ke Spinner menggunakan ArrayAdapter standar
                    // Android akan otomatis memanggil toString() dari objek Category
                    spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(spinnerAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat kategori: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveService() {
        // Validasi Input
        Category selectedCat = (Category) spinnerCategory.getSelectedItem();
        String serviceName = etServiceName.getText().toString().trim();
        String details = etDetails.getText().toString().trim();

        if (selectedCat == null) {
            Toast.makeText(this, "Mohon tunggu hingga kategori termuat", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serviceName.isEmpty()) {
            etServiceName.setError("Nama jasa wajib diisi");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        // Simpan Data Service
        // Format: ProviderID, Kategori (dari spinner), Deskripsi (gabungan Nama + Detail)
        // Kita gabungkan Nama Jasa dan Deskripsi di field 'details' agar sesuai model Service lama
        // Atau sesuaikan model Service Anda jika sudah punya field 'serviceName'

        String fullDescription = serviceName;
        if (!details.isEmpty()) {
            fullDescription += "\n\n" + details;
        }

        Service service = new Service(sessionManager.getUserId(), selectedCat.getName(), fullDescription);

        // Opsional: Jika model Service sudah diupdate punya field khusus nama
        // service.setServiceName(serviceName);

        db.collection("services").add(service)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Layanan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    finish(); // Kembali ke halaman sebelumnya
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Simpan Jasa");
                    Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}