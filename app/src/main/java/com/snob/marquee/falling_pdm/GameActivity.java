package com.snob.marquee.falling_pdm;

import android.os.*;
import android.view.*;
import android.content.*;
import android.hardware.*;
import android.graphics.*;
import android.support.v7.app.*;
import android.support.annotation.*;

import java.util.*;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private final float SPEED = 20;

    private int circleRadius = 150;
    private float circleX;
    private float circleY;

    private GameView canvas;

    private Timer timer;
    private Handler handler;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float sX;
    private float sY;
    private float sZ;

    private long lastSensorUpdateTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.lastSensorUpdateTime = 0;

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenW = size.x;
        int screenH = size.y;

        this.circleX = screenW / 2 - this.circleRadius;
        this.circleY = screenH / 2 - this.circleRadius;

        this.canvas = new GameView(GameActivity.this);
        setContentView(this.canvas);
        getWindow().getDecorView().setSystemUiVisibility(StartActivity.UI_OPTIONS);

        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                canvas.invalidate();
            }
        };

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                circleX += SPEED * sY;
                circleY += SPEED * sX;

                handler.sendEmptyMessage(0);
            }
        }, 0, 75);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor eventSensor = event.sensor;

        if (eventSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - this.lastSensorUpdateTime >= 25) {
                this.lastSensorUpdateTime = currentTime;

                this.sX = event.values[0];
                this.sY = event.values[1];
                this.sZ = event.values[2];
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class GameView extends View {

        private Paint pen;

        public GameView(Context context) {
            super(context);
            setFocusable(true);

            this.pen = new Paint();
        }

        @Override
        protected void onDraw(Canvas screen) {
            this.pen.setStyle(Paint.Style.FILL);
            this.pen.setAntiAlias(true);
            this.pen.setTextSize(30f);

            screen.drawCircle(circleX, circleY, circleRadius, this.pen);
        }
    }
}