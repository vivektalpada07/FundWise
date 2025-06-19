package com.example.fundwiseapp.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.CustomerBudget;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class CustomerBudgetAdapter extends RecyclerView.Adapter<CustomerBudgetAdapter.ViewHolder> {
    private final List<CustomerBudget> customerBudgets;
    private final Context context;
    private final OnAlertClickListener alertClickListener;

    public interface OnAlertClickListener {
        void onSendAlert(String userId, String email);
    }

    public CustomerBudgetAdapter(List<CustomerBudget> budgets, OnAlertClickListener listener) {
        this.context = null; // Remove context if unused
        this.customerBudgets = budgets;
        this.alertClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerBudget cb = customerBudgets.get(position);
        holder.tvEmail.setText(cb.getEmail());
        holder.tvIncome.setText("Income: ₹" + cb.getTotalIncome());
        holder.tvExpense.setText("Expense: ₹" + cb.getTotalExpense());
        holder.tvSavings.setText("Savings: ₹" + cb.getSavings());

        boolean isOverspending = cb.getTotalExpense() > cb.getTotalIncome();
        holder.btnSendAlert.setVisibility(isOverspending ? View.VISIBLE : View.GONE);

        holder.btnSendAlert.setOnClickListener(v -> {
            if (alertClickListener != null) {
                alertClickListener.onSendAlert(cb.getUserId(), cb.getEmail());
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerBudgets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvIncome, tvExpense, tvSavings;
        Button btnSendAlert;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvExpense = itemView.findViewById(R.id.tvExpense);
            tvSavings = itemView.findViewById(R.id.tvSavings);
            btnSendAlert = itemView.findViewById(R.id.btnSendAlert);
        }
    }
}