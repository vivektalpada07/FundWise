package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.PortfolioAdapter;
import com.example.fundwiseapp.api.StockApiService;
import com.example.fundwiseapp.api.StockResponse;
import com.example.fundwiseapp.models.PortfolioItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PortfolioFragment extends Fragment {

    private RecyclerView recyclerView;
    private PortfolioAdapter adapter;
    private final List<PortfolioItem> portfolioList = new ArrayList<>();
    private TextView tvSummary;
    private Button btnRefresh;

    // Symbols of stocks you want live prices for
    private final String[] symbols = {
            "RELIANCE.BSE", "TCS.BSE", "INFY.BSE", "HDFCBANK.BSE", "ICICIBANK.BSE",
            "SBIN.BSE", "KOTAKBANK.BSE", "ITC.BSE", "LT.BSE", "HINDUNILVR.BSE",
            "BHARTIARTL.BSE", "ASIANPAINT.BSE", "BAJFINANCE.BSE", "HCLTECH.BSE",
            "MARUTI.BSE", "WIPRO.BSE", "ONGC.BSE", "POWERGRID.BSE", "TITAN.BSE", "ULTRACEMCO.BSE"
    };

    // Store live prices fetched from API
    private final Map<String, Double> livePrices = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPortfolio);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tvSummary = view.findViewById(R.id.tvPortfolioSummary); // Make sure you added this TextView in XML
        btnRefresh = view.findViewById(R.id.btnRefreshPortfolio); // Make sure you added this Button in XML

        adapter = new PortfolioAdapter(portfolioList);
        recyclerView.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Refreshing portfolio...", Toast.LENGTH_SHORT).show();
            fetchLivePricesAndLoadPortfolio();
        });

        // Load portfolio initially
        fetchLivePricesAndLoadPortfolio();

        return view;
    }

    private void fetchLivePricesAndLoadPortfolio() {
        final int totalStocks = symbols.length;
        final int[] fetchedCount = {0};
        livePrices.clear();

        StockApiService apiService = StockApiService.create();

        for (String symbol : symbols) {
            apiService.getStockQuote(symbol).enqueue(new Callback<StockResponse>() {
                @Override
                public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                    double price = 0.0;
                    if (response.isSuccessful() && response.body() != null
                            && response.body().getGlobalQuote() != null) {
                        try {
                            price = Double.parseDouble(response.body().getGlobalQuote().getPrice());
                        } catch (Exception ignored) { }
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
                        loadPortfolio();
                    }
                }
            });
        }
    }

    private void loadPortfolio() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
}
