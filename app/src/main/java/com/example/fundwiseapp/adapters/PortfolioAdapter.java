package com.example.fundwiseapp.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.PortfolioItem;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    private final List<PortfolioItem> portfolioList;

    public PortfolioAdapter(List<PortfolioItem> portfolioList) {
        this.portfolioList = portfolioList;
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_portfolio, parent, false);
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        PortfolioItem item = portfolioList.get(position);
        holder.tvSymbol.setText(item.getSymbol());
        holder.tvQuantity.setText("Qty: " + item.getQuantity());
        holder.tvAvgPrice.setText("Avg ₹" + item.getAvgPrice());
    }

    @Override
    public int getItemCount() {
        return portfolioList.size();
    }

    static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol, tvQuantity, tvAvgPrice;

        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.tvPortfolioSymbol);
            tvQuantity = itemView.findViewById(R.id.tvPortfolioQuantity);
            tvAvgPrice = itemView.findViewById(R.id.tvPortfolioAvgPrice);
        }
    }
}
