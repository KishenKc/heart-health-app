package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDetailsUpdate extends AppCompatActivity {

    Spinner spnSmoke, spnAlcohol, spnExercise, spnDiet, spnBP, spnCholesterol, spnDiabetes, spnFamily, spnStress, spnChestPain;
    Button btnUpdateAnswers, btnBackDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_details_update);

        // ✅ Link to XML views
        spnSmoke = findViewById(R.id.spnSmoke);
        spnAlcohol = findViewById(R.id.spnAlcohol);
        spnExercise = findViewById(R.id.spnExercise);
        spnDiet = findViewById(R.id.spnDiet);
        spnBP = findViewById(R.id.spnBP);
        spnCholesterol = findViewById(R.id.spnCholesterol);
        spnDiabetes = findViewById(R.id.spnDiabetes);
        spnFamily = findViewById(R.id.spnFamily);
        spnStress = findViewById(R.id.spnStress);
        spnChestPain = findViewById(R.id.spnChestPain);

        btnUpdateAnswers = findViewById(R.id.btnUpdateAnswers);
        btnBackDashboard = findViewById(R.id.btnBackDashboard);

        setupSpinners();

        // ✅ Save to Supabase (Upsert)
        btnUpdateAnswers.setOnClickListener(v -> saveOrUpdateSupabase());

        // ✅ Back to Dashboard
        btnBackDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailsUpdate.this, Dashboard.class);
            startActivity(intent);
            finish();
        });
    }

    // 🔹 Setup dropdown options
    private void setupSpinners() {
        setSpinnerOptions(spnSmoke, new String[]{"Yes", "No"});
        setSpinnerOptions(spnAlcohol, new String[]{"Yes, often", "Occasionally", "Never"});
        setSpinnerOptions(spnExercise, new String[]{"Yes, regularly", "Sometimes", "No, rarely"});
        setSpinnerOptions(spnDiet, new String[]{"Yes, balanced and healthy", "Somewhat healthy", "No, mostly unhealthy"});
        setSpinnerOptions(spnBP, new String[]{"Yes", "No", "Not sure"});
        setSpinnerOptions(spnCholesterol, new String[]{"Yes", "No", "Not sure"});
        setSpinnerOptions(spnDiabetes, new String[]{"Yes", "No"});
        setSpinnerOptions(spnFamily, new String[]{"Yes", "No", "Not sure"});
        setSpinnerOptions(spnStress, new String[]{"Yes, often", "Sometimes", "Rarely"});
        setSpinnerOptions(spnChestPain, new String[]{"Yes", "Occasionally", "Never"});
    }

    private void setSpinnerOptions(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // 🔹 Save or update answers in Supabase
    private void saveOrUpdateSupabase() {
        try {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String email = prefs.getString("email", null);

            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "❗Email not found. Please complete your profile first.", Toast.LENGTH_LONG).show();
                return;
            }

            // Add timestamp (optional)
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("smoke", spnSmoke.getSelectedItem().toString());
            json.put("alcohol", spnAlcohol.getSelectedItem().toString());
            json.put("exercise", spnExercise.getSelectedItem().toString());
            json.put("diet", spnDiet.getSelectedItem().toString());
            json.put("bp", spnBP.getSelectedItem().toString());
            json.put("cholesterol", spnCholesterol.getSelectedItem().toString());
            json.put("diabetes", spnDiabetes.getSelectedItem().toString());
            json.put("family_history", spnFamily.getSelectedItem().toString());
            json.put("stress", spnStress.getSelectedItem().toString());
            json.put("chest_pain", spnChestPain.getSelectedItem().toString());
            json.put("created_at", currentTime);

            // ✅ Correct JSON format for Supabase upsert
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    "[" + json.toString() + "]"
            );

            SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);
            Call<ResponseBody> call = api.upsertLifestyleAnswers(body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PatientDetailsUpdate.this, "✅ Answers saved/updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Toast.makeText(PatientDetailsUpdate.this, "❌ Supabase error: " + response.code() + "\n" + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PatientDetailsUpdate.this, "⚠️ Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
