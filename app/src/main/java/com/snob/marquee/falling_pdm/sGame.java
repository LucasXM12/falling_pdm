package com.snob.marquee.falling_pdm;

import android.util.*;
import android.view.*;
import android.content.*;
import android.graphics.*;
import android.support.annotation.*;
import android.support.annotation.Size;

public class sGame extends View {

    public final float MU = 0.1f;
    public final float RADIUS = 3;
    public final float WEIGHT = 9.8f;
    public final float TSCALE = 0.1f;

    private Paint paint = new Paint();

    private Thread updateAll = new Thread(new Runnable() {

        @Override
        public void run() {
            updateForces();
            updateSpeeds();
            updatePos();
        }
    });

    private final float[] player = new float[3];

    private final float[] angles = new float[3];
    private final float[] speeds = new float[2];
    private final float[] forces = new float[2];

    public sGame(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);

        this.player[0] = context.getResources().getDisplayMetrics().widthPixels / 2;
        this.player[1] = context.getResources().getDisplayMetrics().heightPixels / 2;
        this.player[2] = RADIUS;

        this.angles[0] = 0;
        this.angles[1] = 0;

        this.speeds[0] = 0;
        this.speeds[1] = 0;

        this.forces[0] = 0;
        this.forces[1] = 0;

        this.paint.setStrokeWidth(6);
        this.paint.setAntiAlias(true);
        this.paint.setColor(Color.BLACK);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setStrokeJoin(Paint.Join.ROUND);

        this.updateAll.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawCircle(this.player[0], this.player[1], this.player[2], this.paint);
        canvas.drawText(toString(), 100, 100, this.paint);
    }

    public void setAngles(@Size(3) float[] angles) {
        this.angles[0] = angles[0];
        this.angles[1] = angles[1];
    }

    private void updateForces() {
        this.forces[1] = (float) (WEIGHT * Math.cos(angles[1]));
        this.forces[0] = (float) (WEIGHT * Math.sin(angles[2]) - MU * this.forces[1]);
    }

    private void updateSpeeds() {
        this.speeds[0] += forces[0] * TSCALE;
        this.speeds[1] += forces[1] * TSCALE;
    }

    private void updatePos() {
        this.player[0] += speeds[0] * TSCALE;
        this.player[1] += speeds[1] * TSCALE;
    }

    @Override
    public String toString() {
        String ret = String.format("(%1$.3f, %2$.3f)\n(%3$.3f, %4$.3f, %5$.3f)\n(%6$.3f, %7$.3f)\n(%8$.3f, %9$.3f)", this.player[0], this.player[1],
                this.angles[0], this.angles[1], this.angles[2], this.speeds[0], this.speeds[1], this.forces[0], this.forces[1]);

        return ret;
    }
}
