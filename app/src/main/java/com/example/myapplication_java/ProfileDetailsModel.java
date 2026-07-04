package com.example.myapplication_java;

public class ProfileDetailsModel {

    public String id;          // uuid
    public String user_id;     // uuid (nullable)
    public String first_name;  // text
    public String last_name;   // text
    public int age;            // int4
    public String email;       // text
    public String gender;      // text

    // Optional helper for combining names
    public String getFullName() {
        return first_name + " " + last_name;
    }
}
