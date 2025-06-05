package com.example.fundwiseapp.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoanRequestActivity extends AppCompatActivity {

    private EditText etLoanAmount;
    private Button btnCheckEligibility;
    private TextView tvEligibilityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_request);

        etLoanAmount = findViewById(R.id.etLoanAmount);
        btnCheckEligibility = findViewById(R.id.btnCheckEligibility);
        tvEligibilityResult = findViewById(R.id.tvEligibilityResult);

        btnCheckEligibility.setOnClickListener(v -> checkEligibility());
    }

    private void checkEligibility() {
        String loanAmountStr = etLoanAmount.getText().toString().trim();

        if (TextUtils.isEmpty(loanAmountStr)) {
            etLoanAmount.setError("Please enter loan amount");
            return;
        }

        double loanAmount;
        try {
            loanAmount = Double.parseDouble(loanAmountStr);
        } catch (NumberFormatException e) {
            etLoanAmount.setError("Enter valid number");
            return;
        }

        // Fetch transactions
        FirebaseDatabase.getInstance().getReference("transactions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        double totalIncome = 0;
                        double totalExpenses = 0;

                        for (DataSnapshot data : snapshot.getChildren()) {
                            String type = data.child("type").getValue(String.class);
                            Double amount = data.child("amount").getValue(Double.class);

                            if (type != null && amount != null) {
                                if (type.equalsIgnoreCase("Income")) {
                                    totalIncome += amount;
                                } else if (type.equalsIgnoreCase("Expense")) {
                                    totalExpenses += amount;
                                }
                            }
                        }

                        showEligibility(totalIncome, totalExpenses, loanAmount);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(LoanRequestActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEligibility(double income, double expenses, double loanAmount) {
        double savings = income - expenses;
        double savingsRatio = (income > 0) ? (savings / income) * 100 : 0;

        StringBuilder result = new StringBuilder();

        if (savings <= 0) {
            result.append("❌ Not Eligible: No savings.\n");
        } else if (savingsRatio >= 30 && savings >= loanAmount) {
            result.append("✅ Eligible for Loan!\n");
        } else if (savingsRatio >= 15) {
            result.append("⚠️ Partially Eligible. Try reducing expenses.\n");
        } else {
            result.append("❌ Not Eligible. Improve savings or income.\n");
        }

        result.append("\n📊 Income: ₹").append(income)
                .append("\n💸 Expenses: ₹").append(expenses)
                .append("\n💰 Savings: ₹").append(savings)
                .append("\n🔁 Savings Ratio: ").append(String.format("%.2f", savingsRatio)).append("%");

        tvEligibilityResult.setText(result.toString());
    }
}
