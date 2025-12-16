package com.example.temuskill.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.models.ChatPreview;
import com.example.temuskill.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    // 1. Tambahkan 'final' karena nilainya tidak pernah berubah setelah konstruktor
    private final Context context;
    private final List<ChatPreview> chatList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChatPreview chat);
    }

    public ChatListAdapter(Context context, List<ChatPreview> chatList, OnItemClickListener listener) {
        this.context = context;
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatPreview chat = chatList.get(position);

        holder.tvName.setText("Memuat...");
        holder.tvMessage.setText(chat.getLastMessage());
        holder.tvTime.setText(chat.getTime());

        loadPartnerInfo(chat.getPartnerId(), holder.tvName, holder.ivProfile);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(chat));
    }

    private void loadPartnerInfo(String userId, TextView tvName, ImageView imageView) {
        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User partner = doc.toObject(User.class);
                        if (partner != null) {
                            String realName = partner.getNamaLengkap();
                            tvName.setText((realName != null && !realName.isEmpty()) ? realName : "Tanpa Nama");

                            String photoUrl = partner.getFotoProfilUrl();
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                if (photoUrl.startsWith("http")) {
                                    Glide.with(context).load(photoUrl).placeholder(R.drawable.profile).circleCrop().into(imageView);
                                } else {
                                    // Suppress warning untuk getIdentifier
                                    @SuppressWarnings("DiscouragedApi")
                                    int resId = context.getResources().getIdentifier(photoUrl, "drawable", context.getPackageName());
                                    if (resId != 0) {
                                        Glide.with(context).load(resId).circleCrop().into(imageView);
                                    } else {
                                        imageView.setImageResource(R.drawable.profile);
                                    }
                                }
                            } else {
                                imageView.setImageResource(R.drawable.profile);
                            }
                        }
                    } else {
                        tvName.setText("User Tidak Dikenal");
                    }
                })
                .addOnFailureListener(e -> {
                    tvName.setText("Error");
                    Log.e("ChatAdapter", "Gagal load user: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMessage, tvTime;
        ImageView ivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivProfile = itemView.findViewById(R.id.iv_avatar);
        }
    }
}