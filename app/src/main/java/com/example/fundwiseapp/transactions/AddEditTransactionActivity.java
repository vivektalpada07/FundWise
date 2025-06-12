package com.example.fundwiseapp.transactions;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class AddEditTransactionActivity extends AppCompatActivity {

    private EditText editAmount;
    private RadioGroup radioGroupType;
    private Spinner spinnerDescription;
    private Button btnSave, btnDelete;

    private DatabaseReference transactionsRef;

    private String transactionId; // For editing existing transaction

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_transaction);

        editAmount = findViewById(R.id.editAmount);
        radioGroupType = findViewById(R.id.radioGroupType);
        spinnerDescription = findViewById(R.id.spinnerDescription);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // Setup description dropdown spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_descriptions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDescription.setAdapter(adapter);

        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");

        transactionId = getIntent().getStringExtra("transactionId");
        if (transactionId != null) {
            loadTransaction(transactionId);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> saveTransaction());
        btnDelete.setOnClickListener(v -> deleteTransaction());
    }

    private void loadTransaction(String id) {
        transactionsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Transaction transaction = snapshot.getValue(Transaction.class);
                if (transaction != null) {
                    editAmount.setText(String.valueOf(transaction.getAmount()));

                    // Safely set radio button for type
                    String type = transaction.getType();
                    if ("Income".equalsIgnoreCase(type)) {
                        radioGroupType.check(R.id.radioIncome);
                    } else {
                        radioGroupType.check(R.id.radioExpense);
                    }

                    // Set spinner description safely
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerDescription.getAdapter();
                    int position = adapter.getPosition(transaction.getDescription());
                    if (position >= 0) {
                        spinnerDescription.setSelection(position);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddEditTransactionActivity.this, "Failed to load transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTransaction() {
        String amountStr = editAmount.getText().toString().trim();
        int selectedTypeId = radioGroupType.getCheckedRadioButtonId();
        String description = spinnerDescription.getSelectedItem().toString();

        if (TextUtils.isEmpty(amountStr)) {
            editAmount.setError("Amount required");
            return;
        }
        if (selectedTypeId == -1) {
            Toast.makeText(this, "Select transaction type", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            editAmount.setError("Enter a valid number");
            return;
        }

        RadioButton selectedRadio = findViewById(selectedTypeId);
        String type = selectedRadio.getText().toString();

        if (transactionId == null) {
            transactionId = transactionsRef.push().getKey();
        }

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());


        transactionsRef.child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddEditTransactionActivity.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddEditTransactionActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show());
    }

    private void deleteTransaction() {
        if (transactionId != null) {
            transactionsRef.child(transactionId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddEditTransactionActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddEditTransactionActivity.this, "Failed to delete transaction", Toast.LENGTH_SHORT).show());
        }
    }
}
