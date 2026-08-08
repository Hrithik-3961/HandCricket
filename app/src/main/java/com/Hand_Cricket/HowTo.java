package com.Hand_Cricket;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.Hand_Cricket.ads.BannerAdHelper;

public class HowTo extends AppCompatActivity {

    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            int systemBars = WindowInsetsCompat.Type.systemBars();
            int displayCutout = WindowInsetsCompat.Type.displayCutout();
            v.setPadding(insets.getInsets(systemBars | displayCutout).left,
                    0,
                    insets.getInsets(systemBars | displayCutout).right,
                    insets.getInsets(systemBars | displayCutout).bottom);
            return insets;
        });

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back.performClick();
            }
        });

    }


}