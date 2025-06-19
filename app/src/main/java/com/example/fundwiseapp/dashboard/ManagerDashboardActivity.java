package com.example.fundwiseapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.CustomerBudgetAdapter;
import com.example.fundwiseapp.customer.FinancialLessonsActivity;
import com.example.fundwiseapp.models.CustomerBudget;
import com.example.fundwiseapp.models.Transaction;
import com.google.firebase.database.*;

import java.util.*;

public class ManagerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomerBudgetAdapter adapter;
    private List<CustomerBudget> customerSummaries = new ArrayList<>();
    private DatabaseReference usersRef, transactionsRef, alertsRef;
    private Button btnManageLessons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        btnManageLessons = findViewById(R.id.btnManageLessons);
        recyclerView = findViewById(R.id.recyclerCustomerBudgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerBudgetAdapter(customerSummaries, this::sendAlertToUser);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        alertsRef = FirebaseDatabase.getInstance().getReference("alerts");

        btnManageLessons.setOnClickListener(v ->
                startActivity(new Intent(this, FinancialLessonsActivity.class)));

        loadCustomerBudgets();
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
}
