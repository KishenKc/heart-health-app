package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileDetails extends AppCompatActivity {

    private EditText etFirstName, etLastName, etAge, etGender, etEmail;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details1);

        // ===== Initialize UI =====
        etFirstName = findViewById(R.id.etFirstName);
        etLastName  = findViewById(R.id.etLastName);
        etAge       = findViewById(R.id.etAge);
        etGender    = findViewById(R.id.etGender);
        etEmail     = findViewById(R.id.etEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnSaveProfile.setOnClickListener(v -> {
            if (validateInputs()) saveProfileToSupabase();
        });
    }

    private boolean validateInputs() {
        if (etFirstName.getText().toString().trim().isEmpty() ||
                etLastName.getText().toString().trim().isEmpty()  ||
                etAge.getText().toString().trim().isEmpty()       ||
                etGender.getText().toString().trim().isEmpty()    ||
                etEmail.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Integer.parseInt(etAge.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Age must be a number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveProfileToSupabase() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String age       = etAge.getText().toString().trim();
        String gender    = etGender.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();

        // ✅ Combine first + last name for greeting (local use only)
        String fullName = firstName + " " + lastName;

        try {
            // ✅ Match Supabase table column names exactly
            JSONObject row = new JSONObject();
            row.put("first_name", firstName);
            row.put("last_name", lastName);
            row.put("age", Integer.parseInt(age));
            row.put("gender", gender);
            row.put("email", email);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    "[" + row.toString() + "]"
            );

            Retrofit retrofit = SupabaseClient.getClient();
            SupabaseApi api = retrofit.create(SupabaseApi.class);

            api.insertProfile(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                    if (res.isSuccessful()) {
                        Toast.makeText(ProfileDetails.this,
                                "Profile saved successfully ✅", Toast.LENGTH_SHORT).show();

                        // ✅ Save locally for Dashboard greeting
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("first_name", fullName)
                                .putString("email", email)
                                .putString("age", age)
                                .putString("gender", gender)
                                .putBoolean("profile_completed", true)
                                .apply();

                        // ✅ Go to Dashboard
                        Intent intent = new Intent(ProfileDetails.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ProfileDetails.this,
                                "Supabase error: " + res.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(ProfileDetails.this,
                            "Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (JSONException e) {
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
