package com.example.temuskill.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import java.util.List;

public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.ViewHolder> {

    // Model Data Sederhana
    public static class RoleItem {
        int imageRes;
        String title, desc, btnText, roleKey;

        public RoleItem(int imageRes, String title, String desc, String btnText, String roleKey) {
            this.imageRes = imageRes;
            this.title = title;
            this.desc = desc;
            this.btnText = btnText;
            this.roleKey = roleKey; // "pencari_jasa" atau "penyedia_jasa"
        }
    }

    private List<RoleItem> items;
    private OnRoleSelectedListener listener;

    public interface OnRoleSelectedListener {
        void onSelect(String roleKey);
    }

    public RoleAdapter(List<RoleItem> items, OnRoleSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoleItem item = items.get(position);
        holder.ivImage.setImageResource(item.imageRes);
        holder.tvTitle.setText(item.title);
        holder.tvDesc.setText(item.desc);
        holder.btnSelect.setText(item.btnText);

        holder.btnSelect.setOnClickListener(v -> listener.onSelect(item.roleKey));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDesc;
        Button btnSelect;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_role_image);
            tvTitle = itemView.findViewById(R.id.tv_role_title);
            tvDesc = itemView.findViewById(R.id.tv_role_desc);
            btnSelect = itemView.findViewById(R.id.btn_select_role);
        }
    }
}