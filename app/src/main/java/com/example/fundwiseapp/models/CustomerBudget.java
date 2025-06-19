package com.example.fundwiseapp.models;

public class CustomerBudget {
    private String userId;
    private String email;
    private double totalIncome;
    private double totalExpense;

    public CustomerBudget(String userId, String email, double totalIncome, double totalExpense) {
        this.userId = userId;
        this.email = email;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;

    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public double getSavings() { return totalIncome - totalExpense; }
}
