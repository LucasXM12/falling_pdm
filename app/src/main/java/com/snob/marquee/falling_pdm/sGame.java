package com.snob.marquee.falling_pdm;

import android.util.*;
import android.view.*;
import android.content.*;
import android.graphics.*;

public class sGame extends View {

    public static final float RADIUS = 3;

    private Context context;
    private Paint paint = new Paint();
    private final float[] player = new float[3];
    private final float[] angles = new float[3];

    public sGame(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.player[0] = context.getResources().getDisplayMetrics().widthPixels / 2;
        this.player[1] = context.getResources().getDisplayMetrics().heightPixels / 2;
        this.player[2] = RADIUS;

        this.paint.setStrokeWidth(6);
        this.paint.setAntiAlias(true);
        this.paint.setColor(Color.BLACK);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setStrokeJoin(Paint.Join.ROUND);

        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(this.player[0], this.player[1], this.player[2], this.paint);
    }

    public void setAngles(float[] angles) {
        this.angles = angles;
    }
}
