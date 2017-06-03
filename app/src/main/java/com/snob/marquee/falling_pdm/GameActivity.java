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

    private final float SPEED = 15;
    private final int PLAYER_RADIUS = 150;

    private Player player;

    private GameView canvas;

    private Timer timer;
    private Handler handler;

    private Sensor accelerometer;
    private SensorManager sensorManager;

    private long lastUpdateTime;
    private float[] accelerations;

    private class Player {
        public int radius;
        public float speed;
        public float[] pos = new float[2];

        public Player(@IntRange(from = 1) int radius, float x, float y, float speed) {
            this.radius = radius;
            this.speed = speed;

            this.pos[0] = x;
            this.pos[1] = y;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.lastUpdateTime = System.currentTimeMillis();
        this.accelerations = new float[2];

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenW = size.x;
        int screenH = size.y;

        this.player = new Player(PLAYER_RADIUS, screenW / 2 - PLAYER_RADIUS, screenH / 2 - PLAYER_RADIUS, SPEED);

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
                player.pos[0] += player.speed * accelerations[1];
                player.pos[1] += player.speed * accelerations[0];

                handler.sendEmptyMessage(0);
            }
        }, 0, 60);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor eventSensor = event.sensor;

        if (eventSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - this.lastUpdateTime >= 20) {
                this.lastUpdateTime = currentTime;

                this.accelerations[0] = event.values[0];
                this.accelerations[1] = event.values[1];
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

            screen.drawCircle(player.pos[0], player.pos[1], player.radius, this.pen);
        }
    }
}