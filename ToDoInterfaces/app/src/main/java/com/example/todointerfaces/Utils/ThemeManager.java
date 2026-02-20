package com.example.todointerfaces.Utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager extends BroadcastReceiver {
    public static final String PREFS_NAME = "AppThemePrefs";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final int MODE_DEFAULT = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final int MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES;
    @Override
        public void onReceive(Context context, Intent intent) {
            int mode=AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            if(intent.getAction().equals("com.example.light")){
                Toast.makeText(context,"light mode",Toast.LENGTH_SHORT).show();
                mode=AppCompatDelegate.MODE_NIGHT_NO;
            } else if(intent.getAction().equals("com.example.dark")){
                Toast.makeText(context,"dark mode",Toast.LENGTH_SHORT).show();
                mode=AppCompatDelegate.MODE_NIGHT_YES;
            }
            AppCompatDelegate.setDefaultNightMode(mode);
            prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        }
    public static void loadAndApplySavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedMode = prefs.getInt(KEY_THEME_MODE, MODE_DEFAULT);
        AppCompatDelegate.setDefaultNightMode(savedMode);

    }


}
