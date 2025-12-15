package com.example.temuskill.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.adapters.TalentAdapter;
import com.example.temuskill.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;

    private TalentAdapter talentAdapter;
    private FirebaseFirestore db;
    private List<User> allTalents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        db = FirebaseFirestore.getInstance();

        initViews(view);
        loadAllTalents();
        setupSearch();

        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadAllTalents() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("role", "penyedia_jasa")
                .limit(100) // FIX: Batasi 100 user agar memori hemat
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);

                    allTalents.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        user.setUserId(doc.getId());

                        // Hanya tampilkan user yang aktif
                        if (user.getIsActive() == 1) {
                            allTalents.add(user);
                        }
                    }

                    talentAdapter = new TalentAdapter(allTalents, getContext());
                    rvSearchResults.setAdapter(talentAdapter);

                    updateEmptyState(allTalents.isEmpty());
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    updateEmptyState(true);
                });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (talentAdapter != null) {
                    List<User> filtered = new ArrayList<>();
                    String query = s.toString().toLowerCase().trim();

                    if (query.isEmpty()) {
                        filtered.addAll(allTalents);
                    } else {
                        for (User user : allTalents) {
                            // FIX: Cek Null sebelum toLowerCase() untuk mencegah Crash
                            String name = user.getNamaLengkap() != null ? user.getNamaLengkap().toLowerCase() : "";
                            String skill = user.getKeahlian() != null ? user.getKeahlian().toLowerCase() : "";
                            String bio = user.getBio() != null ? user.getBio().toLowerCase() : "";

                            if (name.contains(query) || skill.contains(query) || bio.contains(query)) {
                                filtered.add(user);
                            }
                        }
                    }

                    talentAdapter.updateData(filtered);
                    updateEmptyState(filtered.isEmpty());
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvSearchResults.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}