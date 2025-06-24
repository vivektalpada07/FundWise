package com.example.fundwiseapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.StockAdapter;
import com.example.fundwiseapp.api.StockApiService;
import com.example.fundwiseapp.api.StockResponse;
import com.example.fundwiseapp.models.StockData;
import com.example.fundwiseapp.models.Trade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockFragment extends Fragment implements StockAdapter.OnStockClickListener {

    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private final List<StockData> stockList = new ArrayList<>();

    private final String[] symbols = {
            "RELIANCE.BSE", "TCS.BSE", "INFY.BSE", "HDFCBANK.BSE", "ICICIBANK.BSE",
            "SBIN.BSE", "KOTAKBANK.BSE", "ITC.BSE", "LT.BSE", "HINDUNILVR.BSE",
            "BHARTIARTL.BSE", "ASIANPAINT.BSE", "BAJFINANCE.BSE", "HCLTECH.BSE",
            "MARUTI.BSE", "WIPRO.BSE", "ONGC.BSE", "POWERGRID.BSE", "TITAN.BSE", "ULTRACEMCO.BSE"
    };

    private final Map<String, Double> mockPrices = new HashMap<String, Double>() {{
        put("RELIANCE.BSE", 25350.0); put("TCS.BSE", 3750.5); put("INFY.BSE", 1462.8);
        put("HDFCBANK.BSE", 1610.2); put("ICICIBANK.BSE", 998.4); put("SBIN.BSE", 675.9);
        put("KOTAKBANK.BSE", 1820.3); put("ITC.BSE", 450.2); put("LT.BSE", 3580.4);
        put("HINDUNILVR.BSE", 2455.7); put("BHARTIARTL.BSE", 1080.3); put("ASIANPAINT.BSE", 3132.1);
        put("BAJFINANCE.BSE", 6852.0); put("HCLTECH.BSE", 1365.6); put("MARUTI.BSE", 10820.2);
        put("WIPRO.BSE", 540.7); put("ONGC.BSE", 214.5); put("POWERGRID.BSE", 285.4);
        put("TITAN.BSE", 3720.6); put("ULTRACEMCO.BSE", 9425.0);
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewStocks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(adapter);

        for (String symbol : symbols) {
            fetchStock(symbol);
        }

        view.findViewById(R.id.btnTradeHistory).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new TradeHistoryFragment())
                        .addToBackStack(null)
                        .commit()
        );

        view.findViewById(R.id.btnPortfolio).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new PortfolioFragment())
                        .addToBackStack(null)
                        .commit()
        );

        return view;
    }

    private void fetchStock(String symbol) {
        StockApiService apiService = StockApiService.create();
        Call<StockResponse> call = apiService.getStockQuote(symbol);

        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(@NonNull Call<StockResponse> call, @NonNull Response<StockResponse> response) {
                String priceStr = null;
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getGlobalQuote() != null) {
                    priceStr = response.body().getGlobalQuote().getPrice();
                }

                double numericPrice = mockPrices.getOrDefault(symbol, 100.0);
                if (priceStr != null) {
                    try {
                        numericPrice = Double.parseDouble(priceStr);
                    } catch (Exception ignored) {}
                }

                StockData stock = new StockData(symbol, "₹" + numericPrice);
                stock.setNumericPrice(numericPrice);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        stockList.add(stock);
                        adapter.notifyItemInserted(stockList.size() - 1);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<StockResponse> call, @NonNull Throwable t) {
                double fallback = mockPrices.getOrDefault(symbol, 100.0);
                StockData stock = new StockData(symbol, "₹" + fallback);
                stock.setNumericPrice(fallback);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        stockList.add(stock);
                        adapter.notifyItemInserted(stockList.size() - 1);
                    });
                }
            }
        });
    }

    @Override
    public void onBuyClick(StockData stock) {
        showTradeDialog(stock, "buy");
    }

    @Override
    public void onSellClick(StockData stock) {
        showTradeDialog(stock, "sell");
    }

    private void showTradeDialog(StockData stock, String type) {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter quantity");

        new AlertDialog.Builder(getContext())
                .setTitle((type.equals("buy") ? "Buy " : "Sell ") + stock.getName())
                .setView(input)
                .setPositiveButton(type.equals("buy") ? "Buy" : "Sell", (dialog, which) -> {
                    String qtyStr = input.getText().toString().trim();
                    if (qtyStr.isEmpty()) {
                        Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int quantity = Integer.parseInt(qtyStr);
                    if (type.equals("buy")) {
                        saveTrade(stock, quantity, "buy");
                        updatePortfolio(stock, quantity, true);
                    } else {
                        checkAndSellStock(stock, quantity);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkAndSellStock(StockData stock, int quantity) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String symbolKey = stock.getSymbol().replace(".", "_");

        DatabaseReference portRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("portfolio")
                .child(symbolKey);

        portRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer ownedQty = snapshot.child("quantity").getValue(Integer.class);
                if (ownedQty == null || ownedQty < quantity) {
                    Toast.makeText(getContext(), "❌ Not enough shares to sell", Toast.LENGTH_SHORT).show();
                } else {
                    saveTrade(stock, quantity, "sell");

                    int remaining = ownedQty - quantity;
                    if (remaining == 0) {
                        portRef.removeValue();
                    } else {
                        portRef.child("quantity").setValue(remaining);
                        // Keep avgPrice unchanged
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "❌ Failed to access portfolio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePortfolio(StockData stock, int buyQty, boolean isBuy) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String symbolKey = stock.getSymbol().replace(".", "_");
        double currentPrice = stock.getNumericPrice();

        DatabaseReference portRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("portfolio")
                .child(symbolKey);

        portRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int existingQty = 0;
                double existingAvg = 0.0;

                if (snapshot.exists()) {
                    Integer q = snapshot.child("quantity").getValue(Integer.class);
                    Double avg = snapshot.child("avgPrice").getValue(Double.class);
                    if (q != null) existingQty = q;
                    if (avg != null) existingAvg = avg;
                }

                int newQty = existingQty + buyQty;
                double newAvgPrice = ((existingQty * existingAvg) + (buyQty * currentPrice)) / newQty;

                Map<String, Object> update = new HashMap<>();
                update.put("symbol", symbolKey);
                update.put("quantity", newQty);
                update.put("avgPrice", newAvgPrice);

                portRef.setValue(update)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(getContext(), "✅ Portfolio updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "❌ Failed to update portfolio", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "❌ Firebase error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTrade(StockData stock, int quantity, String type) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference tradeRef = FirebaseDatabase.getInstance().getReference("trades");
        String tradeId = tradeRef.child(userId).push().getKey();

        Trade trade = new Trade(userId, stock.getSymbol(), stock.getName(),
                stock.getNumericPrice(), quantity, System.currentTimeMillis(), type);

        tradeRef.child(userId).child(tradeId).setValue(trade)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "✅ " + type + " completed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "❌ Trade failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
