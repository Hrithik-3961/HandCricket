package com.Hand_Cricket;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PlayingScreen extends AppCompatActivity implements View.OnClickListener {


    private boolean playerBatting = true, flag = true, oversCompleted = false, soundOn = true, vibrationOn = true, adClosed;
    private int playerPoints = 0, computerPoints = 0, wickets = 3, playerWickets = 0, computerWickets = 0, playerRuns = 0, computerRuns = 0, totalOvers, ballsCount = 0, oversCount = 0;
    private double playerOvers = 0.0, computerOvers = 0.0;
    private String statusKey = "";

    private LottieAnimationView wicket;
    private MediaPlayer myWicketSound, opponentWicketSound, crowdSound;

    private final Button[] btn = new Button[10];
    private TextView status, oversDisplay;
    private ImageView computerHand, playerHand;

    private RewardedAd mRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.playing_screen);

        FrameLayout adContainer = findViewById(R.id.adView);
        Display display = getWindowManager().getDefaultDisplay();
        new HowTo().loadBanner(adContainer, this, display);

        loadAd();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        int choice = bundle.getInt("choice");
        totalOvers = bundle.getInt("overs");

        oversDisplay = findViewById(R.id.overs);
        String str = "Over: 0.0(" + totalOvers + ")";
        oversDisplay.setText(str);

        status = findViewById(R.id.status);
        if (choice == 1) {
            status.setText(getText(R.string.playerBatting));
            playerBatting = true;
        } else {
            status.setText(getText(R.string.playerBowling));
            playerBatting = false;
        }


        soundOn = MyPrefs.getSoundPref(this);
        vibrationOn = MyPrefs.getVibrationPref(this);

        if (soundOn) {
            if (crowdSound == null) {
                crowdSound = MediaPlayer.create(this, R.raw.crowd);
                crowdSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
                crowdSound.setOnCompletionListener(mp -> stopPlayer());
            }
            crowdSound.start();
        }


        final Button back = findViewById(R.id.back);
        back.setOnClickListener(v -> onBackPressed());

        final Button more = findViewById(R.id.more);
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
                        stopPlayer();
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

                item.setShowAsAction(item.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(v);
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                return false;
            });

        });

        int[] resID = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10};
        for (int i = 0; i < 10; i++) {
            btn[i] = findViewById(resID[i]);
            btn[i].setOnClickListener(this);
        }
    }

    public static void setForceShowIcon(PopupMenu popupMenu) {
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.activity_how_to, null);
        alert.setView(mView);

        TextView text = mView.findViewById(R.id.text);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        text.setMovementMethod(new ScrollingMovementMethod());

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        Button backHowTo = mView.findViewById(R.id.back);
        backHowTo.setVisibility(View.GONE);

        Button ok = mView.findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {

        if (crowdSound != null) {
            crowdSound.release();
            crowdSound = null;
        }

        ballsCount++;

        if (ballsCount == 6) {
            oversCount++;
            ballsCount = 0;
        }

        if (oversCount == totalOvers && !oversCompleted) {
            ballsCount = 0;
            if (playerBatting)
                showVideoAdDialog("Over", "");
            else {
                oversCompleted = true;
                showInningsChange("");
            }
            setButtonsEnableDisable(false);
            playerBatting = !playerBatting;
            playerRuns = 0;
            computerRuns = 0;
            if (playerBatting)
                playerPoints = 0;
            else
                computerPoints = 0;
        }

        if (flag) {
            status = findViewById(R.id.status);
            playerRuns = Integer.parseInt(((Button) v).getText().toString());
            computerRuns = (int) (1 + Math.random() * 10);

            if (playerRuns == computerRuns && playerRuns != 0) {
                if (playerBatting) {
                    playerWickets++;
                    setButtonsEnableDisable(false);
                    showWicketAnimation();
                    if ((playerWickets == wickets || oversCount == totalOvers) && !oversCompleted) {
                        ballsCount = 0;
                        oversCompleted = true;
                        setButtonsEnableDisable(false);
                        showInningsChange("");
                        playerBatting = false;
                        computerRuns = 0;
                        computerPoints = 0;
                        playerRuns = 0;
                    }
                } else {
                    computerWickets++;
                    setButtonsEnableDisable(false);
                    showWicketAnimation();
                    if ((computerWickets == wickets || oversCount == totalOvers) && !oversCompleted) {
                        ballsCount = 0;
                        oversCompleted = true;
                        setButtonsEnableDisable(false);
                        showInningsChange("");
                        playerBatting = true;
                        playerRuns = 0;
                        playerPoints = 0;
                        computerRuns = 0;
                    }
                }
            } else {
                if (playerBatting)
                    playerPoints += playerRuns;
                else
                    computerPoints += computerRuns;
            }


            if (ballsCount == 0 && oversCount == 0 && oversCompleted) {
                playerRuns = 0;
                computerRuns = 0;
                if (playerBatting) {
                    playerPoints = 0;
                    playerWickets = 0;
                } else {
                    computerPoints = 0;
                    computerWickets = 0;
                }
                update();
            }

            if (playerBatting && (playerPoints > computerPoints) && !(computerPoints == 0 && computerWickets == 0)) {
                showInningsChange("You Won");
                flag = false;
            } else if (!playerBatting && (computerPoints > playerPoints) && !(playerPoints == 0 && playerWickets == 0)) {
                showInningsChange("You Lost");
                flag = false;
            }

            if (oversCompleted && oversCount == totalOvers) {
                if (winStatus().equals("You Lost") && playerBatting)
                    showVideoAdDialog("Over", winStatus());
                else
                    showInningsChange(winStatus());
            }

            update();
        }

    }

    private String winStatus() {

        if (playerPoints > computerPoints)
            return "You Won";
        else if (computerPoints > playerPoints)
            return "You Lost";
        else
            return "Game Draw";

    }

    @SuppressLint("SetTextI18n")
    public void update() {

        if (playerBatting)
            status.setText(getText(R.string.playerBatting));
        else
            status.setText(getText(R.string.playerBowling));

        playerHand = findViewById(R.id.playerHand);
        final String[] imageID = {"image" + playerRuns};
        final int[] resID = {getResources().getIdentifier(imageID[0], "drawable", getPackageName())};
        playerHand.setBackgroundResource(resID[0]);

        String s = getText(R.string.over).toString() + oversCount + "." + ballsCount + "(" + totalOvers + ")";
        oversDisplay.setText(s);

        TextView score2 = findViewById(R.id.score2);
        score2.setText(playerPoints + "/" + playerWickets);

        TextView score1 = findViewById(R.id.score1);
        score1.setText(computerPoints + "/" + computerWickets);

        imageID[0] = "image" + computerRuns;
        resID[0] = getResources().getIdentifier(imageID[0], "drawable", getPackageName());
        computerHand = findViewById(R.id.computerHand);
        computerHand.setBackgroundResource(resID[0]);
    }

    private void showWicketAnimation() {
        wicket = findViewById(R.id.wicket);
        wicket.setVisibility(View.VISIBLE);
        wicket.playAnimation();

        if (vibrationOn) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            assert vibrator != null;
            long[] pattern = {0, 100, 200, 100};
            vibrator.vibrate(pattern, -1);
        }

        if (soundOn) {
            if (playerBatting) {
                if (myWicketSound == null) {
                    myWicketSound = MediaPlayer.create(this, R.raw.my_wicket);
                    myWicketSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    myWicketSound.setOnCompletionListener(mp -> stopPlayer());
                }
                myWicketSound.start();
            } else {
                if (opponentWicketSound == null) {
                    opponentWicketSound = MediaPlayer.create(this, R.raw.opponent_wicket);
                    opponentWicketSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    opponentWicketSound.setOnCompletionListener(mp -> stopPlayer());
                }
                opponentWicketSound.start();
            }
        }
        wicket.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setButtonsEnableDisable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setButtonsEnableDisable(true);
                wicket.setVisibility(View.GONE);
                if (playerBatting && playerWickets == wickets && oversCompleted)
                    showVideoAdDialog("Wicket", winStatus());
                else if (!playerBatting && computerWickets == wickets && oversCompleted)
                    showInningsChange(winStatus());

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

    }

    public void stopPlayer() {
        if (myWicketSound != null) {
            myWicketSound.release();
            myWicketSound = null;
        }
        if (opponentWicketSound != null) {
            opponentWicketSound.release();
            opponentWicketSound = null;
        }

        if (crowdSound != null) {
            crowdSound.release();
            crowdSound = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

    @SuppressLint("SetTextI18n")
    private void showInningsChange(final String str) {

        AlertDialog.Builder alert = new AlertDialog.Builder(PlayingScreen.this);
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.score_board, null);

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

        if (oversCompleted && !str.equals(""))
            text2.setVisibility(View.GONE);

        ok.setOnClickListener(v -> {

            if (oversCompleted && !str.equals("")) {
                Intent intent = new Intent(PlayingScreen.this, AfterGameEnds.class);
                intent.putExtra("Win status", str);
                intent.putExtra("total overs", totalOvers);
                intent.putExtra("player points", playerPoints);
                intent.putExtra("player wickets", playerWickets);
                intent.putExtra("computer points", computerPoints);
                intent.putExtra("computer wickets", computerWickets);
                intent.putExtra("player overs", playerOvers);
                intent.putExtra("computer overs", computerOvers);
                intent.putExtra("player batting", playerBatting);
                startActivity(intent);
                finish();
            }

            if (str.equals("")) {
                if (soundOn) {
                    if (crowdSound == null) {
                        crowdSound = MediaPlayer.create(PlayingScreen.this, R.raw.crowd);
                        crowdSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        crowdSound.setOnCompletionListener(mp -> stopPlayer());
                    }
                    crowdSound.start();
                }
            }
            alertDialog.cancel();
            setButtonsEnableDisable(true);
        });

        if (playerBatting)
            playerOvers = oversCount + ballsCount / 10.0;
        else
            computerOvers = oversCount + ballsCount / 10.0;
        oversDisp.setText(oversCount + "." + ballsCount);

        if (playerBatting) {

            text1.setText(R.string.Player);
            runsDisplay.setText(String.valueOf(playerPoints));
            wicketsDisplay.setText(String.valueOf(playerWickets));
            text2.setText(getText(R.string.Computer).toString() + " needs " + (playerPoints + 1) + " in " + totalOvers + " overs to Win.");
        } else {

            text1.setText(R.string.Computer);
            runsDisplay.setText(String.valueOf(computerPoints));
            wicketsDisplay.setText(String.valueOf(computerWickets));
            text2.setText(getText(R.string.Player).toString() + " needs " + (computerPoints + 1) + " in " + totalOvers + " overs to Win.");
        }
        alertDialog.show();

        if (str.equals("")) {
            oversCount = 0;
            ballsCount = 0;
            computerHand.setBackgroundResource(R.drawable.image0);
            playerHand.setBackgroundResource(R.drawable.image0);
            update();
        }
    }

    private void setButtonsEnableDisable(boolean flag) {
        int[] resID = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10};
        for (int i = 0; i < 10; i++) {
            btn[i] = findViewById(resID[i]);
            btn[i].setEnabled(flag);
        }
    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.exitMessage))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<font color = '#FF0101'>Yes</font>"), (dialog, which) -> {

                    Intent intent = new Intent(PlayingScreen.this, HomeScreen.class);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton(Html.fromHtml("<font color = '#FF0101'>No</font>"), (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.RewardedID),
                adRequest, new RewardedAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                loadAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                mRewardedAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mRewardedAd = null;
                    }
                });
    }

    private void showVideoAdDialog(String s, final String status) {
        adClosed = true;
        statusKey = status;
        AlertDialog.Builder alert = new AlertDialog.Builder(PlayingScreen.this);
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.rewarded_video_dialog, null);
        alert.setView(mView);

        TextView text = mView.findViewById(R.id.text);
        ImageButton cancel = mView.findViewById(R.id.cancel);
        Button continueBtn = mView.findViewById(R.id.continueBtn);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        if (s.equals("Over"))
            text.setText(getResources().getString(R.string.extra_over));
        else if (s.equals("Wicket"))
            text.setText(getResources().getString(R.string.extra_wicket));

        cancel.setOnClickListener(view -> {
            alertDialog.dismiss();

            if (status.equals(""))
                oversCompleted = true;

            showInningsChange(status);
        });

        continueBtn.setOnClickListener(view -> {
            if (mRewardedAd != null) {
                Activity activityContext = PlayingScreen.this;
                mRewardedAd.show(activityContext, rewardItem -> {
                    // Handle the reward.
                    Log.d("myTag", "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                });
            }
            alertDialog.dismiss();
        });

        alertDialog.show();
    }
}