package com.dannywi.labyrinth;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Ball {
    private final Paint paint = new Paint();
    private final Bitmap ballBitmap;
    private final RectF rect;

    public interface OnMoveListener {
        int getCanMoveHorizontalDistance(RectF ballRect, int xOffset);

        int getCanMoveVerticalDistance(RectF ballRect, int yOffset);
    }

    private final OnMoveListener listener;

    public Ball(Bitmap bmp, int left, int top, OnMoveListener onMoveListener) {
        ballBitmap = bmp;
        int right = left + bmp.getWidth();
        int bottom = top + bmp.getHeight();
        rect = new RectF(left, top, right, bottom);
        this.listener = onMoveListener;
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(ballBitmap, rect.left, rect.top, paint);
    }

    void move(float xOffset, float yOffset) {
        xOffset = listener.getCanMoveHorizontalDistance(rect, Math.round(xOffset));
        rect.offset(xOffset, 0);
        yOffset = listener.getCanMoveVerticalDistance(rect, Math.round(yOffset));
        rect.offset(0, yOffset);
    }
}
