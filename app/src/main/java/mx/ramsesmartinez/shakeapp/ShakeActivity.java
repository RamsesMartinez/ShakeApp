package mx.ramsesmartinez.shakeapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ShakeActivity extends AppCompatActivity implements SensorEventListener, OnClickListener{

    // Constants for sensors
    private static final float SHAKE_THRESHOLD = 1.4f;
    private static final int SHAKE_WAIT_TIME_MS = 250;

    //Values to set the counter
    private static final long TIME = 30000;
    private static final long TICK_TIME = 50;
    private long timePause;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    Toolbar toolbar;
    long shakeTime = 0;

    String name;
    String urlImageProfile;
    String statusFloatingActionButton;
    int counterScore;

    CallbackManager callbackManager;
    CountDownTimer counterDownTimer;
    FloatingActionButton floatingActionButtonPlay;
    MediaPlayer shakeSound;
    ShareDialog shareDialog;
    Snackbar snackbar;
    TextView textViewChronometer;
    TextView textViewScore;
    TextView textViewShake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_shake);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name = getIntent().getStringExtra("NAME");
        urlImageProfile = getIntent().getStringExtra("PHOTO");
        counterScore = 0;
        statusFloatingActionButton = "stop";

        floatingActionButtonPlay = (FloatingActionButton) findViewById(R.id.floating_action_button);
        textViewChronometer = (TextView) findViewById(R.id.chronometer);
        textViewScore = (TextView) findViewById(R.id.text_view_number_score);
        textViewShake = (TextView) findViewById(R.id.text_view_shake);

        textViewScore.setText(String.valueOf(counterScore));
        floatingActionButtonPlay.setOnClickListener(this);

        //Sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Facebook Manager
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // If the counter is running
        if(statusFloatingActionButton.equals("play")){
            onStopChronometer();
            savedInstanceState.putString("STATUS_COUNTER","play");
            savedInstanceState.putInt("SCORE",counterScore);
            savedInstanceState.putLong("TIME",timePause);
        }else{
            savedInstanceState.putString("STATUS_COUNTER","stop");
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Validates if the counter was enabled
        if(savedInstanceState.getString("STATUS_COUNTER").equals("play")){
            counterScore = savedInstanceState.getInt("SCORE");
            timePause = savedInstanceState.getLong("TIME");
            //Prepares the counter to be started
            onStartChronometer(counterScore);
        }

        // Restore variables on screen orientation change.
//        counterScore = savedInstanceState.getInt("SCORE");
//        textViewScore.setText(String.valueOf(counterScore));
//        timePause = savedInstanceState.getInt("TIME");
//        statusFloatingActionButton = (savedInstanceState.getString("PLAY_BUTTON"));
//
//        Toast.makeText(getApplicationContext(),savedInstanceState.getString("PLAY_BUTTON"),Toast.LENGTH_SHORT).show();
//
//        if(savedInstanceState.getBoolean("COUNTER"))
//            onStartChronometer();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Toolbar Methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_logout:
                disconnectFromFacebook();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Register listener
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            }
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * OnClickListener
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.floating_action_button:
                if(statusFloatingActionButton.equals("stop"))
                    onStartChronometer();
                else
                    onStopChronometer();
                break;
        }
    }

    /**
     * Shows a Snackbar when the counter is over
     */
    public void snackBarShare() {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        snackbar = Snackbar.make(coordinatorLayout, getString(R.string.final_score) + " " + counterScore, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.facebook_share), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        facebookShareScore();
                    }
                });

        //Action text color
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.icons));

        //Background color
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.com_facebook_button_background_color));

        //Message text color
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.icons));

        snackbar.show();
    }

    // Chronometer methods
    /**
     * This chronometer method starts the chronometer with the counterScore=0
     */
    public void onStartChronometer(){

        counterScore = 0;
        statusFloatingActionButton = "play";

        floatingActionButtonPlay.setImageResource(R.drawable.ic_stop);
        textViewShake.setVisibility(View.VISIBLE);
        textViewChronometer.setVisibility(View.VISIBLE);
        textViewScore.setText(String.valueOf(counterScore));

        if(snackbar != null)
            snackbar.dismiss();

        // Starts the counter
        counterDT();
        counterDownTimer.start();

        //Starts the SensorManager
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * This method is called when the user rotates the display
     * @param counter sets the initial Score
     */
    public void onStartChronometer(int counter){
        counterScore = counter;
        statusFloatingActionButton = "play";

        floatingActionButtonPlay.setImageResource(R.drawable.ic_stop);
        textViewShake.setVisibility(View.VISIBLE);
        textViewChronometer.setVisibility(View.VISIBLE);
        textViewScore.setText(String.valueOf(counterScore));

        if(snackbar != null)
            snackbar.dismiss();

        // Starts the counter
        counterDT(timePause);
        counterDownTimer.start();

        //Starts the SensorManager
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStopChronometer(){
        floatingActionButtonPlay.setImageResource(R.drawable.ic_play);
        textViewShake.setVisibility(View.GONE);
        statusFloatingActionButton = "stop";
//        timePause = TIME;
        mSensorManager.unregisterListener(this);
        counterDownTimer.cancel();
    }

    /**
     * Sets the basic counterDownTimer, its called bye the onCreate method
     */
    private void counterDT(){
        counterDownTimer = new CountDownTimer(TIME, TICK_TIME) {
            public void onTick(long millisUntilFinished) {
                String hms = String.format("%02d:%02d.%03d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toMillis(millisUntilFinished)  - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)));
                textViewChronometer.setText(hms);
                timePause = millisUntilFinished;
            }

            public void onFinish() {
                textViewChronometer.setText(R.string.chronometer);
                snackBarShare();
                onStopChronometer();
                timePause = TIME;
            }
        };
    }

    /**
     * Sets the counterDownTimer but whit a different time
     * @param time
     */
    private void counterDT(final long time){
        counterDownTimer = new CountDownTimer(time, TICK_TIME) {
            public void onTick(long millisUntilFinished) {
                String hms = String.format("%02d:%02d.%03d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toMillis(millisUntilFinished)  - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)));
                textViewChronometer.setText(hms);
                timePause = millisUntilFinished;
            }

            public void onFinish() {
                textViewChronometer.setText(R.string.chronometer);
                snackBarShare();
                onStopChronometer();
                timePause = TIME;
            }
        };
    }


    /**
     * Facebook Methods
     */
    private void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            LoginManager.getInstance().logOut();
            Intent intentMainActivity = new Intent(ShakeActivity.this,MainActivity.class);
            startActivity(intentMainActivity);
            Toast.makeText(getApplicationContext(),"Sesión finalizada",Toast.LENGTH_SHORT).show();
            finish();
            return; // already logged out
        }

        /**
         * Simple Logout
         */
        LoginManager.getInstance().logOut();
        Intent intentMainActivity = new Intent(ShakeActivity.this,MainActivity.class);
        startActivity(intentMainActivity);
        Toast.makeText(getApplicationContext(),"Sesión finalizada",Toast.LENGTH_SHORT).show();
        finish();

        /**
         * Methods to completely de-couple
         */
//        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
//                .Callback() {
//            @Override
//            public void onCompleted(GraphResponse graphResponse) {
//                LoginManager.getInstance().logOut();
//                Intent intentMainActivity = new Intent(ShakeActivity.this,MainActivity.class);
//                startActivity(intentMainActivity);
//                finish();
//                Toast.makeText(getApplicationContext(),"Sesión finalizada",Toast.LENGTH_SHORT).show();
//            }
//        }).executeAsync();

    }

    private void facebookShareScore(){
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Mi Puntaje: " + String.valueOf(counterScore))
                    .setContentDescription(
                            "¿Podrás superar mi puntaje?")
                    .setContentUrl(Uri.parse("http://shakeapp-android.blogspot.mx/"))
                    .build();

            shareDialog.show(linkContent);
        }
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                counterScore = 0;
//                textViewChronometer.setBase(SystemClock.elapsedRealtime());
                onStopChronometer();
                textViewScore.setText(String.valueOf(counterScore));
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    /**
     * Shakes detector
     */
    private void detectShake(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - shakeTime) > SHAKE_WAIT_TIME_MS) {
            shakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD) {
                textViewScore.setText(String.valueOf(counterScore++));
                shakeSound = MediaPlayer.create(this, R.raw.sound_jump_1);
                shakeSound.start();
                shakeSound.stop();
            }
        }
    }
}
