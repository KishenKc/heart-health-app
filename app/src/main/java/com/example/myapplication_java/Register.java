package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    Button btnRegComp;
    ImageButton btnBack;
    EditText inputName, inputEmail, inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ✅ Initialize back button (ImageButton, NOT Toolbar)
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Toast.makeText(this, "Going back...", Toast.LENGTH_SHORT).show();
            finish();
        });

        // ✅ Initialize input fields
        inputName = findViewById(R.id.RegFullName);
        inputEmail = findViewById(R.id.RegEmail);
        inputPassword = findViewById(R.id.RegPassword);
        btnRegComp = findViewById(R.id.btnRegComp);

        // ✅ Register button click
        btnRegComp.setOnClickListener(view -> {
            String nameStored = inputName.getText().toString().trim();
            String emailStored = inputEmail.getText().toString().trim();
            String passwordStored = inputPassword.getText().toString().trim();

            // ✅ Check for empty fields
            if (nameStored.isEmpty() || emailStored.isEmpty() || passwordStored.isEmpty()) {
                Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Password rule: must be at least 8 characters
            if (passwordStored.length() < 8) {
                inputPassword.setError("Password must be at least 8 characters long");
                inputPassword.requestFocus();
                return;
            }

            // ✅ Hash password before sending to Supabase
            String hashedPassword = PasswordHasher.hashPassword(passwordStored);
            insertUserToSupabase(nameStored, emailStored, hashedPassword);
        });
    }

    private void insertUserToSupabase(String name, String email, String password) {
        SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);

        User newUser = new User();
        newUser.email = email;
        newUser.password = password;
        newUser.created_at = null;
        newUser.id = null;

        List<User> userList = new ArrayList<>();
        userList.add(newUser);

        Call<List<User>> call = api.insertUsers(userList);

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("profile_completed", false).apply();

                    Intent intent = new Intent(Register.this, ProfileDetails.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Register.this, "Error: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(Register.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Handle device back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}