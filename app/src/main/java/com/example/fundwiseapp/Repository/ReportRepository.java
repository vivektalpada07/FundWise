package com.example.fundwiseapp.Repository;

import android.util.Log;

import com.example.fundwiseapp.room.FinancialReport;
import com.example.fundwiseapp.room.ReportDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportRepository {

    private final ReportDao reportDao;
    private final ExecutorService executorService;

    public ReportRepository(ReportDao reportDao) {
        this.reportDao = reportDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertReport(FinancialReport report) {
        executorService.execute(() -> {
            reportDao.insertReport(report);
            Log.d("RoomDB", "Inserted report: income=" + report.getTotalIncome() +
                    ", expense=" + report.getTotalExpense() +
                    ", savings=" + report.getSavings());

            List<FinancialReport> reports = reportDao.getAllReports();
            Log.d("RoomDB", "Total reports in DB: " + reports.size());
            for (FinancialReport r : reports) {
                Log.d("RoomDB", "Report id=" + r.getId() +
                        ", income=" + r.getTotalIncome() +
                        ", expense=" + r.getTotalExpense() +
                        ", savings=" + r.getSavings() +
                        ", timestamp=" + r.getTimestamp());
            }
        });
    }

    public void deleteAllReports() {
        executorService.execute(() -> {
            reportDao.deleteAll();
            Log.d("RoomDB", "Deleted all reports");
        });
    }
}
