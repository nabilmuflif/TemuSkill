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
import com.example.temuskill.adapters.ChatListAdapter;
import com.example.temuskill.models.ChatPreview;
import com.example.temuskill.models.Order;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// IMPORT PENTING UNTUK MENGHILANGKAN DUPLIKAT
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatListFragment extends Fragment {

    private RecyclerView rvChatList;
    private ChatListAdapter adapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvChatList = view.findViewById(R.id.rv_chat_list);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatListFromCloud();
    }

    private void loadChatListFromCloud() {
        String myUid = sessionManager.getUserId();
        boolean isProvider = sessionManager.isPenyediaJasa();
        String searchField = isProvider ? "providerId" : "clientId";

        db.collection("orders")
                .whereEqualTo(searchField, myUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    List<ChatPreview> previews = new ArrayList<>();

                    // --- SIAPKAN PENYARING DUPLIKAT ---
                    Set<String> addedPartnerIds = new HashSet<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        String orderId = doc.getId();

                        String partnerId;

                        // Tentukan Partner
                        if (isProvider) {
                            partnerId = order.getClientId();
                        } else {
                            partnerId = order.getProviderId();
                        }

                        // --- CEK DUPLIKASI ---
                        // Jika partnerId ini SUDAH ADA di daftar 'addedPartnerIds', SKIP!
                        if (partnerId == null || addedPartnerIds.contains(partnerId)) {
                            continue; // Lewati ke order berikutnya
                        }

                        // Jika belum ada, catat partnerId ini
                        addedPartnerIds.add(partnerId);

                        // Tambahkan ke list chat
                        // Kita biarkan namanya "Memuat...", nanti Adapter yang mengisi dengan benar
                        previews.add(new ChatPreview(orderId, "Memuat...", "Ketuk untuk chat", "Chat", partnerId));
                    }

                    setupAdapter(previews);
                });
    }

    private void setupAdapter(List<ChatPreview> previews) {
        adapter = new ChatListAdapter(getContext(), previews, item -> {
            ChatFragment chatFragment = new ChatFragment();
            Bundle args = new Bundle();
            args.putString("ORDER_ID", item.getOrderId());
            args.putString("PARTNER_ID", item.getPartnerId());
            chatFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvChatList.setAdapter(adapter);
    }
}