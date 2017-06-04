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
    private final float SPEED = 20;
    private final float PLAYER_SCALE = 0.025f;

    //De tempo:
    private final long SENSOR_RATE = 20;
    private final long TIMER_PERIOD = 60;

    //De piso:
    private final float SCALE_INC_RATE = 0.03f;
    private final float RECT_MAX_SCALE = 0.80f;

    //Variáveis: ___________________________________________________________________________________
    private Point screenSize; //Tamanho da tela (largura, altura)

    private Player player; //Objeto do jogador
    private float realRadius; //Raio real do jogador calculado a partir da tela e escala do jogador

    private GameView canvas; //View onde o jogo é desenhado

    private Timer timer; //Temporizador que atualiza os objs do jogo
    private Handler handler; //Trata as mensagens disparadas pelo timer

    private Sensor accelerometer;
    private SensorManager sensorManager;

    private float[] accels; //Medições feitas pelo sensor (x, y)
    private long lastUpdate; //Horário da última atualização do sensor

    private Random numberGen; //Gerador de números aleatórios

    private float[] maxRectPoints; //Pontos do piso com escala máxima

    private int gridRes; //Número de partições em x e y no piso

    private float[] offSetX; //Xs limitadores no movimento do jogador
    private float[] offSetY; //Ys limitadores no movimento do jogador

    private float rectScale; //Escala atual do piso

    private int maxHoles; //Número máximo de furos em cada piso
    private int numHoles; //Número de furos no piso atual

    private ArrayList<Float> holes; //Coordenadas de cada furo no piso
    private boolean[][] floorMatrix; //Indica a posição de cada furo no piso

    private class Player {
        public float speed;
        public final float[] pos = new float[2];

        public Player(float x, float y, float speed) {
            this.speed = speed;

            this.pos[0] = x;
            this.pos[1] = y;
        }

        public void movX(float accelX) {
            float deltaX = this.speed * accelX;
            float futureX = this.pos[0] + deltaX;

            if (futureX >= offSetX[0] && futureX <= offSetX[1])
                this.pos[0] += deltaX;
            else if (futureX < offSetX[0])
                this.pos[0] = offSetX[0];
            else
                this.pos[0] = offSetX[1];
        }

        public void movY(float accelY) {
            float deltaY = this.speed * accelY;
            float futureY = this.pos[1] + deltaY;

            if (futureY >= offSetY[0] && futureY <= offSetY[1])
                this.pos[1] += deltaY;
            else if (futureY < offSetY[0])
                this.pos[1] = offSetY[0];
            else
                this.pos[1] = offSetY[1];
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.rectScale = 0;
        this.screenSize = new Point();
        this.offSetX = new float[2];
        this.offSetY = new float[2];
        this.numberGen = new Random();
        this.holes = new ArrayList<>();
        this.accels = new float[2];
        this.lastUpdate = System.currentTimeMillis();

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        getWindowManager().getDefaultDisplay().getSize(this.screenSize);

        this.maxRectPoints = calcRectPoints(RECT_MAX_SCALE);

        this.realRadius = this.screenSize.x * PLAYER_SCALE;

        this.gridRes = Math.round(RECT_MAX_SCALE * this.screenSize.y / 3 / this.realRadius);
        this.floorMatrix = new boolean[this.gridRes][this.gridRes];
        this.maxHoles = this.gridRes * this.gridRes / 3;

        raffleMatrix();

        this.offSetX[0] = this.maxRectPoints[0] + this.realRadius;
        this.offSetX[1] = this.maxRectPoints[2] - this.realRadius;

        this.offSetY[0] = this.maxRectPoints[1] + this.realRadius;
        this.offSetY[1] = this.maxRectPoints[3] - this.realRadius;

        float speed = this.screenSize.x / 1920 * SPEED;
        float startX = this.screenSize.x / 2 - this.realRadius;
        float startY = this.screenSize.y / 2 - this.realRadius;

        this.player = new Player(startX, startY, speed);

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

                this.accels[0] = Math.min(Math.abs(aX), 10) * Math.signum(aX);
                this.accels[1] = Math.min(Math.abs(aY), 10) * Math.signum(aY);
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
        this.floorMatrix = new boolean[this.gridRes][this.gridRes];

        this.numHoles = 1 + this.numberGen.nextInt(maxHoles);

        for (int c = 0; c < this.numHoles; c++) {
            int i = this.numberGen.nextInt(this.gridRes);
            int j = this.numberGen.nextInt(this.gridRes);

            this.floorMatrix[i][j] = true;
        }
    }

    public void createRects(float x, float y) {
        this.holes.clear();

        float holesW, holesH, a, b;
        int added = 0;

        synchronized (this.floorMatrix) {
            for (int i = 0; i < this.gridRes; i++)
                for (int j = 0; j < this.gridRes; j++)
                    if (this.floorMatrix[i][j]) {
                        holesW = this.screenSize.x * this.rectScale / this.gridRes;
                        holesH = this.screenSize.y * this.rectScale / this.gridRes;

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