package com.example.fundwiseapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.ReportAdapter;
import com.example.fundwiseapp.models.Transaction;
import com.example.fundwiseapp.room.AppDatabase;
import com.example.fundwiseapp.room.FinancialReport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CustomerReportFragment extends Fragment {

    private TextView tvIncome, tvExpense, tvSavings, tvEligibility;
    private EditText etLoanAmount;
    private Button btnCheckEligibility, btnShareReport;
    private RecyclerView rvReports;
    private ReportAdapter reportAdapter;

    private double totalIncome = 0;
    private double totalExpense = 0;

    private final DatabaseReference transactionRef =
            FirebaseDatabase.getInstance().getReference("transactions");

    private String lastEligibilityMessage = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customer_report, container, false);

        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvSavings = view.findViewById(R.id.tvSavings);
        tvEligibility = view.findViewById(R.id.tvEligibility);
        etLoanAmount = view.findViewById(R.id.etLoanAmount);
        btnCheckEligibility = view.findViewById(R.id.btnCheckEligibility);
        btnShareReport = view.findViewById(R.id.btnShareReport);
        rvReports = view.findViewById(R.id.rvReports);

        rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        reportAdapter = new ReportAdapter(new ArrayList<>());
        rvReports.setAdapter(reportAdapter);

        loadTransactionSummary();

        btnCheckEligibility.setOnClickListener(v -> checkLoanEligibility());
        btnShareReport.setOnClickListener(v -> shareReport());

        loadReportsFromRoom();

        return view;
    }

    private void loadTransactionSummary() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalIncome = 0;
                totalExpense = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t != null && userId.equals(t.getUserId())) {
                        if ("Income".equalsIgnoreCase(t.getType())) {
                            totalIncome += t.getAmount();
                        } else {
                            totalExpense += t.getAmount();
                        }
                    }
                }

                double savings = totalIncome - totalExpense;

                tvIncome.setText("Income: ₹" + totalIncome);
                tvExpense.setText("Expense: ₹" + totalExpense);
                tvSavings.setText("Savings: ₹" + savings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLoanEligibility() {
        String loanStr = etLoanAmount.getText().toString().trim();
        if (TextUtils.isEmpty(loanStr)) {
            etLoanAmount.setError("Enter loan amount");
            return;
        }

        double loanAmount;
        try {
            loanAmount = Double.parseDouble(loanStr);
        } catch (NumberFormatException e) {
            etLoanAmount.setError("Invalid number");
            return;
        }

        double savings = totalIncome - totalExpense;
        double savingsRatio = (totalIncome > 0) ? (savings / totalIncome) * 100 : 0;

        StringBuilder msg = new StringBuilder();
        msg.append("📝 Requested Loan: ₹").append(loanAmount).append("\n")
                .append("💰 Savings: ₹").append(savings)
                .append(" (").append(String.format("%.2f", savingsRatio)).append("%)\n\n");

        if (savingsRatio >= 20 && loanAmount <= savings * 10) {
            msg.append("✅ Eligible for loan. Your savings and income support this amount.");
            tvEligibility.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (savingsRatio >= 10 && loanAmount <= savings * 5) {
            msg.append("⚠️ Partial eligibility. Consider reducing requested loan or improving savings.");
            tvEligibility.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            msg.append("❌ Not eligible. Increase savings or reduce requested loan amount.");
            tvEligibility.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        lastEligibilityMessage = msg.toString();
        tvEligibility.setText(lastEligibilityMessage);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FinancialReport report = new FinancialReport(userId, totalIncome, totalExpense, savings, System.currentTimeMillis());

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            db.reportDao().insertReport(report);
            Log.d("RoomDB", "Report inserted for user: " + userId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadReportsFromRoom);
            }
        }).start();
    }

    private void loadReportsFromRoom() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<FinancialReport> reports = db.reportDao().getReportsForUser(userId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> reportAdapter.setReports(reports));
            }
        }).start();
    }

    private void shareReport() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<FinancialReport> reports = db.reportDao().getReportsForUser(userId);

            if (reports.isEmpty()) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "No reports to share.", Toast.LENGTH_SHORT).show()
                    );
                }
                return;
            }

            FinancialReport latest = reports.get(0);
            String shareText = "📊 Financial Report:\n\n"
                    + "Income: ₹" + latest.getTotalIncome() + "\n"
                    + "Expense: ₹" + latest.getTotalExpense() + "\n"
                    + "Savings: ₹" + latest.getSavings() + "\n\n"
                    + (lastEligibilityMessage.isEmpty() ? "" : lastEligibilityMessage);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Financial Report");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        startActivity(Intent.createChooser(shareIntent, "Share Report via"))
                );
            }
        }).start();
    }
}
