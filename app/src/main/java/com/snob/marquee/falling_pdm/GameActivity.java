package com.snob.marquee.falling_pdm;

import android.os.*;
import android.app.*;
import android.widget.*;
import android.content.*;
import android.hardware.*;

public class GameActivity extends Activity implements SensorEventListener {

    private sGame content;

    private SensorManager mSensorManager;

    private final float[] mMagnetometerReading = new float[3];
    private final float[] mAccelerometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.content = new sGame(this, null);
        setContentView(this.content);

        getWindow().getDecorView().setSystemUiVisibility(StartActivity.UI_OPTIONS);

        this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();

        this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(this.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)))
            System.arraycopy(event.values, 0, this.mAccelerometerReading, 0, this.mAccelerometerReading.length);

        else if (event.sensor.equals(this.mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)))
            System.arraycopy(event.values, 0, this.mMagnetometerReading, 0, this.mMagnetometerReading.length);

        updateOrientationAngles();
    }

    public void updateOrientationAngles() {
        this.mSensorManager.getRotationMatrix(this.mRotationMatrix, null, this.mAccelerometerReading, this.mMagnetometerReading);
        this.mSensorManager.getOrientation(this.mRotationMatrix, this.mOrientationAngles);
    }
}