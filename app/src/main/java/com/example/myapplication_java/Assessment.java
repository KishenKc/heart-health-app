package com.example.myapplication_java;

public class Assessment {
    public String id;
    public String email;
    public String risk_level;
    public int risk_score;
    public String assessment; // ✅ this holds the short reason/explanation
    public String reason; // ✅ this stores the short reason summary
    public Object factors;     // or use Map<String, Object> if you prefer
    public String created_at;


}
