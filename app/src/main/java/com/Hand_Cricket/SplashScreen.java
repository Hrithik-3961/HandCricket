package com.Hand_Cricket;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);

        Toast toast = Toast.makeText(this, getText(R.string.toast), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 80);
        toast.show();

        setAlarm(this);

        new Handler().postDelayed(() -> {

            Application application = getApplication();
            if (!(application instanceof MyApplication)) {
                Log.e("myTag", "Failed to cast application to MyApplication.");
                startMainActivity();
                return;
            }

            ((MyApplication) application)
                    .showAdIfAvailable(
                            SplashScreen.this,
                            (MyApplication.OnShowAdCompleteListener) this::startMainActivity);
        }, 3000);

    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashScreen.this, HomeScreen.class);
        startActivity(intent);
        finish();
    }

    public void setAlarm(Context context) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long timeInMillis = calendar.getTimeInMillis() + 86400000;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}