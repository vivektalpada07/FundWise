package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.PortfolioAdapter;
import com.example.fundwiseapp.api.StockApiService;
import com.example.fundwiseapp.api.StockResponse;
import com.example.fundwiseapp.models.PortfolioItem;
import com.google.firebase.database.*;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPortfolioFragment extends Fragment {

    private Spinner spinnerUsers;
    private RecyclerView recyclerView;
    private PortfolioAdapter adapter;
    private TextView tvSummary;
    private Button btnResetPortfolio, btnResetTrades, btnInjectFakeNews;

    private List<String> userIds = new ArrayList<>();
    private List<String> userEmails = new ArrayList<>();
    private List<PortfolioItem> portfolioList = new ArrayList<>();

    private String selectedUserId = null;

    private final Map<String, Double> livePrices = new HashMap<>();

    // Your stock symbols, same as before
    private final String[] symbols = {
            "RELIANCE.BSE", "TCS.BSE", "INFY.BSE", "HDFCBANK.BSE", "ICICIBANK.BSE",
            "SBIN.BSE", "KOTAKBANK.BSE", "ITC.BSE", "LT.BSE", "HINDUNILVR.BSE",
            "BHARTIARTL.BSE", "ASIANPAINT.BSE", "BAJFINANCE.BSE", "HCLTECH.BSE",
            "MARUTI.BSE", "WIPRO.BSE", "ONGC.BSE", "POWERGRID.BSE", "TITAN.BSE", "ULTRACEMCO.BSE"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_portfolio, container, false);

        spinnerUsers = view.findViewById(R.id.spinnerUsers);
        recyclerView = view.findViewById(R.id.recyclerViewAdminPortfolio);
        tvSummary = view.findViewById(R.id.tvAdminPortfolioSummary);
        btnResetPortfolio = view.findViewById(R.id.btnResetPortfolio);
        btnResetTrades = view.findViewById(R.id.btnResetTrades);
        btnInjectFakeNews = view.findViewById(R.id.btnInjectFakeNews);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PortfolioAdapter(portfolioList);
        recyclerView.setAdapter(adapter);

        loadUsers();

        spinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = userIds.get(position);
                fetchLivePricesAndLoadPortfolio(selectedUserId);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnResetPortfolio.setOnClickListener(v -> resetUserPortfolio());
        btnResetTrades.setOnClickListener(v -> resetUserTrades());
        btnInjectFakeNews.setOnClickListener(v -> injectFakeNews());

        return view;
    }

    private void loadUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIds.clear();
                userEmails.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String email = userSnap.child("email").getValue(String.class);
                    userIds.add(uid);
                    userEmails.add(email != null ? email : uid);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, userEmails);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUsers.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLivePricesAndLoadPortfolio(String userId) {
        final int totalStocks = symbols.length;
        final int[] fetchedCount = {0};
        livePrices.clear();

        StockApiService apiService = StockApiService.create();
        for (String symbol : symbols) {
            apiService.getStockQuote(symbol).enqueue(new Callback<StockResponse>() {
                @Override
                public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                    double price = 0.0;
                    if (response.isSuccessful() && response.body() != null &&
                            response.body().getGlobalQuote() != null) {
                        try {
                            price = Double.parseDouble(response.body().getGlobalQuote().getPrice());
                        } catch (Exception ignored) {}
                    }
                    livePrices.put(symbol.replace('.', '_'), price);
                    checkAllPricesFetched();
                }
                @Override
                public void onFailure(Call<StockResponse> call, Throwable t) {
                    livePrices.put(symbol.replace('.', '_'), 0.0);
                    checkAllPricesFetched();
                }
                private void checkAllPricesFetched() {
                    fetchedCount[0]++;
                    if (fetchedCount[0] == totalStocks) {
                        loadPortfolio(userId);
                    }
                }
            });
        }
    }

    private void loadPortfolio(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("portfolio");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                portfolioList.clear();
                double totalInvested = 0;
                double currentValue = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PortfolioItem item = ds.getValue(PortfolioItem.class);
                    if (item != null) {
                        String symbolKey = ds.getKey(); // eg "RELIANCE_BSE"
                        item.setSymbol(symbolKey.replace("_", "."));
                        portfolioList.add(item);

                        totalInvested += item.getAvgPrice() * item.getQuantity();

                        double livePrice = livePrices.getOrDefault(symbolKey, 0.0);
                        currentValue += livePrice * item.getQuantity();
                    }
                }
                adapter.notifyDataSetChanged();

                double netPL = currentValue - totalInvested;
                int color = netPL >= 0 ? 0xFF4CAF50 : 0xFFF44336; // green or red

                String summary = String.format(Locale.US,
                        "\u20B9 %.2f Invested\n\u20B9 %.2f Current Value\nNet P/L: \u20B9 %.2f",
                        totalInvested, currentValue, netPL);

                tvSummary.setText(summary);
                tvSummary.setTextColor(color);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load portfolio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetUserPortfolio() {
        if (selectedUserId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(selectedUserId).child("portfolio");
        ref.removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Portfolio reset for user", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to reset portfolio", Toast.LENGTH_SHORT).show());
    }

    private void resetUserTrades() {
        if (selectedUserId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("trades").child(selectedUserId);
        ref.removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Trade history reset for user", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to reset trades", Toast.LENGTH_SHORT).show());
    }

    private void injectFakeNews() {
        // Example: multiply all live prices by a random factor between 0.8 and 1.2 to simulate market effect
        double multiplier = 0.8 + Math.random() * 0.4;
        Map<String, Double> fakePrices = new HashMap<>();
        for (String symbol : livePrices.keySet()) {
            fakePrices.put(symbol, livePrices.get(symbol) * multiplier);
        }
        livePrices.clear();
        livePrices.putAll(fakePrices);

        // Reload portfolio with fake prices
        if (selectedUserId != null) {
            loadPortfolio(selectedUserId);
            Toast.makeText(getContext(), "Fake news injected! Prices adjusted by x" + String.format("%.2f", multiplier), Toast.LENGTH_SHORT).show();
        }
    }
}
