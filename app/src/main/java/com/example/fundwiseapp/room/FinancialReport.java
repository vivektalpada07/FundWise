package com.example.fundwiseapp.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "financial_reports")
public class FinancialReport {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String userId;
    private double totalIncome;
    private double totalExpense;
    private double savings;
    private long timestamp;

    public FinancialReport(String userId, double totalIncome, double totalExpense, double savings, long timestamp) {
        this.userId = userId;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.savings = savings;
        this.timestamp = timestamp;
    }

    public FinancialReport() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public double getSavings() { return savings; }
    public void setSavings(double savings) { this.savings = savings; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
