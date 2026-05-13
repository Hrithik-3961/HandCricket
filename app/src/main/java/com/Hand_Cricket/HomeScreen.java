package com.Hand_Cricket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.Hand_Cricket.ads.InterstitialAdHelper;

import java.util.Calendar;

public class HomeScreen extends AppCompatActivity {

    private InterstitialAdHelper interstitialHelper;

    private boolean soundOn = true, vibrationOn = true;

    private ImageButton sound, vibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

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

        interstitialHelper = new InterstitialAdHelper(this);
        interstitialHelper.load();

        Button playButton = findViewById(R.id.PlayButton);
        playButton.setOnClickListener(v -> interstitialHelper.show(() -> {
            Intent intent = new Intent(HomeScreen.this, Play.class);
            startActivity(intent);
        }));

        Button howToButton = findViewById(R.id.HowToButton);
        howToButton.setOnClickListener(v -> interstitialHelper.show(() -> {
            Intent intent = new Intent(HomeScreen.this, HowTo.class);
            startActivity(intent);
            finish();
        }));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

    }


    public void updateSoundIcon() {

        if (!soundOn)
            sound.setImageResource(R.drawable.sound_off_icon);
        else
            sound.setImageResource(R.drawable.sound_on_icon);
    }

    public void updateVibrationIcon() {

        if (!vibrationOn)
            vibration.setImageResource(R.drawable.vibration_off_icon);
        else
            vibration.setImageResource(R.drawable.vibration_on_icon);
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