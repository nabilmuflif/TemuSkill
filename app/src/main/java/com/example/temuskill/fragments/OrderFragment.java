package com.example.temuskill.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.adapters.OrderAdapter;
import com.example.temuskill.models.Order;
import com.example.temuskill.utils.Constants;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {

    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private CircleImageView ivProfileHeader;
    private TextView filterAll, filterActive, filterUnreviewed, filterCompleted, filterCancelled;

    private OrderAdapter adapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private ListenerRegistration firestoreListener;
    private ActivityResultLauncher<Intent> rateOrderLauncher;

    // 1. Tambahkan 'final'
    private final List<Order> allOrderList = new ArrayList<>();
    private final List<Order> displayedList = new ArrayList<>();

    private String currentFilter = "ALL";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());

        setupActivityResultLauncher();
        initViews(view);
        setupAdapter();
        setupFilterListeners();

        return view;
    }

    private void setupActivityResultLauncher() {
        rateOrderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (firestoreListener != null) firestoreListener.remove();
                        allOrderList.clear();
                        displayedList.clear();
                        new Handler(Looper.getMainLooper()).postDelayed(this::startRealtimeUpdates, 500);
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileImage();
        startRealtimeUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firestoreListener != null) firestoreListener.remove();
    }

    private void initViews(View view) {
        rvOrders = view.findViewById(R.id.rv_orders);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        ivProfileHeader = view.findViewById(R.id.iv_profile_header);

        filterAll = view.findViewById(R.id.filter_all);
        filterActive = view.findViewById(R.id.filter_active);
        filterUnreviewed = view.findViewById(R.id.filter_unreviewed);
        filterCompleted = view.findViewById(R.id.filter_completed);
        filterCancelled = view.findViewById(R.id.filter_cancelled);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupAdapter() {
        // 2. Gunakan requireContext() untuk keamanan null safety
        if (getContext() != null) {
            adapter = new OrderAdapter(displayedList, requireContext(), sessionManager.isPenyediaJasa(), rateOrderLauncher);
            rvOrders.setAdapter(adapter);
        }
    }

    private void setupFilterListeners() {
        filterAll.setOnClickListener(v -> applyFilter("ALL"));
        filterActive.setOnClickListener(v -> applyFilter("ACTIVE"));
        filterUnreviewed.setOnClickListener(v -> applyFilter("UNREVIEWED"));
        filterCompleted.setOnClickListener(v -> applyFilter("COMPLETED"));
        filterCancelled.setOnClickListener(v -> applyFilter("CANCELLED"));
    }

    private void startRealtimeUpdates() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String myUid = sessionManager.getUserId();
        if (myUid == null) return;

        String searchField = sessionManager.isPenyediaJasa() ? "providerId" : "clientId";

        Query query = db.collection("orders")
                .whereEqualTo(searchField, myUid)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        if (firestoreListener != null) firestoreListener.remove();

        firestoreListener = query.addSnapshotListener((value, error) -> {
            if (!isAdded()) return;
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (error != null) return;

            if (value != null) {
                allOrderList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Order order = doc.toObject(Order.class);
                        order.setOrderId(doc.getId());
                        allOrderList.add(order);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                applyFilter(currentFilter);
            }
        });
    }

    private void applyFilter(String filterType) {
        this.currentFilter = filterType;
        updateFilterUI(filterType);

        displayedList.clear();

        if (filterType.equals("ALL")) {
            displayedList.addAll(allOrderList);
        } else {
            for (Order order : allOrderList) {
                String status = order.getStatusPesanan();
                // 3. Switch statement untuk logika filter yang lebih rapi (optional, tapi membersihkan warning if-else chain)
                switch (filterType) {
                    case "ACTIVE":
                        if (status.equals(Constants.ORDER_STATUS_PENDING) ||
                                status.equals(Constants.ORDER_STATUS_CONFIRMED) ||
                                status.equals(Constants.ORDER_STATUS_IN_PROGRESS)) {
                            displayedList.add(order);
                        }
                        break;
                    case "UNREVIEWED":
                        if (status.equals(Constants.ORDER_STATUS_COMPLETED)) {
                            displayedList.add(order);
                        }
                        break;
                    case "COMPLETED":
                        if (status.equals(Constants.ORDER_STATUS_REVIEWED)) {
                            displayedList.add(order);
                        }
                        break;
                    case "CANCELLED":
                        if (status.equals(Constants.ORDER_STATUS_CANCELLED)) {
                            displayedList.add(order);
                        }
                        break;
                }
            }
        }

        if (adapter != null) adapter.notifyDataSetChanged();

        if (displayedList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    private void updateFilterUI(String activeFilter) {
        resetChip(filterAll);
        resetChip(filterActive);
        resetChip(filterUnreviewed);
        resetChip(filterCompleted);
        resetChip(filterCancelled);

        TextView target = null;
        switch (activeFilter) {
            case "ALL": target = filterAll; break;
            case "ACTIVE": target = filterActive; break;
            case "UNREVIEWED": target = filterUnreviewed; break;
            case "COMPLETED": target = filterCompleted; break;
            case "CANCELLED": target = filterCancelled; break;
        }

        if (target != null) {
            target.setBackgroundResource(R.drawable.bg_chip_selected);
            target.setTextColor(Color.WHITE);
        }
    }

    private void resetChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_unselected);
        chip.setTextColor(Color.parseColor("#757575"));
    }

    private void loadProfileImage() {
        String uid = sessionManager.getUserId();
        if(uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if(isAdded() && doc.exists()) {
                    String url = doc.getString("foto_profil");
                    if(url != null && !url.isEmpty() && ivProfileHeader != null) {
                        if(url.startsWith("http")) {
                            Glide.with(this).load(url).placeholder(R.drawable.profile).into(ivProfileHeader);
                        } else {
                            // 4. Suppress Warning DiscouragedApi
                            @SuppressWarnings("DiscouragedApi")
                            int resId = getResources().getIdentifier(url, "drawable", requireContext().getPackageName());
                            if(resId != 0) Glide.with(this).load(resId).into(ivProfileHeader);
                        }
                    }
                }
            });
        }
    }
}