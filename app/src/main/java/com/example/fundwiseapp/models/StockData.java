package com.example.fundwiseapp.models;

public class StockData {
    private final String name;
    private final String price;

    public StockData(String name, String price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }
}
