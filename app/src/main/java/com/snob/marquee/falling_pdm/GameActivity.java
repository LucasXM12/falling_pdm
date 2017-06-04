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

    //Constantes: __________________________________________________________________________________
    //De jogador:
    private final int DIFFICULT = 5;
    private final float PLYR_SPEED = 20;
    private final float PLAYER_SCALE = 0.03f;

    //De tempo:
    private final long SENSOR_RATE = 20;
    private final long TIMER_PERIOD = 60;

    //De piso:
    private final float SCALE_INC_RATE = 0.03f;
    private final float RECT_MAX_SCALE = 0.80f;

    private final int GRID_RES = 5; //Número de partições em x e y no piso

    private final Random NUMBER_GEN = new Random(System.currentTimeMillis()); //Gerador de números

    //Variáveis: ___________________________________________________________________________________
    private final Point screenSize = new Point(); //Tamanho da tela (largura, altura)

    private Player player; //Objeto do jogador
    private float realRadius; //Raio real do jogador calculado a partir da tela e escala do jogador

    private GameView canvas; //View onde o jogo é desenhado

    private Timer timer; //Temporizador que atualiza os objs do jogo
    private Handler handler; //Trata as mensagens disparadas pelo timer

    private Sensor accelerometer;
    private SensorManager sensorManager;

    private long lastUpdate; //Horário da última atualização do sensor
    private final float[] accels = new float[2]; //Medições feitas pelo sensor (x, y)

    private float rectScale = 0; //Escala atual do piso

    private int maxHoles; //Número máximo de furos em cada piso
    private int numHoles; //Número de furos no piso atual

    private boolean[][] floorMatrix; //Indica a posição de cada furo no piso
    private final ArrayList<Float> holes = new ArrayList<>(); //Coordenadas de cada furo no piso

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.lastUpdate = System.currentTimeMillis();

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.accelerometer,
                SensorManager.SENSOR_DELAY_GAME);

        getWindowManager().getDefaultDisplay().getSize(this.screenSize);

        this.realRadius = PLAYER_SCALE * this.screenSize.x;

        this.floorMatrix = new boolean[GRID_RES][GRID_RES];
        this.maxHoles = (int) Math.round(Math.ceil(GRID_RES * GRID_RES / (double) DIFFICULT));

        raffleMatrix();

        float speed = this.screenSize.x / 1920 * PLYR_SPEED;
        float startX = this.screenSize.x / 2 - this.realRadius;
        float startY = this.screenSize.y / 2 - this.realRadius;

        float[] maxRectPoints = calcRectPoints(RECT_MAX_SCALE);
        maxRectPoints[0] += this.realRadius;
        maxRectPoints[2] -= this.realRadius;

        maxRectPoints[1] += this.realRadius;
        maxRectPoints[3] -= this.realRadius;

        this.player = new Player(startX, startY, speed, maxRectPoints);

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
                player.movX(accels[1]);
                player.movY(accels[0]);

                synchronized (floorMatrix) {
                    rectScale += SCALE_INC_RATE;

                    if (rectScale >= RECT_MAX_SCALE) {
                        rectScale = 0;
                        raffleMatrix();
                    }
                }

                handler.sendEmptyMessage(0);
            }
        }, 0, TIMER_PERIOD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor eventSensor = event.sensor;

        if (eventSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - this.lastUpdate >= SENSOR_RATE) {
                this.lastUpdate = currentTime;

                float aX = event.values[0];
                float aY = event.values[1];

                this.accels[0] = Math.min(Math.abs(aX), SensorManager.STANDARD_GRAVITY) * Math.signum(aX);
                this.accels[1] = Math.min(Math.abs(aY), SensorManager.STANDARD_GRAVITY) * Math.signum(aY);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public float[] calcRectPoints(float scale) {
        float[] ret = new float[4];
        ret[0] = (screenSize.x - screenSize.x * scale) / 2;
        ret[1] = (screenSize.y - screenSize.y * scale) / 2;

        ret[2] = (screenSize.x + screenSize.x * scale) / 2;
        ret[3] = (screenSize.y + screenSize.y * scale) / 2;

        return ret;
    }

    public void raffleMatrix() {
        this.floorMatrix = new boolean[GRID_RES][GRID_RES];

        this.numHoles = 1 + NUMBER_GEN.nextInt(maxHoles);

        for (int c = 0; c < this.numHoles; c++) {
            int i = NUMBER_GEN.nextInt(GRID_RES);
            int j = NUMBER_GEN.nextInt(GRID_RES);

            this.floorMatrix[i][j] = true;
        }
    }

    public void createRects(float x, float y) {
        this.holes.clear();

        float holesW, holesH, a, b;
        int added = 0;

        synchronized (this.floorMatrix) {
            for (int i = 0; i < GRID_RES; i++)
                for (int j = 0; j < GRID_RES; j++)
                    if (this.floorMatrix[i][j]) {
                        holesW = this.screenSize.x * this.rectScale / GRID_RES;
                        holesH = this.screenSize.y * this.rectScale / GRID_RES;

                        a = x + i * holesW;
                        b = y + j * holesH;

                        this.holes.add(a);
                        this.holes.add(b);
                        this.holes.add(a + holesW);
                        this.holes.add(b + holesH);

                        if (this.numHoles == ++added)
                            return;
                    }
        }
    }

    private class GameView extends View {

        private Paint pen;

        public GameView(Context context) {
            super(context);
            setFocusable(true);

            this.pen = new Paint();
            this.pen.setTextSize(50f);
            this.pen.setAntiAlias(true);
            this.pen.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas screen) {
            //screen.drawText(debugStr, screenSize.x / 2, screenSize.y / 2, this.pen);

            this.pen.setARGB(255, 0, 0, 0);
            float[] floor = calcRectPoints(rectScale);
            screen.drawRect(floor[0], floor[1], floor[2], floor[3], this.pen);

            this.pen.setARGB(255, 255, 255, 255);

            createRects(floor[0], floor[1]);
            for (int c = 0; c < holes.size(); c += 4)
                screen.drawRect(holes.get(c), holes.get(c + 1), holes.get(c + 2), holes.get(c + 3), this.pen);

            this.pen.setARGB(255, 0, 0, 255);
            screen.drawCircle(player.pos[0], player.pos[1], realRadius, this.pen);
        }
    }
}