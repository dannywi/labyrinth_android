package com.dannywi.labyrinth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class LabyrinthView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int DRAW_INTERVAL = 1000 / 60;
    private static final float TEXT_SIZE = 40f;
    private static final float ALPHA = 0.9f;
    private static final float ACCEL_WEIGHT = 16f;

    private final Paint paint = new Paint();
    private final Paint textPaint = new Paint();

    private final Bitmap ballBitmap;
    //    private float ballX = 50f;
    //    private float ballY = 50f;
    private Ball ball;
    private float canvasWidth = 500f;
    private float canvasHeight = 500f;

    private Map map;

    public LabyrinthView(Context context) {
        super(context);

        ballBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        textPaint.setColor(Color.CYAN);
        textPaint.setTextSize(TEXT_SIZE);

        getHolder().addCallback(this);
    }

    private DrawThread drawThread;

    private class DrawThread extends Thread {
        private final AtomicBoolean isFinished = new AtomicBoolean();

        public void finish() {
            isFinished.set(true);
        }

        @Override
        public void run() {
            SurfaceHolder holder = getHolder();
            while (!isFinished.get()) {
                if (holder.isCreating())
                    continue;

                Canvas canvas = holder.lockCanvas();
                if (canvas == null)
                    continue;
                drawLabyrinth(canvas);
                holder.unlockCanvasAndPost(canvas);

                synchronized (this) {
                    try {
                        wait(DRAW_INTERVAL);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public void startDrawThread() {
        stopDrawThread();
        drawThread = new DrawThread();
        drawThread.start();
    }

    public boolean stopDrawThread() {
        if (drawThread == null)
            return false;
        drawThread.finish();
        drawThread = null;
        return true;
    }

    private float[] sensorValuesRaw;
    private float[] sensorValues;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            sensorValuesRaw = event.values;

            if (sensorValues == null) {
                sensorValues = new float[3];
                for (Jiku i : Jiku.values)
                    sensorValues[i.ordinal()] = event.values[i.ordinal()];
            }

            for (Jiku i : Jiku.values)
                sensorValues[i.ordinal()] =
                        sensorValues[i.ordinal()] * ALPHA + event.values[i.ordinal()] * (1f - ALPHA);

            if (ball != null) {
                float ballX = -sensorValues[Jiku.X.ordinal()] * ACCEL_WEIGHT;
                float ballY = sensorValues[Jiku.Y.ordinal()] * ACCEL_WEIGHT;

                //ballX = Math.max(0, Math.min(ballX, canvasWidth - ballBitmap.getWidth()));
                //ballY = Math.max(0, Math.min(ballY, canvasHeight - ballBitmap.getHeight()));

                ball.move(ballX, ballY);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public void startSensor() {
        sensorValuesRaw = null;

        SensorManager sm = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopSensor() {
        SensorManager sm = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(sensorEventListener);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startDrawThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();
    }

    private enum Jiku {
        X, Y, Z;
        public static final Jiku values[] = values();
    }

    public void drawLabyrinth(final Canvas canvas) {
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        int blockSize = ballBitmap.getHeight();

        canvas.drawColor(Color.BLACK);

        if (map == null)
            map = new Map((int) canvasWidth, (int) canvasHeight, blockSize);
        if (ball == null)
            ball = new Ball(ballBitmap, blockSize, blockSize, map);

        map.draw(canvas);
        //canvas.drawBitmap(ballBitmap, ballX, ballY, paint);
        ball.draw(canvas);

        final float lineHeight = TEXT_SIZE + 10f;
        Float firstLinePos = 150f;
        BiConsumer<float[], Float> printPosFn = (float[] sensorValues, Float currentLinePos) -> {
            for (Jiku i : Jiku.values) {
                canvas.drawText(
                        String.format("sensor[%d] (%s) = %f",
                                i.ordinal(), Jiku.values[i.ordinal()], sensorValues[i.ordinal()]),
                        10, currentLinePos, textPaint);
                currentLinePos += lineHeight;
            }
        };

        if (sensorValuesRaw != null) {
            canvas.drawText(" -- raw --", 10, firstLinePos, textPaint);
            printPosFn.accept(sensorValuesRaw, firstLinePos + lineHeight);
        }

        if (sensorValues != null) {
            firstLinePos += 5 * lineHeight;
            canvas.drawText(String.format(" -- alpha (%.2f) --", ALPHA), 10, firstLinePos, textPaint);
            printPosFn.accept(sensorValues, firstLinePos + lineHeight);
        }
    }
}
