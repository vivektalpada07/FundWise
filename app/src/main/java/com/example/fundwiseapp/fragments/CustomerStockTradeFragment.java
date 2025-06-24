package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerStockTradeFragment extends Fragment {

    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private List<StockData> stockList = new ArrayList<>();

    private EditText etQuantity;
    private Button btnTrade;
    private TextView tvSelectedStock, tvWallet;
    private String selectedSymbol = null;
    private double currentPrice = 0;
    private String currentAction = ""; // buy or sell

    private DatabaseReference usersRef, tradesRef;
    private String userId;
    private double walletBalance = 0;

    private final String[] symbols = {
            "RELIANCE.BSE", "TCS.BSE", "INFY.BSE", "HDFCBANK.BSE", "ICICIBANK.BSE"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_stock_trade, container, false);

        recyclerView = view.findViewById(R.id.recyclerTradeStocks);
        etQuantity = view.findViewById(R.id.etQuantity);
        btnTrade = view.findViewById(R.id.btnBuyStock); // Shared for buy/sell
        tvSelectedStock = view.findViewById(R.id.tvSelectedStock);
        tvWallet = view.findViewById(R.id.tvWallet);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StockAdapter(stockList, new StockAdapter.OnStockClickListener() {
            @Override
            public void onBuyClick(StockData stock) {
                selectedSymbol = stock.getSymbol();
                currentPrice = stock.getNumericPrice();
                currentAction = "buy";
                tvSelectedStock.setText("Buy: " + selectedSymbol + " @ ₹" + currentPrice);
            }

            @Override
            public void onSellClick(StockData stock) {
                selectedSymbol = stock.getSymbol();
                currentPrice = stock.getNumericPrice();
                currentAction = "sell";
                tvSelectedStock.setText("Sell: " + selectedSymbol + " @ ₹" + currentPrice);
            }
        });
        recyclerView.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        tradesRef = FirebaseDatabase.getInstance().getReference("trades");

        btnTrade.setOnClickListener(v -> {
            if ("buy".equals(currentAction)) performBuy();
            else if ("sell".equals(currentAction)) performSell();
            else Toast.makeText(getContext(), "Please select a stock to trade", Toast.LENGTH_SHORT).show();
        });

        loadWallet();
        for (String symbol : symbols) fetchStock(symbol);

        return view;
    }

    private void fetchStock(String symbol) {
        StockApiService apiService = StockApiService.create();
        Call<StockResponse> call = apiService.getStockQuote(symbol);

        call.enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(@NonNull Call<StockResponse> call, @NonNull Response<StockResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                StockResponse.GlobalQuote quote = response.body().getGlobalQuote();
                if (quote == null || quote.getPrice() == null) return;

                String price = quote.getPrice();
                StockData stock = new StockData(symbol, "₹" + price);
                stock.setNumericPrice(Double.parseDouble(price));

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        stockList.add(stock);
                        adapter.notifyItemInserted(stockList.size() - 1);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<StockResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadWallet() {
        usersRef.child("wallet").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                walletBalance = snapshot.getValue(Double.class) != null ? snapshot.getValue(Double.class) : 100000.0;
                tvWallet.setText("💰 Wallet: ₹" + walletBalance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvWallet.setText("Failed to load wallet");
            }
        });
    }

    private boolean validateInput() {
        if (selectedSymbol == null) {
            Toast.makeText(getContext(), "Select a stock", Toast.LENGTH_SHORT).show();
            return false;
        }
        String qtyStr = etQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(qtyStr)) {
            etQuantity.setError("Enter quantity");
            return false;
        }
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                etQuantity.setError("Quantity must be > 0");
                return false;
            }
        } catch (Exception e) {
            etQuantity.setError("Invalid quantity");
            return false;
        }
        return true;
    }

    private void performBuy() {
        if (!validateInput()) return;
        int qty = Integer.parseInt(etQuantity.getText().toString().trim());
        double totalCost = qty * currentPrice;

        if (walletBalance < totalCost) {
            Toast.makeText(getContext(), "❌ Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        walletBalance -= totalCost;
        usersRef.child("wallet").setValue(walletBalance);
        updatePortfolio(qty, true);
        logTrade(qty, "buy");

        Toast.makeText(getContext(), "✅ Bought successfully", Toast.LENGTH_SHORT).show();
        tvWallet.setText("💰 Wallet: ₹" + walletBalance);
        etQuantity.setText("");
    }

    private void performSell() {
        if (!validateInput()) return;
        int qty = Integer.parseInt(etQuantity.getText().toString().trim());

        DatabaseReference portRef = usersRef.child("portfolio").child(selectedSymbol);
        portRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                int ownedQty = snap.child("quantity").getValue(Integer.class) != null ?
                        snap.child("quantity").getValue(Integer.class) : 0;

                if (ownedQty < qty) {
                    Toast.makeText(getContext(), "❌ Not enough stock to sell", Toast.LENGTH_SHORT).show();
                    return;
                }

                int newQty = ownedQty - qty;
                double avgPrice = snap.child("avgPrice").getValue(Double.class) != null ?
                        snap.child("avgPrice").getValue(Double.class) : 0;

                Map<String, Object> portData = new HashMap<>();
                portData.put("quantity", newQty);
                portData.put("avgPrice", avgPrice); // keep same avg price
                portRef.setValue(portData);

                walletBalance += qty * currentPrice;
                usersRef.child("wallet").setValue(walletBalance);
                tvWallet.setText("💰 Wallet: ₹" + walletBalance);

                logTrade(qty, "sell");
                Toast.makeText(getContext(), "✅ Sold successfully", Toast.LENGTH_SHORT).show();
                etQuantity.setText("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updatePortfolio(int qty, boolean isBuy) {
        DatabaseReference portRef = usersRef.child("portfolio").child(selectedSymbol);
        portRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                int existingQty = snap.child("quantity").getValue(Integer.class) != null ?
                        snap.child("quantity").getValue(Integer.class) : 0;
                double avgPrice = snap.child("avgPrice").getValue(Double.class) != null ?
                        snap.child("avgPrice").getValue(Double.class) : 0;

                int newQty = isBuy ? existingQty + qty : Math.max(0, existingQty - qty);
                double newAvgPrice = ((avgPrice * existingQty) + (currentPrice * qty)) / Math.max(1, newQty);

                Map<String, Object> portData = new HashMap<>();
                portData.put("quantity", newQty);
                portData.put("avgPrice", newAvgPrice);
                portRef.setValue(portData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void logTrade(int qty, String type) {
        String tradeId = tradesRef.push().getKey();
        Map<String, Object> trade = new HashMap<>();
        trade.put("userId", userId);
        trade.put("symbol", selectedSymbol);
        trade.put("price", currentPrice);
        trade.put("quantity", qty);
        trade.put("type", type);
        trade.put("timestamp", System.currentTimeMillis());

        if (tradeId != null)
            tradesRef.child(tradeId).setValue(trade);
    }
}