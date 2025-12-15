package com.example.temuskill.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.models.Service;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyServicesFragment extends Fragment {
    private RecyclerView rvMyServices;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_services, container, false);
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());

        rvMyServices = view.findViewById(R.id.rv_my_services);
        rvMyServices.setLayoutManager(new LinearLayoutManager(getContext()));

        loadMyServices();
        return view;
    }

    private void loadMyServices() {
        db.collection("services")
                .whereEqualTo("providerId", sessionManager.getUserId())
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Service> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Service s = doc.toObject(Service.class);
                        s.setServiceId(doc.getId());
                        list.add(s);
                    }
                });
    }
}