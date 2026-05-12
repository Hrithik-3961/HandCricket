package com.Hand_Cricket;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.Hand_Cricket.ads.AdManager;
import com.Hand_Cricket.ads.AppOpenAdHelper;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "Notification Channel";

    @Override
    public void onCreate() {
        super.onCreate();
        AdManager.getInstance().initialize(this);

        AppOpenAdHelper appOpenAdHelper = new AppOpenAdHelper(this);
        appOpenAdHelper.loadAd();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Game Invitation", NotificationManager.IMPORTANCE_HIGH);

        channel.setDescription("Game Invitation");

        NotificationManager manager = getSystemService(NotificationManager.class);
        assert manager != null;
        manager.createNotificationChannel(channel);
    }
}
