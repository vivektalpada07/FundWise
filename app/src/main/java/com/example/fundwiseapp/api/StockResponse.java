package com.example.fundwiseapp.api;

import com.google.gson.annotations.SerializedName;

public class StockResponse {

    @SerializedName("Global Quote")
    private GlobalQuote globalQuote;

    public GlobalQuote getGlobalQuote() {
        return globalQuote;
    }

    public static class GlobalQuote {
        @SerializedName("01. symbol")
        private String symbol;

        @SerializedName("05. price")
        private String price;

        public String getSymbol() {
            return symbol;
        }

        public String getPrice() {
            return price;
        }
    }
}
