package com.example.fundwiseapp.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fundwiseapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.IOException;
import java.util.UUID;

public class ProfileSettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView ivProfilePic;
    private EditText etName;
    private Button btnSave, btnSelectImage;
    private Uri imageUri;

    private DatabaseReference userRef;
    private StorageReference storageRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        ivProfilePic = findViewById(R.id.ivProfilePic);
        etName = findViewById(R.id.etName);
        btnSave = findViewById(R.id.btnSave);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        userId = FirebaseAuth.getInstance().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        btnSelectImage.setOnClickListener(v -> chooseImage());
        btnSave.setOnClickListener(v -> saveProfile());

        loadProfileData();
    }

    private void chooseImage() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    private void loadProfileData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                if (name != null) etName.setText(name);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(ProfileSettingsActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(ivProfilePic);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Enter name");
            return;
        }

        userRef.child("name").setValue(name);

        if (imageUri != null) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Uploading...");
            dialog.show();

            StorageReference imageRef = storageRef.child(userId + ".jpg");
            imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userRef.child("imageUrl").setValue(uri.toString());
                        dialog.dismiss();
                        Toast.makeText(this, "✅ Profile updated", Toast.LENGTH_SHORT).show();
                    })
            ).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(this, "❌ Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "✅ Profile updated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivProfilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
