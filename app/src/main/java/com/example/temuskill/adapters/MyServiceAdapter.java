package com.example.temuskill.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.temuskill.R;
import com.example.temuskill.models.Service;
import java.util.List;

public class MyServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADD = 1;
    private List<Service> services;
    private OnAddClickListener listener;

    public interface OnAddClickListener {
        void onAddClick();
    }

    public MyServiceAdapter(List<Service> services, OnAddClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == services.size()) ? TYPE_ADD : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_service, parent, false);
            return new ServiceViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_service, parent, false);
            return new AddViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ServiceViewHolder) {
            Service service = services.get(position);
            ((ServiceViewHolder) holder).bind(service);
        } else if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).itemView.setOnClickListener(v -> listener.onAddClick());
        }
    }

    @Override
    public int getItemCount() {
        return services.size() + 1;
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDetails, tvCount;
        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDetails = itemView.findViewById(R.id.tv_details);
            tvCount = itemView.findViewById(R.id.tv_count);
        }
        void bind(Service service) {
            tvCategory.setText(service.getCategory());
            tvDetails.setText(service.getDetails());
            tvCount.setText(service.getOrderCount() + "+ Pesanan");
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) { super(itemView); }
    }
}