package com.example.myapplication_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    Button btnLogin_login;
    EditText inputName, inputPassword;
    TextView signUpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputName = findViewById(R.id.LoginName);
        inputPassword = findViewById(R.id.LoginPassword);
        btnLogin_login = findViewById(R.id.btnLogin_Login);
        signUpTextView = findViewById(R.id.signUpTextView);

        // 🔹 Redirect to Register
        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        // 🔹 Handle Login button
        btnLogin_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = inputName.getText().toString().trim();
                String passwordInput = inputPassword.getText().toString().trim();

                if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                    Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                verifyUser(emailInput, passwordInput);
            }
        });
    }

    private void verifyUser(String email, String enteredPassword) {
        SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);
        Call<List<User>> call = api.getUsers("*");

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    boolean found = false;

                    for (User u : users) {
                        if (u.email != null && u.email.equalsIgnoreCase(email)) {
                            found = true;
                            boolean ok = PasswordHasher.verifyPassword(enteredPassword, u.password);

                            if (ok) {
                                // ✅ Clear any previous session before saving new user
                                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.clear();
                                editor.apply();

                                Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                // ✅ Save new user info
                                editor.putString("email", u.email);
                                editor.putBoolean("profile_completed", true);
                                editor.apply();

                                // ✅ Fetch corresponding profile info from Supabase
                                loadUserProfile(u.email);
                                return;
                            } else {
                                Toast.makeText(Login.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }

                    if (!found) {
                        Toast.makeText(Login.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, "Error: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(Login.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Fetch the logged-in user's profile details from Supabase
    private void loadUserProfile(String email) {
        SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);
        Call<List<ProfileDetailsModel>> call = api.getProfilesByEmail("eq." + email, "*");

        call.enqueue(new Callback<List<ProfileDetailsModel>>() {
            @Override
            public void onResponse(Call<List<ProfileDetailsModel>> call, Response<List<ProfileDetailsModel>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ProfileDetailsModel profile = response.body().get(0);

                    String firstName = profile.first_name;
                    String lastName = profile.last_name;
                    String fullName = firstName + " " + lastName;

                    // ✅ Save current user info locally
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("first_name", fullName);
                    editor.putString("email", profile.email);
                    editor.putString("gender", profile.gender);
                    editor.putString("age", String.valueOf(profile.age));
                    editor.putBoolean("profile_completed", true);
                    editor.apply();

                    // ✅ Go to Dashboard
                    Intent intent = new Intent(Login.this, Dashboard.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(Login.this, "Profile not found for user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProfileDetailsModel>> call, Throwable t) {
                Toast.makeText(Login.this, "Failed to fetch profile: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
