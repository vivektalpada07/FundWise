package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.LessonsAdapter;
import com.example.fundwiseapp.models.Lesson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class LessonsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LessonsAdapter adapter;
    private List<Lesson> lessonList = new ArrayList<>();

    private DatabaseReference lessonRef, usersRef;
    private String currentUserRole = "", currentUserId = "", currentUserEmail = "";

    private EditText etTitle, etContent, etCategory;
    private Button btnAddLesson;
    private LinearLayout lessonForm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lessons, container, false);

        recyclerView = view.findViewById(R.id.lessonRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LessonsAdapter(lessonList);
        recyclerView.setAdapter(adapter);

        lessonForm = view.findViewById(R.id.lessonForm);
        etTitle = view.findViewById(R.id.etLessonTitle);
        etContent = view.findViewById(R.id.etLessonContent);
        etCategory = view.findViewById(R.id.etLessonCategory);
        btnAddLesson = view.findViewById(R.id.btnAddLesson);

        lessonRef = FirebaseDatabase.getInstance().getReference("lessons");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchUserDetails();

        btnAddLesson.setOnClickListener(v -> addLesson());

        return view;
    }

    private void fetchUserDetails() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserRole = snapshot.child("role").getValue(String.class);
                currentUserEmail = snapshot.child("email").getValue(String.class);

                boolean isPrivileged = "admin".equals(currentUserRole) || "manager".equals(currentUserRole);
                lessonForm.setVisibility(isPrivileged ? View.VISIBLE : View.GONE);

                loadLessons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLesson() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content) || TextUtils.isEmpty(category)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = lessonRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        Lesson lesson = new Lesson(
                id, title, content, category,
                currentUserEmail, currentUserRole, currentUserId, timestamp
        );

        lessonRef.child(id).setValue(lesson).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "✅ Lesson added", Toast.LENGTH_SHORT).show();
            etTitle.setText("");
            etContent.setText("");
            etCategory.setText("");
        });
    }

    private void loadLessons() {
        lessonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lessonList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Lesson lesson = ds.getValue(Lesson.class);
                    if (lesson != null) {
                        lessonList.add(lesson);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load lessons", Toast.LENGTH_SHORT).show();
            }
        });
    }
}