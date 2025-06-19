package com.example.fundwiseapp.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.LessonsAdapter;
import com.example.fundwiseapp.models.Lesson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class FinancialLessonsActivity extends AppCompatActivity {

    EditText etTitle, etContent, etCategory;
    Button btnAddLesson;
    LinearLayout lessonForm;
    RecyclerView lessonRecyclerView;

    List<Lesson> lessonList = new ArrayList<>();
    LessonsAdapter lessonsAdapter;

    DatabaseReference lessonsRef, usersRef;
    String currentUserId, currentUserRole = "", currentUserEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_lessons);

        // View bindings
        etTitle = findViewById(R.id.etLessonTitle);
        etContent = findViewById(R.id.etLessonContent);
        etCategory = findViewById(R.id.etLessonCategory);
        btnAddLesson = findViewById(R.id.btnAddLesson);
        lessonForm = findViewById(R.id.lessonForm);
        lessonRecyclerView = findViewById(R.id.lessonRecyclerView);

        lessonRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessonsAdapter = new LessonsAdapter(lessonList);
        lessonRecyclerView.setAdapter(lessonsAdapter);

        // Firebase refs
        currentUserId = FirebaseAuth.getInstance().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        lessonsRef = FirebaseDatabase.getInstance().getReference("lessons");

        fetchUserInfoAndLoadLessons();

        btnAddLesson.setOnClickListener(v -> addLesson());
    }

    private void fetchUserInfoAndLoadLessons() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserRole = snapshot.child("role").getValue(String.class);
                currentUserEmail = snapshot.child("email").getValue(String.class);

                boolean isEditor = currentUserRole != null &&
                        (currentUserRole.equalsIgnoreCase("admin") || currentUserRole.equalsIgnoreCase("manager"));

                lessonForm.setVisibility(isEditor ? View.VISIBLE : View.GONE);
                loadLessons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FinancialLessonsActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLesson() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = lessonsRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        Lesson lesson = new Lesson(
                id,
                title,
                content,
                category,
                currentUserEmail,
                currentUserRole,
                currentUserId,
                timestamp
        );

        lessonsRef.child(id).setValue(lesson).addOnSuccessListener(unused -> {
            Toast.makeText(this, "✅ Lesson added", Toast.LENGTH_SHORT).show();
            etTitle.setText("");
            etContent.setText("");
            etCategory.setText("");
        });
    }

    private void loadLessons() {
        lessonsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lessonList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Lesson lesson = ds.getValue(Lesson.class);
                    if (lesson != null) {
                        lessonList.add(lesson);
                    }
                }
                lessonsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FinancialLessonsActivity.this, "Failed to load lessons", Toast.LENGTH_SHORT).show();
            }
        });
    }
}