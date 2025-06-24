package com.example.fundwiseapp.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.adapters.TradeHistoryAdapter;
import com.example.fundwiseapp.models.Trade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class TradeHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TradeHistoryAdapter adapter;
    private final List<Trade> tradeList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade_history, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTradeHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TradeHistoryAdapter(tradeList);
        recyclerView.setAdapter(adapter);

        loadTradeHistory();
        return view;
    }

    private void loadTradeHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference tradeRef = FirebaseDatabase.getInstance().getReference("trades").child(userId);

        tradeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tradeList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Trade trade = ds.getValue(Trade.class);
                    if (trade != null) {
                        tradeList.add(trade);
                    }
                }
                Collections.reverse(tradeList); // newest first
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load trade history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
