package com.example.myapplication_java;

import java.util.List;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // ===== USERS =====
    @GET("users")
    Call<List<User>> getUsers(@Query("select") String select);

    @Headers({
            "Prefer: return=representation",
            "Content-Type: application/json"
    })
    @POST("users")
    Call<List<User>> insertUsers(@Body List<User> users);

    // ===== PROFILES =====
    @Headers({
            "Prefer: return=representation",
            "Content-Type: application/json"
    })
    @POST("profiles")
    Call<ResponseBody> insertProfile(@Body RequestBody body);

    // ✅ FIXED: Use ProfileDetailsModel for Retrofit mapping
    @GET("profiles")
    Call<List<ProfileDetailsModel>> getProfilesByEmail(
            @Query("email") String emailEq,
            @Query("select") String select
    );

    // ===== LIFESTYLE ANSWERS =====
    @Headers({
            "Prefer: resolution=merge-duplicates",
            "Prefer: return=representation",
            "Content-Type: application/json"
    })
    @POST("lifestyle_answers?on_conflict=email")
    Call<ResponseBody> upsertLifestyleAnswers(@Body RequestBody body);

    @GET("lifestyle_answers")
    Call<List<LifestyleAnswers>> getLifestyleAnswersByEmail(
            @Query("email") String emailEq,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") String limit
    );

    // ===== VITAL SIGNS =====
    @Headers({
            "Prefer: return=representation",
            "Content-Type: application/json"
    })
    @POST("vital_signs")
    Call<ResponseBody> insertVitalSigns(@Body RequestBody body);

    // ✅ Proper filtering and full select for assessments
    @GET("vital_signs")
    Call<List<VitalSigns>> getVitalSignsByEmail(
            @Query("user_email") String emailEq,      // e.g. "eq.user@example.com"
            @Query("select") String select,           // e.g. "user_email,assessment,created_at"
            @Query("order") String order,             // e.g. "created_at.desc"
            @Query("limit") String limit              // optional
    );

    // Optional: Without limit
    @GET("vital_signs")
    Call<List<VitalSigns>> getVitalSigns(
            @Query("user_email") String emailEq,
            @Query("select") String select,
            @Query("order") String order
    );

    // ===== ASSESSMENTS =====
    @Headers({
            "Prefer: return=representation",
            "Content-Type: application/json"
    })
    @POST("assessments")
    Call<ResponseBody> insertAssessment(@Body RequestBody body);

    @GET("assessments")
    Call<List<Assessment>> getAssessmentsByEmail(
            @Query("email") String emailEq,          // e.g. "eq.user@example.com"
            @Query("select") String select,          // e.g. "email,risk_level,risk_score,created_at"
            @Query("order") String order,            // e.g. "created_at.desc"
            @Query("limit") String limit
    );
}
