package com.example.fundwiseapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fundwiseapp.auth.LoginActivity;
import com.example.fundwiseapp.fragments.*;
import com.example.fundwiseapp.settings.ProfileSettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class MainNavigationActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        // 🚪 Logout button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        // 👤 Edit Profile button
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileSettingsActivity.class))
        );

        // Bottom Navigation setup
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadFragment(new TransactionFragment()); // Default fragment

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_transaction) {
                fragment = new TransactionFragment();
            } else if (itemId == R.id.nav_stocks) {
                fragment = new StockFragment();
            } else if (itemId == R.id.nav_report) {
                fragment = new CustomerReportFragment();
            } else if (itemId == R.id.nav_lessons) {
                fragment = new LessonsFragment();
            }

            return loadFragment(fragment);
        });

        // 🔔 Listen for alerts from manager (fake news, budget warnings)
        listenForAlerts();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void listenForAlerts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference alertsRef = FirebaseDatabase.getInstance()
                .getReference("alerts")
                .child(userId);

        alertsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String message = snapshot.getValue(String.class);
                if (message != null) {
                    Toast.makeText(MainNavigationActivity.this, message, Toast.LENGTH_LONG).show();
                    snapshot.getRef().removeValue(); // Remove the alert after displaying
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
