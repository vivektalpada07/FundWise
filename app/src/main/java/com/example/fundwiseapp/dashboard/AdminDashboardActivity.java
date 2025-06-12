package com.example.fundwiseapp.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView userListView;
    private Button btnAddManager;
    private ArrayList<String> userDisplayList = new ArrayList<>();
    private ArrayList<String> userIdList = new ArrayList<>();
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        userListView = findViewById(R.id.userListView);
        btnAddManager = findViewById(R.id.btnAddManager);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUsers();

        userListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUserId = userIdList.get(position);
            usersRef.child(selectedUserId).child("role").setValue("manager")
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "✅ Role set to manager", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "❌ Failed to update role", Toast.LENGTH_SHORT).show());
        });

        btnAddManager.setOnClickListener(v -> showAddManagerDialog());
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userDisplayList.clear();
                userIdList.clear();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String email = userSnap.child("email").getValue(String.class);
                    String role = userSnap.child("role").getValue(String.class);

                    if (email == null) continue;
                    if (!"admin".equalsIgnoreCase(role)) {
                        userDisplayList.add(email + " (Tap to assign manager)");
                        userIdList.add(uid);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminDashboardActivity.this,
                        android.R.layout.simple_list_item_1, userDisplayList);
                userListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "❌ Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
