package com.Hand_Cricket;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.Hand_Cricket.ads.BannerAdHelper;

public class HowTo extends AppCompatActivity {

    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_how_to);

        FrameLayout adContainer = findViewById(R.id.adView);
        Display display = getWindowManager().getDefaultDisplay();
        BannerAdHelper.loadBanner(adContainer, this, display);

        TextView text = findViewById(R.id.text);
        text.setMovementMethod(new ScrollingMovementMethod());

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(HowTo.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

    }


    @Override
    public void onBackPressed() {
        back.performClick();
    }
}