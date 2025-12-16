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
    private String receiverName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());
        myUid = sessionManager.getUserId();
        messageList = new ArrayList<>();

        if (getArguments() != null) {
            orderId = getArguments().getString("ORDER_ID");
            receiverId = getArguments().getString("TARGET_USER_ID");
            receiverName = getArguments().getString("TARGET_USER_NAME");
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

        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        setupRecyclerView();

        if (receiverId != null) {
            fetchPartnerProfile(receiverId);
            if (receiverName != null) {
                tvTitle.setText(receiverName);
            }
        } else {
            fetchOrderAndPartnerInfo();
        }

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
                            if (myUid.equals(order.getClientId())) {
                                receiverId = order.getProviderId();
                            } else {
                                receiverId = order.getClientId();
                            }
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
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            String name = user.getNamaLengkap();
                            if (name != null && !name.isEmpty()) {
                                tvTitle.setText(name);
                            } else {
                                tvTitle.setText("User");
                            }

                            String photoUrl = user.getFotoProfilUrl();
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                if (photoUrl.startsWith("http")) {
                                    Glide.with(this).load(photoUrl).circleCrop().placeholder(R.drawable.profile).into(ivHeaderAvatar);
                                } else {
                                    // 1. Suppress Warning
                                    @SuppressWarnings("DiscouragedApi")
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

                            if (messageAdapter != null) {
                                messageAdapter.setPartnerPhotoUrl(photoUrl);
                                messageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        // 2. requireContext()
        if (getContext() != null) {
            messageAdapter = new MessageAdapter(requireContext(), messageList, myUid);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setStackFromEnd(true);
            rvMessages.setLayoutManager(layoutManager);
            rvMessages.setAdapter(messageAdapter);
        }
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
                            if (messageAdapter != null) {
                                messageAdapter.notifyItemInserted(messageList.size() - 1);
                                rvMessages.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (orderId == null || receiverId == null) {
            // 3. requireContext()
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Sedang memuat data...", Toast.LENGTH_SHORT).show();
            }
            if (orderId != null && receiverId == null) fetchOrderAndPartnerInfo();
            return;
        }

        Message message = new Message(myUid, receiverId, text);
        etMessage.setText("");

        db.collection("orders").document(orderId).collection("messages")
                .add(message)
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Gagal mengirim pesan", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}