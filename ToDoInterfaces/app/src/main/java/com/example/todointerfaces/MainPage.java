package com.example.todointerfaces;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todointerfaces.Utils.ThemeManager;

public class MainPage extends AppCompatActivity {

    Button signUpBtn, loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signUpBtn = findViewById(R.id.button);
        loginBtn  = findViewById(R.id.button2);

        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainPage.this, SignUp.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainPage.this, LogIn.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

}
