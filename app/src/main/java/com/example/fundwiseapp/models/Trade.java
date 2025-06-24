package com.example.fundwiseapp.models;

public class Trade {
    private String userId;
    private String symbol;
    private String stockName;
    private double priceAtTrade;
    private int quantity;
    private long timestamp;
    private String type; // "buy" or "sell"

    // ✅ Required for Firebase
    public Trade() {}

    // ✅ Constructor
    public Trade(String userId, String symbol, String stockName,
                 double priceAtTrade, int quantity, long timestamp, String type) {
        this.userId = userId;
        this.symbol = symbol;
        this.stockName = stockName;
        this.priceAtTrade = priceAtTrade;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.type = type != null ? type : "buy"; // Null-safe fallback
    }

    // ✅ Getters
    public String getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getStockName() {
        return stockName;
    }

    public double getPriceAtTrade() {
        return priceAtTrade;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type != null ? type : "buy"; // Prevent null pointer exception
    }

    // ✅ Setters (needed by Firebase)
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public void setPriceAtTrade(double priceAtTrade) {
        this.priceAtTrade = priceAtTrade;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(String type) {
        this.type = type;
    }
}