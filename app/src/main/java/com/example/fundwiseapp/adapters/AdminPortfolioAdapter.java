package com.example.fundwiseapp.adapters;

import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;

import java.util.List;

public class AdminPortfolioAdapter extends RecyclerView.Adapter<AdminPortfolioAdapter.ViewHolder> {

    private final List<String> portfolioDisplayList;

    public AdminPortfolioAdapter(List<String> portfolioDisplayList) {
        this.portfolioDisplayList = portfolioDisplayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(portfolioDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return portfolioDisplayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tvAdminPortfolioItem);
        }
    }
}