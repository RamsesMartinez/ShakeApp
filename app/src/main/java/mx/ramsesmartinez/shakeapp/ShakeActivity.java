package mx.ramsesmartinez.shakeapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
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

public class ShakeActivity extends AppCompatActivity implements SensorEventListener, OnClickListener, Chronometer.OnChronometerTickListener {

    // Constants for sensors
    private static final float SHAKE_THRESHOLD = 1.1f;
    private static final int SHAKE_WAIT_TIME_MS = 250;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    Toolbar toolbar;
    long shakeTime = 0;

    String name;
    String urlImageProfile;
    String statusFloatingActionButton;
    int counter;
    final String time = "00:15";

    Button buttonShareFacebook;
    CallbackManager callbackManager;
    Chronometer chronometer;
    FloatingActionButton floatingActionButton;
    MediaPlayer soundWhip;
    ShareDialog shareDialog;
//    ShareButton shareButtonScore;
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
        counter = 0;
        statusFloatingActionButton = "stop";

        buttonShareFacebook = (Button) findViewById(R.id.button_facebook_share);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        textViewScore = (TextView) findViewById(R.id.text_view_number_score);
        textViewShake = (TextView) findViewById(R.id.text_view_shake);


//        soundHS = MediaPlayer.create(this, R.raw.sound_harlem_shake);
        chronometer.setOnChronometerTickListener(this);
        textViewScore.setText(String.valueOf(counter));
        floatingActionButton.setOnClickListener(this);

        //Sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        /**
         * Share button fabceook
         */
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

//        shareButtonScore = (ShareButton) findViewById(R.id.button_facebook_share);
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
            case R.id.action_settings:

                break;
            case R.id.action_logout:
                disconnectFromFacebook();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Facebook Methods
     */

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
                if(statusFloatingActionButton.equals("stop"))  onStartChronometer();
                else onStopChronometer(false);
                break;
        }
    }


    @Override
    public void onChronometerTick(Chronometer c) {
        long elapsedTime = SystemClock.elapsedRealtime() - c.getBase();
        String strElapsedTime = String.valueOf(DateFormat.format("mm:ss", elapsedTime));

        if(strElapsedTime.equals(time)) onStopChronometer(true);
    }





    /**
     * Chronometer methods
     */
    public void onStartChronometer(){
        counter = 0;
        statusFloatingActionButton = "play";

        buttonShareFacebook.setVisibility(View.GONE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        textViewScore.setVisibility(View.VISIBLE);
        textViewShake.setVisibility(View.VISIBLE);
        chronometer.setVisibility(View.VISIBLE);
        textViewScore.setText(String.valueOf(counter));

        floatingActionButton.setImageResource(R.drawable.ic_stop);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void onStopChronometer(boolean readyShare){
        if(readyShare) {
            buttonShareFacebook.setVisibility(View.VISIBLE);
            chronometer.setVisibility(View.GONE);
        } else {
            buttonShareFacebook.setVisibility(View.GONE);
        }

        chronometer.stop();
        statusFloatingActionButton = "stop";
        floatingActionButton.setImageResource(R.drawable.ic_play);

        textViewShake.setVisibility(View.GONE);
        mSensorManager.unregisterListener(this);
    }

    /**
     * Facebook Methods
     */
    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            Toast.makeText(getApplicationContext(),"Ya estás desconectado",Toast.LENGTH_SHORT).show();
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

    public void onClickShareScore(final View view){
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Mi Score: " + String.valueOf(counter))
                    .setContentDescription(
                            "¿Podrás superar mi puntaje?")
                    .setContentUrl(Uri.parse("Facebook.com"))
                    .build();

            shareDialog.show(linkContent);
        }
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                counter = 0;
                chronometer.setBase(SystemClock.elapsedRealtime());
                onStopChronometer(false);
                textViewScore.setText(String.valueOf(counter));
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

            // gForce will be close to 1 when there is no movement
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if (gForce > SHAKE_THRESHOLD) {
                textViewScore.setText(String.valueOf(++counter));
                soundWhip = MediaPlayer.create(this, R.raw.sound_shot);
                soundWhip.start();
            }
        }
    }


}
