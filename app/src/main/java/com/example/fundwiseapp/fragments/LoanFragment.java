package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoanFragment extends Fragment {

    private EditText etLoanAmount;
    private TextView tvIncome, tvExpenses, tvEligibilityResult;
    private Button btnCheck;
    private double totalIncome = 0;
    private double totalExpenses = 0;

    private final DatabaseReference transactionRef =
            FirebaseDatabase.getInstance().getReference("transactions");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_loan, container, false);

        etLoanAmount = view.findViewById(R.id.etLoanAmount);
        tvIncome = view.findViewById(R.id.tvTotalIncome);
        tvExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvEligibilityResult = view.findViewById(R.id.tvEligibilityResult);
        btnCheck = view.findViewById(R.id.btnCheckLoanEligibility);

        fetchTransactionTotals();

        btnCheck.setOnClickListener(v -> checkEligibility());

        return view;
    }

    private void fetchTransactionTotals() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalIncome = 0;
                totalExpenses = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t != null && currentUserId.equals(t.getUserId())) {
                        if ("Income".equalsIgnoreCase(t.getType())) {
                            totalIncome += t.getAmount();
                        } else {
                            totalExpenses += t.getAmount();
                        }
                    }
                }


                tvIncome.setText("Total Income: ₹" + totalIncome);
                tvExpenses.setText("Total Expenses: ₹" + totalExpenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkEligibility() {
        String loanStr = etLoanAmount.getText().toString().trim();

        if (loanStr.isEmpty()) {
            etLoanAmount.setError("Please enter the loan amount.");
            return;
        }

        double loanAmount;
        try {
            loanAmount = Double.parseDouble(loanStr);
        } catch (NumberFormatException e) {
            etLoanAmount.setError("Invalid number format.");
            return;
        }

        if (totalIncome <= 0) {
            tvEligibilityResult.setText("⚠️ Please enter some income transactions first.");
            tvEligibilityResult.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            return;
        }

        double savings = totalIncome - totalExpenses;
        double ratio = (savings / totalIncome) * 100;
        double maxLoan = savings * 10;

        String msg = "📊 Your Financial Summary:\n"
                + "🪙 Income: ₹" + totalIncome + "\n"
                + "💸 Expenses: ₹" + totalExpenses + "\n"
                + "💰 Savings: ₹" + savings + " (" + String.format("%.2f", ratio) + "%)\n"
                + "📝 Requested Loan: ₹" + loanAmount + "\n\n";

        if (ratio >= 30) {
            msg += "🎉 Congratulations! You're eligible for the loan. 🎯\n"
                    + "You're managing your money like a pro! 🚀\n";
            tvEligibilityResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (ratio >= 15) {
            msg += "⚠️ You're partially eligible for a loan.\n"
                    + "Try cutting down a few expenses or boosting your income. 📈\n";
            tvEligibilityResult.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            msg += "❌ Sorry! You're not eligible right now. 😞\n"
                    + "Start saving more. Your future self will thank you! 💪\n";
            tvEligibilityResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        msg += "\n😂 Taanu pagal hai 🤪";
        tvEligibilityResult.setText(msg);
    }

}