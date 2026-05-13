package com.Hand_Cricket;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Objects;

public class Play extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button toss, heads, tails, toastButton;
    private ImageButton close;
    private TextView text;
    private LottieAnimationView tossAnimation;
    private boolean flag = true, soundOn = true;

    private String spinnerText;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        soundOn = MyPrefs.getSoundPref(this);

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.overs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        toss = findViewById(R.id.toss);

        toastButton = findViewById(R.id.toast);
        toastButton.setOnClickListener(v -> Toast.makeText(Play.this, getText(R.string.toast2), Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerText = parent.getItemAtPosition(position).toString();

        if (spinnerText.equals("--Select Overs--") || spinnerText.isEmpty()) {
            toss.setEnabled(false);
            toastButton.setEnabled(true);
            toastButton.setVisibility(View.VISIBLE);
        } else {
            toastButton.setEnabled(false);
            toastButton.setVisibility(View.GONE);
            toss.setEnabled(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void onBtnClicked(View view) {

        AlertDialog.Builder alert = new AlertDialog.Builder(Play.this);
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.toss, null);

        heads = mView.findViewById(R.id.heads);
        tails = mView.findViewById(R.id.tails);
        close = mView.findViewById(R.id.close);
        text = mView.findViewById(R.id.text);
        tossAnimation = mView.findViewById(R.id.tossAnimation);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        heads.setOnClickListener(v -> process(1));

        tails.setOnClickListener(v -> process(2));

        close.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void process(final int a) {
        heads.setVisibility(View.INVISIBLE);
        tails.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        close.setVisibility(View.INVISIBLE);


        if (flag) {
            tossAnimation.setVisibility(View.VISIBLE);
            tossAnimation.playAnimation();

            if (soundOn) {
                if (mediaPlayer == null) {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build();
                    mediaPlayer = MediaPlayer.create(this, R.raw.coinflip, audioAttributes, 0);
                    mediaPlayer.setOnCompletionListener(mp -> stopPlayer());
                }
                mediaPlayer.start();
            }

            tossAnimation.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    flag = false;
                    tossAnimation.setVisibility(View.GONE);
                    close.setVisibility(View.GONE);
                    text.setVisibility(View.VISIBLE);
                    int random = 1 + (int) (Math.random() * 2);
                    if (random == a) {
                        heads.setVisibility(View.VISIBLE);
                        tails.setVisibility(View.VISIBLE);
                        heads.setText(getString(R.string.Batting));
                        tails.setText(getString(R.string.Bowling));
                        text.setText(getString(R.string.TossWin));
                    } else {
                        random = 1 + (int) (Math.random() * 2);
                        String s = (random == 1) ? getString(R.string.TossLost1) : getString(R.string.TossLost2);
                        heads.setVisibility(View.GONE);
                        tails.setVisibility(View.GONE);
                        text.setText(s);
                        final int finalRandom = random;
                        new Handler().postDelayed(() -> startPlaying(finalRandom), 2500);

                    }
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {
                }
            });
        } else {
            startPlaying(a);
        }
    }

    public void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

    private void startPlaying(int a) {
        close.performClick();
        int overs = Integer.parseInt(spinnerText);
        Intent intent = new Intent(Play.this, PlayingScreen.class);
        intent.putExtra("choice", a);
        intent.putExtra("overs", overs);
        startActivity(intent);
        finish();
    }
}