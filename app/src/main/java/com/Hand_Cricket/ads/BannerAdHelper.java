package com.Hand_Cricket.ads;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class BannerAdHelper {

    public static void loadBanner(FrameLayout container, Activity activity, Display display) {

        AdSize adSize = getAdsize(activity, display);

        AdView adView = new AdView(activity);
        adView.setAdUnitId(AdUnitProvider.getAdUnits().getBannerId());
        adView.setAdSize(adSize);

        container.removeAllViews();
        container.addView(adView);

        AdRequest request = new AdRequest.Builder().build();

        adView.loadAd(request);

    }

    private static AdSize getAdsize(Context context, Display display) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

}
