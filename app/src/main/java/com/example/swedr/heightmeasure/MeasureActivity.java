package com.example.swedr.heightmeasure;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

//          DESCRIPTION

// IT'S IMPOSSIBLE TO MEASURE HEIGHT BY USING ACCELEROMETER DUE TO THE FACT THAT PHONE IS ROTATING DURING THE THROW WHICH AFFECTS ACCELEROMETER
// NEVERTHELESS I'VE DONE (BECAUSE I HAD TO) THIS APP ASSUMING THAT THROW WOULD BE IDEAL ( I SHOULD ALSO INCLUDE LOSE OF KINETIC ENERGY TO MOTION RESISTANCE
// BUT IT WOULD MAKE EVEN MORE RIDICULOUS OUTCOME - LOST OF ENERGY TO ROTATION COVER UP IT SOMEHOW

// AFTER MANY TIRES WITH DIFFRENT APPROCHES I DECIDE TO MAKE TWO STAGES OF MEASUREMENT COZ SOMETIMES IT GIVES QUITE GOOD RESULTS(STILL RARLY THOUGH)
// FIRST STAGE: LEAST 0.1 s  (in reallity 0.2 #sensorDelays) I SAVE START ACCELERATION AND HEIGHT OF THIS STAGE USING h=1/2*a*t^2
// SECOND STAGE: GETTING T(tEnd(stop in midair) - t of beginning second stage) (it's main issue) v=a*T and then aprox by h=v^2/2g

public class MeasureActivity extends Activity implements SensorEventListener {

    @BindView(R.id.measureText)
    TextView measureText;
    @BindView(R.id.measureBgResult)
    ImageView measureBgResult;
    @BindView(R.id.measureBgThrow)
    ImageView measureBgThrow;


    private Sensor accelerometer;
    private Sensor rotationV;
    private SensorManager sensorManager;

    //delete global var as possible later on
    long timeStartStageOne; //mentioned in descript
    long timeStartStageTwo; //mentioned in descript
    long timeEnd;           //mentioned in descript
    boolean locked;     // flag during one throw
    boolean canThrow;    // marking need to measure throw
    boolean stageOne;       //mentioned in descript
    boolean stageTwo;       //mentioned in descript
    boolean phonePosition; // true if phone is in the right position to throw
    double acceleration;    // starting acceleration
    double gravity;         // gravity used in stage two

    double height;          // var to gather heights from stage one and two

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        ButterKnife.bind(this);

        locked = false;
        stageOne = false;
        stageTwo = false;
        canThrow = false;
        phonePosition = false;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        rotationV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this, rotationV, SensorManager.SENSOR_DELAY_NORMAL);

        height = 0;

        gravity = 9.81;

        setContentView(R.layout.proper_pos); //setting layout with a view to obtain proper position of the phone
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor sensor = sensorEvent.sensor;

        if (!canThrow & !locked) {
            if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                if (Math.abs(sensorEvent.values[0]) < 0.1 & Math.abs(sensorEvent.values[1]) < 0.1)  // checking pos of device
                {
                    setContentView(R.layout.activity_measure);  //setting proper
                    ButterKnife.bind(this);
                    canThrow = true;
                }

            }
        }

        if (canThrow) {

            if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {


                if (sensorEvent.values[2] > 3 & !locked & !stageTwo) {
                    timeStartStageOne = System.currentTimeMillis();
                    acceleration = sensorEvent.values[2];
                    locked = true;
                    stageOne = true;

                } else if (System.currentTimeMillis() > (timeStartStageOne + 100) & stageOne & !stageTwo) {
                    timeStartStageTwo = System.currentTimeMillis();
                    calculateStageOne(100, acceleration);
                    stageTwo = true;
                }

                if (sensorEvent.values[0] < 0.5 & sensorEvent.values[1] < 0.5 & sensorEvent.values[2] < 0.5 & stageTwo) {
                    timeEnd = System.currentTimeMillis();
                    calculateStageTwo(timeEnd - timeStartStageTwo, acceleration);
                    canThrow = false;
                    result();
                }

            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    public void calculateStageOne(int stageOneDuration, double acceleration)
    {
        height += Math.pow((double) stageOneDuration / 1000, 2) * acceleration / 2.0; //Math.pow((double)deltaT/1000,2)*acceleration /2.0
    }


    public void calculateStageTwo(long stageTwoDuration, double acceleration)
    {

        // FEEL FREE TO REMOVE IT
        if (stageTwoDuration > 2000)
            stageTwoDuration = 2000;                        // to prevent ridiculous time (reasons in descript at the top)
        
        
        height += Math.pow(acceleration * stageTwoDuration / 1000, 2) / (2 * gravity); // h=1/2*a*t^2

    }

    public void result()
    {   measureBgThrow.setVisibility(View.INVISIBLE);
        measureBgResult.setVisibility(View.VISIBLE);
        measureText.setText("bold");
        measureText.setTextSize(30);
        measureText.setText("Your result: " + String.format("%f", height) +" m");
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.applause);
        mp.start();
    }

    public void reset() {
        measureBgThrow.setVisibility(View.VISIBLE);
        measureBgResult.setVisibility(View.INVISIBLE);
        timeStartStageOne = 0;
        timeStartStageTwo = 0;
        timeEnd = 0;
        locked = false;
        canThrow = false;
        stageOne = false;
        stageTwo = false;
        phonePosition = false;
        acceleration = 0;
        height = 0;
        setContentView(R.layout.proper_pos);
        measureText.setTextSize(40);
        measureText.setText("@string/throw_it");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.measuremenu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(true);
        switch (id) {
            case R.id.earth:
                gravity = 9.81;
                break;
            case R.id.moon:
                gravity = 1.62;
                break;
            case R.id.sun:
                gravity = 274;
                break;
            case R.id.new_throw:
                reset();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
