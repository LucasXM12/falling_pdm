package com.snob.marquee.falling_pdm;

import android.support.annotation.*;

public class Player {

    private final float[] offSetX = new float[2]; //Xs limitadores no movimento do jogador
    private final float[] offSetY = new float[2]; //Ys limitadores no movimento do jogador

    public float speed;
    public final float[] pos = new float[2];

    public Player(@FloatRange(from = 0) float x, @FloatRange(from = 0) float y, float speed,
                  @Size(4) float[] maxRectPoints) {
        this.speed = speed;

        this.pos[0] = x;
        this.pos[1] = y;

        this.offSetX[0] = maxRectPoints[0];
        this.offSetX[1] = maxRectPoints[2];

        this.offSetY[0] = maxRectPoints[1];
        this.offSetY[1] = maxRectPoints[3];
    }

    //Move o jogador no eixo x tomando em conta o retângulo limitante (colisão com a parede):
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

    //Move o jogador no eixo y tomando em conta o retângulo limitante (colisão com a parede):
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
