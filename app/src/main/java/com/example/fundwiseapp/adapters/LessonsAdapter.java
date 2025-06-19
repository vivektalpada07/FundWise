package com.example.fundwiseapp.adapters;

import android.app.AlertDialog;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.Lesson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.ViewHolder> {
    private final List<Lesson> lessonList;

    public LessonsAdapter(List<Lesson> lessons) {
        this.lessonList = lessons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);

        holder.tvLessonTitle.setText("📘 " + lesson.getTitle());
        holder.tvLessonContent.setText(lesson.getContent());
        holder.tvLessonCategory.setText("📂 " + lesson.getCategory());

        String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(lesson.getTimestamp()));
        holder.tvLessonMeta.setText("By " + lesson.getAuthor() + " (" + lesson.getRole() + ") • " + dateStr);

        String currentUid = FirebaseAuth.getInstance().getUid();
        boolean isOwner = currentUid != null && currentUid.equals(lesson.getOwnerId());

        holder.btnEdit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Lesson")
                    .setMessage("Are you sure you want to delete this lesson?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("lessons")
                                .child(lesson.getId()).removeValue();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.btnEdit.setOnClickListener(v -> {
            showEditDialog(v, lesson);
        });

        holder.itemView.setOnClickListener(v -> {
            showFullLessonDialog(v, lesson);
        });
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonTitle, tvLessonContent, tvLessonCategory, tvLessonMeta;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvLessonContent = itemView.findViewById(R.id.tvLessonContent);
            tvLessonCategory = itemView.findViewById(R.id.tvLessonCategory);
            tvLessonMeta = itemView.findViewById(R.id.tvLessonMeta);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void showEditDialog(View contextView, Lesson lesson) {
        View dialogView = LayoutInflater.from(contextView.getContext())
                .inflate(R.layout.dialog_edit_lesson, null);

        EditText etTitle = dialogView.findViewById(R.id.etEditLessonTitle);
        EditText etContent = dialogView.findViewById(R.id.etEditLessonContent);
        EditText etCategory = dialogView.findViewById(R.id.etEditLessonCategory);

        etTitle.setText(lesson.getTitle());
        etContent.setText(lesson.getContent());
        etCategory.setText(lesson.getCategory());

        new AlertDialog.Builder(contextView.getContext())
                .setTitle("Edit Lesson")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newContent = etContent.getText().toString().trim();
                    String newCategory = etCategory.getText().toString().trim();

                    if (newTitle.isEmpty() || newContent.isEmpty() || newCategory.isEmpty()) {
                        Toast.makeText(contextView.getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("title", newTitle);
                    updates.put("content", newContent);
                    updates.put("category", newCategory);

                    FirebaseDatabase.getInstance().getReference("lessons")
                            .child(lesson.getId())
                            .updateChildren(updates)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(contextView.getContext(), "✅ Lesson updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(contextView.getContext(), "❌ Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFullLessonDialog(View contextView, Lesson lesson) {
        View dialogView = LayoutInflater.from(contextView.getContext())
                .inflate(R.layout.dialog_lesson_details, null);

        TextView tvFullTitle = dialogView.findViewById(R.id.tvFullTitle);
        TextView tvFullCategory = dialogView.findViewById(R.id.tvFullCategory);
        TextView tvFullMeta = dialogView.findViewById(R.id.tvFullMeta);
        TextView tvFullContent = dialogView.findViewById(R.id.tvFullContent);

        tvFullTitle.setText("📘 " + lesson.getTitle());
        tvFullCategory.setText("📂 " + lesson.getCategory());

        String meta = "By " + lesson.getAuthor() + " (" + lesson.getRole() + ") • "
                + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(lesson.getTimestamp()));
        tvFullMeta.setText(meta);
        tvFullContent.setText(lesson.getContent());

        new AlertDialog.Builder(contextView.getContext())
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
}