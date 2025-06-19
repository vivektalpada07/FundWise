package com.example.fundwiseapp.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.StockData;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final List<StockData> stockList;

    public StockAdapter(List<StockData> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockData stock = stockList.get(position);
        holder.nameTextView.setText(stock.getName());
        holder.priceTextView.setText(stock.getPrice());
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvStockName);
            priceTextView = itemView.findViewById(R.id.tvStockPrice);
        }
    }
}
