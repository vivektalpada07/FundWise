package com.example.fundwiseapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.CustomerBudgetAdapter;
import com.example.fundwiseapp.adapters.MockPriceAdapter;
import com.example.fundwiseapp.auth.LoginActivity;
import com.example.fundwiseapp.customer.FinancialLessonsActivity;
import com.example.fundwiseapp.models.CustomerBudget;
import com.example.fundwiseapp.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ManagerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomerBudgetAdapter adapter;
    private List<CustomerBudget> customerSummaries = new ArrayList<>();
    private DatabaseReference usersRef, transactionsRef, alertsRef, fakeNewsRef;
    private Button btnManageLessons, btnViewFakePrices;
    private RecyclerView recyclerMockPrices;
    private MockPriceAdapter mockPriceAdapter;
    private final List<String> mockSymbols = new ArrayList<>();
    private final Map<String, Double> mockPriceMap = new HashMap<>();

    private final String[] stockSymbols = {
            "RELIANCE.BSE", "TCS.BSE", "INFY.BSE", "HDFCBANK.BSE", "ICICIBANK.BSE",
            "SBIN.BSE", "KOTAKBANK.BSE", "ITC.BSE", "LT.BSE", "HINDUNILVR.BSE",
            "BHARTIARTL.BSE", "ASIANPAINT.BSE", "BAJFINANCE.BSE", "HCLTECH.BSE",
            "MARUTI.BSE", "WIPRO.BSE", "ONGC.BSE", "POWERGRID.BSE", "TITAN.BSE", "ULTRACEMCO.BSE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        btnManageLessons = findViewById(R.id.btnManageLessons);
        Button btnUpdateMockPrice = findViewById(R.id.btnUpdateMockPrice);
        btnUpdateMockPrice.setOnClickListener(v -> showMockPriceDialog());
        recyclerMockPrices = findViewById(R.id.recyclerMockPrices);
        recyclerMockPrices.setLayoutManager(new LinearLayoutManager(this));
        mockPriceAdapter = new MockPriceAdapter(mockSymbols, mockPriceMap);
        recyclerMockPrices.setAdapter(mockPriceAdapter);

        loadMockPrices();

        btnViewFakePrices = findViewById(R.id.btnViewFakePrices);

        recyclerView = findViewById(R.id.recyclerCustomerBudgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerBudgetAdapter(customerSummaries, this::sendAlertToUser);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        alertsRef = FirebaseDatabase.getInstance().getReference("alerts");
        fakeNewsRef = FirebaseDatabase.getInstance().getReference("fakeNewsAlerts");

        btnLogout.setOnClickListener(v -> logout());
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.fundwiseapp.settings.ProfileSettingsActivity.class))
        );
        btnManageLessons.setOnClickListener(v ->
                startActivity(new Intent(this, FinancialLessonsActivity.class))
        );
        btnViewFakePrices.setOnClickListener(v -> simulateFakeMarketEvent());

        loadCustomerBudgets();
        Button btnClearMockPrices = findViewById(R.id.btnClearMockPrices);
        btnClearMockPrices.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("mockPrices").removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Mock prices cleared", Toast.LENGTH_SHORT).show();
                        loadMockPrices();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to clear mock prices", Toast.LENGTH_SHORT).show());
        });

    }
    private void showMockPriceDialog() {
        final String[] stocks = {
                "RELIANCE_BSE", "TCS_BSE", "INFY_BSE", "HDFCBANK_BSE", "ICICIBANK_BSE",
                "SBIN_BSE", "KOTAKBANK_BSE", "ITC_BSE", "LT_BSE", "HINDUNILVR_BSE",
                "BHARTIARTL_BSE", "ASIANPAINT_BSE", "BAJFINANCE_BSE", "HCLTECH_BSE",
                "MARUTI_BSE", "WIPRO_BSE", "ONGC_BSE", "POWERGRID_BSE", "TITAN_BSE", "ULTRACEMCO_BSE"
        };

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_price, null);
        Spinner spinnerStock = dialogView.findViewById(R.id.spinnerStock);
        EditText etNewPrice = dialogView.findViewById(R.id.etNewPrice);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stocks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStock.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Update Mock Price")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String selected = spinnerStock.getSelectedItem().toString();
                    String priceText = etNewPrice.getText().toString().trim();
                    if (!priceText.isEmpty()) {
                        try {
                            double newPrice = Double.parseDouble(priceText);
                            FirebaseDatabase.getInstance().getReference("mockPrices")
                                    .child(selected).setValue(newPrice)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(this, "Mock price updated", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to update price", Toast.LENGTH_SHORT).show());
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Enter a price", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void sendFakeNewsAlertToAllCustomers(String newsMessage) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("alerts");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user : snapshot.getChildren()) {
                    String uid = user.getKey();
                    String role = user.child("role").getValue(String.class);
                    if ("customer".equalsIgnoreCase(role)) {
                        alertsRef.child(uid).push().setValue(newsMessage);
                    }
                }
                Toast.makeText(ManagerDashboardActivity.this, "Fake news alert sent to all customers", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to send alerts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMockPrices() {
        FirebaseDatabase.getInstance().getReference("mockPrices")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mockSymbols.clear();
                        mockPriceMap.clear();

                        for (DataSnapshot stock : snapshot.getChildren()) {
                            mockSymbols.add(stock.getKey());
                            mockPriceMap.put(stock.getKey(), stock.getValue(Double.class));
                        }

                        mockPriceAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ManagerDashboardActivity.this, "Failed to load mock prices", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadCustomerBudgets() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnap) {
                Map<String, String> userEmailMap = new HashMap<>();
                for (DataSnapshot user : usersSnap.getChildren()) {
                    String uid = user.getKey();
                    String role = user.child("role").getValue(String.class);
                    String email = user.child("email").getValue(String.class);
                    if ("customer".equalsIgnoreCase(role)) {
                        userEmailMap.put(uid, email);
                    }
                }

                transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot txnSnap) {
                        Map<String, Double> incomeMap = new HashMap<>();
                        Map<String, Double> expenseMap = new HashMap<>();

                        for (DataSnapshot txn : txnSnap.getChildren()) {
                            Transaction t = txn.getValue(Transaction.class);
                            if (t != null && userEmailMap.containsKey(t.getUserId())) {
                                String uid = t.getUserId();
                                double amt = t.getAmount();
                                if ("Income".equalsIgnoreCase(t.getType())) {
                                    incomeMap.put(uid, incomeMap.getOrDefault(uid, 0.0) + amt);
                                } else {
                                    expenseMap.put(uid, expenseMap.getOrDefault(uid, 0.0) + amt);
                                }
                            }
                        }

                        customerSummaries.clear();
                        for (String uid : userEmailMap.keySet()) {
                            double income = incomeMap.getOrDefault(uid, 0.0);
                            double expense = expenseMap.getOrDefault(uid, 0.0);
                            double savings = income - expense;
                            String email = userEmailMap.get(uid);

                            customerSummaries.add(new CustomerBudget(uid, email, income, expense));
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ManagerDashboardActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAlertToUser(String userId, String email) {
        String alertMessage = "⚠ Dear user, your expenses exceed your income. Please review your budget.";
        alertsRef.child(userId).push().setValue(alertMessage)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Alert sent to " + email, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send alert", Toast.LENGTH_SHORT).show());
    }

    private void simulateFakeMarketEvent() {
        for (String symbol : stockSymbols) {
            double fakePrice = generateFakePrice();
            String reason = "📈 Breaking: " + symbol + " expected to rise! Consider investing.";

            // Save to fakeNewsAlerts
            Map<String, Object> alert = new HashMap<>();
            alert.put("symbol", symbol);
            alert.put("price", fakePrice);
            alert.put("reason", reason);
            alert.put("timestamp", System.currentTimeMillis());

            fakeNewsRef.push().setValue(alert);

            // Also broadcast to all customers' alerts
            FirebaseDatabase.getInstance().getReference("users")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot user : snapshot.getChildren()) {
                                String uid = user.getKey();
                                String role = user.child("role").getValue(String.class);
                                if ("customer".equalsIgnoreCase(role)) {
                                    FirebaseDatabase.getInstance()
                                            .getReference("alerts")
                                            .child(uid)
                                            .push()
                                            .setValue(reason);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ManagerDashboardActivity.this, "❌ Failed to send customer alerts", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        Toast.makeText(this, "📢 Simulated stock alerts sent to customers.", Toast.LENGTH_SHORT).show();
    }


    private double generateFakePrice() {
        double multiplier = 0.8 + new Random().nextDouble() * 0.4; // 0.8 to 1.2
        return Math.round(1000 * multiplier * 100.0) / 100.0;
    }
}
