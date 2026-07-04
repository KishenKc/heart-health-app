package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewProfile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvAge, tvGender, tvInitial;
    private ImageView profileAvatar;
    private Button btnBackToDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // ✅ Link UI elements
        profileAvatar = findViewById(R.id.profileAvatar);
        tvInitial = findViewById(R.id.tvInitial);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvAge = findViewById(R.id.tvProfileAge);
        tvGender = findViewById(R.id.tvProfileGender);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        // ✅ Load user data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String firstName = prefs.getString("first_name", "User");
        String lastName = prefs.getString("last_name", "");
        String email = prefs.getString("email", "Not provided");
        String age = prefs.getString("age", "N/A");
        String gender = prefs.getString("gender", "N/A");

        // ✅ Set full name
        String fullName = firstName.trim() + (lastName.isEmpty() ? "" : " " + lastName.trim());
        tvName.setText(fullName);
        tvEmail.setText(email);
        tvAge.setText("Age: " + age);
        tvGender.setText("Gender: " + gender);

        // ✅ Create initial avatar
        if (!firstName.isEmpty()) {
            String initial = String.valueOf(firstName.charAt(0)).toUpperCase();
            tvInitial.setText(initial);
        } else {
            tvInitial.setText("U");
        }

        // ✅ Optional: set placeholder image for avatar
        profileAvatar.setImageResource(R.drawable.ic_profile);

        // ✅ Handle back button
        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProfile.this, Dashboard.class);
            startActivity(intent);
            finish();
        });
    }
}
