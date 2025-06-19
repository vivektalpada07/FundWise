package com.example.fundwiseapp.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class CustomerReportActivity extends AppCompatActivity {

    private TextView tvIncome, tvExpense, tvSavings, tvEligibility;
    private EditText etLoanAmount;
    private Button btnCheckEligibility;

    private double totalIncome = 0;
    private double totalExpense = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_report);

        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvSavings = findViewById(R.id.tvSavings);
        tvEligibility = findViewById(R.id.tvEligibility);
        etLoanAmount = findViewById(R.id.etLoanAmount);
        btnCheckEligibility = findViewById(R.id.btnCheckEligibility);

        loadTransactions();

        btnCheckEligibility.setOnClickListener(v -> checkEligibility());
    }

    private void loadTransactions() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("transactions");

        ref.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String type = snap.child("description").getValue(String.class);
                            Double amount = snap.child("amount").getValue(Double.class);

                            if (amount != null && type != null) {
                                if (type.toLowerCase().contains("income")) {
                                    totalIncome += amount;
                                } else if (type.toLowerCase().contains("expense")) {
                                    totalExpense += amount;
                                }
                            }
                        }

                        double savings = totalIncome - totalExpense;

                        tvIncome.setText("₹" + totalIncome);
                        tvExpense.setText("₹" + totalExpense);
                        tvSavings.setText("₹" + savings);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CustomerReportActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkEligibility() {
        String loanAmountStr = etLoanAmount.getText().toString().trim();

        if (TextUtils.isEmpty(loanAmountStr)) {
            etLoanAmount.setError("Enter loan amount");
            return;
        }

        double loanAmount;
        try {
            loanAmount = Double.parseDouble(loanAmountStr);
        } catch (NumberFormatException e) {
            etLoanAmount.setError("Invalid number");
            return;
        }

        double savings = totalIncome - totalExpense;

        if (savings >= loanAmount) {
            tvEligibility.setText("✅ Eligible");
        } else {
            tvEligibility.setText("❌ Not Eligible");
        }
    }
}
