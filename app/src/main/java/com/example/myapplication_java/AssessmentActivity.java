package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssessmentActivity extends AppCompatActivity {

    private TextView tvInputsPreview, tvResult;
    private Button btnGenerateAssessment, btnViewHistory, btnDashboard;
    private LifestyleAnswers latest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        // ✅ Initialize views
        tvInputsPreview = findViewById(R.id.tvInputsPreview);
        tvResult = findViewById(R.id.tvResult);
        btnGenerateAssessment = findViewById(R.id.btnGenerateAssessment);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnDashboard = findViewById(R.id.btnDashboard);

        // ✅ Dashboard button click
        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, Dashboard.class);
                startActivity(intent);
                finish();
            });
        }

        // ✅ View history click
        btnViewHistory.setOnClickListener(v ->
                startActivity(new Intent(this, AssessmentHistoryActivity.class))
        );

        // ✅ Load the latest lifestyle answers from Supabase
        loadLatestLifestyleAnswers();

        // ✅ Generate assessment button click
        btnGenerateAssessment.setOnClickListener(v -> {
            if (latest == null) {
                Toast.makeText(this, "No answers found. Please complete your lifestyle details first.", Toast.LENGTH_LONG).show();
                return;
            }

            Assessment result = runRiskEngine(latest);
            tvResult.setText(String.format(Locale.getDefault(),
                    "%s Risk (%d/10)\n%s",
                    result.risk_level,
                    result.risk_score,
                    result.reason));

            insertAssessment(result);
        });
    }

    // 🔹 Load the most recent lifestyle answers
    private void loadLatestLifestyleAnswers() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);
        if (email == null) {
            Toast.makeText(this, "Email not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);
        Call<List<LifestyleAnswers>> call = api.getLifestyleAnswersByEmail(
                "eq." + email,
                "email,smoke,alcohol,exercise,diet,bp,cholesterol,diabetes,family_history,stress,chest_pain,created_at",
                "created_at.desc",
                "1"
        );

        call.enqueue(new Callback<List<LifestyleAnswers>>() {
            @Override
            public void onResponse(Call<List<LifestyleAnswers>> call, Response<List<LifestyleAnswers>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    latest = response.body().get(0);
                    tvInputsPreview.setText(formatPreview(latest));
                } else {
                    tvInputsPreview.setText("(No lifestyle answers found)");
                }
            }

            @Override
            public void onFailure(Call<List<LifestyleAnswers>> call, Throwable t) {
                Toast.makeText(AssessmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // 🔹 Format the user's lifestyle answers for preview display
    private String formatPreview(LifestyleAnswers a) {
        return "Smoke: " + a.smoke + "\n" +
                "Alcohol: " + a.alcohol + "\n" +
                "Exercise: " + a.exercise + "\n" +
                "Diet: " + a.diet + "\n" +
                "BP: " + a.bp + "\n" +
                "Cholesterol: " + a.cholesterol + "\n" +
                "Diabetes: " + a.diabetes + "\n" +
                "Family history: " + a.family_history + "\n" +
                "Stress: " + a.stress + "\n" +
                "Chest pain: " + a.chest_pain;
    }

    // 🔹 Run the AI-style heart risk scoring system
    private Assessment runRiskEngine(LifestyleAnswers a) {
        int score = 0;
        StringBuilder reason = new StringBuilder();

        if ("Yes".equalsIgnoreCase(a.smoke)) { score += 3; reason.append("Smokes regularly. "); }
        if ("Yes, often".equalsIgnoreCase(a.alcohol)) { score += 2; reason.append("Drinks alcohol often. "); }
        else if ("Occasionally".equalsIgnoreCase(a.alcohol)) { score += 1; reason.append("Drinks occasionally. "); }

        if ("No, rarely".equalsIgnoreCase(a.exercise)) { score += 2; reason.append("Rarely exercises. "); }
        else if ("Sometimes".equalsIgnoreCase(a.exercise)) { score += 1; reason.append("Exercises sometimes. "); }

        if ("No, mostly unhealthy".equalsIgnoreCase(a.diet)) { score += 2; reason.append("Unhealthy diet. "); }
        else if ("Somewhat healthy".equalsIgnoreCase(a.diet)) { score += 1; reason.append("Diet could improve. "); }

        if ("Yes".equalsIgnoreCase(a.bp)) { score += 2; reason.append("High blood pressure. "); }
        if ("Yes".equalsIgnoreCase(a.cholesterol)) { score += 2; reason.append("High cholesterol. "); }
        if ("Yes".equalsIgnoreCase(a.diabetes)) { score += 3; reason.append("Has diabetes. "); }
        if ("Yes".equalsIgnoreCase(a.family_history)) { score += 2; reason.append("Family heart disease history. "); }
        if ("Yes, often".equalsIgnoreCase(a.stress)) { score += 1; reason.append("Often stressed. "); }
        if ("Yes".equalsIgnoreCase(a.chest_pain)) { score += 3; reason.append("Chest pain reported. "); }

        // ✅ Cap score to stay within 0–10
        if (score > 10) score = 10;
        if (score < 0) score = 0;

        String level;
        if (score >= 8) level = "HIGH";
        else if (score >= 4) level = "MEDIUM";
        else level = "LOW";

        String reasonText;
        if (level.equals("HIGH"))
            reasonText = "At high risk due to " + reason.toString().trim();
        else if (level.equals("MEDIUM"))
            reasonText = "Moderate risk — " + reason.toString().trim();
        else
            reasonText = "Low risk — overall healthy lifestyle.";

        Assessment as = new Assessment();
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        as.email = prefs.getString("email", null);
        as.risk_level = level;
        as.risk_score = score;
        as.reason = reasonText;

        HashMap<String, Object> factors = new HashMap<>();
        factors.put("smoke", a.smoke);
        factors.put("alcohol", a.alcohol);
        factors.put("exercise", a.exercise);
        factors.put("diet", a.diet);
        factors.put("bp", a.bp);
        factors.put("cholesterol", a.cholesterol);
        factors.put("diabetes", a.diabetes);
        factors.put("family_history", a.family_history);
        factors.put("stress", a.stress);
        factors.put("chest_pain", a.chest_pain);
        as.factors = factors;

        return as;
    }

    // 🔹 Save the generated assessment to Supabase
    private void insertAssessment(Assessment as) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", as.email);
            json.put("risk_level", as.risk_level);
            json.put("risk_score", as.risk_score);
            json.put("assessment", as.reason);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    "[" + json.toString() + "]"
            );

            SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);
            api.insertAssessment(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AssessmentActivity.this, "Assessment saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AssessmentActivity.this, "Supabase error: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AssessmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
