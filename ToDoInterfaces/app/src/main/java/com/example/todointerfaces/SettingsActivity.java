package com.example.todointerfaces;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.todointerfaces.Utils.DatabaseHandler;
import com.example.todointerfaces.Utils.ThemeManager;
import com.example.todointerfaces.Utils.UserSessionManager;
import com.example.todointerfaces.Utils.settingsUtlity;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
public class SettingsActivity extends AppCompatActivity {

    private LinearLayout rowEditProfile;
    private LinearLayout rowEditTheme;
    private LinearLayout rowDeleteAccount;
    private LinearLayout rowAbout;
    private Switch switchNotifications;
    private ImageView imageProfile;
    private ImageView iconEditProfilePic;
    private LinearLayout rowLogout;
    private static final String PREFS_NAME = "app_prefs";
    private static final String IMAGE_FILE = "profile_image.jpg";
    private ActivityResultLauncher<String> pickImageLauncher;
    private static final String KEY_PROFILE_PIC_URI = "profile_pic_uri";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        imageProfile = findViewById(R.id.image_profile);
        iconEditProfilePic = findViewById(R.id.icon_edit_profile_pic);
        rowEditProfile = findViewById(R.id.row_edit_profile);
        rowEditTheme = findViewById(R.id.row_edit_theme);
        rowAbout = findViewById(R.id.row_about);
        rowDeleteAccount = findViewById(R.id.row_delete_account);
        switchNotifications = findViewById(R.id.switch_notifications);
        rowLogout = findViewById(R.id.row_logout);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            saveImageToInternalStorage(uri);
                        }
                    }
                }
        );
        imageProfile.setOnClickListener(v -> openImagePicker());
        iconEditProfilePic.setOnClickListener(v -> openImagePicker());
        switchNotifications.setChecked(settingsUtlity.areNotificationsEnabled(this));
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsUtlity.openNotificationSettings(SettingsActivity.this);
            Toast.makeText(SettingsActivity.this, "Manage notification settings in the system app settings.", Toast.LENGTH_LONG).show();

        });
        loadSavedProfileImage();
        rowEditProfile.setOnClickListener(v -> openProfileThemeEdit("profile"));
        rowEditTheme.setOnClickListener(v -> openProfileThemeEdit("theme"));
        rowAbout.setOnClickListener(v -> openProfileThemeEdit("about"));
        rowDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        rowLogout.setOnClickListener(v -> showLogoutDialog());
    }
    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }
    private void saveImageToInternalStorage(Uri sourceUri) {
        try {
            InputStream input = getContentResolver().openInputStream(sourceUri);
            File outFile = new File(getFilesDir(), IMAGE_FILE);
            OutputStream output = new FileOutputStream(outFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            input.close();
            output.close();
            imageProfile.setImageURI(Uri.fromFile(outFile));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadSavedProfileImage() {
        File savedImage = new File(getFilesDir(), IMAGE_FILE);
        if (savedImage.exists()) {
            imageProfile.setImageURI(Uri.fromFile(savedImage));
        }
    }
    private void openProfileThemeEdit(String mode) {
        Intent intent = new Intent(this, ProfileThemeEditActivity.class);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (DialogInterface dialog, int which) -> {
                    UserSessionManager session = new UserSessionManager(SettingsActivity.this);
                    int userId = session.getUserId();
                    if (userId > 0) {
                        DatabaseHandler db = new DatabaseHandler(SettingsActivity.this);
                        db.openDataBase();
                        db.deleteUserAndTasks(userId);
                        db.close();
                        deleteProfileImage();
                        session.logout();
                        Toast.makeText(SettingsActivity.this, "Your account has been successfully deleted.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SettingsActivity.this, MainPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
                    }                   
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfileImage() {
        File savedImage = new File(getFilesDir(), IMAGE_FILE);
        if (savedImage.exists()) {
            if (savedImage.delete()) {
                Log.d("SettingsActivity", "Profile image deleted successfully.");
            } else {
                 Log.e("SettingsActivity", "Failed to delete profile image.");
            }
        }
    }
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    UserSessionManager session = new UserSessionManager(this);
                    session.logout();
                    Intent intent = new Intent(SettingsActivity.this, MainPage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    @Override
    protected void onResume() {
        super.onResume();
        boolean isEnabled = settingsUtlity.areNotificationsEnabled(this);
        switchNotifications.setChecked(isEnabled);
    }

}
