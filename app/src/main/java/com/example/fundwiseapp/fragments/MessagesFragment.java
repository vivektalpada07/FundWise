package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.AlertAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.database.annotations.Nullable;

import java.util.*;

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlertAdapter adapter;
    private List<String> alertList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        recyclerView = view.findViewById(R.id.recyclerMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AlertAdapter(alertList);
        recyclerView.setAdapter(adapter);

        loadAlerts();

        return view;
    }

    private void loadAlerts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "⚠ User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("alerts").child(userId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alertList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String msg = snap.getValue(String.class);
                    if (msg != null) alertList.add(msg);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "⚠ Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
