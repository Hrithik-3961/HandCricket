package com.Hand_Cricket.ads;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AppOpenAdHelper implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private final Application application;

    private AppOpenAd appOpenAd;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;

    private long loadTime = 0;

    private Activity currentActivity;

    public AppOpenAdHelper(Application application) {

        this.application = application;

        application.registerActivityLifecycleCallbacks(this);

        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(this);
    }

    // ------------------------
    // Lifecycle
    // ------------------------

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {

        showAdIfAvailable();
    }

    // ------------------------
    // Loading
    // ------------------------

    public void loadAd() {

        if (isLoadingAd || isAdAvailable()) {
            return;
        }

        isLoadingAd = true;

        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(
                application,
                AdUnitProvider.getAdUnits().getAppOpenId(),
                request,
                new AppOpenAd.AppOpenAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        isLoadingAd = false;
                        loadTime = SystemClock.elapsedRealtime();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isLoadingAd = false;
                    }
                });
    }

    // ------------------------
    // Show
    // ------------------------

    public void showAdIfAvailable() {

        if (isShowingAd) {
            return;
        }

        if (!isAdAvailable()) {
            loadAd();
            return;
        }

        if (currentActivity == null) {
            return;
        }

        appOpenAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        isShowingAd = true;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        appOpenAd = null;
                        isShowingAd = false;
                        loadAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        appOpenAd = null;
                        isShowingAd = false;
                        loadAd();
                    }
                });

        appOpenAd.show(currentActivity);
    }

    // ------------------------
    // Availability
    // ------------------------

    private boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long hours) {

        long currentTime = SystemClock.elapsedRealtime();
        return currentTime - loadTime < hours * 60 * 60 * 1000;
    }

    // ------------------------
    // Activity Tracking
    // ------------------------

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (!isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull android.os.Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}