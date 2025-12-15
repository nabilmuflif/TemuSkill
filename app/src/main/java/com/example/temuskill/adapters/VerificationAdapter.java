package com.example.temuskill.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.activities.TalentDetailActivity;
import com.example.temuskill.models.ProviderProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VerificationAdapter extends RecyclerView.Adapter<VerificationAdapter.ViewHolder> {

    private List<ProviderProfile> requests;
    private Context context;
    private FirebaseFirestore db;

    public VerificationAdapter(Context context, List<ProviderProfile> requests) {
        this.context = context;
        this.requests = requests;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_verification_req, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProviderProfile profile = requests.get(position);

        // Format Tanggal
        if (profile.getRequestDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            holder.tvDate.setText(sdf.format(new Date(profile.getRequestDate())));
        }

        // Ambil Data Nama & Foto dari collection 'users'
        db.collection("users").document(profile.getUserId()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("nama_lengkap");
                        String photo = doc.getString("foto_profil");

                        holder.tvName.setText(name != null ? name : "User");

                        // Load Foto
                        if (photo != null && !photo.isEmpty()) {
                            if (photo.startsWith("http")) {
                                Glide.with(context).load(photo).placeholder(R.drawable.profile).into(holder.ivAvatar);
                            } else {
                                int resId = context.getResources().getIdentifier(photo, "drawable", context.getPackageName());
                                if (resId != 0) Glide.with(context).load(resId).placeholder(R.drawable.profile).into(holder.ivAvatar);
                            }
                        }
                    }
                });

        // Klik Item -> Buka Detail Talent
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TalentDetailActivity.class);
            intent.putExtra("TALENT_ID", profile.getUserId());
            intent.putExtra("IS_ADMIN_VIEW", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}