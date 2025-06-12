package com.example.fundwiseapp.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class TransactionUtils {

    public static void saveTransaction(DatabaseReference databaseRef,
                                       double amount,
                                       String description,
                                       String type,
                                       Context context,
                                       Runnable onSuccessCallback) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", amount);
        transaction.put("description", description);
        transaction.put("timestamp", System.currentTimeMillis());
        transaction.put("userId", currentUser.getUid());

        if (type != null && !type.isEmpty()) {
            transaction.put("type", type);
        }

        databaseRef.push().setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Transaction added!", Toast.LENGTH_SHORT).show();
                    if (onSuccessCallback != null) onSuccessCallback.run();
                })
                .addOnFailureListener(e -> Toast.makeText(context,
                        "Failed to add transaction: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }
}
