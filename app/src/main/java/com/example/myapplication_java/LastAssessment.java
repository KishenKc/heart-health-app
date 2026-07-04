package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LastAssessment extends AppCompatActivity {

    private TextView tvRiskLevel, tvRiskScore, tvDate;
    private Button btnBackDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_assessment);

        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskScore = findViewById(R.id.tvRiskScore);
        tvDate = findViewById(R.id.tvDate);
        btnBackDashboard = findViewById(R.id.btnBackDashboard);

        btnBackDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, Dashboard.class));
            finish();
        });

        // Example data (replace later with Supabase fetch)
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String riskLevel = prefs.getString("last_risk_level", "Not Available");
        int riskScore = prefs.getInt("last_risk_score", 0);
        String date = prefs.getString("last_assessment_date", "No date");

        tvRiskLevel.setText("Risk Level: " + riskLevel);
        tvRiskScore.setText("Score: " + riskScore);
        tvDate.setText("Date: " + date);
    }
}
