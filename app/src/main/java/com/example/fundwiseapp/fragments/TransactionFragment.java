package com.example.fundwiseapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.Transaction;
import com.example.fundwiseapp.transactions.TransactionListActivity;
import com.example.fundwiseapp.utils.TransactionUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class TransactionFragment extends Fragment {

    private EditText editAmount;
    private Spinner spinnerType, spinnerDescription;
    private Button btnAddTransaction, btnViewTransactions, btnViewMessages;

    private TextView tvIncome, tvExpense, tvSavings, tvInsights;
    private PieChart pieChart;
    private BarChart barChart;

    private DatabaseReference databaseRef;
    private String currentUserId;

    private double totalIncome = 0;
    private double totalExpense = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        // Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("transactions");
        currentUserId = FirebaseAuth.getInstance().getUid();

        // Form UI
        editAmount = view.findViewById(R.id.editAmount);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerDescription = view.findViewById(R.id.spinnerDescription);
        btnAddTransaction = view.findViewById(R.id.btnOpenAddTransaction);
        btnViewTransactions = view.findViewById(R.id.btnViewTransactions);
        btnViewMessages = view.findViewById(R.id.btnViewMessages);
        // Budget UI
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvSavings = view.findViewById(R.id.tvSavings);
        tvInsights = view.findViewById(R.id.tvInsights);

        // Charts
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);

        // Spinners
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> descAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.transaction_descriptions, android.R.layout.simple_spinner_item);
        descAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDescription.setAdapter(descAdapter);

        btnAddTransaction.setOnClickListener(v -> addTransaction());
        btnViewTransactions.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), TransactionListActivity.class))
        );
        btnViewMessages.setOnClickListener(v -> {
            Fragment messagesFragment = new MessagesFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, messagesFragment)
                    .addToBackStack(null)
                    .commit();
        });
        loadAndAnalyzeTransactions();
        return view;
    }

    private void addTransaction() {
        String amountStr = editAmount.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String description = spinnerDescription.getSelectedItem().toString();

        if (TextUtils.isEmpty(amountStr)) {
            editAmount.setError("Amount required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            editAmount.setError("Enter valid amount");
            return;
        }

        TransactionUtils.saveTransaction(databaseRef, amount, description, type, getContext(), () -> {
            editAmount.setText("");
            spinnerType.setSelection(0);
            spinnerDescription.setSelection(0);
        });
    }

    private void loadAndAnalyzeTransactions() {
        databaseRef.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        totalIncome = 0;
                        totalExpense = 0;

                        Map<String, Double> expenseByCategory = new HashMap<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Transaction tx = ds.getValue(Transaction.class);
                            if (tx == null) continue;

                            if ("income".equalsIgnoreCase(tx.getType())) {
                                totalIncome += tx.getAmount();
                            } else if ("expense".equalsIgnoreCase(tx.getType())) {
                                totalExpense += tx.getAmount();
                                expenseByCategory.put(tx.getDescription(),
                                        expenseByCategory.getOrDefault(tx.getDescription(), 0.0) + tx.getAmount());
                            }
                        }

                        double savings = totalIncome - totalExpense;

                        tvIncome.setText("Income: ₹" + (int) totalIncome);
                        tvExpense.setText("Expenses: ₹" + (int) totalExpense);
                        tvSavings.setText("Savings: ₹" + (int) savings);
                        tvInsights.setText(generateInsight(savings));

                        updatePieChart(expenseByCategory);
                        updateBarChart();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateInsight(double savings) {
        if (savings < 0) return "⚠ You're spending more than your income.";
        else if (savings == 0) return "💡 Try to save a portion of your income.";
        else if (savings < 500) return "🧠 Good! Try to increase your savings.";
        else return "🎉 You're saving well! Keep it up.";
    }

    private void updatePieChart(Map<String, Double> expenseMap) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : expenseMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(Color.RED, Color.BLUE, Color.MAGENTA, Color.GREEN, Color.CYAN);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void updateBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) totalIncome));
        entries.add(new BarEntry(1, (float) totalExpense));

        BarDataSet dataSet = new BarDataSet(entries, "Income vs Expense");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] labels = {"Income", "Expense"};
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.length) {
                    return labels[(int) value];
                } else {
                    return "";
                }
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }
}