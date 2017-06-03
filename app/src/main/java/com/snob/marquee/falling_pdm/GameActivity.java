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
    private final float PLAYER_SCALE = 0.075f;

    private final long SENSOR_RATE = 20;
    private final long TIMER_PERIOD = 60;

    private Point size;
    private Player player;

    private GameView canvas;

    private Timer timer;
    private Handler handler;

    private Sensor accelerometer;
    private SensorManager sensorManager;

    private long lastUpdateTime;
    private float[] accelerations;

    private class Player {
        public float scale;
        public float speed;
        public float[] pos = new float[2];

        public Player(@FloatRange(from = 0) float scale, float x, float y, float speed) {
            this.scale = scale;
            this.speed = speed;

            this.pos[0] = x;
            this.pos[1] = y;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.size = new Point();
        this.accelerations = new float[2];
        this.lastUpdateTime = System.currentTimeMillis();

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        getWindowManager().getDefaultDisplay().getSize(this.size);

        float speed = this.size.x / 1920 * SPEED;
        float startX = this.size.x * (0.5f - PLAYER_SCALE);
        float startY = this.size.y / 2 - PLAYER_SCALE * this.size.x;

        this.player = new Player(PLAYER_SCALE, startX, startY, speed);

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
        }, 0, TIMER_PERIOD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor eventSensor = event.sensor;

        if (eventSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - this.lastUpdateTime >= SENSOR_RATE) {
                this.lastUpdateTime = currentTime;

                float aX = event.values[0];
                float aY = event.values[1];

                this.accelerations[0] = Math.min(Math.abs(aX), 10) * Math.signum(aX);
                this.accelerations[1] = Math.min(Math.abs(aY), 10) * Math.signum(aY);
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
            this.pen.setTextSize(50f);

            screen.drawCircle(player.pos[0], player.pos[1], player.scale * size.x, this.pen);
        }
    }
}