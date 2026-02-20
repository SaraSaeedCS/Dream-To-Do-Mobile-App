package com.example.todointerfaces.Utils;
import android.content.Context;
import android.content.SharedPreferences;
public class UserSessionManager {
    private static final String PREF_NAME = "user_session_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    public void createLoginSession(int userId, String username) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);   // -1 = not logged in
    }
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
    public boolean isLoggedIn() {
        return getUserId() != -1;
    }
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
