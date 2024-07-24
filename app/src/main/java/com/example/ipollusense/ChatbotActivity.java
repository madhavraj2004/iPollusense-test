package com.example.ipollusense;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class ChatbotActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        WebView webView = findViewById(R.id.webview_chatbot);
        webView.setWebViewClient(new WebViewClient());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // Enable DOM storage
        webSettings.setLoadWithOverviewMode(true); // Load webpage in overview mode
        webSettings.setUseWideViewPort(true); // Enable viewport to fit the webpage

        // Load the URL of your chatbot
        webView.loadUrl("https://madhavraj2004.github.io/chatbot-integration/");
    }
}
