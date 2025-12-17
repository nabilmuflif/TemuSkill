package com.example.temuskill.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.activities.OrderDetailActivity;
import com.example.temuskill.activities.RateOrderActivity;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.PriceFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private boolean isProviderView; // TAMBAHAN: untuk cek role

    // CONSTRUCTOR BARU: tambah parameter isProviderView
    public OrderAdapter(Context context, List<Order> orderList, boolean isProviderView) {
        this.context = context;
        this.orderList = orderList;
        this.db = FirebaseFirestore.getInstance();
        this.isProviderView = isProviderView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        // 1. DATA DASAR
        holder.tvServiceName.setText(order.getServiceName());
        holder.tvTotalBiaya.setText(PriceFormatter.formatPrice(order.getTotalBiaya()));

        String fullJadwal = order.getJadwalKerja();
        if (fullJadwal != null && fullJadwal.contains(",")) {
            String[] parts = fullJadwal.split(",");
            holder.tvJadwal.setText(parts[0].trim());
            if (parts.length > 1) holder.tvJam.setText(parts[1].trim());
        } else {
            holder.tvJadwal.setText(fullJadwal);
            holder.tvJam.setText("-");
        }

        // 2. INFO PROVIDER/CLIENT (tergantung role)
        String targetUserId = isProviderView ? order.getClientId() : order.getProviderId();

        if (targetUserId != null) {
            db.collection("users").document(targetUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                holder.tvProviderName.setText(user.getNamaLengkap());
                                if (user.getFotoProfilUrl() != null && !user.getFotoProfilUrl().isEmpty()) {
                                    if (user.getFotoProfilUrl().startsWith("http")) {
                                        Glide.with(context).load(user.getFotoProfilUrl()).placeholder(R.drawable.profile).into(holder.ivProvider);
                                    } else {
                                        int resId = context.getResources().getIdentifier(user.getFotoProfilUrl(), "drawable", context.getPackageName());
                                        if (resId != 0) Glide.with(context).load(resId).into(holder.ivProvider);
                                    }
                                }
                            }
                        }
                    });
        } else {
            holder.tvProviderName.setText(isProviderView ? "Pencari Jasa" : "Penyedia Jasa");
        }

        // 3. LOGIKA STATUS & TOMBOL (DENGAN ROLE CHECK)
        String rawStatus = order.getStatusPesanan();
        String status = (rawStatus != null) ? rawStatus.trim().toLowerCase() : "pending";

        String statusText = "Menunggu";
        int bgColor = Color.GRAY;
        int textColor = Color.WHITE;

        // Reset Default
        holder.btnAction.setVisibility(View.GONE);
        holder.tvStatusLabel.setVisibility(View.GONE);

        switch (status) {
            case "menunggu":
            case "pending":
                statusText = "Menunggu Konfirmasi";
                bgColor = Color.parseColor("#FFC107");
                textColor = Color.BLACK;
                holder.tvStatusLabel.setVisibility(View.VISIBLE);
                break;

            case "aktif":
            case "confirmed":
            case "diproses":
                statusText = "Sedang Diproses";
                bgColor = Color.parseColor("#2196F3");
                holder.tvStatusLabel.setVisibility(View.VISIBLE);
                break;

            case "dalam perjalanan":
            case "otw":
                statusText = "Provider OTW";
                bgColor = Color.parseColor("#FF9800");
                holder.tvStatusLabel.setVisibility(View.VISIBLE);
                break;

            case "dalam pengerjaan":
            case "in_progress":
                statusText = "Sedang Dikerjakan";
                bgColor = Color.parseColor("#4CAF50");
                holder.tvStatusLabel.setVisibility(View.VISIBLE);
                break;

            // STATUS COMPLETED: BEDA TAMPILAN CLIENT vs PROVIDER
            case "selesai":
            case "completed":
                if (isProviderView) {
                    // PROVIDER: Hanya tampilkan label "Selesai" abu-abu
                    statusText = "Selesai";
                    bgColor = Color.parseColor("#757575");
                    holder.tvStatusLabel.setVisibility(View.VISIBLE);
                    holder.btnAction.setVisibility(View.GONE);
                } else {
                    // CLIENT: Tampilkan tombol "Beri Penilaian"
                    holder.tvStatusLabel.setVisibility(View.GONE);
                    holder.btnAction.setVisibility(View.VISIBLE);
                    holder.btnAction.setText("Beri Penilaian");
                    holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#111D5E")));
                    holder.btnAction.setOnClickListener(v -> {
                        Intent intent = new Intent(context, RateOrderActivity.class);
                        intent.putExtra("ORDER_ID", order.getOrderId());
                        intent.putExtra("PROVIDER_ID", order.getProviderId());
                        context.startActivity(intent);
                    });
                }
                break;

            // STATUS REVIEWED: Semua tampilkan label "Selesai"
            case "reviewed":
            case "diulas":
            case "sudah dinilai":
                statusText = "Selesai";
                bgColor = Color.parseColor("#757575");
                holder.tvStatusLabel.setVisibility(View.VISIBLE);
                holder.btnAction.setVisibility(View.GONE);
                break;

            case "dibatalkan":
            case "cancelled":
                statusText = "Dibatalkan";
                bgColor = Color.parseColor("#F44336");
                holder.tvStatusLabel.setVisibility(View.VISIBLE);
                break;

            default:
                statusText = status;
                bgColor = Color.GRAY;
                holder.tvStatusLabel.setVisibility(View.VISIBLE);
                break;
        }

        holder.tvStatusLabel.setText(statusText);
        holder.tvStatusLabel.setTextColor(textColor);
        holder.tvStatusLabel.setBackgroundTintList(ColorStateList.valueOf(bgColor));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProviderName, tvServiceName, tvJadwal, tvJam, tvTotalBiaya, tvStatusLabel;
        ImageView ivProvider;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProviderName = itemView.findViewById(R.id.tv_provider_name);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvJadwal = itemView.findViewById(R.id.tv_jadwal);
            tvJam = itemView.findViewById(R.id.tv_jam);
            tvTotalBiaya = itemView.findViewById(R.id.tv_total_biaya);
            tvStatusLabel = itemView.findViewById(R.id.tv_status_label);
            ivProvider = itemView.findViewById(R.id.iv_provider);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}