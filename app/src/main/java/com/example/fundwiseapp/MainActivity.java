package com.example.fundwiseapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseRef;
    EditText editAmount, editType, editDescription;
    Button btnAddTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);

        String databaseUrl = "https://fundwiseapp1-default-rtdb.firebaseio.com/";
        databaseRef = FirebaseDatabase.getInstance(databaseUrl).getReference();

        // Find views
        editAmount = findViewById(R.id.editAmount);
        editType = findViewById(R.id.editType);
        editDescription = findViewById(R.id.editDescription);
        btnAddTransaction = findViewById(R.id.btnOpenAddTransaction);

        btnAddTransaction.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();
            String typeStr = editType.getText().toString().trim();
            String descriptionStr = editDescription.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(amountStr)) {
                editAmount.setError("Amount required");
                return;
            }
            if (TextUtils.isEmpty(typeStr)) {
                editType.setError("Type required");
                return;
            }
            if (TextUtils.isEmpty(descriptionStr)) {
                editDescription.setError("Description required");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                editAmount.setError("Enter a valid number");
                return;
            }

            // Prepare transaction data
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("amount", amount);
            transaction.put("type", typeStr);
            transaction.put("description", descriptionStr);
            transaction.put("timestamp", System.currentTimeMillis());

            // Save transaction to Firebase under "transactions" node
            databaseRef.child("transactions").push().setValue(transaction)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Transaction added", Toast.LENGTH_SHORT).show();
                        // Clear inputs after success
                        editAmount.setText("");
                        editType.setText("");
                        editDescription.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, "Failed to add transaction: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }
}
