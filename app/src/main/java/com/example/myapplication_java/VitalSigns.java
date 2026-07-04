package com.example.myapplication_java;

public class VitalSigns {
    public String id;
    public String user_email;
    public String heart_rate;
    public String blood_pressure;
    public String cholesterol;
    public String symptoms;
    public String created_at;
    public String assessment; // ✅ Added this line to match your Supabase column

    // Empty constructor (required for Retrofit/Supabase)
    public VitalSigns() {}

    // Constructor without assessment
    public VitalSigns(String user_email, String heart_rate, String blood_pressure,
                      String cholesterol, String symptoms) {
        this.user_email = user_email;
        this.heart_rate = heart_rate;
        this.blood_pressure = blood_pressure;
        this.cholesterol = cholesterol;
        this.symptoms = symptoms;
    }

    // Optional constructor including assessment
    public VitalSigns(String user_email, String heart_rate, String blood_pressure,
                      String cholesterol, String symptoms, String assessment) {
        this.user_email = user_email;
        this.heart_rate = heart_rate;
        this.blood_pressure = blood_pressure;
        this.cholesterol = cholesterol;
        this.symptoms = symptoms;
        this.assessment = assessment;
    }
}
