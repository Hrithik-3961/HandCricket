package com.Hand_Cricket.ads;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class RewardedAdHelper {

    private final Activity activity;

    private RewardedAd rewardedAd;

    public RewardedAdHelper(Activity activity) {
        this.activity = activity;
    }

    public void load() {

        AdRequest request = new AdRequest.Builder().build();

        RewardedAd.load(
                activity,
                AdUnitProvider.getAdUnits().getRewardedId(),
                request,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        Log.d("RewardedAd", "Ad loaded successfully");
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.e("RewardedAd", "Failed to load: " + error.getMessage() + " (Code: " + error.getCode() + ")");
                        rewardedAd = null;
                    }

                });

    }

    public void show(Listener listener) {
        if (rewardedAd == null) {
            load();
            return;
        }

        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                rewardedAd = null;
                load();
                listener.onAdClosed();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                rewardedAd = null;
                listener.onAdFailed();
                load();
            }
        });

        rewardedAd.show(activity, rewardItem -> listener.onRewardEarned());
    }

    public boolean isAdAvailable() {
        return rewardedAd != null;
    }

    public interface Listener {
        void onRewardEarned();
        void onAdFailed();

        void onAdClosed();
    }

}
