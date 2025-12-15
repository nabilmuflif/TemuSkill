package com.example.temuskill.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.activities.UserDetailActivity;
import com.example.temuskill.models.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    private List<User> users;
    private Context context;

    public AdminUserAdapter(List<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getNamaLengkap());

        // Format Tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String date = sdf.format(new Date(user.getCreatedAt()));
        holder.tvDate.setText("Bergabung: " + date);

        // Format Role
        String role = user.getRole().replace("_", " ");
        holder.tvRole.setText(role.substring(0, 1).toUpperCase() + role.substring(1));

        // LOGIKA WARNA BADGE
        if (user.getIsActive() == 1) {
            holder.tvStatus.setText("Aktif");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Hijau
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            holder.tvStatus.setText("Nonaktif");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Merah
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
        }

        // Klik Item -> Buka Detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvRole, tvStatus;
        ImageView ivAvatar;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvStatus = itemView.findViewById(R.id.tv_status_badge);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}