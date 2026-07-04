package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class History extends AppCompatActivity {

    Button btnBackHistory;
    TextView tvPatientAge, tvPatientGender, tvPatientWeight, tvPatientHeight, tvPatientSmoker,
            tvPatientHeartDisease, tvPatientDiabetes, tvPatientHypertension;

    // 🔹 SharedPreferences keys (copied so this class works independently)
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String AGE = "age";
    public static final String GENDER = "gender";
    public static final String WEIGHT = "weight";
    public static final String HEIGHT = "height";
    public static final String SMOKER = "smoker";
    public static final String HEART_DISEASE = "heart_disease";
    public static final String DIABETES = "diabetes";
    public static final String HYPERTENSION = "hypertension";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_history);

        // Link TextViews
        tvPatientAge = findViewById(R.id.tvPatientAge);
        tvPatientGender = findViewById(R.id.tvPatientGender);
        tvPatientWeight = findViewById(R.id.tvPatientWeight);
        tvPatientHeight = findViewById(R.id.tvPatientHeight);
        tvPatientSmoker = findViewById(R.id.tvPatientSmoker);
        tvPatientHeartDisease = findViewById(R.id.tvPatientHeartDisease);
        tvPatientDiabetes = findViewById(R.id.tvPatientDiabetes);
        tvPatientHypertension = findViewById(R.id.tvPatientHypertension);

        // Load patient data
        loadPatientData();

        // Back button → return to Dashboard
        btnBackHistory = findViewById(R.id.btnHistoryBack);
        btnBackHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(History.this, Dashboard.class);
                startActivity(intent);
            }
        });
    }

    private void loadPatientData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String age = sharedPreferences.getString(AGE, "N/A");
        String gender = sharedPreferences.getString(GENDER, "N/A");
        String weight = sharedPreferences.getString(WEIGHT, "N/A");
        String height = sharedPreferences.getString(HEIGHT, "N/A");
        String smoker = sharedPreferences.getString(SMOKER, "N/A");
        boolean hasHeartDisease = sharedPreferences.getBoolean(HEART_DISEASE, false);
        boolean hasDiabetes = sharedPreferences.getBoolean(DIABETES, false);
        boolean hasHypertension = sharedPreferences.getBoolean(HYPERTENSION, false);

        tvPatientAge.setText("Age: " + age);
        tvPatientGender.setText("Gender: " + gender);
        tvPatientWeight.setText("Weight: " + weight);
        tvPatientHeight.setText("Height: " + height);
        tvPatientSmoker.setText("Smoker: " + smoker);
        tvPatientHeartDisease.setText("Heart Disease: " + (hasHeartDisease ? "Yes" : "No"));
        tvPatientDiabetes.setText("Diabetes: " + (hasDiabetes ? "Yes" : "No"));
        tvPatientHypertension.setText("Hypertension: " + (hasHypertension ? "Yes" : "No"));
    }
}
