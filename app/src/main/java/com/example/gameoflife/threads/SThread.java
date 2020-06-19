package com.example.gameoflife.threads;


import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import com.example.gameoflife.classes.Field;
import com.example.gameoflife.classes.Point;


public class SThread extends Thread {

    private final int REDRAW_TIME = 20;
    private Field field;
    boolean isFixed = false;
    private final SurfaceHolder mSurfaceHolder;
    boolean isPlaying = true, isMoving = true, isEditing = false;
    private boolean mRunning;
    private long mPrevRedrawTime;

    private Paint mPaint;

    public SThread(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        mRunning = false;

        field = new Field(new Point(500, 500));

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setRunning(boolean running) {
        mRunning = running;
        mPrevRedrawTime = getTime();
    }

    public long getTime() {
        return System.nanoTime() / 1_000_000;
    }

    @Override
    public void run() {
        Canvas canvas;
        int g = 0;
        while (mRunning) {
            long curTime = getTime();
            long elapsedTime = curTime - mPrevRedrawTime;
            if (elapsedTime < REDRAW_TIME)
                continue;
            canvas = null;
            try {
                canvas = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    draw(canvas);
                    g++;
                    if (g >= 20 && isPlaying) {
                        field.move();
                        g = 0;
                    }
                }
            } catch (NullPointerException e) {
            } finally {
                if (canvas != null)
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
            }

            mPrevRedrawTime = curTime;
        }
    }

    public void setShift(Point p) {
        field.shift = p;
    }

    void fixShift() {
        field.fix_shift = new Point(field.shift.getX() + field.fix_shift.getX(), field.shift.getY() + field.fix_shift.getY());
        isFixed = true;
    }

    void setBool(boolean isPlaying, boolean isMoving, boolean isEditing) {
        System.out.println(isPlaying);
        this.isEditing = isEditing;
        this.isPlaying = isPlaying;
        this.isMoving = isMoving;
        if (isEditing)
            field.reZero();
    }

    void setCoff(double coff, Point p) {
        field.coefficient = coff;
        field.p = p;
        field.spw = (int) ((field.width / 2 - p.getX()) * (coff - 1)) / 2;
        field.sph = (int) ((field.height / 2 - p.getY()) * (coff - 1)) / 2;
    }

    void fixCoff() {
        field.fix_coefficient = field.coefficient * field.fix_coefficient;
        field.coefficient = 1;
        field.fix_spw = field.fix_spw + field.spw;
        field.fix_sph = field.fix_sph + field.sph;
        field.spw = 0;
        field.sph = 0;
    }

    void addordel(Point p) {
        field.touch(p);
    }

    private void draw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if (isFixed) {
            field.shift = new Point(0, 0);
            isFixed = false;
        }
        field.draw(canvas);

    }
}
