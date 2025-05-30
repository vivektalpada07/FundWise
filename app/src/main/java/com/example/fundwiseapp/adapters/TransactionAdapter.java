package com.example.fundwiseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.models.Transaction;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onEdit(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactionList, OnTransactionClickListener listener) {
        this.context = context;
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.txtAmount.setText("₹" + transaction.getAmount());

        // Format timestamp to readable date
        String formattedDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(transaction.getTimestamp()));
        holder.txtTimestamp.setText(formattedDate);

        // Type radio buttons
        if ("Income".equalsIgnoreCase(transaction.getType())) {
            holder.radioIncome.setChecked(true);
        } else {
            holder.radioExpense.setChecked(true);
        }

        // Description radio buttons
        switch (transaction.getDescription()) {
            case "Salary":
                holder.radioSalary.setChecked(true);
                break;
            case "Food":
                holder.radioFood.setChecked(true);
                break;
            case "Rent":
                holder.radioRent.setChecked(true);
                break;
        }

        // Disable radio buttons (only for viewing)
        holder.radioIncome.setEnabled(false);
        holder.radioExpense.setEnabled(false);
        holder.radioSalary.setEnabled(false);
        holder.radioFood.setEnabled(false);
        holder.radioRent.setEnabled(false);

        // Delete functionality
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("transactions")
                    .child(transaction.getId())
                    .removeValue();
        });

        // Edit functionality
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView txtAmount, txtTimestamp;
        RadioButton radioIncome, radioExpense, radioSalary, radioFood, radioRent;
        Button btnEdit, btnDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            radioIncome = itemView.findViewById(R.id.radioIncome);
            radioExpense = itemView.findViewById(R.id.radioExpense);
            radioSalary = itemView.findViewById(R.id.radioSalary);
            radioFood = itemView.findViewById(R.id.radioFood);
            radioRent = itemView.findViewById(R.id.radioRent);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
