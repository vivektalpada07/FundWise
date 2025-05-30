package com.example.fundwiseapp.models;

public class Transaction {
    private String id;
    private double amount;
    private String description;
    private String type;  // Income or Expense
    private long timestamp;

    public Transaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() {
        return type != null ? type : "Expense"; // Default to Expense if null
    }

    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
