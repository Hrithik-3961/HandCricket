package com.Hand_Cricket.ads;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class InterstitialAdHelper {

    private final Activity activity;

    private InterstitialAd interstitialAd;
    private boolean isLoading = false;

    public InterstitialAdHelper(Activity activity) {
        this.activity = activity;
    }

    public void load() {

        if (isLoading || interstitialAd != null) {
            return;
        }

        isLoading = true;

        AdRequest request = new AdRequest.Builder().build();

        InterstitialAd.load(
                activity,
                AdUnitProvider.getAdUnits().getInterstitialId(),
                request,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        isLoading = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        interstitialAd = null;
                        isLoading = false;
                    }

                });

    }

    public void show(Listener listener) {

        if (interstitialAd == null) {
            listener.onAdClosedOrFailed();
            load();
            return;

        }

        interstitialAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        interstitialAd = null;
                        listener.onAdClosedOrFailed();
                        load();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        interstitialAd = null;
                        listener.onAdClosedOrFailed();
                        load();
                    }
                });

        interstitialAd.show(activity);

    }

    public interface Listener {
        void onAdClosedOrFailed();
    }

}
