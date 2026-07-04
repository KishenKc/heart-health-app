package com.example.myapplication_java;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    // 🔹 Your Supabase project details
    private static final String BASE_URL = "https://bsjbifnnjdahxlvljtlx.supabase.co/rest/v1/";
    private static final String API_KEY  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzamJpZm5uamRhaHhsdmxqdGx4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4NjUxMzEsImV4cCI6MjA3NjQ0MTEzMX0.YYZ_PvATgw_Kiub3taFY0Pc6vA5ak6lbaTqYvCBb5zI";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {

        // 🔹 Logging interceptor (shows requests and responses in Logcat)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 🔹 OkHttp client setup with required Supabase headers
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("apikey", API_KEY)
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(newRequest);
                })
                .addInterceptor(logging) // ✅ Ensures requests are logged
                .build();

        // 🔹 Retrofit client setup
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit;

    }
}
