package com.example.fundwiseapp.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fundwiseapp.R;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {
    private final List<String> alerts;

    public AlertAdapter(List<String> alerts) {
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertAdapter.ViewHolder holder, int position) {
        holder.textView.setText(alerts.get(position));
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
