package com.Hand_Cricket.ads;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class AdManager {
    private AdManager() {}

    private static final class InstanceHolder {
        private static final AdManager instance = new AdManager();
    }

    public static AdManager getInstance() {
        return InstanceHolder.instance;
    }

    public void initialize(Application application) {
        MobileAds.initialize(application, initializationStatus -> {});
    }

}
