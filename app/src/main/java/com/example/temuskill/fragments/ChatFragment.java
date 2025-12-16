package com.example.temuskill.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.adapters.MessageAdapter;
import com.example.temuskill.models.Message;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.SessionManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageView btnBack, ivHeaderAvatar;
    private TextView tvTitle;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private SessionManager sessionManager;

    private String orderId;
    private String myUid;
    private String receiverId;
    private String receiverName; // Tambahan untuk optimasi judul

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());
        myUid = sessionManager.getUserId();
        messageList = new ArrayList<>();

        // === 1. TANGKAP DATA DARI ACTIVITY (MainActivity) ===
        if (getArguments() != null) {
            orderId = getArguments().getString("ORDER_ID");

            // Coba ambil ID mitra langsung (Biar nama & foto cepat muncul)
            receiverId = getArguments().getString("TARGET_USER_ID");

            // Coba ambil Nama mitra langsung
            receiverName = getArguments().getString("TARGET_USER_NAME");

            // Jaga-jaga jika key yang dikirim berbeda (misal dari ChatListFragment)
            if (receiverId == null) {
                receiverId = getArguments().getString("PARTNER_ID");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        // Setup Tombol
        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> {
            // Cek apakah bisa kembali (pop) atau harus menutup activity
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        setupRecyclerView();

        // === LOGIKA UTAMA LOAD DATA ===
        // Skenario A: Data Mitra sudah dikirim (Instan)
        if (receiverId != null) {
            fetchPartnerProfile(receiverId);
            if (receiverName != null) {
                tvTitle.setText(receiverName);
            }
        } else {
            // Skenario B: Data Mitra belum ada, cari lewat Order ID (Sedikit loading)
            fetchOrderAndPartnerInfo();
        }

        // Mulai dengarkan pesan masuk
        listenForMessages();
    }

    private void initViews(View view) {
        rvMessages = view.findViewById(R.id.rv_messages);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);
        tvTitle = view.findViewById(R.id.tv_title);
        btnBack = view.findViewById(R.id.btn_back);
        ivHeaderAvatar = view.findViewById(R.id.iv_header_avatar);
    }

    private void fetchOrderAndPartnerInfo() {
        if (orderId == null) return;

        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            // Tentukan siapa lawan bicara kita
                            if (myUid.equals(order.getClientId())) {
                                receiverId = order.getProviderId();
                            } else {
                                receiverId = order.getClientId();
                            }
                            // Load profilnya
                            fetchPartnerProfile(receiverId);
                        }
                    }
                });
    }

    private void fetchPartnerProfile(String uid) {
        if (uid == null) return;

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && isAdded()) {
                        // Gunakan mapping User.class agar aman sesuai field Firestore
                        User user = doc.toObject(User.class);

                        if (user != null) {
                            // 1. Set Nama
                            String name = user.getNamaLengkap();
                            if (name != null && !name.isEmpty()) {
                                tvTitle.setText(name);
                            } else {
                                tvTitle.setText("User");
                            }

                            // 2. Set Foto Profil Header
                            String photoUrl = user.getFotoProfilUrl();
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                if (photoUrl.startsWith("http")) {
                                    Glide.with(this).load(photoUrl)
                                            .circleCrop()
                                            .placeholder(R.drawable.profile)
                                            .into(ivHeaderAvatar);
                                } else {
                                    int resId = getResources().getIdentifier(photoUrl, "drawable", requireContext().getPackageName());
                                    if (resId != 0) {
                                        Glide.with(this).load(resId).circleCrop().into(ivHeaderAvatar);
                                    } else {
                                        ivHeaderAvatar.setImageResource(R.drawable.profile);
                                    }
                                }
                            } else {
                                ivHeaderAvatar.setImageResource(R.drawable.profile);
                            }

                            // 3. Update Foto di Bubble Chat Adapter (Penting!)
                            if (messageAdapter != null) {
                                messageAdapter.setPartnerPhotoUrl(photoUrl);
                                messageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(getContext(), messageList, myUid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Pesan terbaru di bawah
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void listenForMessages() {
        if (orderId == null) {
            return; // Tidak bisa load pesan kalau tidak ada Order ID
        }

        db.collection("orders").document(orderId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Message msg = dc.getDocument().toObject(Message.class);
                            messageList.add(msg);

                            // Notifikasi adapter ada item baru di posisi terakhir
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            // Scroll otomatis ke bawah
                            rvMessages.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        // Validasi data
        if (orderId == null || receiverId == null) {
            Toast.makeText(getContext(), "Sedang memuat data...", Toast.LENGTH_SHORT).show();
            // Coba panggil ulang data jika masih null
            if (orderId != null && receiverId == null) fetchOrderAndPartnerInfo();
            return;
        }

        // Buat objek pesan
        Message message = new Message(myUid, receiverId, text);

        // Bersihkan input segera agar terasa responsif
        etMessage.setText("");

        // Kirim ke Firestore
        db.collection("orders").document(orderId).collection("messages")
                .add(message)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengirim pesan", Toast.LENGTH_SHORT).show();
                });
    }
}