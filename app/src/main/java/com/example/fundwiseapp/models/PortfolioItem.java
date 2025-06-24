package com.example.fundwiseapp.models;

public class PortfolioItem {
    private String symbol;
    private int quantity;
    private double avgPrice;

    public PortfolioItem() {
        // Required for Firebase
    }

    public PortfolioItem(String symbol, int quantity, double avgPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }
}
