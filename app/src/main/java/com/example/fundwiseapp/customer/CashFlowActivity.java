package com.example.fundwiseapp.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.utils.TransactionUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CashFlowActivity extends AppCompatActivity {

    private DatabaseReference transactionsRef;
    private EditText etAmount, etDescription;
    private Button btnAddTransaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_flow);

        FirebaseApp.initializeApp(this);
        String databaseUrl = "https://fundwiseapp1-default-rtdb.firebaseio.com/";
        transactionsRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("transactions");

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);

        btnAddTransaction.setOnClickListener(v -> addTransaction());
    }

    private void addTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Enter a valid number");
            return;
        }

        TransactionUtils.saveTransaction(transactionsRef, amount, description, "cashflow", this, () -> {
            etAmount.setText("");
            etDescription.setText("");
        });
    }
}
