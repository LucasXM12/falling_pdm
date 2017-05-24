package com.snob.marquee.falling_pdm;

import android.os.*;
import android.app.*;
import android.widget.*;
import android.content.*;
import android.hardware.*;

public class SensorActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private TextView txtRotation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getWindow().getDecorView().setSystemUiVisibility(StartActivity.UI_OPTIONS);

        this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        this.txtRotation = (TextView) findViewById(R.id.txtRotation);
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

        StringBuffer strBuffer = new StringBuffer();

        for (float v : this.mOrientationAngles)
            strBuffer.append(String.format("%.3f ", v));

        this.txtRotation.setText(strBuffer.toString());
    }

    public void updateOrientationAngles() {
        this.mSensorManager.getRotationMatrix(this.mRotationMatrix, null, this.mAccelerometerReading, this.mMagnetometerReading);
        this.mSensorManager.getOrientation(this.mRotationMatrix, this.mOrientationAngles);
    }
}