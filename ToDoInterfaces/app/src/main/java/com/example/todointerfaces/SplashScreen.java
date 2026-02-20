package com.example.todointerfaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todointerfaces.Utils.ThemeManager;
import com.example.todointerfaces.Utils.UserSessionManager;

public class SplashScreen extends AppCompatActivity {
    private static final int SPLASH_SCREEN_TIMEOUT = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        new Handler().postDelayed(() -> {
            UserSessionManager session = new UserSessionManager(SplashScreen.this);
            Intent intent;
            if (session.isLoggedIn()) {
                intent = new Intent(SplashScreen.this, HomePage.class);
            } else {
                intent = new Intent(SplashScreen.this, MainPage.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_TIMEOUT);
    }
}
