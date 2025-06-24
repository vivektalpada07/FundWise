package com.example.fundwiseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.StockData;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final List<StockData> stockList;
    private final OnStockClickListener clickListener;

    // Constructor
    public StockAdapter(List<StockData> stockList, OnStockClickListener clickListener) {
        this.stockList = stockList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockData stock = stockList.get(position);
        holder.nameTextView.setText(stock.getName());
        holder.priceTextView.setText(stock.getPrice());

        holder.btnBuy.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onBuyClick(stock);
        });

        holder.btnSell.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onSellClick(stock);
        });
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;
        Button btnBuy, btnSell;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvStockName);
            priceTextView = itemView.findViewById(R.id.tvStockPrice);
            btnBuy = itemView.findViewById(R.id.btnBuyStock);
            btnSell = itemView.findViewById(R.id.btnSellStock);
        }
    }

    public interface OnStockClickListener {
        void onBuyClick(StockData stock);
        void onSellClick(StockData stock);
    }
}