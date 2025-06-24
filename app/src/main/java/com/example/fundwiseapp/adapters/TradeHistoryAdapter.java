package com.example.fundwiseapp.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.Trade;

import java.text.SimpleDateFormat;
import java.util.*;

public class TradeHistoryAdapter extends RecyclerView.Adapter<TradeHistoryAdapter.TradeViewHolder> {

    private final List<Trade> tradeList;

    public TradeHistoryAdapter(List<Trade> tradeList) {
        this.tradeList = tradeList;
    }

    @NonNull
    @Override
    public TradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trade_history, parent, false);
        return new TradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TradeViewHolder holder, int position) {
        Trade trade = tradeList.get(position);

        holder.symbolText.setText(trade.getSymbol());
        holder.quantityText.setText("Qty: " + trade.getQuantity());
        holder.priceText.setText("₹" + trade.getPriceAtTrade());

        String type = trade.getType() != null ? trade.getType() : "buy"; // ✅ null safety
        holder.typeText.setText(type.toUpperCase(Locale.ROOT));

        // ✅ Time format
        String time = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(trade.getTimestamp()));
        holder.timeText.setText(time);

        // ✅ Color coding
        int color = type.equalsIgnoreCase("buy") ? 0xFF2E7D32 : 0xFFC62828;
        holder.typeText.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return tradeList.size();
    }

    static class TradeViewHolder extends RecyclerView.ViewHolder {
        TextView symbolText, typeText, quantityText, priceText, timeText;

        public TradeViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.tvSymbol);
            typeText = itemView.findViewById(R.id.tvType);
            quantityText = itemView.findViewById(R.id.tvQuantity);
            priceText = itemView.findViewById(R.id.tvPrice);
            timeText = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}