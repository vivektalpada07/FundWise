package com.example.fundwiseapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.transactions.TransactionListActivity;
import com.example.fundwiseapp.utils.TransactionUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseRef;
    EditText editAmount;
    Spinner spinnerType, spinnerDescription;
    Button btnAddTransaction, btnViewTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        String databaseUrl = "https://fundwiseapp1-default-rtdb.firebaseio.com/";
        databaseRef = FirebaseDatabase.getInstance(databaseUrl).getReference("transactions");

        editAmount = findViewById(R.id.editAmount);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerDescription = findViewById(R.id.spinnerDescription);
        btnAddTransaction = findViewById(R.id.btnOpenAddTransaction);
        btnViewTransactions = findViewById(R.id.btnViewTransactions);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.transaction_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> descriptionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.transaction_descriptions,
                android.R.layout.simple_spinner_item
        );
        descriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDescription.setAdapter(descriptionAdapter);

        btnAddTransaction.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();
            String typeStr = spinnerType.getSelectedItem().toString();
            String descriptionStr = spinnerDescription.getSelectedItem().toString();

            if (TextUtils.isEmpty(amountStr)) {
                editAmount.setError("Amount required");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                editAmount.setError("Enter a valid number");
                return;
            }

            TransactionUtils.saveTransaction(databaseRef, amount, descriptionStr, typeStr, MainActivity.this, () -> {
                Toast.makeText(MainActivity.this, "Transaction added", Toast.LENGTH_SHORT).show();
                editAmount.setText("");
                spinnerType.setSelection(0);
                spinnerDescription.setSelection(0);
            });
        });

        btnViewTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionListActivity.class);
            startActivity(intent);
        });
    }
}
