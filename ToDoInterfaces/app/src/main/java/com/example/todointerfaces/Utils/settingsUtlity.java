package com.example.todointerfaces.Utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class settingsUtlity {
        public static boolean areNotificationsEnabled(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For API 26+ devices see if notifications enabled.
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                return notificationManager.areNotificationsEnabled();
            } else {
                return true;
            }
        }
        public static void openNotificationSettings(Context context) {
            Intent intent = new Intent();
            // Android 8.0 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            }
            // Android 5.0  to 7.1
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else {
                // old devices
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
            }
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Could not open settings automatically.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
}
