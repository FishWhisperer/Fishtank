package com.xd.akvarij;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends Activity implements SensorEventListener {
    // inputs from main menu -> new -> generate
    boolean fresh;
    int population;
    int pickedPrimaryColor;
    int pickedSecondaryColor;
    boolean randomNewGame;
    // background
    VideoView videoView;
    // game view
    GameView gameView;
    FrameLayout gameFrame;
    ConstraintLayout gameOverlay;
    Button btnFeed;
    Button btnClean;
    Button btnStats;
    Button btnPausePlay;
    Button btnOptions;
    Button btnGarry;
    TextView txtDays;
    TextView txtInfoTop;
    TextView txtInfoMiddle;
    boolean paused = false;
    AnimationDrawable garryAnimation;
    // stats
    ConstraintLayout gameOverlayStats;
    LinearLayout gameOverlayStatsSummary;
    RecyclerView recyclerViewStats;
    FishAdapterNeeds fishAdapterNeeds;
    FishAdapterTraits fishAdapterTraits;
    boolean statsOpen = false;
    public List<Fish> fishData = new ArrayList<>();
    TextView txtCounterStartingPopulation;
    TextView txtCounterFishAlive;
    TextView txtCounterFishDied;
    TextView txtCounterFishBabies;
    TextView txtCounterFishFeeding;
    TextView txtCounterTankCleaning;
    TextView txtCounterGeneration;
    Button btnStatsSummary;
    Button btnStatsFishNeeds;
    Button btnStatsFishTraits;
    TextView txtStatsDayLength, txtStatsLifeStages, txtStatsMaxSpeed, txtStatsPregnancyDays, txtStatsPregnancyChance,
    txtStatsTwinsChance, txtStatsMutationChance;
    // pause menu view
    ConstraintLayout gameLayoutPause;
    Button btnSimSettings;
    Button btnSave;
    Button btnExit;
    // simulation tweaks view
    ConstraintLayout gameLayoutSimSettings;
    Button btnSimSettingsConfirm;
    TextView txtSettingDays, txtSettingAgeInfant, txtSettingAgeTeen, txtSettingAgeAdult,
            txtSettingAgeElder, txtSettingSpeedH, txtSettingSpeedV, txtSettingPregnancyDays,
            txtSettingPregnancyChance, txtSettingTwinsChance, txtSettingMutationChance;
    // sensor stuff
    private SensorManager sensorMan;
    private Sensor accelerometer;
    private float[] gravity;
    private float[] linear_acceleration;
    int currentOrientation;
    // saving and loading
    public static final String myPrefs = "myTankPrefs";
    public static final String myPrefsKey = "tank";
    // other
    MyCallback myCallback;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // gets input from main menu -> new -> generate
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            population = extras.getInt("POPULATION");
            fresh = extras.getBoolean("FRESH");
            randomNewGame = extras.getBoolean("RANDOM");
            pickedPrimaryColor = extras.getInt("COLOR_PRIMARY");
            pickedSecondaryColor = extras.getInt("COLOR_SECONDARY");
        } else {
            // error
        }
        context = this;
        gameView = new GameView(this);
        View rootView = getLayoutInflater().inflate(R.layout.activity_game, null, true);
        // background video
        videoView = rootView.findViewById(R.id.myvideoview2);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fishtank_menu_new);
        videoView.setVideoURI(video);
        videoView.setVisibility(View.INVISIBLE);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (videoView.getVisibility() == View.VISIBLE)
                    videoView.start();
            }
        });
        // main game view
        gameFrame = rootView.findViewById(R.id.gameFrame);
        gameOverlay = rootView.findViewById(R.id.gameOverlay);
        gameOverlay.setVisibility(View.VISIBLE);
        btnFeed = gameOverlay.findViewById(R.id.btnFeed);
        btnClean = gameOverlay.findViewById(R.id.btnClean);
        btnStats = gameOverlay.findViewById(R.id.btnStats);
        btnPausePlay = gameOverlay.findViewById(R.id.btnPausePlay);
        btnOptions = gameOverlay.findViewById(R.id.btnOptions);
        txtDays = gameOverlay.findViewById(R.id.txtDays);
        txtInfoTop = gameOverlay.findViewById(R.id.txtInfo);
        txtInfoMiddle = gameOverlay.findViewById(R.id.txtGameOverlayMiddle);
        // garry
        btnGarry = gameOverlay.findViewById(R.id.btnGarry);
        btnGarry.setBackgroundResource(R.drawable.garry_animation);
        garryAnimation = (AnimationDrawable) btnGarry.getBackground();
        // game stats
        gameOverlayStats = rootView.findViewById(R.id.gameOverlayStats);
        gameOverlayStats.setVisibility(View.GONE);
        gameOverlayStatsSummary = gameOverlayStats.findViewById(R.id.layoutGameOverlayStatsSummary);
        recyclerViewStats = gameOverlayStats.findViewById(R.id.recyclerViewStats);
        btnStatsSummary = gameOverlayStats.findViewById(R.id.btnGameOverlayStatsOverall);
        btnStatsFishNeeds = gameOverlayStats.findViewById(R.id.btnGameOverlayStatsFish);
        btnStatsFishTraits = gameOverlayStats.findViewById(R.id.btnGameOverlayStatsTraits);
        // game stats summary
        txtCounterStartingPopulation = gameOverlayStats.findViewById(R.id.textView14);
        txtCounterFishAlive = gameOverlayStats.findViewById(R.id.textView15);
        txtCounterFishDied = gameOverlayStats.findViewById(R.id.textView16);
        txtCounterFishBabies = gameOverlayStats.findViewById(R.id.textView17);
        txtCounterGeneration = gameOverlayStats.findViewById(R.id.textView18);
        txtCounterFishFeeding = gameOverlayStats.findViewById(R.id.textView19);
        txtCounterTankCleaning = gameOverlayStats.findViewById(R.id.textView20);
        txtStatsDayLength = gameOverlayStats.findViewById(R.id.textView25);
        txtStatsLifeStages = gameOverlayStats.findViewById(R.id.textView26);
        txtStatsMaxSpeed = gameOverlayStats.findViewById(R.id.textView27);
        txtStatsPregnancyDays = gameOverlayStats.findViewById(R.id.textView28);
        txtStatsPregnancyChance = gameOverlayStats.findViewById(R.id.textView34);
        txtStatsTwinsChance = gameOverlayStats.findViewById(R.id.textView32);
        txtStatsMutationChance = gameOverlayStats.findViewById(R.id.textView33);
        // game menu view
        gameLayoutPause = rootView.findViewById(R.id.gameOverlayPaused);
        gameLayoutPause.setVisibility(View.GONE);
        btnSimSettings = gameLayoutPause.findViewById(R.id.btnGameOverlayPausedTweaks);
        btnSave = gameLayoutPause.findViewById(R.id.btnGameOverlayPausedSave);
        btnExit = gameLayoutPause.findViewById(R.id.btnGameOverlayPausedExit);
        // simulation tweaks view
        gameLayoutSimSettings = rootView.findViewById(R.id.gameOverlaySimSettings);
        gameLayoutSimSettings.setVisibility(View.GONE);
        btnSimSettingsConfirm = gameLayoutSimSettings.findViewById(R.id.btnGameOverlaySimSettingsConfirm);
        txtSettingDays = gameLayoutSimSettings.findViewById(R.id.txtEditDayLength);
        txtSettingAgeInfant = gameLayoutSimSettings.findViewById(R.id.txtEditMaxInfantAge);
        txtSettingAgeTeen = gameLayoutSimSettings.findViewById(R.id.txtEditMaxTeenAge);
        txtSettingAgeAdult = gameLayoutSimSettings.findViewById(R.id.txtEditMaxAdultAge);
        txtSettingAgeElder = gameLayoutSimSettings.findViewById(R.id.txtEditMaxElderAge);
        txtSettingSpeedH = gameLayoutSimSettings.findViewById(R.id.txtEditMaxHorizontalSpeed);
        txtSettingSpeedV = gameLayoutSimSettings.findViewById(R.id.txtEditMaxVerticalSpeed);
        txtSettingPregnancyDays = gameLayoutSimSettings.findViewById(R.id.txtEditBreeding);
        txtSettingPregnancyChance = gameLayoutSimSettings.findViewById(R.id.txtEditBreeding1);
        txtSettingTwinsChance = gameLayoutSimSettings.findViewById(R.id.txtEditBreeding2);
        txtSettingMutationChance = gameLayoutSimSettings.findViewById(R.id.txtEditBreeding3);
        txtSettingDays.setText(Integer.toString(Constants.DAY_LENGTH_IN_SECONDS));
        txtSettingAgeInfant.setText(Integer.toString(Constants.AGE_MAX_INFANT));
        txtSettingAgeTeen.setText(Integer.toString(Constants.AGE_MAX_TEEN));
        txtSettingAgeAdult.setText(Integer.toString(Constants.AGE_MAX_ADULT));
        txtSettingAgeElder.setText(Integer.toString(Constants.AGE_MAX));
        txtSettingSpeedH.setText(Integer.toString(Constants.MAX_HORIZONTAL_SPEED));
        txtSettingSpeedV.setText(Integer.toString(Constants.MAX_VERTICAL_SPEED));
        txtSettingPregnancyDays.setText(Integer.toString(Constants.PREGNANCY_DAYS));
        txtSettingPregnancyChance.setText(Integer.toString(Constants.PREGNANCY_CHANCE));
        txtSettingTwinsChance.setText(Integer.toString(Constants.PREGNANCY_TWINS_CHANCE));
        txtSettingMutationChance.setText(Integer.toString(Constants.MUTATION_CHANCE));
        // set up the views
        gameFrame.removeView(gameOverlay);
        gameFrame.removeView(gameOverlayStats);
        gameFrame.removeView(gameLayoutPause);
        gameFrame.removeView(gameLayoutSimSettings);
        gameFrame.addView(gameView);
        gameFrame.addView(gameOverlay);
        gameFrame.addView(gameOverlayStats);
        gameFrame.addView(gameLayoutPause);
        gameFrame.addView(gameLayoutSimSettings);
        // enables interaction from other classes
        myCallback = new MyCallback() {
            // main game overlay
            @Override
            public void updateTxtDays(final String myString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtDays.setText(myString);
                    }
                });
            }
            @Override
            public void updateTxtInfoTop(final String myString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtInfoTop.setText(myString);
                    }
                });
            }
            @Override
            public void updateTxtMiddle() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtInfoMiddle.setVisibility(View.VISIBLE);
                    }
                });
            }
            // stats game overlay
            @Override
            public void updateAdapter() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerViewStats.getRecycledViewPool().clear();
                        fishData.clear();
                        fishData.addAll(gameView.tank.fish);
                        fishAdapterNeeds.notifyDataSetChanged();
                        fishAdapterTraits.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void statsUpdateStartingPopulation(final int counter) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCounterStartingPopulation.setText("> STARTING POPULATION: " + counter);
                    }
                });
            }
            @Override
            public void statsUpdateCurrentlyAlive() {
                gameView.tank.countFishAlive = gameView.tank.countAlive();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCounterFishAlive.setText("> CURRENTLY ALIVE: " + gameView.tank.countFishAlive);
                    }
                });
            }
            @Override
            public void statsUpdateFishDeaths() {
                gameView.tank.countFishDeaths++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCounterFishDied.setText("> DEAD FISH TOTAL: " + gameView.tank.countFishDeaths);
                    }
                });
            }
            @Override
            public void statsUpdateFishOffspring() {
                gameView.tank.countFishBabies++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCounterFishBabies.setText("> BABY FISH TOTAL: " + gameView.tank.countFishBabies);
                    }
                });
            }
            @Override
            public void statsUpdateGenerationReached(final int counter) {
                gameView.tank.countGenerationReached = counter;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCounterGeneration.setText("> GENERATION REACHED: " +
                                gameView.tank.countGenerationReached);
                    }
                });
            }
            @Override
            public void removeFish(Fish f, int position) {
                gameView.tank.fish.remove(f);
                fishData.clear();
                fishData.addAll(gameView.tank.fish);
                fishAdapterTraits.notifyItemRemoved(position);
                fishAdapterTraits.notifyDataSetChanged();
                fishAdapterTraits.notifyItemRangeChanged(position, gameView.tank.fish.size());
                fishAdapterNeeds.notifyItemRemoved(position);
                fishAdapterNeeds.notifyDataSetChanged();
                fishAdapterNeeds.notifyItemRangeChanged(position, gameView.tank.fish.size());
                statsUpdateFishDeaths();
                statsUpdateCurrentlyAlive();
            }

            @Override
            public int getCurrentGeneration() {
                return gameView.tank.countGenerationReached;
            }

            @Override
            public void decreaseEnvironment() {
                gameView.tank.decreaseEnvironment();
            }
        };
        // button feed fish
        btnFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameView.thread.isAlive()) {
                    gameView.tank.feedFish();
                    gameView.tank.countFishFeeding++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtCounterFishFeeding.setText("You fed your fish " + gameView.tank.countFishFeeding + " times.");
                        }
                    });
                }
            }
        });
        // button clean tank
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameView.tank.food.size() > 0 || gameView.tank.poop.size() > 0) {
                    gameView.tank.cleanPoop();
                    gameView.tank.countTankCleaning++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtCounterTankCleaning.setText("You cleaned the tank " + gameView.tank.countTankCleaning + " times.");
                        }
                    });
                }
            }
        });
        // button garry
        btnGarry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                garryAnimation.stop();
                garryAnimation.start();
            }
        });
        // toggle stats
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!statsOpen) {
                    gameOverlayStats.setAlpha(0.0f);
                    gameOverlayStats.setVisibility(View.VISIBLE);
                    gameOverlayStats.animate().alpha(1.0f);
                    statsOpen = true;
                } else {
                    statsOpen = false;
                    gameOverlayStats.setAlpha(1.0f);
                    gameOverlayStats.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!statsOpen) {
                                super.onAnimationEnd(animation);
                                gameOverlayStats.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
        // stats summary
        btnStatsSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerViewStats.getVisibility() == View.VISIBLE) {
                    recyclerViewStats.setVisibility(View.GONE);
                }
                gameOverlayStatsSummary.setAlpha(0.0f);
                gameOverlayStatsSummary.setVisibility(View.VISIBLE);
                gameOverlayStatsSummary.animate().alpha(1.0f);
            }
        });
        // stats needs
        btnStatsFishNeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOverlayStatsSummary.getVisibility() == View.VISIBLE) {
                    gameOverlayStatsSummary.setVisibility(View.GONE);
                }
                recyclerViewStats.setAlpha(0.0f);
                recyclerViewStats.setAdapter(fishAdapterNeeds);
                recyclerViewStats.setVisibility(View.VISIBLE);
                recyclerViewStats.animate().alpha(1.0f);
            }
        });
        // stats traits
        btnStatsFishTraits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameOverlayStatsSummary.getVisibility() == View.VISIBLE) {
                    gameOverlayStatsSummary.setVisibility(View.GONE);
                }
                recyclerViewStats.setAlpha(0.0f);
                recyclerViewStats.setAdapter(fishAdapterTraits);
                recyclerViewStats.setVisibility(View.VISIBLE);
                recyclerViewStats.animate().alpha(1.0f);
            }
        });
        // pause play thread
        btnPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paused) {
                    paused = false;
                    gameView.thread = new MainThread(gameView.getHolder(), gameView);
                    gameView.thread.setRunning(true);
                    gameView.thread.start();
                } else {
                    try {
                        paused = true;
                        gameView.thread.setRunning(false);
                        gameView.thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // open pause menu
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    gameView.thread.setRunning(false);
                    gameView.thread.join();
                    gameOverlay.setVisibility(View.GONE);
                    gameOverlayStats.setVisibility(View.GONE);
                    gameLayoutSimSettings.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.start();
                    gameLayoutPause.setAlpha(0.0f);
                    gameLayoutPause.setVisibility(View.VISIBLE);
                    Thread.sleep(200);
                    gameLayoutPause.animate().alpha(1.0f);
                    //saveData(context, myPrefs, myPrefsKey, gameView.tank);
                    //Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    //startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // simulation tweaks menu
        btnSimSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameLayoutPause.setVisibility(View.GONE);
                gameLayoutSimSettings.setVisibility(View.VISIBLE);
            }
        });
        // simulation tweaks menu go back to pause menu
        btnSimSettingsConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.DAY_LENGTH_IN_SECONDS = Integer.parseInt(txtSettingDays.getText().toString());
                Constants.AGE_MAX_INFANT = Integer.parseInt(txtSettingAgeInfant.getText().toString());
                Constants.AGE_MAX_TEEN = Integer.parseInt(txtSettingAgeTeen.getText().toString());
                Constants.AGE_MAX_ADULT = Integer.parseInt(txtSettingAgeAdult.getText().toString());
                Constants.AGE_MAX = Integer.parseInt(txtSettingAgeElder.getText().toString());
                Constants.MAX_HORIZONTAL_SPEED = Integer.parseInt(txtSettingSpeedH.getText().toString());
                Constants.MAX_VERTICAL_SPEED = Integer.parseInt(txtSettingSpeedV.getText().toString());
                Constants.PREGNANCY_DAYS = Integer.parseInt(txtSettingPregnancyDays.getText().toString());
                Constants.PREGNANCY_TWINS_CHANCE = Integer.parseInt(txtSettingTwinsChance.getText().toString());
                Constants.MUTATION_CHANCE = Integer.parseInt(txtSettingMutationChance.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtSettingDays.setText(Integer.toString(Constants.DAY_LENGTH_IN_SECONDS));
                        txtSettingAgeInfant.setText(Integer.toString(Constants.AGE_MAX_INFANT));
                        txtSettingAgeTeen.setText(Integer.toString(Constants.AGE_MAX_TEEN));
                        txtSettingAgeAdult.setText(Integer.toString(Constants.AGE_MAX_ADULT));
                        txtSettingAgeElder.setText(Integer.toString(Constants.AGE_MAX));
                        txtSettingSpeedH.setText(Integer.toString(Constants.MAX_HORIZONTAL_SPEED));
                        txtSettingSpeedV.setText(Integer.toString(Constants.MAX_VERTICAL_SPEED));
                        txtSettingPregnancyDays.setText(Integer.toString(Constants.PREGNANCY_DAYS));
                        txtSettingPregnancyChance.setText(Integer.toString(Constants.PREGNANCY_CHANCE));
                        txtSettingTwinsChance.setText(Integer.toString(Constants.PREGNANCY_TWINS_CHANCE));
                        txtSettingMutationChance.setText(Integer.toString(Constants.MUTATION_CHANCE));
                    }
                });
                gameLayoutSimSettings.setVisibility(View.GONE);
                gameLayoutPause.setVisibility(View.VISIBLE);
            }
        });
        // sensor stuff
        sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = new float[3];
        linear_acceleration = new float[3];
        // landscape
        final OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == 90)
                    currentOrientation = orientation;
                if (orientation == 270)
                    currentOrientation = orientation;
            }
        };
        if (orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();
        // stats recycler view needs
        fishAdapterNeeds = new FishAdapterNeeds(fishData);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewStats.setLayoutManager(mLayoutManager);
        recyclerViewStats.setItemAnimator(new DefaultItemAnimator());
        recyclerViewStats.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerViewStats.setAdapter(fishAdapterNeeds);
        // stats recycler view needs
        fishAdapterTraits = new FishAdapterTraits(fishData, myCallback);
        // get stats from constants
        txtStatsDayLength.setText("> DAY LENGTH: " + Constants.DAY_LENGTH_IN_SECONDS);
        txtStatsLifeStages.setText("> LIFE STAGES: " + Constants.AGE_MAX_INFANT + " - " +
                Constants.AGE_MAX_TEEN + " - " + Constants.AGE_MAX_ADULT + " - "+ Constants.AGE_MAX);
        txtStatsMaxSpeed.setText("> H - V MAX SPEED: " + Constants.MAX_HORIZONTAL_SPEED + " - "
                + Constants.MAX_VERTICAL_SPEED);
        txtStatsPregnancyDays.setText("> PREGNANCY LENGTH: " + Constants.PREGNANCY_DAYS);
        txtStatsPregnancyChance.setText("> PREGNANCY CHANCE: " + Constants.PREGNANCY_CHANCE);
        txtStatsTwinsChance.setText("> TWINS CHANCE: " + Constants.PREGNANCY_TWINS_CHANCE);
        txtStatsMutationChance.setText("> MUTATION CHANCE: " + Constants.MUTATION_CHANCE);
        // start the engines
        loadData(population, this);
        setContentView(gameFrame);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMan.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float alpha = 0.8f;
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];
            if (linear_acceleration[0] > 0.01 || linear_acceleration[0] < -0.01) {
                if (currentOrientation == 90)
                    gameView.tank.shakingStart(-event.values[1], -event.values[0]);
                else
                    gameView.tank.shakingStart(event.values[1], event.values[0]);

            } else
                gameView.tank.shakingStop();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void saveData(Context context, String preferenceFileName, String serializedObjectKey,
                         Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    private void loadData(int popSize, Context context) {
        if (fresh) {
            gameView.tank = new Tank(popSize, myCallback, context);
            if (randomNewGame)
                gameView.tank.generateRandomNew();
            else
                gameView.tank.generateCustomNew(pickedPrimaryColor, pickedSecondaryColor);
            txtCounterFishAlive.setText("> CURRENTLY ALIVE: " + popSize);
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences(myPrefs, 0);
            if (sharedPreferences.contains(myPrefsKey)) {
                final Gson gson = new Gson();
                gameView.tank = gson.fromJson(sharedPreferences.getString(myPrefsKey, ""),
                        Tank.class);
                gameView.daytime = gameView.tank.dayTime;
                gameView.tank.myCallback = myCallback;
                txtDays.setText("DAY " + gameView.tank.dayCounter);
            } else {
                // DISPLAY ERROR
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (gameLayoutPause.getVisibility() == View.VISIBLE) {
            videoView.stopPlayback();
            videoView.setVisibility(View.GONE);
            gameLayoutPause.setVisibility(View.GONE);
            gameView.thread = new MainThread(gameView.getHolder(), gameView);
            gameView.thread.setRunning(true);
            gameView.thread.start();
            gameOverlay.setVisibility(View.VISIBLE);
        } else if (gameLayoutSimSettings.getVisibility() == View.VISIBLE) {
            gameLayoutSimSettings.setVisibility(View.GONE);
            gameLayoutPause.setVisibility(View.VISIBLE);
        } else {
            // TODO save before exiting?
            super.onBackPressed();
        }
    }
}
