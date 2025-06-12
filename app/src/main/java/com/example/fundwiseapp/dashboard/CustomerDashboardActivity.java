package com.example.fundwiseapp.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.MainNavigationActivity;

public class CustomerDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, MainNavigationActivity.class));
        finish(); // Close the activity once navigation is triggered
    }
}
