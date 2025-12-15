package com.example.temuskill.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<String> categories;
    private Context context;
    private int selectedPosition = 0; // Default terpilih index 0 ("Semua")
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category, int position);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan layout item_category_chip.xml yang sudah dibuat sebelumnya
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);

        // Ubah warna jika item ini sedang dipilih
        if (selectedPosition == position) {
            holder.tvCategoryName.setBackgroundResource(R.drawable.bg_chip_active);
            holder.tvCategoryName.setTextColor(Color.WHITE);
        } else {
            holder.tvCategoryName.setBackgroundResource(R.drawable.bg_chip_inactive);
            holder.tvCategoryName.setTextColor(Color.parseColor("#757575"));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPos);
            notifyItemChanged(selectedPosition);

            listener.onCategoryClick(category, selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pastikan ID ini sesuai dengan yang ada di item_category_chip.xml
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}