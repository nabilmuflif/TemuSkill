package com.example.temuskill.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.adapters.AdminUserAdapter;
import com.example.temuskill.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private RecyclerView rvUsers;
    private FirebaseFirestore db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        rvUsers = view.findViewById(R.id.rv_users);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        loadUsers();
    }

    private void loadUsers() {
        db.collection("users").whereNotEqualTo("role", "admin").get()
                .addOnSuccessListener(snapshots -> {
                    if(!isAdded()) return;
                    List<User> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        User u = doc.toObject(User.class);
                        u.setUserId(doc.getId());
                        list.add(u);
                    }
                    rvUsers.setAdapter(new AdminUserAdapter(list, getContext()));
                });
    }
}