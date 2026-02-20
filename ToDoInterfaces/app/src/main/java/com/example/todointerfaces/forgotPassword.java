package com.example.todointerfaces;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todointerfaces.Utils.DatabaseHandler;
import com.example.todointerfaces.Utils.ThemeManager;

public class forgotPassword extends AppCompatActivity {

    private EditText editTextNewPassword,confirmPassword;
    private TextView textViewUserEmail;
    private Button buttonUpdatePassword;
    private DatabaseHandler db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHandler(this);
        db.openDataBase();
        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Error: Email not received.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }else if(!db.isEmailExists(userEmail)){
            Toast.makeText(this, "Error: This Email Dose not Exist.", Toast.LENGTH_LONG).show();
            finish();
            return;
        };
        textViewUserEmail = findViewById(R.id.EmailView);
        editTextNewPassword = findViewById(R.id.forgetPassE);
        buttonUpdatePassword = findViewById(R.id.submitfp);
        confirmPassword = findViewById(R.id.conpass);
        textViewUserEmail.setText("Resetting password for: " + userEmail);
        buttonUpdatePassword.setOnClickListener(v -> onUpdatePasswordClick());
    }

    public void onUpdatePasswordClick() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirm = editTextNewPassword.getText().toString().trim();
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPassword(newPassword)) {
            Toast.makeText(this,
                    "Password must be ≥ 8 chars, include upper, lower, number, and !@#$%",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!newPassword.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        db.updateUserPasswordByEmail(userEmail, newPassword);
        Toast.makeText(this, "Password for " + userEmail + " updated successfully! Please log in.", Toast.LENGTH_LONG).show();
        finish();
    }
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%".indexOf(c) >= 0) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}