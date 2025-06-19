package com.example.fundwiseapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockApiService {

    @GET("query?function=GLOBAL_QUOTE&apikey=HPPBOCDMYNY3SOJG")
    Call<StockResponse> getStockQuote(@Query("symbol") String symbol);

    static StockApiService create() {
        return ApiClient.getClient().create(StockApiService.class);
    }
}
