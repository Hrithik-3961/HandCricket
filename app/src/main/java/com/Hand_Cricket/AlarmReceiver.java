package com.Hand_Cricket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeating_intent = new Intent(context, HomeScreen.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(context, 100, repeating_intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Notification Channel")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.splash_logo)
                .setColor(ContextCompat.getColor(context, R.color.white))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.splash_logo))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentTitle("Game Invitation")
                .setContentText("Click here to play Hand Cricket now!")
                .setAutoCancel(true);
        assert notificationManager != null;
        notificationManager.notify(100, builder.build());
    }
}