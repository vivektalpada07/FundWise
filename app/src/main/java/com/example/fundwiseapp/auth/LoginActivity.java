package com.example.fundwiseapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.dashboard.AdminDashboardActivity;
import com.example.fundwiseapp.dashboard.ManagerDashboardActivity;
import com.example.fundwiseapp.MainNavigationActivity;
import com.example.fundwiseapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = mAuth.getCurrentUser().getUid();

                        FirebaseDatabase.getInstance().getReference("users")
                                .child(uid)
                                .get()
                                .addOnSuccessListener(dataSnapshot -> {
                                    User user = dataSnapshot.getValue(User.class);
                                    if (user != null) {
                                        switch (user.getRole()) {
                                            case "admin":
                                                startActivity(new Intent(this, AdminDashboardActivity.class));
                                                break;
                                            case "manager":
                                                startActivity(new Intent(this, ManagerDashboardActivity.class));
                                                break;
                                            case "customer":
                                            default:
                                                startActivity(new Intent(this, MainNavigationActivity.class));
                                                break;
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }
}
