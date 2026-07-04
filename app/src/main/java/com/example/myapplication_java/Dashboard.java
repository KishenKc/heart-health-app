package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class Dashboard extends AppCompatActivity {

    MaterialCardView btnPatientInfo, btnVitalSigns, btnRiskAssessment, btnViewHistory;
    ImageButton btnProfile;
    Button btnLogout, btnAIHealthAssistant;
    TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // ===== Initialize UI elements =====
        btnPatientInfo = findViewById(R.id.btnHealth);
        btnVitalSigns = findViewById(R.id.btnVital);
        btnRiskAssessment = findViewById(R.id.btnRiskAssessment);
        btnViewHistory = findViewById(R.id.btnHistory);
        btnProfile = findViewById(R.id.btnProfileDash);
        btnLogout = findViewById(R.id.btnLogoutDash);
        btnAIHealthAssistant = findViewById(R.id.btnAIHealthAssistant);
        welcomeText = findViewById(R.id.welcomeText);

        // ===== Fetch saved name for greeting =====
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("first_name", null);
        String email = prefs.getString("email", null);

        // ✅ Ensure greeting uses only the first name
        if (fullName != null && !fullName.isEmpty()) {
            // Split by space and use only the first part
            String firstName = fullName.contains(" ") ? fullName.split(" ")[0] : fullName;
            welcomeText.setText("Welcome, " + firstName + " 👋");
        } else if (email != null && !email.isEmpty()) {
            // Fallback: use the email prefix
            String username = email.split("@")[0];
            welcomeText.setText("Welcome, " + username + " 👋");
        } else {
            welcomeText.setText("Welcome 👋");
        }

        // ===== Navigation =====
        btnPatientInfo.setOnClickListener(v -> startActivity(new Intent(this, PatientDetailsUpdate.class)));
        btnVitalSigns.setOnClickListener(v -> startActivity(new Intent(this, PatientsVitalSigns.class)));
        btnRiskAssessment.setOnClickListener(v -> startActivity(new Intent(this, AssessmentActivity.class)));
        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, AssessmentHistoryActivity.class)));
        btnAIHealthAssistant.setOnClickListener(v -> startActivity(new Intent(this, ChatBot.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ViewProfile.class)));

        // ===== Logout =====
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(Dashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
