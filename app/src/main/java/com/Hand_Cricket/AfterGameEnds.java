package com.Hand_Cricket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.Hand_Cricket.ads.BannerAdHelper;
import com.Hand_Cricket.ads.InterstitialAdHelper;
import com.airbnb.lottie.LottieAnimationView;

import java.util.Objects;

public class AfterGameEnds extends AppCompatActivity {

    private boolean playerBatting;

    private int winStatus, playerPoints, playerWickets, computerPoints, computerWickets, overs;
    private double playerOvers, computerOvers;

    private MediaPlayer gameWonSound, gameLostSound;

    private InterstitialAdHelper interstitialHelper;
    private AlertDialog scoreCardDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_game_ends);

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        boolean soundOn = MyPrefs.getSoundPref(this);

        FrameLayout adContainer = findViewById(R.id.adView);
        Display display = getWindowManager().getDefaultDisplay();
        BannerAdHelper.loadBanner(adContainer, this, display);

        interstitialHelper = new InterstitialAdHelper(this);
        interstitialHelper.load();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        winStatus = bundle.getInt(GameConstants.WIN_STATUS);
        overs = bundle.getInt(GameConstants.TOTAL_OVERS);
        playerPoints = bundle.getInt(GameConstants.PLAYER_RUNS);
        playerWickets = bundle.getInt(GameConstants.PLAYER_WICKETS);
        computerPoints = bundle.getInt(GameConstants.COMPUTER_RUNS);
        computerWickets = bundle.getInt(GameConstants.COMPUTER_WICKETS);
        playerOvers = bundle.getDouble(GameConstants.PLAYER_OVERS);
        computerOvers = bundle.getDouble(GameConstants.COMPUTER_OVERS);
        playerBatting = bundle.getBoolean(GameConstants.PLAYER_BATTING);

        TextView winStatusMsg = findViewById(R.id.winStatusMsg);
        winStatusMsg.setText(getResources().getStringArray(R.array.game_end_message)[winStatus-1]);

        LottieAnimationView animation = findViewById(R.id.animation);

        switch (winStatus) {
            case 1:
                animation.setAnimation("winners-animation.json");
                if (soundOn) {
                    if (gameWonSound == null) {
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .build();
                        gameWonSound = MediaPlayer.create(this, R.raw.game_won, audioAttributes, 0);
                        gameWonSound.setOnCompletionListener(mp -> stopPlayer());
                    }
                    gameWonSound.start();
                }
                break;
            case 2:
                animation.setAnimation("sad.json");
                if (soundOn) {
                    if (gameLostSound == null) {
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .build();
                        gameLostSound = MediaPlayer.create(this, R.raw.game_lost, audioAttributes, 0);
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
            stopPlayer();
            Intent intent = new Intent(AfterGameEnds.this, Play.class);
            startActivity(intent);
            finish();
        });

        Button mainMenu = findViewById(R.id.mainMenu);
        mainMenu.setOnClickListener(v -> interstitialHelper.show(() -> {
            stopPlayer();
            Intent intent = new Intent(AfterGameEnds.this, HomeScreen.class);
            startActivity(intent);
            finish();
        }));

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

    @Override
    protected void onDestroy() {
        if (scoreCardDialog != null && scoreCardDialog.isShowing()) {
            scoreCardDialog.dismiss();
        }
        super.onDestroy();
    }

    public void scoreCard(View view) {
        if (isFinishing() || (scoreCardDialog != null && scoreCardDialog.isShowing())) {
            return;
        }
        stopPlayer();

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.score_card, null);

        TextView totalOvers = mView.findViewById(R.id.totalOvers);
        TextView playerSummary = mView.findViewById(R.id.playerSummary);
        TextView computerSummary = mView.findViewById(R.id.computerSummary);
        TextView summary = mView.findViewById(R.id.summary);
        Button ok = mView.findViewById(R.id.ok);

        alert.setView(mView);

        scoreCardDialog = alert.create();
        scoreCardDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(scoreCardDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        totalOvers.setText(getString(R.string.total_overs, overs));
        playerSummary.setText(getResources().getQuantityString(R.plurals.score_summary, playerOvers <= 1 ? 1:2, playerPoints, playerWickets, playerOvers));
        computerSummary.setText(getResources().getQuantityString(R.plurals.score_summary, computerOvers <= 1 ? 1:2, computerPoints, computerWickets, computerOvers));

        if(winStatus == 3)
            summary.setText(getResources().getStringArray(R.array.game_end_message)[winStatus-1]);
        else {
            if (playerBatting) {
                if (winStatus == 1) {
                    int diff = 3 - playerWickets;
                    if(diff < 0)
                        diff = 0;
                    summary.setText(getResources().getQuantityString(R.plurals.summary_wickets, (3 - playerWickets) <= 1 ? 1 : 2, getText(R.string.Player), getText(R.string.Computer), diff));
                } else
                    summary.setText(getResources().getQuantityString(R.plurals.summary_runs, (computerPoints - playerPoints) <= 1 ? 1:2, getText(R.string.Computer), getText(R.string.Player), (computerPoints - playerPoints)));
            } else {
                if (winStatus == 1)
                    summary.setText(getResources().getQuantityString(R.plurals.summary_runs, (playerPoints - computerPoints) <= 1 ? 1:2, getText(R.string.Player), getText(R.string.Computer), (playerPoints - computerPoints)));
                else
                    summary.setText(getResources().getQuantityString(R.plurals.summary_wickets, (3 - computerWickets) <= 1 ? 1:2, getText(R.string.Computer), getText(R.string.Player), (3 - computerWickets)));
            }
        }

        ok.setOnClickListener(v -> scoreCardDialog.cancel());
        scoreCardDialog.show();
    }
}