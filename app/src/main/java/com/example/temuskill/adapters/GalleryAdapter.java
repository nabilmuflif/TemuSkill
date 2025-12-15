package com.example.temuskill.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private List<String> urls;

    public GalleryAdapter(List<String> urls) {
        this.urls = urls;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)); // Tinggi fix
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setPadding(8,8,8,8);
        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = urls.get(position);
        Glide.with(holder.itemView.getContext()).load(url).into((ImageView) holder.itemView);
    }

    @Override
    public int getItemCount() { return urls.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) { super(itemView); }
    }
}