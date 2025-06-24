package com.example.fundwiseapp.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.auth.LoginActivity;
import com.example.fundwiseapp.customer.FinancialLessonsActivity;
import com.example.fundwiseapp.fragments.AdminPortfolioFragment;
import com.example.fundwiseapp.models.User;
import com.google.firebase.database.*;

import java.util.*;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView userListView;
    private Button btnAddManager, btnViewLessons, btnRefreshUsers, btnViewPortfolios;
    private ArrayList<String> userDisplayList = new ArrayList<>();
    private ArrayList<String> userIdList = new ArrayList<>();
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Logout
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // View references
        userListView = findViewById(R.id.userListView);
        btnAddManager = findViewById(R.id.btnAddManager);
        btnViewLessons = findViewById(R.id.btnManageLessons);
        btnRefreshUsers = findViewById(R.id.btnRefreshUsers);
        btnViewPortfolios = findViewById(R.id.btnViewPortfolios);

        // Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Listeners
        btnAddManager.setOnClickListener(v -> showAddManagerDialog());
        btnViewLessons.setOnClickListener(v -> startActivity(new Intent(this, FinancialLessonsActivity.class)));
        btnRefreshUsers.setOnClickListener(v -> loadUsers());

        // Open Portfolio Fragment
        btnViewPortfolios.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, new AdminPortfolioFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Promote user to manager
        userListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUserId = userIdList.get(position);
            usersRef.child(selectedUserId).child("role").setValue("manager")
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "✅ Role updated to Manager", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "❌ Failed to update role", Toast.LENGTH_SHORT).show());
        });

        // Initial Load
        loadUsers();
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userDisplayList.clear();
                userIdList.clear();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String email = userSnap.child("email").getValue(String.class);
                    String role = userSnap.child("role").getValue(String.class);

                    if (email == null) continue;
                    if (!"admin".equalsIgnoreCase(role)) {
                        userDisplayList.add(email + " (" + role + ")");
                        userIdList.add(uid);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AdminDashboardActivity.this,
                        android.R.layout.simple_list_item_1,
                        userDisplayList
                );
                userListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "❌ Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddManagerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_manager, null);
        EditText etName = dialogView.findViewById(R.id.etManagerName);
        EditText etEmail = dialogView.findViewById(R.id.etManagerEmail);

        new AlertDialog.Builder(this)
                .setTitle("Add Manager")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
                        Toast.makeText(this, "⚠ Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = usersRef.push().getKey();
                    if (uid == null) return;

                    User newUser = new User(uid, name, email, "manager");

                    usersRef.child(uid).setValue(newUser)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "✅ Manager added", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
