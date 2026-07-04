package com.example.myapplication_java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PatientsActivity extends AppCompatActivity {

    public static final String EXTRA_AI_RESPONSE = "com.example.myapplication_java.AI_RESPONSE";

    Button btnCheckActivity;
    EditText inputActivity, inputDuration, inputCalories, inputDate, inputFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_activity);

        inputActivity = findViewById(R.id.patientActivityType);
        inputDuration = findViewById(R.id.patientDuration);
        inputCalories = findViewById(R.id.patientCalories);
        inputDate = findViewById(R.id.patientDate);
        inputFood = findViewById(R.id.patientFood);
        btnCheckActivity = findViewById(R.id.btnCheckActivity);

        btnCheckActivity.setOnClickListener(v -> {
            String activity = inputActivity.getText().toString().trim();
            String duration = inputDuration.getText().toString().trim();
            String calories = inputCalories.getText().toString().trim();
            String date = inputDate.getText().toString().trim();
            String food = inputFood.getText().toString().trim();

            boolean hasError = false;
            if (activity.isEmpty()) {
                inputActivity.setError("This field is required");
                hasError = true;
            }
            if (duration.isEmpty()) {
                inputDuration.setError("This field is required");
                hasError = true;
            }
            if (calories.isEmpty()) {
                inputCalories.setError("This field is required");
                hasError = true;
            }
            if (date.isEmpty()) {
                inputDate.setError("This field is required");
                hasError = true;
            }
            if (food.isEmpty()) {
                inputFood.setError("This field is required");
                hasError = true;
            }

            if (hasError) {
                Toast.makeText(PatientsActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(PatientsActivity.this, "Analyzing activity, please wait...", Toast.LENGTH_LONG).show();

            String prompt = "Analyze this patient’s activity:\n" +
                    "Activity: " + activity + "\n" +
                    "Duration: " + duration + " minutes\n" +
                    "Calories burned: " + calories + "\n" +
                    "Date: " + date + "\n" +
                    "Food intake: " + food + "\n\n" +
                    "Tell me if this is a good activity for the patient and suggest improvements.";

            // ✅ Use AIHelper callback (async response)
            AIHelper.askAI(prompt, new AIHelper.AIResponseCallback() {
                @Override
                public void onResponse(String reply) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(PatientsActivity.this, AIresult.class);
                        intent.putExtra(EXTRA_AI_RESPONSE, reply);
                        startActivity(intent);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(PatientsActivity.this, "AI Error: " + error, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });
    }
}
