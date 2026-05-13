package com.Hand_Cricket;

import android.animation.Animator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.Hand_Cricket.ads.BannerAdHelper;
import com.Hand_Cricket.ads.RewardedAdHelper;
import com.airbnb.lottie.LottieAnimationView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.IntStream;

public class PlayingScreen extends AppCompatActivity implements View.OnClickListener {

    private int MAX_PLAYER_WICKETS = 3;
    private final int MAX_COMPUTER_WICKETS = 3;
    private int MAX_PLAYER_OVERS = 1;
    private int MAX_COMPUTER_OVERS = 1;
    private int playerRuns = 0;
    private int computerRuns = 0;
    private int playerWickets = 0;
    private int computerWickets = 0;
    private int currentBalls = 0;
    private int playerOver = 0;
    private int computerOver = 0;
    private int playerChosenNumber = 0;
    private int computerChosenNumber = 0;
    private int extraStatus = -1;
    private double playerOversAndBalls = 0.0, computerOversAndBalls = 0.0;
    private boolean isPlayerBatting;
    private boolean firstInningsOver = false;
    private boolean soundOn = true;
    private boolean vibrationOn = true;
    private boolean addExtraWicket = true;
    private boolean addExtraOver = true;

    private LottieAnimationView wicketAnimation;
    private MediaPlayer myWicketSound, opponentWicketSound, crowdSound;

    private TextView playerStatus, oversDisplay;
    private ImageView computerHand, playerHand;
    private RewardedAdHelper rewardedHelper;
    private AlertDialog rewardedAdDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.playing_screen);

        FrameLayout adContainer = findViewById(R.id.adView);
        Display display = getWindowManager().getDefaultDisplay();
        BannerAdHelper.loadBanner(adContainer, this, display);

        rewardedHelper = new RewardedAdHelper(this);
        rewardedHelper.load();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        int choice = bundle.getInt("choice");
        int overs = bundle.getInt("overs");
        MAX_PLAYER_OVERS = overs;
        MAX_COMPUTER_OVERS = overs;

        playerStatus = findViewById(R.id.playerStatus);
        oversDisplay = findViewById(R.id.overs);
        computerHand = findViewById(R.id.computerHand);
        playerHand = findViewById(R.id.playerHand);

        String str = "Over: 0.0(" + overs + ")";
        oversDisplay.setText(str);

        if(choice == 1) {
            isPlayerBatting = true;
            playerStatus.setText(getText(R.string.playerBatting));
        } else {
            isPlayerBatting = false;
            playerStatus.setText(getText(R.string.playerBowling));
        }

        soundOn = MyPrefs.getSoundPref(this);
        vibrationOn = MyPrefs.getVibrationPref(this);

        if (soundOn) {
            if (crowdSound == null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build();
                crowdSound = MediaPlayer.create(this, R.raw.crowd, audioAttributes, 0);
                crowdSound.setOnCompletionListener(mp -> stopSound());
            }
            crowdSound.start();
        }


        final ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(v -> showExitDialog());

        final ImageButton more = findViewById(R.id.more);
        more.setOnClickListener(v -> {

            final PopupMenu popupMenu = new PopupMenu(PlayingScreen.this, v);
            popupMenu.inflate(R.menu.playing_screen_menu);
            popupMenu.show();
            setForceShowIcon(popupMenu);

            Menu menu = popupMenu.getMenu();

            if (soundOn) {
                menu.getItem(1).setIcon(R.drawable.sound_on_icon);
                menu.getItem(1).setChecked(true);
            } else {
                menu.getItem(1).setIcon(R.drawable.sound_off_icon);
                menu.getItem(1).setChecked(false);
            }

            if (vibrationOn) {
                menu.getItem(2).setIcon(R.drawable.vibration_on_icon);
                menu.getItem(2).setChecked(true);
            } else {
                menu.getItem(2).setIcon(R.drawable.vibration_off_icon);
                menu.getItem(2).setChecked(false);
            }


            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.howToMenu) {
                    showHowTo();
                    return true;
                } else if (item.getItemId() == R.id.soundMenu) {
                    soundOn = !soundOn;
                    item.setChecked(soundOn);
                    item.setIcon(soundOn ? R.drawable.sound_on_icon : R.drawable.sound_off_icon);
                    String str1 = getString(soundOn ? R.string.soundON : R.string.soundOFF);
                    if (!soundOn)
                        stopSound();
                    Toast.makeText(PlayingScreen.this, str1, Toast.LENGTH_SHORT).show();
                    MyPrefs.setSoundPref(PlayingScreen.this, soundOn);
                } else if (item.getItemId() == R.id.vibrateMenu) {
                    vibrationOn = !vibrationOn;
                    item.setChecked(vibrationOn);
                    item.setIcon(vibrationOn ? R.drawable.vibration_on_icon : R.drawable.vibration_off_icon);
                    String str1 = getString(vibrationOn ? R.string.vibrationON : R.string.vibrationOFF);
                    Toast.makeText(PlayingScreen.this, str1, Toast.LENGTH_SHORT).show();
                    MyPrefs.setVibrationPref(PlayingScreen.this, vibrationOn);
                } else if (item.getItemId() == R.id.shareMenu) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String body = getString(R.string.shareBody);
                    intent.putExtra(Intent.EXTRA_TEXT, body);
                    startActivity(Intent.createChooser(intent, "Share Via"));
                    return true;
                } else if (item.getItemId() == R.id.rateMenu) {
                    openPlayStore();
                    return true;
                }

                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(v);
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                        return false;
                    }
                });
                return false;
            });

        });

        IntStream.range(0,10).mapToObj(i -> (Button) findViewById(GameConstants.btnResID[i])).forEach(btn -> btn.setOnClickListener(this));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    public void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    assert menuPopupHelper != null;
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable ignore) {}
    }

    public void openPlayStore() {

        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.Hand_Cricket")));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.appLink))));
        }
    }

    public void showHowTo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(PlayingScreen.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_how_to, null);
        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button ok = mView.findViewById(R.id.ok);
        ok.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {

        if (crowdSound != null && crowdSound.isPlaying()) {
            crowdSound.pause();
            crowdSound.seekTo(0);
        }

        currentBalls++;
        if (currentBalls == GameConstants.MAX_BALLS_PER_OVER) {
            currentBalls = 0;
            if (isPlayerBatting)
                playerOver++;
            else
                computerOver++;
        }
        if (isPlayerBatting)
            playerOversAndBalls = playerOver + currentBalls / 10.0;
        else
            computerOversAndBalls = computerOver + currentBalls / 10.0;

        playerChosenNumber = getPlayerChoice(v);
        computerChosenNumber = getRandomNumber();

        if (isPlayerBatting) {
            if (playerChosenNumber == computerChosenNumber) {
                playerWickets++;
                setButtonsEnableDisable(false);
                showWicketAnimation();
            } else {
                playerRuns += playerChosenNumber;
            }
        } else {
            if (playerChosenNumber == computerChosenNumber) {
                computerWickets++;
                setButtonsEnableDisable(false);
                showWicketAnimation();
            } else {
                computerRuns += computerChosenNumber;
            }
        }

        if (!isPlayerBatting && (computerOver >= MAX_COMPUTER_OVERS || computerWickets >= MAX_COMPUTER_WICKETS)) {
            handleInningsChange();
            updateView();
            return;
        }

        if (firstInningsOver) {
            int status = checkGameOverByRuns();
            if ((isPlayerBatting && status == 1) || (!isPlayerBatting && status == 2)) {
                showInningsChange(status);
                updateView();
                return;
            }
        }

        extraStatus = -1;//1 - wicket only, 2 - overs only, 3 - both wickets and overs
        boolean isExtraWicket = false, isExtraOver = false;
        if (isPlayerBatting && playerWickets >= MAX_PLAYER_WICKETS)
            isExtraWicket = true;

        if (isPlayerBatting && (playerOver >= MAX_PLAYER_OVERS))
            isExtraOver = true;

        if (isExtraWicket) {
            if (isExtraOver) {
                if (computerOver == 0)// if player is batting first
                    extraStatus = 3;
                else if (computerOver >= MAX_COMPUTER_OVERS && playerRuns <= computerRuns)// if player is batting 2nd and runs not achieved
                    extraStatus = 3;
                else
                    extraStatus = 1;
            } else
                extraStatus = 1;
        } else if (isExtraOver)
            extraStatus = 2;

        if (extraStatus != -1) {
            if ((extraStatus == 1 && addExtraWicket) || (extraStatus == 2 && addExtraOver) || (extraStatus == 3 && addExtraWicket && addExtraOver))
                showVideoAdDialog();
            else
                onRewardCancelled();
            updateView();
            return;
        }

        updateView();
    }

    public void updateView() {

        if (isPlayerBatting)
            playerStatus.setText(getText(R.string.playerBatting));
        else
            playerStatus.setText(getText(R.string.playerBowling));

        playerHand = findViewById(R.id.playerHand);
        playerHand.setImageResource(GameConstants.handResID[playerChosenNumber]);

        String s;
        if(isPlayerBatting)
            s = getString(R.string.over_playing_screen, String.valueOf(playerOversAndBalls), MAX_PLAYER_OVERS);
        else
            s = getString(R.string.over_playing_screen, String.valueOf(computerOversAndBalls), MAX_COMPUTER_OVERS);
        oversDisplay.setText(s);

        TextView score2 = findViewById(R.id.score2);
        s = playerRuns + "/" + playerWickets;
        score2.setText(s);

        TextView score1 = findViewById(R.id.score1);
        s = computerRuns + "/" + computerWickets;
        score1.setText(s);

        computerHand = findViewById(R.id.computerHand);
        computerHand.setImageResource(GameConstants.handResID[computerChosenNumber]);
    }

    private void addExtraWicket() {
        MAX_PLAYER_WICKETS += 1;
        addExtraWicket = false;
    }

    private void addExtraOver() {
        MAX_PLAYER_OVERS += 1;
        addExtraOver = false;
    }

    private void handleInningsChange() {
        if(!firstInningsOver) {
            showInningsChange(-1);
            isPlayerBatting = !isPlayerBatting;
            currentBalls = 0;
            firstInningsOver = true;
        } else {
            int status = checkGameOverByWickets();
            if(status == -1)
                status = checkGameOverByRuns();
            showInningsChange(status);
        }
    }

    private int checkGameOverByRuns() {
        int status;// 1 = player won, 2 = computer won, 3 = tie

        if(playerRuns > computerRuns)
            status = 1;
        else if(computerRuns > playerRuns)
            status = 2;
        else
            status = 3;

        return status;
    }

    private int checkGameOverByWickets() {
        int status = -1;// 1 = player won, 2 = computer won

        if(isPlayerBatting && playerWickets >= MAX_PLAYER_WICKETS)
            status = 2;
        else if(!isPlayerBatting && computerWickets >= MAX_COMPUTER_WICKETS)
            status = 1;

        return status;
    }

    private void showInningsChange(int status) {

        AlertDialog.Builder alert = new AlertDialog.Builder(PlayingScreen.this);
        View mView = getLayoutInflater().inflate(R.layout.score_board, null);

        TextView text1, text2, oversDisp, runsDisplay, wicketsDisplay;
        Button ok;

        text1 = mView.findViewById(R.id.text1);
        text2 = mView.findViewById(R.id.text2);
        oversDisp = mView.findViewById(R.id.oversDisp);
        runsDisplay = mView.findViewById(R.id.runsDisplay);
        wicketsDisplay = mView.findViewById(R.id.wicketsDisplay);

        ok = mView.findViewById(R.id.ok);

        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (firstInningsOver && status != -1)
            text2.setVisibility(View.GONE);

        if (isPlayerBatting)
            oversDisp.setText(String.valueOf(playerOversAndBalls));
        else
            oversDisp.setText(String.valueOf(computerOversAndBalls));

        if (isPlayerBatting) {
            text1.setText(R.string.Player);
            runsDisplay.setText(String.valueOf(playerRuns));
            wicketsDisplay.setText(String.valueOf(playerWickets));
            text2.setText(getResources().getQuantityString(R.plurals.inningsChangeMessage, MAX_COMPUTER_OVERS <= 1 ? 1:2, getText(R.string.Computer), (playerRuns + 1), MAX_COMPUTER_OVERS));
        } else {
            text1.setText(R.string.Computer);
            runsDisplay.setText(String.valueOf(computerRuns));
            wicketsDisplay.setText(String.valueOf(computerWickets));
            text2.setText(getResources().getQuantityString(R.plurals.inningsChangeMessage, MAX_PLAYER_OVERS <= 1 ? 1:2, getText(R.string.Player), (computerRuns + 1), MAX_PLAYER_OVERS));
        }
        alertDialog.show();

        if (status == -1) {
            computerHand.setImageResource(R.drawable.image0);
            playerHand.setImageResource(R.drawable.image0);
            updateView();
        }

        ok.setOnClickListener(v -> {

            if (firstInningsOver && status != -1) {
                Intent intent = getIntent(status);
                startActivity(intent);
                finish();
            }

            if (status == -1) {
                if (soundOn) {
                    if (crowdSound == null) {
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .build();
                        crowdSound = MediaPlayer.create(PlayingScreen.this, R.raw.crowd, audioAttributes, 0);
                        crowdSound.setOnCompletionListener(mp -> stopSound());
                    }
                    crowdSound.start();
                }
            }
            alertDialog.cancel();
            setButtonsEnableDisable(true);
        });
    }

    @NonNull
    private Intent getIntent(int status) {
        Intent intent = new Intent(PlayingScreen.this, AfterGameEnds.class);
        intent.putExtra(GameConstants.WIN_STATUS, status);
        intent.putExtra(GameConstants.TOTAL_OVERS, MAX_PLAYER_OVERS);
        intent.putExtra(GameConstants.PLAYER_RUNS, playerRuns);
        intent.putExtra(GameConstants.PLAYER_WICKETS, playerWickets);
        intent.putExtra(GameConstants.COMPUTER_RUNS, computerRuns);
        intent.putExtra(GameConstants.COMPUTER_WICKETS, computerWickets);
        intent.putExtra(GameConstants.PLAYER_OVERS, playerOversAndBalls);
        intent.putExtra(GameConstants.COMPUTER_OVERS, computerOversAndBalls);
        intent.putExtra(GameConstants.PLAYER_BATTING, isPlayerBatting);
        return intent;
    }

    private void setButtonsEnableDisable(boolean flag) {
        IntStream.range(0,10).mapToObj(i -> (Button) findViewById(GameConstants.btnResID[i])).forEach(btn -> btn.setEnabled(flag));
    }

    private void showWicketAnimation() {
        wicketAnimation = findViewById(R.id.wicket);
        wicketAnimation.setVisibility(View.VISIBLE);
        wicketAnimation.playAnimation();

        if (vibrationOn) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            assert vibrator != null;
            long[] pattern = {0, 100, 200, 100};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }

        if (soundOn) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            if (isPlayerBatting) {
                if (myWicketSound == null) {
                    myWicketSound = MediaPlayer.create(this, R.raw.my_wicket, audioAttributes, 0);
                    myWicketSound.setOnCompletionListener(mp -> stopSound());
                }
                myWicketSound.start();
            } else {
                if (opponentWicketSound == null) {
                    opponentWicketSound = MediaPlayer.create(this, R.raw.opponent_wicket, audioAttributes, 0);
                    opponentWicketSound.setOnCompletionListener(mp -> stopSound());
                }
                opponentWicketSound.start();
            }
        }
        wicketAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                setButtonsEnableDisable(false);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                setButtonsEnableDisable(true);
                wicketAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });

    }

    private void showExitDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.exitMessage))
                .setCancelable(false)
                .setPositiveButton(HtmlCompat.fromHtml("<font color = '#FF0101'>Yes</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> {

                    Intent intent = new Intent(PlayingScreen.this, HomeScreen.class);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton(HtmlCompat.fromHtml("<font color = '#FF0101'>No</font>", HtmlCompat.FROM_HTML_MODE_LEGACY), (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void stopSound() {
        if (myWicketSound != null && myWicketSound.isPlaying()) {
            myWicketSound.pause();
            myWicketSound.seekTo(0);
        }
        if (opponentWicketSound != null && opponentWicketSound.isPlaying()) {
            opponentWicketSound.pause();
            opponentWicketSound.seekTo(0);
        }

        if (crowdSound != null && crowdSound.isPlaying()) {
            crowdSound.pause();
            crowdSound.seekTo(0);
        }
    }

    private void releasePlayer(MediaPlayer player) {
        if (player != null) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
            } catch (Exception ignore) {}
        }
    }

    private int getPlayerChoice(View v) {
        return Integer.parseInt(((Button) v).getText().toString());
    }

    private int getRandomNumber() {
        return (int) (1 + Math.random() * 10);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releasePlayer(crowdSound);
        crowdSound = null;

        releasePlayer(myWicketSound);
        myWicketSound = null;

        releasePlayer(opponentWicketSound);
        opponentWicketSound = null;
    }

    private void showVideoAdDialog() {
        if(!rewardedHelper.isAdAvailable()) {
            rewardedHelper.load();
            onRewardCancelled();
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(PlayingScreen.this);
        View mView = getLayoutInflater().inflate(R.layout.rewarded_video_dialog, null);
        alert.setView(mView);

        TextView text = mView.findViewById(R.id.text);
        ImageButton cancel = mView.findViewById(R.id.cancel);
        Button continueBtn = mView.findViewById(R.id.continueBtn);

        rewardedAdDialog = alert.create();
        rewardedAdDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(rewardedAdDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        String msg = "";
        switch (extraStatus) {
            case 1: msg = getString(R.string.add_extra_resources, GameConstants.EXTRA_WICKET);
                    break;
            case 2: msg = getString(R.string.add_extra_resources, GameConstants.EXTRA_OVER);
                break;
            case 3: msg = getString(R.string.add_extra_resources, GameConstants.EXTRA_WICKET_AND_OVER);
                break;
        }
        text.setText(msg);

        cancel.setOnClickListener(view -> onRewardCancelled());

        continueBtn.setOnClickListener(view -> {
            rewardedHelper.show(new RewardedAdHelper.Listener() {
                private boolean isRewardEarned = false;
                @Override
                public void onRewardEarned() {
                    isRewardEarned = true;
                    Log.d("myTag", "The user earned the reward.");
                    switch (extraStatus) {
                        case 1: addExtraWicket();
                            break;
                        case 2: addExtraOver();
                            break;
                        case 3: addExtraWicket();
                            addExtraOver();
                            break;
                    }
                }

                @Override
                public void onAdFailed() {
                    Log.d("myTag", "The ad failed to load.");
                    onRewardCancelled();
                }

                @Override
                public void onAdClosed() {
                    if (isRewardEarned) {
                        updateView();
                        setButtonsEnableDisable(true);
                    } else {
                        onRewardCancelled();
                    }
                }
            });
            rewardedAdDialog.dismiss();
        });

        rewardedAdDialog.show();
    }

    public void onRewardCancelled() {
        handleInningsChange();
        if (rewardedAdDialog != null)
            rewardedAdDialog.dismiss();
    }
}