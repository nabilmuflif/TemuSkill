package com.example.temuskill.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import com.example.temuskill.activities.OrderDetailActivity;
import com.example.temuskill.activities.ProviderOrderDetailActivity;
import com.example.temuskill.activities.RateOrderActivity;
import com.example.temuskill.models.Order;
import com.example.temuskill.models.User;
import com.example.temuskill.utils.Constants;
import com.example.temuskill.utils.PriceFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Order> orders;
    private Context context;
    private boolean isProvider;
    private ActivityResultLauncher<Intent> rateOrderLauncher;

    public OrderAdapter(List<Order> orders, Context context, boolean isProvider, ActivityResultLauncher<Intent> rateOrderLauncher) {
        this.orders = orders;
        this.context = context;
        this.isProvider = isProvider;
        this.rateOrderLauncher = rateOrderLauncher;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() { return orders.size(); }
    @Override
    public long getItemId(int position) { return position; }
    @Override
    public int getItemViewType(int position) { return position; }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvService, tvPrice, tvDate, tvTime, tvStatusLabel;
        Button btnAction;
        ImageView ivProfile;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_provider_name);
            tvService = itemView.findViewById(R.id.tv_service_name);
            tvPrice = itemView.findViewById(R.id.tv_total_biaya);
            tvDate = itemView.findViewById(R.id.tv_jadwal);
            tvTime = itemView.findViewById(R.id.tv_jam);
            tvStatusLabel = itemView.findViewById(R.id.tv_status_label);
            btnAction = itemView.findViewById(R.id.btn_action);
            ivProfile = itemView.findViewById(R.id.iv_provider);
        }

        void bind(Order order) {
            tvService.setText(order.getServiceName());
            tvPrice.setText(PriceFormatter.formatPrice(order.getTotalBiaya()));

            if (order.getCreatedAt() > 0) {
                Date date = new Date(order.getCreatedAt());
                tvDate.setText(new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID")).format(date));
                tvTime.setText(new SimpleDateFormat("HH:mm", new Locale("id", "ID")).format(date));
            } else {
                tvDate.setText("-");
                tvTime.setText("-");
            }

            String targetUserId = isProvider ? order.getClientId() : order.getProviderId();
            tvName.setText(isProvider ? "Pelanggan" : order.getProviderName());
            loadUserImageAndName(targetUserId);

            setupStatusUI(order.getStatusPesanan(), order);

            itemView.setOnClickListener(v -> {
                Intent intent;
                if (isProvider) {
                    intent = new Intent(context, ProviderOrderDetailActivity.class);
                } else {
                    intent = new Intent(context, OrderDetailActivity.class);
                }
                intent.putExtra("ORDER_ID", order.getOrderId());
                context.startActivity(intent);
            });
        }

        private void loadUserImageAndName(String uid) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                tvName.setText(user.getNamaLengkap());
                                if (user.getFotoProfilUrl() != null) {
                                    String url = user.getFotoProfilUrl();
                                    if(url.startsWith("http")) Glide.with(context).load(url).placeholder(R.drawable.profile).into(ivProfile);
                                    else {
                                        int resId = context.getResources().getIdentifier(url, "drawable", context.getPackageName());
                                        if(resId!=0) Glide.with(context).load(resId).into(ivProfile);
                                    }
                                }
                            }
                        }
                    });
        }

        private void setupStatusUI(String status, Order order) {
            tvStatusLabel.setVisibility(View.VISIBLE);
            btnAction.setVisibility(View.GONE);

            switch (status) {
                case Constants.ORDER_STATUS_PENDING:
                    showLabel("Menunggu", "#FF9800", R.drawable.bg_status_active);
                    break;

                case Constants.ORDER_STATUS_CONFIRMED:
                    showLabel("Aktif", "#4CAF50", R.drawable.bg_status_green);
                    break;

                // [LOGIKA BARU YANG LEBIH SIMPEL]

                case Constants.ORDER_STATUS_COMPLETED:
                    // Status 'completed' artinya Pekerjaan Selesai, tapi BELUM direview
                    if (isProvider) {
                        // Provider melihat "Menunggu Review" atau "Selesai" (tergantung preferensi)
                        showLabel("Menunggu Review", "#FF9800", R.drawable.bg_status_active);
                    } else {
                        // Pencari Jasa melihat Tombol Review
                        tvStatusLabel.setVisibility(View.GONE);
                        btnAction.setVisibility(View.VISIBLE);
                        btnAction.setText("Beri Penilaian");

                        btnAction.setOnClickListener(v -> {
                            Intent intent = new Intent(context, RateOrderActivity.class);
                            intent.putExtra("ORDER_ID", order.getOrderId());
                            intent.putExtra("SERVICE_ID", order.getServiceId());

                            if (rateOrderLauncher != null) {
                                rateOrderLauncher.launch(intent);
                            } else {
                                context.startActivity(intent);
                            }
                        });
                    }
                    break;

                case Constants.ORDER_STATUS_REVIEWED:
                    // Status 'reviewed' artinya SUDAH BERES TOTAL
                    showLabel("Selesai", "#111D5E", R.drawable.bg_status_light_blue);
                    break;

                case Constants.ORDER_STATUS_CANCELLED:
                    showLabel("Dibatalkan", "#F44336", R.drawable.bg_status_active);
                    break;
            }
        }

        private void showLabel(String text, String colorHex, int bgRes) {
            tvStatusLabel.setText(text);
            tvStatusLabel.setTextColor(Color.parseColor(colorHex));
            tvStatusLabel.setBackgroundResource(bgRes);
        }
    }
}