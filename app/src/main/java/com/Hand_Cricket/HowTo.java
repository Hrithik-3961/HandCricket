package com.Hand_Cricket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class HowTo extends AppCompatActivity {

    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_how_to);

        FrameLayout adContainer = findViewById(R.id.adView);
        Display display = getWindowManager().getDefaultDisplay();
        loadBanner(adContainer, this, display);

        TextView text = findViewById(R.id.text);
        text.setMovementMethod(new ScrollingMovementMethod());

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(HowTo.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

    }

    void loadBanner(FrameLayout adContainer, Context context, Display display) {

        MobileAds.initialize(context, initializationStatus -> {
        });

        AdView mAdView = new AdView(context);
        mAdView.setAdUnitId(context.getString(R.string.BannerID));
        adContainer.addView(mAdView);

        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdsize(context, display);
        mAdView.setAdSize(adSize);
        mAdView.loadAd(adRequest);
    }

    AdSize getAdsize(Context context, Display display) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    @Override
    public void onBackPressed() {
        back.performClick();
    }
}