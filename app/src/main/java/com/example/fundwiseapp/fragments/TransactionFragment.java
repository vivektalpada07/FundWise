package com.example.fundwiseapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.transactions.TransactionListActivity;
import com.example.fundwiseapp.utils.TransactionUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TransactionFragment extends Fragment {

    private EditText editAmount;
    private Spinner spinnerType, spinnerDescription;
    private Button btnAddTransaction, btnViewTransactions;

    private DatabaseReference databaseRef;

    public TransactionFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        databaseRef = FirebaseDatabase.getInstance("https://fundwiseapp1-default-rtdb.firebaseio.com/")
                .getReference("transactions");

        editAmount = view.findViewById(R.id.editAmount);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerDescription = view.findViewById(R.id.spinnerDescription);
        btnAddTransaction = view.findViewById(R.id.btnOpenAddTransaction);
        btnViewTransactions = view.findViewById(R.id.btnViewTransactions);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.transaction_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> descriptionAdapter = ArrayAdapter.createFromResource(
                getContext(),
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

            TransactionUtils.saveTransaction(databaseRef, amount, descriptionStr, typeStr, getContext(), () -> {
                Toast.makeText(getContext(), "Transaction added", Toast.LENGTH_SHORT).show();
                editAmount.setText("");
                spinnerType.setSelection(0);
                spinnerDescription.setSelection(0);
            });
        });

        btnViewTransactions.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TransactionListActivity.class));
        });

        return view;
    }
}
