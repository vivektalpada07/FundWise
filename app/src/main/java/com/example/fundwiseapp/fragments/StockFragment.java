package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.StockAdapter;
import com.example.fundwiseapp.api.StockApiService;
import com.example.fundwiseapp.api.StockResponse;
import com.example.fundwiseapp.models.StockData;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockFragment extends Fragment {

    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private final List<StockData> stockList = new ArrayList<>();

    private final String[] symbols = {
            "RELIANCE.BSE", "TCS.BSE", "INFY.BSE", "HDFCBANK.BSE", "ICICIBANK.BSE",
            "SBIN.BSE", "KOTAKBANK.BSE", "ITC.BSE", "LT.BSE", "HINDUNILVR.BSE",
            "BHARTIARTL.BSE", "ASIANPAINT.BSE", "BAJFINANCE.BSE", "HCLTECH.BSE",
            "MARUTI.BSE", "WIPRO.BSE", "ONGC.BSE", "POWERGRID.BSE", "TITAN.BSE", "ULTRACEMCO.BSE"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewStocks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StockAdapter(stockList);
        recyclerView.setAdapter(adapter);

        for (String symbol : symbols) {
            fetchStock(symbol);
        }

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
                if (quote == null || quote.getPrice() == null) {
                    return; // Skip this stock, no data available
                }
                String price = quote.getPrice();

                StockData stock = new StockData(symbol, "₹" + price);

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
}
