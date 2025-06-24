package com.example.fundwiseapp.models;

public class StockData {
    private String name;      // Display name, e.g. RELIANCE.BSE
    private String price;     // Price with ₹, e.g. ₹2400.00
    private double numericPrice; // Parsed double value of the price
    private String symbol;    // Actual symbol for trading

    public StockData(String symbol, String price) {
        this.symbol = symbol;
        this.name = symbol;
        this.price = price;

        try {
            // Remove ₹ and parse
            this.numericPrice = Double.parseDouble(price.replace("₹", "").trim());
        } catch (NumberFormatException e) {
            this.numericPrice = 0.0;
        }
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public double getNumericPrice() {
        return numericPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setPrice(String price) {
        this.price = price;
        try {
            this.numericPrice = Double.parseDouble(price.replace("₹", "").trim());
        } catch (Exception e) {
            this.numericPrice = 0.0;
        }
    }
    public void setNumericPrice(double numericPrice) {
        this.numericPrice = numericPrice;
    }
}
