package com.example.temuskill.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.activities.TalentDetailActivity;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.PriceFormatter;
import java.util.List;

public class TalentAdapter extends RecyclerView.Adapter<TalentAdapter.ViewHolder> {
    private List<User> talents;
    private Context context;

    public TalentAdapter(List<User> talents, Context context) {
        this.talents = talents;
        this.context = context;
    }

    public void updateData(List<User> newTalents) {
        this.talents = newTalents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_talent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = talents.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() { return talents.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName, tvKeahlian, tvPrice, tvRatingBadge, tvLocation;
        ImageView ivImage;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_talent);
            tvName = itemView.findViewById(R.id.tv_talent_name);
            tvKeahlian = itemView.findViewById(R.id.tv_keahlian);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvRatingBadge = itemView.findViewById(R.id.tv_rating_badge);
            tvLocation = itemView.findViewById(R.id.tv_location);
            ivImage = itemView.findViewById(R.id.iv_talent_image);
        }

        void bind(User user) {
            tvName.setText(user.getNamaLengkap());
            tvKeahlian.setText(user.getKeahlian() != null ? user.getKeahlian() : "Jasa Umum");

            if(user.getTarif() > 0) {
                String satuan = user.getSatuanTarif() != null ? "/" + user.getSatuanTarif() : "";
                tvPrice.setText(PriceFormatter.formatPrice(user.getTarif()) + " " + satuan);
            } else {
                tvPrice.setText("Hubungi");
            }

            // === LOGIKA LOAD GAMBAR ===
            String imgUrl = user.getFotoProfilUrl();

            if (imgUrl != null && !imgUrl.isEmpty()) {
                // 1. Cek apakah ini URL Online (Cloudinary/Firebase Storage)
                if (imgUrl.startsWith("http")) {
                    Glide.with(context)
                            .load(imgUrl)
                            .placeholder(R.drawable.profile)
                            .centerCrop()
                            .into(ivImage);
                }
                // 2. Jika bukan link, anggap ini nama file di Drawable (Local Resource)
                else {
                    // Cari ID resource berdasarkan nama string (contoh: "talent_1" -> R.drawable.talent_1)
                    int resId = context.getResources().getIdentifier(imgUrl, "drawable", context.getPackageName());

                    if (resId != 0) {
                        // Jika file ditemukan
                        Glide.with(context)
                                .load(resId)
                                .placeholder(R.drawable.profile)
                                .centerCrop()
                                .into(ivImage);
                    } else {
                        // Jika tidak ketemu, pakai default
                        ivImage.setImageResource(R.drawable.profile);
                    }
                }
            } else {
                // Data kosong
                ivImage.setImageResource(R.drawable.profile);
            }
            // =========================

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, TalentDetailActivity.class);
                intent.putExtra("TALENT_ID", user.getUserId());
                context.startActivity(intent);
            });
        }
    }
}