package com.example.temuskill.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.activities.MyReviewsActivity;
import com.example.temuskill.activities.NotificationActivity; // Pastikan ini merahnya hilang (class sudah ada)
import com.example.temuskill.models.Order;
import com.example.temuskill.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProviderHomeFragment extends Fragment {

    // UI Components
    private CircleImageView ivProfile;
    private TextView tvGreeting, tvTransactionCount, tvIncome, tvRatingValue, tvScheduleText, tvJadwalTitle;
    private ImageView btnRefresh, btnNotification;
    private CardView cardRating, cardBalance;
    private Button btnViewAllSchedule;

    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // PENTING: Pastikan nama file XML layout provider kamu benar.
        // Cek di folder layout, apakah namanya 'fragment_provider_home' atau 'fragment_home'
        // Jika namanya fragment_home, ubah baris di bawah jadi R.layout.fragment_home
        View view = inflater.inflate(R.layout.fragment_provider_home, container, false);

        sessionManager = new SessionManager(getContext());
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProviderProfile();
        loadDashboardStats();
        loadTodaySchedule();
    }

    private void initViews(View view) {
        // --- Header ---
        ivProfile = view.findViewById(R.id.iv_profile);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        btnNotification = view.findViewById(R.id.btn_notif);

        // --- Card Saldo (Balance) ---
        cardBalance = view.findViewById(R.id.card_balance);
        tvTransactionCount = view.findViewById(R.id.tv_transaction_count);
        tvIncome = view.findViewById(R.id.tv_income);
        btnRefresh = view.findViewById(R.id.btn_refresh);

        // --- Card Rating ---
        cardRating = view.findViewById(R.id.card_rating);
        tvRatingValue = view.findViewById(R.id.tv_rating_value);

        // --- Jadwal (Schedule) ---
        tvJadwalTitle = view.findViewById(R.id.tv_jadwal_title);
        tvScheduleText = view.findViewById(R.id.tv_schedule_text_1);
        btnViewAllSchedule = view.findViewById(R.id.btn_view_all_schedule);

        // Set Default Name
        String userName = sessionManager.getUserName();
        tvGreeting.setText(userName != null ? "Halo, " + userName + "!" : "Halo, Mitra!");
    }

    private void setupListeners() {
        // 1. Refresh Data Saldo
        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Memperbarui data...", Toast.LENGTH_SHORT).show();
            loadDashboardStats();
            loadTodaySchedule();
        });

        // 2. Klik Notifikasi (Membuka Activity Notification)
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NotificationActivity.class);
            startActivity(intent);
        });

        // 3. Klik Rating -> Buka Review
        cardRating.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyReviewsActivity.class);
            startActivity(intent);
        });

        // 4. Klik Lihat Semua Jadwal -> Pindah ke Tab Pesanan
        btnViewAllSchedule.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    // ID ini harus sama dengan ID menu bottom nav kamu (misal: nav_orders atau pesananBtn)
                    bottomNav.setSelectedItemId(R.id.pesananBtn);
                }
            }
        });
    }

    private void loadProviderProfile() {
        String uid = sessionManager.getUserId();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (!isAdded()) return;
            if (doc.exists()) {
                String name = doc.getString("nama_lengkap"); // Sesuai database kamu
                String photoUrl = doc.getString("foto_profil"); // Sesuai database kamu

                if (name != null) tvGreeting.setText("Halo, " + name + "!");

                // Load Foto
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    if (photoUrl.startsWith("http") || photoUrl.startsWith("android.resource")) {
                        Glide.with(this).load(photoUrl).placeholder(R.drawable.profile).into(ivProfile);
                    } else {
                        // Handle resource dummy
                        int resId = getResources().getIdentifier(photoUrl, "drawable", requireContext().getPackageName());
                        if (resId != 0) Glide.with(this).load(resId).into(ivProfile);
                    }
                }
            }
        });
    }

    private void loadDashboardStats() {
        String myUid = sessionManager.getUserId();

        // --- Hitung Pendapatan ---
        db.collection("orders")
                .whereEqualTo("providerId", myUid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;

                    double totalIncome = 0;
                    int countSuccess = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order order = doc.toObject(Order.class);

                        // Safety check
                        if (order == null || order.getStatusPesanan() == null) continue;

                        String status = order.getStatusPesanan();

                        // LOGIKA SALDO:
                        // Saya tambahkan "confirmed" agar 20.000 kamu terbaca.
                        if (status.equalsIgnoreCase("completed") ||
                                status.equalsIgnoreCase("reviewed") ||
                                status.equalsIgnoreCase("confirmed")) { // <-- INI KUNCINYA

                            // Mengambil nilai totalBiaya dari Model Order
                            totalIncome += order.getTotalBiaya();
                            countSuccess++;
                        }
                    }

                    // Format Rupiah Manual (Contoh: Rp 20.000)
                    String formattedIncome = "Rp " + String.format(Locale.GERMANY, "%,.0f", totalIncome).replace(',', '.');

                    tvIncome.setText(formattedIncome);
                    tvTransactionCount.setText(String.valueOf(countSuccess));
                })
                .addOnFailureListener(e -> {
                    // Jika gagal load
                    tvIncome.setText("Rp 0");
                });

        // --- Hitung Rating ---
        db.collection("reviews")
                .whereEqualTo("providerId", myUid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    if (snapshots.isEmpty()) {
                        tvRatingValue.setText("0.0");
                        return;
                    }

                    double totalStars = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Double rating = doc.getDouble("rating");
                        if (rating != null) totalStars += rating;
                    }
                    double avg = totalStars / snapshots.size();
                    tvRatingValue.setText(String.format(Locale.US, "%.1f", avg));
                });
    }

    private void loadTodaySchedule() {
        String myUid = sessionManager.getUserId();

        // Ambil 1 pesanan aktif
        db.collection("orders")
                .whereEqualTo("providerId", myUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;

                    Order activeOrder = null;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order order = doc.toObject(Order.class);
                        String status = order.getStatusPesanan();

                        if ("pending".equalsIgnoreCase(status) ||
                                "confirmed".equalsIgnoreCase(status) ||
                                "on_progress".equalsIgnoreCase(status)) {
                            activeOrder = order;
                            break; // Ambil satu saja yang paling baru
                        }
                    }

                    if (activeOrder != null) {
                        String statusStr = "";
                        if(activeOrder.getStatusPesanan().equalsIgnoreCase("pending")) statusStr = "(Menunggu Konfirmasi)";
                        else if(activeOrder.getStatusPesanan().equalsIgnoreCase("confirmed")) statusStr = "(Segera Kerjakan)";
                        else if(activeOrder.getStatusPesanan().equalsIgnoreCase("on_progress")) statusStr = "(Sedang Dikerjakan)";

                        String displayText = activeOrder.getServiceName() + "\n" + activeOrder.getJadwalKerja() + " " + statusStr;
                        tvScheduleText.setText(displayText);
                    } else {
                        tvScheduleText.setText("Belum ada jadwal spesifik hari ini.");
                    }
                });
    }
}