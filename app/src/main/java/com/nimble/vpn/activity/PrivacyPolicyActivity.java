package com.nimble.vpn.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.webkit.WebView;

import com.nimble.vpn.BuildConfig;
import com.nimble.vpn.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    Toolbar toolbar;
    WebView privacy_policy_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        privacy_policy_view = findViewById(R.id.privacy_policy_view);
        //load Privacy Policy HTML document
        privacy_policy_view.loadUrl(BuildConfig.PRIVACY_POLICY);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
