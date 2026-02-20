package com.example.todointerfaces;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todointerfaces.Utils.DatabaseHandler;
import com.example.todointerfaces.Utils.ThemeManager;
import com.example.todointerfaces.Utils.UserSessionManager;

public class LogIn extends AppCompatActivity {

    EditText uET, pET;
    Button logiInBtn;
    TextView passT;
    private DatabaseHandler db;
    private UserSessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        passT=findViewById(R.id.Fpass);
        uET = findViewById(R.id.editTU);
        pET = findViewById(R.id.editpass);
        logiInBtn = findViewById(R.id.loginBT);
        db = new DatabaseHandler(this);
        db.openDataBase();
        session = new UserSessionManager(this);
        String prefillEmail = getIntent().getStringExtra("email");
        if (prefillEmail != null) {
            uET.setText(prefillEmail);
        }
        passT.setOnClickListener(v->passActivity());
        logiInBtn.setOnClickListener(v -> handleLogin());
    }
    private void passActivity() {
        String email = uET.getText().toString().trim().toLowerCase();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            if (email.isEmpty()) uET.setError("Required");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uET.setError("Invalid email format");
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, forgotPassword.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
    private void handleLogin() {
        String email = uET.getText().toString().trim().toLowerCase();
        String password = pET.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            if (email.isEmpty()) uET.setError("Required");
            if (password.isEmpty()) pET.setError("Required");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uET.setError("Invalid email format");
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        int userId = db.getUserIdByEmailPassword(email, password);


        if (userId == -1) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        } else {
            session.createLoginSession(userId, email);


            Intent intent = new Intent(LogIn.this, HomePage.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

}

