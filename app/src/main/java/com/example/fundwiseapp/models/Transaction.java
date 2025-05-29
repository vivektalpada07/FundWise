package com.example.fundwiseapp.models;

public class Transaction {
    private String id;
    private double amount;
    private String description;
    private long timestamp;

    // Empty constructor for Firestore
    public Transaction() {}

    public Transaction(String id, double amount, String description, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
