package com.example.todointerfaces.Utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todointerfaces.HomePage;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String note  = intent.getStringExtra("note");
        int taskId   = intent.getIntExtra("taskId", 0);
        Intent next = new Intent(context, HomePage.class);
        next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                next,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(note)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat.from(context).notify(taskId, builder.build());
        Log.d("AlarmReceiver", "Alarm fired! Title: " + title);
    }
}


