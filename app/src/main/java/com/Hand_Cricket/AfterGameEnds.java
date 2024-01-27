package com.Hand_Cricket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AfterGameEnds extends AppCompatActivity {

    private boolean flag;
    private boolean playerBatting;

    private int playerPoints, playerWickets, computerPoints, computerWickets, overs;
    private double playerOvers, computerOvers;

    private MediaPlayer gameWonSound, gameLostSound;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.after_game_ends);

        boolean soundOn = MyPrefs.getSoundPref(this);

        FrameLayout adContainer = findViewById(R.id.adView);
        Display display = getWindowManager().getDefaultDisplay();
        new HowTo().loadBanner(adContainer, this, display);

        loadAd();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        int status = bundle.getInt(GameConstants.WIN_STATUS);
        overs = bundle.getInt(GameConstants.TOTAL_OVERS);
        playerPoints = bundle.getInt(GameConstants.PLAYER_RUNS);
        playerWickets = bundle.getInt(GameConstants.PLAYER_WICKETS);
        computerPoints = bundle.getInt(GameConstants.COMPUTER_RUNS);
        computerWickets = bundle.getInt(GameConstants.COMPUTER_WICKETS);
        playerOvers = bundle.getDouble(GameConstants.PLAYER_OVERS);
        computerOvers = bundle.getDouble(GameConstants.COMPUTER_OVERS);
        playerBatting = bundle.getBoolean(GameConstants.PLAYER_BATTING);

        TextView winStatus = findViewById(R.id.winStatus);
        winStatus.setText(getGameOverMessage(status));

        LottieAnimationView animation = findViewById(R.id.animation);

        switch (status) {
            case 1:
                animation.setAnimation("winners-animation.json");
                if (soundOn) {
                    if (gameWonSound == null) {
                        gameWonSound = MediaPlayer.create(this, R.raw.game_won);
                        gameWonSound.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                        gameWonSound.setOnCompletionListener(mp -> stopPlayer());
                    }
                    gameWonSound.start();
                }
                break;
            case 2:
                animation.setAnimation("sad.json");
                if (soundOn) {
                    if (gameLostSound == null) {
                        gameLostSound = MediaPlayer.create(this, R.raw.game_lost);
                        gameLostSound.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                        gameLostSound.setOnCompletionListener(mp -> stopPlayer());
                    }
                    gameLostSound.start();
                }
                break;
            case 3:
                animation.setVisibility(View.GONE);
                break;
        }

        animation.playAnimation();

        Button playAgain = findViewById(R.id.playAgain);
        playAgain.setOnClickListener(v -> {

            flag = true;

            if (mInterstitialAd != null) {
                mInterstitialAd.show(AfterGameEnds.this);
            } else
                resumeActivity();
        });

        Button mainMenu = findViewById(R.id.mainMenu);
        mainMenu.setOnClickListener(v -> {

            flag = false;

            if (mInterstitialAd != null) {
                mInterstitialAd.show(AfterGameEnds.this);
            } else
                resumeActivity();
        });

    }

    private String getGameOverMessage(int status) {
        switch (status) {
            case 1:
                return "Congratulations! You won the match!";
            case 2:
                return "Oops! You lost the match!";
            case 3:
                return "It's a tie!";
            default:
                return "";
        }
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

    private void stopPlayer() {
        if (gameWonSound != null) {
            gameWonSound.release();
            gameWonSound = null;
        }
        if (gameLostSound != null) {
            gameLostSound.release();
            gameLostSound = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

    private void resumeActivity() {

        stopPlayer();

        if(mInterstitialAd == null)
            loadAd();

        Intent intent;
        if (flag) {
            intent = new Intent(AfterGameEnds.this, Play.class);
        } else {
            intent = new Intent(AfterGameEnds.this, HomeScreen.class);
        }
        startActivity(intent);
        finish();
    }

    public void scoreCard(View view) {

        stopPlayer();

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.score_card, null);

        TextView totalOvers = mView.findViewById(R.id.totalOvers);
        TextView playerSummary = mView.findViewById(R.id.playerSummary);
        TextView computerSummary = mView.findViewById(R.id.computerSummary);
        TextView winMessage = mView.findViewById(R.id.winMessage);
        Button ok = mView.findViewById(R.id.ok);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        String str = "Total Overs: " + overs;
        totalOvers.setText(str);

        str = playerPoints + "/" + playerWickets + " in " + playerOvers + " over(s)";
        playerSummary.setText(str);

        str = computerPoints + "/" + computerWickets + " in " + computerOvers + " over(s)";
        computerSummary.setText(str);

        if (playerPoints > computerPoints) {
            if (playerBatting)
                str = "Player beats Computer by " + (3 - playerWickets) + " wicket(s).";
            else
                str = "Player beats Computer by " + (playerPoints - computerPoints) + " run(s).";
        } else if (computerPoints > playerPoints) {
            if (!playerBatting)
                str = "Computer beats Player by " + (3 - computerWickets) + " wicket(s).";
            else
                str = "Computer beats Player by " + (computerPoints - playerPoints) + " run(s).";
        }

        winMessage.setText(str);

        ok.setOnClickListener(v -> alertDialog.cancel());


        alertDialog.show();
    }
}