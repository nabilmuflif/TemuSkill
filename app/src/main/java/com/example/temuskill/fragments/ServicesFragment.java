package com.example.temuskill.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.activities.AddCategoryAdminActivity;
import com.example.temuskill.adapters.AdminCategoryAdapter;
import com.example.temuskill.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ServicesFragment extends Fragment {

    private RecyclerView rvCategories;
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        rvCategories = view.findViewById(R.id.rv_categories);
        fabAdd = view.findViewById(R.id.fab_add_category);

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        // FAB tetap membuka Activity form karena itu halaman input
        fabAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), AddCategoryAdminActivity.class)));

        loadCategories();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories(); // Refresh saat kembali dari add
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(snapshots -> {
            if(!isAdded()) return;
            List<Category> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                Category cat = doc.toObject(Category.class);
                cat.setCategoryId(doc.getId());
                list.add(cat);
            }
            rvCategories.setAdapter(new AdminCategoryAdapter(list, category -> showDeleteDialog(category)));
        });
    }

    private void showDeleteDialog(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Kategori?")
                .setMessage("Menghapus " + category.getName())
                .setPositiveButton("Hapus", (d, w) -> {
                    db.collection("categories").document(category.getCategoryId()).delete()
                            .addOnSuccessListener(v -> loadCategories());
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}