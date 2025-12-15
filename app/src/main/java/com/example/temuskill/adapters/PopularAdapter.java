package com.example.temuskill.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder> {

    public static class BannerItem {
        String title, desc, tag, colorHex;
        public BannerItem(String title, String desc, String tag, String colorHex) {
            this.title = title; this.desc = desc; this.tag = tag; this.colorHex = colorHex;
        }
    }

    private List<BannerItem> items;
    public PopularAdapter(List<BannerItem> items) { this.items = items; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BannerItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvDesc.setText(item.desc);
        holder.tvTag.setText("★ " + item.tag);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTag;
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvTag = itemView.findViewById(R.id.tv_tag);
        }
    }
}