package com.example.fundwiseapp.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TransactionUtils {

    public static void saveTransaction(DatabaseReference databaseRef,
                                       double amount,
                                       String description,
                                       String type, // pass null if unknown
                                       Context context,
                                       Runnable onSuccessCallback) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        long timestamp = System.currentTimeMillis();
        String resolvedType = type != null && !type.isEmpty() ? type : inferType(amount);
        String category = autoCategorize(description);

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", amount);
        transaction.put("description", description);
        transaction.put("type", resolvedType);         // income/expense
        transaction.put("category", category);         // auto-generated
        transaction.put("timestamp", timestamp);
        transaction.put("userId", uid);

        databaseRef.push().setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "✅ Transaction added!", Toast.LENGTH_SHORT).show();
                    if (onSuccessCallback != null) onSuccessCallback.run();
                })
                .addOnFailureListener(e -> Toast.makeText(context,
                        "❌ Failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    // Inference if type is missing
    private static String inferType(double amount) {
        return amount >= 0 ? "income" : "expense";
    }

    // Auto-categorize based on description
    public static String autoCategorize(String description) {
        if (description == null) return "Other";
        String desc = description.toLowerCase(Locale.ROOT);

        if (desc.contains("salary") || desc.contains("bonus")) return "Income";
        if (desc.contains("rent") || desc.contains("house") || desc.contains("flat")) return "Housing";
        if (desc.contains("uber") || desc.contains("taxi") || desc.contains("bus")) return "Transport";
        if (desc.contains("food") || desc.contains("grocery") || desc.contains("restaurant")) return "Food";
        if (desc.contains("movie") || desc.contains("netflix") || desc.contains("fun")) return "Entertainment";
        if (desc.contains("school") || desc.contains("book") || desc.contains("course")) return "Education";

        return "Other";
    }
}