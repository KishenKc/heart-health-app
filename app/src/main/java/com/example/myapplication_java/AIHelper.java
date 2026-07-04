package com.example.myapplication_java;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.concurrent.TimeUnit;

public class AIHelper {

    private static final String API_URL = "";
    private static final String API_KEY = "";  //

    // ✅ Optimized OkHttpClient for speed and reliability
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build();

    public interface AIResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    // ✅ Small dictionary for quick typo correction
    private static final Set<String> DICTIONARY = new HashSet<>(Arrays.asList(
            "heart", "disease", "risk", "assessment", "doctor", "health", "patient",
            "pressure", "cholesterol", "symptoms", "pain", "exercise", "diet", "nutrition",
            "lifestyle", "mental", "stress", "sleep", "hydration", "vitamins", "anxiety",
            "medicine", "cardio", "test", "diagnosis", "result", "healthy", "therapy"
    ));

    /**
     * ✅ Main method to send a user message to the AI
     */
    public static void askAI(String userMessage, AIResponseCallback callback) {
        long startTime = System.currentTimeMillis();

        try {
            // ✅ Step 1: Autocorrect input
            String correctedMessage = autoCorrectSentence(userMessage);

            // ✅ Step 2: Smart context injection (dynamic)
            // Automatically tell the AI that the app is health-focused
            String contextPrompt = "You are an intelligent and empathetic health & lifestyle assistant. " +
                    "Always answer accurately and clearly. Provide detail where necessary " +
                    "and offer practical, evidence-based advice on heart health, fitness, diet, sleep, " +
                    "and overall wellbeing. Avoid generic answers.";

            // ✅ Step 3: Prepare the request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "openai/gpt-3.5-turbo"); // fast, detailed model
            jsonBody.put("stream", false);

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", contextPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", correctedMessage));
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json")
            );

            // ✅ Step 4: Build request
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("HTTP-Referer", "https://openrouter.ai")
                    .addHeader("X-Title", "AI Health & Lifestyle Assistant")
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            // ✅ Step 5: Send async request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError("API error: " + response.code());
                            return;
                        }

                        // ✅ Handle compressed (gzip) responses safely
                        InputStream inputStream = response.body().byteStream();
                        String encoding = response.header("Content-Encoding");
                        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                            inputStream = new GZIPInputStream(inputStream);
                        }

                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);

                        String responseBody = sb.toString();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                        String content = message.getString("content").trim();

                        long endTime = System.currentTimeMillis();
                        android.util.Log.d("AIHelper", "Response time: " + (endTime - startTime) + "ms");
                        callback.onResponse(content);

                    } catch (Exception e) {
                        callback.onError("Error parsing AI response: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }

    // ✅ Auto-corrects simple misspellings using dictionary
    private static String autoCorrectSentence(String input) {
        StringBuilder corrected = new StringBuilder();
        for (String word : input.split(" ")) {
            String cleaned = word.toLowerCase().replaceAll("[^a-z]", "");
            if (DICTIONARY.contains(cleaned)) {
                corrected.append(word).append(" ");
            } else {
                String suggestion = findClosestWord(cleaned);
                corrected.append(suggestion.isEmpty() ? word : suggestion).append(" ");
            }
        }
        return corrected.toString().trim();
    }

    // ✅ Finds closest match using Levenshtein Distance
    private static String findClosestWord(String word) {
        int bestDistance = Integer.MAX_VALUE;
        String bestMatch = "";
        for (String dictWord : DICTIONARY) {
            int distance = levenshteinDistance(word, dictWord);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestMatch = dictWord;
            }
        }
        return bestDistance <= 2 ? bestMatch : "";
    }

    // ✅ Levenshtein algorithm for typo correction
    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                ), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
