package com.example.todointerfaces;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todointerfaces.Utils.DatabaseHandler;
import com.example.todointerfaces.Utils.ThemeManager;

public class SignUp extends AppCompatActivity {

    private EditText etUsername, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button signUpButton;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHandler(this);
        db.openDataBase();

        etUsername = findViewById(R.id.editTextText);
        etEmail    = findViewById(R.id.editTextText2);
        etPhone    = findViewById(R.id.editTextText5);
        etPassword = findViewById(R.id.editTextText4);
        etConfirmPassword = findViewById(R.id.editTextText3);
        signUpButton = findViewById(R.id.button3);

        signUpButton.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String username = etUsername.getText().toString().trim();
        String email    = etEmail.getText().toString().trim().toLowerCase();
        String phone    = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            if (username.isEmpty()) etUsername.setError("Required");
            if (email.isEmpty())    etEmail.setError("Required");
            if (phone.isEmpty())    etPhone.setError("Required");
            if (password.isEmpty()) etPassword.setError("Required");
            if (confirm.isEmpty())  etConfirmPassword.setError("Required");
            return;
        }
        if (db.isEmailExists(email)) {
            etEmail.setError("This email is already registered");
            Toast.makeText(this, "Email already in use", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPhone(phone)) {
            etPhone.setError("Phone must start with 05 and be 10 digits");
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError("Weak password");
            Toast.makeText(this,
                    "Password must be at least 8 characters, include upper, lower, number, and !@#$%",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = db.insertUser(username, email, phone, password);
        if (userId == -1) {
            Toast.makeText(this, "Error creating account. Please try again.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Account is successfully created! Please log in.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUp.this, LogIn.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        }
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

    private boolean isValidPhone(String phone) {
        if (phone.length() != 10) return false;
        if (!phone.startsWith("05")) return false;
        for (char c : phone.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

}
