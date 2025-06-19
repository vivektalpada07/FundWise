package com.example.fundwiseapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fundwiseapp.fragments.LoanFragment;
import com.example.fundwiseapp.fragments.StockFragment;
import com.example.fundwiseapp.fragments.TransactionFragment;
import com.example.fundwiseapp.fragments.CustomerReportFragment;
import com.example.fundwiseapp.fragments.LessonsFragment;




import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavigationActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load default fragment
        loadFragment(new TransactionFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_transaction) {
                fragment = new TransactionFragment();
            } else if (itemId == R.id.nav_loan) {
                fragment = new LoanFragment();
            } else if (itemId == R.id.nav_stocks) {
                fragment = new StockFragment();
            }else if (itemId == R.id.nav_report) {
                fragment = new CustomerReportFragment();
            }else if (item.getItemId() == R.id.nav_lessons) {
                fragment = new LessonsFragment();
            }


            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
