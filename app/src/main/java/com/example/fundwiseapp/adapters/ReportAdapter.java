package com.example.fundwiseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fundwiseapp.R;
import com.example.fundwiseapp.room.FinancialReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<FinancialReport> reportList;

    public ReportAdapter(List<FinancialReport> reports) {
        this.reportList = reports;
    }

    public void setReports(List<FinancialReport> reports) {
        this.reportList = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_financial_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        FinancialReport report = reportList.get(position);

        holder.tvIncome.setText("Income: ₹" + report.getTotalIncome());
        holder.tvExpense.setText("Expense: ₹" + report.getTotalExpense());
        holder.tvSavings.setText("Savings: ₹" + report.getSavings());



        // Format timestamp nicely
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(report.getTimestamp()));
        holder.tvTimestamp.setText(dateStr);
    }

    @Override
    public int getItemCount() {
        return reportList == null ? 0 : reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvIncome, tvExpense, tvSavings, tvTimestamp;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvExpense = itemView.findViewById(R.id.tvExpense);
            tvSavings = itemView.findViewById(R.id.tvSavings);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
