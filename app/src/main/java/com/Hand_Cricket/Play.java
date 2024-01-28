package com.Hand_Cricket;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class Play extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button toss, heads, tails, close, toastButton;
    private TextView text;
    private LottieAnimationView tossAnimation;
    private boolean flag = true, soundOn = true;

    private String spinnerText;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.play);

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

        if (spinnerText.equals("--Select Overs--") || spinnerText.equals("")) {
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
                    mediaPlayer = MediaPlayer.create(this, R.raw.coinflip);
                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
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