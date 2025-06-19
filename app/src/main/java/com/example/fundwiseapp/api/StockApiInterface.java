package com.example.fundwiseapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockApiInterface {
    @GET("query")
    Call<StockResponse> getStockQuote(
            @Query("function") String function,
            @Query("symbol") String symbol,
            @Query("apikey") String apiKey
    );
}
