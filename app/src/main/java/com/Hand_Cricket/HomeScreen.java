package com.Hand_Cricket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Calendar;

public class HomeScreen extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;

    private boolean flag, soundOn = true, vibrationOn = true;

    private Button sound, vibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.home_page);

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        setAlarm(HomeScreen.this);
                        content.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
        soundOn = MyPrefs.getSoundPref(this);
        vibrationOn = MyPrefs.getVibrationPref(this);

        sound = findViewById(R.id.sound);
        sound.setOnClickListener(v -> {
            soundOn = !soundOn;
            MyPrefs.setSoundPref(HomeScreen.this, soundOn);
            String str = getString(soundOn ? R.string.soundON : R.string.soundOFF);
            Toast.makeText(HomeScreen.this, str, Toast.LENGTH_SHORT).show();
            updateSoundIcon();
        });
        updateSoundIcon();

        vibration = findViewById(R.id.vibrate);
        vibration.setOnClickListener(v -> {
            vibrationOn = !vibrationOn;
            MyPrefs.setVibrationPref(HomeScreen.this, vibrationOn);
            String str = getString(vibrationOn ? R.string.vibrationON : R.string.vibrationOFF);
            Toast.makeText(HomeScreen.this, str, Toast.LENGTH_SHORT).show();
            updateVibrationIcon();
        });
        updateVibrationIcon();

        loadAd();

        Button playButton = findViewById(R.id.PlayButton);
        playButton.setOnClickListener(v -> {

            flag = true;

            if (mInterstitialAd != null) {
                mInterstitialAd.show(HomeScreen.this);
            } else
                resumeActivity();
        });

        Button howToButton = findViewById(R.id.HowToButton);
        howToButton.setOnClickListener(v -> {

            flag = false;

            if (mInterstitialAd != null) {
                mInterstitialAd.show(HomeScreen.this);
            } else
                resumeActivity();
        });

    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.InterstitialID), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                mInterstitialAd = null;
                                resumeActivity();
                                Log.d("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                // Called when fullscreen content failed to show.
                                mInterstitialAd = null;
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d("myTag", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    private void resumeActivity() {

        if(mInterstitialAd == null)
            loadAd();

        if (flag) {
            Intent intent = new Intent(HomeScreen.this, Play.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(HomeScreen.this, HowTo.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void updateSoundIcon() {

        if (!soundOn)
            sound.setBackgroundResource(R.drawable.sound_off_icon);
        else
            sound.setBackgroundResource(R.drawable.sound_on_icon);
    }

    public void updateVibrationIcon() {

        if (!vibrationOn)
            vibration.setBackgroundResource(R.drawable.vibration_off_icon);
        else
            vibration.setBackgroundResource(R.drawable.vibration_on_icon);
    }

    public void rate(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.Hand_Cricket")));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.appLink))));
        }
    }

    public void share(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String body = getString(R.string.shareBody);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    public static void setAlarm(Context context) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long timeInMillis = calendar.getTimeInMillis() + 86400000;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}