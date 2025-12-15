package com.example.temuskill.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temuskill.R;
import java.util.List;

public class EditGalleryAdapter extends RecyclerView.Adapter<EditGalleryAdapter.ViewHolder> {

    // Kita simpan Object agar bisa menampung String (URL lama) atau Uri (Foto baru)
    private List<Object> galleryItems;
    private OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public EditGalleryAdapter(List<Object> galleryItems, OnDeleteListener listener) {
        this.galleryItems = galleryItems;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = galleryItems.get(position);

        // Load gambar (bisa dari URL String atau URI Lokal)
        Glide.with(holder.itemView.getContext()).load(item).centerCrop().into(holder.ivImage);

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() { return galleryItems.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}