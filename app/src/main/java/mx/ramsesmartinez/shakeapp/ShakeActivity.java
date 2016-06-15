package mx.ramsesmartinez.shakeapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.TextView;

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

    TextView textViewScore;
    FloatingActionButton floatingActionButton;
    Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name = getIntent().getStringExtra("NAME");
        urlImageProfile = getIntent().getStringExtra("PHOTO");
        counter = 0;
        statusFloatingActionButton = "stop";

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        textViewScore = (TextView) findViewById(R.id.text_view_score);

        chronometer.setOnChronometerTickListener(this);
        textViewScore.setText(String.valueOf(counter));
        floatingActionButton.setOnClickListener(this);

        //Sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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

    /**
     * Toolbar Methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
     * OnClicLsitener
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.floating_action_button:
                if(statusFloatingActionButton.equals("stop")){
                    onStartChronometer();
                    statusFloatingActionButton = "play";
                } else {
                    onStopChronometer();
                    statusFloatingActionButton = "stop";
                }
                break;
        }
    }


    /**
     * Listens chronometer changes
     */
    @Override
    public void onChronometerTick(Chronometer c) {
        long elapsedTime = SystemClock.elapsedRealtime() - c.getBase();
        String strElapsedTime = String.valueOf(DateFormat.format("mm:ss", elapsedTime));

        if(strElapsedTime.equals("00:10")){
            onStopChronometer();
        }
    }

    /**
     * Chronometer methods
     */
    public void onStartChronometer(){
        counter = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        textViewScore.setText(String.valueOf(counter));
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void onStopChronometer(){
        mSensorManager.unregisterListener(this);
        chronometer.stop();
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
            }
        }
    }

    /**
     * Sets the toolbar
     */

}
