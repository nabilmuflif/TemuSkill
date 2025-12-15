package com.example.temuskill.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.adapters.MyServiceAdapter;
import com.example.temuskill.models.Service;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyServicesActivity extends AppCompatActivity {
    private RecyclerView rvMyServices;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_services);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        rvMyServices = findViewById(R.id.rv_my_services);
        rvMyServices.setLayoutManager(new GridLayoutManager(this, 2));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServices();
    }

    private void loadServices() {
        String uid = sessionManager.getUserId();
        db.collection("services")
                .whereEqualTo("providerId", uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Service> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Service.class));
                    }
                    MyServiceAdapter adapter = new MyServiceAdapter(list, () -> {
                        startActivity(new Intent(this, AddServiceActivity.class));
                    });
                    rvMyServices.setAdapter(adapter);
                });
    }
}