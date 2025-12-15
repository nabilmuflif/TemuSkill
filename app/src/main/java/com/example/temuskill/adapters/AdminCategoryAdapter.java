package com.example.temuskill.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.models.Category;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    private List<Category> categories;
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(Category category);
    }

    public AdminCategoryAdapter(List<Category> categories, OnDeleteClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);
        holder.tvName.setText(cat.getName());

        // Cek jika ada field deskripsi di Model Category Anda
        // Jika belum ada getter getDescription, tambahkan di Model atau hapus baris ini
        // holder.tvDesc.setText(cat.getDescription());
        holder.tvDesc.setText("Kategori Jasa Tersedia"); // Placeholder jika deskripsi null

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(cat));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_cat_name);
            tvDesc = itemView.findViewById(R.id.tv_cat_desc);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}