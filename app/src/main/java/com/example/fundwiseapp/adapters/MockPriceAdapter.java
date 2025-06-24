package com.example.fundwiseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class MockPriceAdapter extends RecyclerView.Adapter<MockPriceAdapter.MockViewHolder> {
    private final List<String> stockList;
    private final Map<String, Double> priceMap;

    public MockPriceAdapter(List<String> stockList, Map<String, Double> priceMap) {
        this.stockList = stockList;
        this.priceMap = priceMap;
    }

    @NonNull
    @Override
    public MockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new MockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockViewHolder holder, int position) {
        String symbol = stockList.get(position);
        holder.title.setText(symbol.replace("_", "."));
        holder.subtitle.setText("₹" + priceMap.getOrDefault(symbol, 0.0));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    static class MockViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;

        MockViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}

