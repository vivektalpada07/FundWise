package com.example.fundwiseapp.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReportDao {
    @Insert
    void insertReport(FinancialReport report);

    @Query("SELECT * FROM financial_reports WHERE userId = :userId ORDER BY timestamp DESC")
    List<FinancialReport> getReportsForUser(String userId);

    // ADD THESE IF YOU USE THEM
    @Query("SELECT * FROM financial_reports")
    List<FinancialReport> getAllReports();

    @Query("DELETE FROM financial_reports")
    void deleteAll();
}
