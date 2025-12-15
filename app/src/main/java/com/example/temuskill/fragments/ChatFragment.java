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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString("ORDER_ID");
        }
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());
        myUid = sessionManager.getUserId();
        messageList = new ArrayList<>();
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
        setupRecyclerView();

        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // 1. Fetch Order Data to determine the partner
        fetchOrderAndPartnerInfo();
        // 2. Start listening for incoming messages
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
                            // Determine Chat Partner
                            if (myUid.equals(order.getClientId())) {
                                receiverId = order.getProviderId();
                            } else {
                                receiverId = order.getClientId();
                            }
                            // Fetch Partner Profile (Name & Photo)
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
                        // 1. GET NAME
                        String name = doc.getString("nama_lengkap");
                        // Set Name to Title
                        if (name != null && !name.isEmpty()) {
                            tvTitle.setText(name);
                        } else {
                            tvTitle.setText("User"); // Fallback
                        }

                        // 2. GET PHOTO
                        String photoUrl = doc.getString("foto_profil");

                        // === LOGIC: LOAD PROFILE PHOTO (Support Dummy & URL) ===
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            // Check if URL (Internet) or URI (Android Resource)
                            if (photoUrl.startsWith("http") || photoUrl.startsWith("android.resource")) {
                                Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.profile)
                                        .circleCrop()
                                        .into(ivHeaderAvatar);
                            } else {
                                // If not URL, assume it's a Drawable Resource Name (Dummy Data)
                                int resId = getResources().getIdentifier(photoUrl, "drawable", requireContext().getPackageName());
                                if (resId != 0) {
                                    Glide.with(this)
                                            .load(resId)
                                            .placeholder(R.drawable.profile)
                                            .circleCrop()
                                            .into(ivHeaderAvatar);
                                } else {
                                    ivHeaderAvatar.setImageResource(R.drawable.profile);
                                }
                            }
                        } else {
                            ivHeaderAvatar.setImageResource(R.drawable.profile);
                        }

                        // Pass partner's photo to adapter so it appears in left chat bubbles
                        if (messageAdapter != null) {
                            messageAdapter.setPartnerPhotoUrl(photoUrl);
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(getContext(), messageList, myUid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Auto-scroll to bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void listenForMessages() {
        if (orderId == null) return;
        db.collection("orders").document(orderId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Message msg = dc.getDocument().toObject(Message.class);
                            messageList.add(msg);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            rvMessages.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (orderId == null || receiverId == null) {
            Toast.makeText(getContext(), "Loading data...", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(myUid, receiverId, text);
        db.collection("orders").document(orderId).collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to send", Toast.LENGTH_SHORT).show();
                });
    }
}