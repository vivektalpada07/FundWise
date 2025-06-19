package com.example.fundwiseapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AlphaVantageApi {
    @GET("query?function=GLOBAL_QUOTE")
    Call<StockResponse> getStockQuote(
            @Query("symbol") String symbol,
            @Query("apikey") String apiKey
    );
}
