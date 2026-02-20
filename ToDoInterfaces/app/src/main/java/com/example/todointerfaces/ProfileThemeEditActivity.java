package com.example.todointerfaces;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.todointerfaces.Utils.ThemeManager;

public class ProfileThemeEditActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.loadAndApplySavedTheme(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_theme_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_theme_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = "profile";
        Fragment fragment;
        if ("theme".equals(mode)) {
            fragment = new EditThemeFragment();
        }else if("about".equals(mode)){
            fragment = new aboutFragment();
        }
        else {
            fragment = new EditProfileFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

}
