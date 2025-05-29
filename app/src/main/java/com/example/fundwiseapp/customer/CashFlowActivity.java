package com.example.fundwiseapp.customer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.models.Transaction;
import com.example.fundwiseapp.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class CashFlowActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference transactionsRef;

    private EditText etAmount, etDescription;
    private Button btnAddTransaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_flow);

        db = FirebaseFirestore.getInstance();
        transactionsRef = db.collection("transactions");

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);

        btnAddTransaction.setOnClickListener(v -> addTransaction());
    }

    private void addTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (amountStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter amount and description", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                amount,
                description,
                System.currentTimeMillis()
        );

        transactionsRef.document(transaction.getId())
                .set(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Transaction added!", Toast.LENGTH_SHORT).show();
                    etAmount.setText("");
                    etDescription.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
