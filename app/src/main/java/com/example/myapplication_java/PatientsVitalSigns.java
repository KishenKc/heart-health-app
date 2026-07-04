package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientsVitalSigns extends AppCompatActivity {

    Button btnSaveVitals, btnSelectSymptoms, btnDashboard;
    EditText inputHeartRate, inputBloodPressure, inputCholesterol;
    TextView tvSelectedSymptoms;
    private List<String> selectedSymptoms = new ArrayList<>();
    private String userEmail;

    private static final String TAG = "PatientsVitalSigns";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_vital_signs);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userEmail = prefs.getString("email", "");

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        inputHeartRate = findViewById(R.id.patientHeartRate);
        inputBloodPressure = findViewById(R.id.patientBloodPressure);
        inputCholesterol = findViewById(R.id.patientCholesterol);
        tvSelectedSymptoms = findViewById(R.id.tvSelectedSymptoms);

        btnSelectSymptoms = findViewById(R.id.btnSelectSymptoms);
        btnSaveVitals = findViewById(R.id.btnVitalSignsNext);
        btnDashboard = findViewById(R.id.btnDashboard); // ✅ Updated ID
    }

    private void setupClickListeners() {
        btnSelectSymptoms.setOnClickListener(v -> showSymptomsDialog());
        btnSaveVitals.setOnClickListener(v -> saveVitalsAndGenerateScore());
        btnDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(PatientsVitalSigns.this, Dashboard.class);
            startActivity(intent);
            finish();
        });
    }

    private void showSymptomsDialog() {
        final String[] SYMPTOMS = {
                "None",
                "Chest pain or discomfort", "Shortness of breath", "Fatigue",
                "Dizziness", "Swelling in legs", "Rapid heartbeat"
        };
        final boolean[] checkedItems = new boolean[SYMPTOMS.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Symptoms")
                .setMultiChoiceItems(SYMPTOMS, checkedItems, (dialog, which, isChecked) -> {
                    if (SYMPTOMS[which].equals("None") && isChecked) {
                        for (int i = 1; i < checkedItems.length; i++) {
                            checkedItems[i] = false;
                            ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                        }
                    } else if (!SYMPTOMS[which].equals("None") && isChecked) {
                        checkedItems[0] = false;
                        ((AlertDialog) dialog).getListView().setItemChecked(0, false);
                    }
                })
                .setPositiveButton("OK", (dialog, id) -> {
                    selectedSymptoms.clear();
                    for (int i = 0; i < SYMPTOMS.length; i++) {
                        if (checkedItems[i]) selectedSymptoms.add(SYMPTOMS[i]);
                    }
                    tvSelectedSymptoms.setText(selectedSymptoms.isEmpty()
                            ? "No symptoms selected"
                            : String.join(", ", selectedSymptoms));
                })
                .setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveVitalsAndGenerateScore() {
        String heartRateStr = inputHeartRate.getText().toString().trim();
        String bpStr = inputBloodPressure.getText().toString().trim();
        String cholStr = inputCholesterol.getText().toString().trim();

        if (heartRateStr.isEmpty() || bpStr.isEmpty() || cholStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all vitals", Toast.LENGTH_SHORT).show();
            return;
        }

        int heartRate = Integer.parseInt(heartRateStr);
        int cholesterol = Integer.parseInt(cholStr);
        int systolic = 0;
        try {
            String[] parts = bpStr.split("/");
            systolic = Integer.parseInt(parts[0]);
        } catch (Exception e) {
            Log.w(TAG, "Invalid BP format, using 0");
        }

        // 🎯 Scoring system
        int score = 10;
        if (heartRate < 60 || heartRate > 100) score -= 3;
        if (systolic > 140) score -= 3;
        if (cholesterol > 220) score -= 2;
        if (!selectedSymptoms.isEmpty() && !selectedSymptoms.contains("None")) score -= 1;

        score = Math.max(0, Math.min(score, 10));

        String range;
        if (score >= 8) {
            range = "GOOD (Healthy range)";
        } else if (score >= 5) {
            range = "MEDIUM (Borderline)";
        } else {
            range = "BAD (Unhealthy range)";
        }

        String vitalAssessment = score + "/10 — " + range;

        String symptomsJson = new JSONArray(selectedSymptoms).toString();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        try {
            JSONObject vital = new JSONObject();
            vital.put("user_email", userEmail);
            vital.put("heart_rate", heartRateStr);
            vital.put("blood_pressure", bpStr);
            vital.put("cholesterol", cholStr);
            vital.put("symptoms", symptomsJson);
            vital.put("created_at", currentTime);
            vital.put("assessment", vitalAssessment);

            String jsonBody = "[" + vital.toString() + "]";
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);

            SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);
            Call<ResponseBody> call = api.insertVitalSigns(body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PatientsVitalSigns.this, "Vitals saved! Score: " + vitalAssessment, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(PatientsVitalSigns.this, AssessmentHistoryActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PatientsVitalSigns.this, "Failed to save vitals", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PatientsVitalSigns.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error building request: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
