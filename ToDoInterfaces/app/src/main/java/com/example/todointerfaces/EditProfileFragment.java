package com.example.todointerfaces;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Patterns;

import com.example.todointerfaces.Utils.DatabaseHandler;
import com.example.todointerfaces.Utils.UserSessionManager;


public class EditProfileFragment extends Fragment {
    private TextView textEmailValue;
    private TextView textPasswordValue;
    private Button buttonEditEmail;
    private Button buttonEditPassword;
    private String currentEmail = "";
    private DatabaseHandler db;
    private UserSessionManager session;
    private int currentUserId = -1;
    public EditProfileFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        textEmailValue = view.findViewById(R.id.text_email_value);
        textPasswordValue = view.findViewById(R.id.text_password_value);
        buttonEditEmail = view.findViewById(R.id.button_edit_email);
        buttonEditPassword = view.findViewById(R.id.button_edit_password);
        db = new DatabaseHandler(requireContext());
        db.openDataBase();

        session = new UserSessionManager(requireContext());
        currentUserId = session.getUserId();
        if (currentUserId != -1) {
            String emailFromDb = db.getUserEmailById(currentUserId);
            if (emailFromDb != null) {
                currentEmail = emailFromDb;
            }
        }
        textEmailValue.setText(currentEmail);
        textPasswordValue.setText("••••••••"); // we never show real password
        buttonEditEmail.setOnClickListener(v -> showEditEmailDialog());
        buttonEditPassword.setOnClickListener(v -> showEditPasswordDialog());
        return view;
    }
    private void showEditEmailDialog() {
        if (getContext() == null) return;
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setText(currentEmail);
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Email")
                .setMessage("Enter your new email address:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newEmail = input.getText().toString().trim();
                    if (newEmail.isEmpty()) {
                        Toast.makeText(getContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isValidEmail(newEmail)) {
                        Toast.makeText(getContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newEmail.equalsIgnoreCase(currentEmail)) {
                        if (db.isEmailExists(newEmail)) {
                            Toast.makeText(getContext(), "This email is already used", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if (currentUserId != -1) {
                        db.updateUserEmail(currentUserId, newEmail);
                        currentEmail = newEmail;
                        textEmailValue.setText(currentEmail);
                        Toast.makeText(getContext(), "Email updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void showEditPasswordDialog() {
        if (getContext() == null) return;
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Password")
                .setMessage("Enter your new password:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newPassword = input.getText().toString();
                    if (newPassword.isEmpty()) {
                        Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isValidPassword(newPassword)) {
                        Toast.makeText(
                                getContext(),
                                "Password must be ≥ 8 chars, include upper, lower, number, and !@#$%",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }
                    if (currentUserId != -1) {
                        db.updateUserPassword(currentUserId, newPassword);
                        textPasswordValue.setText("••••••••");
                        Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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

}
