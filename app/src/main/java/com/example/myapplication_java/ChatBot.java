package com.example.myapplication_java;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ChatBot extends AppCompatActivity {

    private EditText etMessage;
    private Button btnSend;
    private TextView tvChatLog;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AI Health Assistant");
        }

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvChatLog = findViewById(R.id.tvChatLog);
        scrollView = findViewById(R.id.scrollView);

        tvChatLog.setText("💬 Ask me anything about your heart health!\n");

        btnSend.setOnClickListener(v -> {
            String userMessage = etMessage.getText().toString().trim();
            if (userMessage.isEmpty()) return;

            appendMessage("🧍‍♂️ You", userMessage);
            etMessage.setText("");

            AIHelper.askAI(userMessage, new AIHelper.AIResponseCallback() {
                @Override
                public void onResponse(String reply) {
                    runOnUiThread(() -> appendMessage("🤖 AI", reply));
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> appendMessage("⚠️ Error", error));
                }
            });
        });
    }

    private void appendMessage(String sender, String message) {
        tvChatLog.append("\n" + sender + ": " + message + "\n");
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    // Handle back arrow press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Explicitly go back to Dashboard
            Intent intent = new Intent(ChatBot.this, Dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
