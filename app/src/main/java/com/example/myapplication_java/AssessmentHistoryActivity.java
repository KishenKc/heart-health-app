package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssessmentHistoryActivity extends AppCompatActivity {

    private ListView listAssessments;
    private ArrayAdapter<String> adapter;
    private final List<String> rows = new ArrayList<>();
    private Button btnBackToDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_history);

        listAssessments = findViewById(R.id.listAssessments);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows);
        listAssessments.setAdapter(adapter);

        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(AssessmentHistoryActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
        });

        loadCombinedHistory();
    }

    private void loadCombinedHistory() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        rows.clear();
        rows.add("🧠 RISK ASSESSMENTS\n─────────────────────────────");

        SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);

        // === Load RISK ASSESSMENTS ===
        Call<List<Assessment>> call1 = api.getAssessmentsByEmail(
                "eq." + email,
                "email,risk_level,risk_score,assessment,created_at", // ✅ added assessment field
                "created_at.desc",
                "10"
        );

        call1.enqueue(new Callback<List<Assessment>>() {
            @Override
            public void onResponse(Call<List<Assessment>> call, Response<List<Assessment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (Assessment a : response.body()) {
                        try {
                            String date = a.created_at.split("T")[0];
                            String reason = (a.assessment != null && !a.assessment.trim().isEmpty()) ? "\n" + a.assessment : "";
                            String formatted = date + " • " + a.risk_level.toUpperCase() + " (" + a.risk_score + ")" + reason;
                            rows.add(formatted);
                        } catch (Exception e) {
                            rows.add("Invalid date format");
                        }
                    }
                } else {
                    rows.add("No risk assessments found.");
                }

                // Load VITAL ASSESSMENTS next
                loadVitalAssessments(email);
            }

            @Override
            public void onFailure(Call<List<Assessment>> call, Throwable t) {
                rows.add("Failed to load risk assessments: " + t.getMessage());
                loadVitalAssessments(email);
            }
        });
    }

    private void loadVitalAssessments(String email) {
        rows.add("\n❤️ VITAL ASSESSMENTS\n─────────────────────────────");

        SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);
        Call<List<VitalSigns>> call2 = api.getVitalSignsByEmail(
                "eq." + email,
                "user_email,assessment,created_at",
                "created_at.desc",
                "10"
        );

        call2.enqueue(new Callback<List<VitalSigns>>() {
            @Override
            public void onResponse(Call<List<VitalSigns>> call, Response<List<VitalSigns>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasData = false;
                    for (VitalSigns v : response.body()) {
                        if (v.assessment != null && !v.assessment.trim().isEmpty()) {
                            hasData = true;
                            String date = v.created_at.contains("T")
                                    ? v.created_at.split("T")[0]
                                    : v.created_at.split(" ")[0];
                            String formatted = date + " • " + v.assessment;
                            rows.add(formatted);
                        }
                    }

                    if (!hasData) {
                        rows.add("No vital assessments found.");
                    }
                } else {
                    rows.add("No vital assessments found.");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<VitalSigns>> call, Throwable t) {
                rows.add("Failed to load vital assessments: " + t.getMessage());
                adapter.notifyDataSetChanged();
            }
        });
    }
}
