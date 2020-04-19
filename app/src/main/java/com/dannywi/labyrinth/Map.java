package com.dannywi.labyrinth;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class Map implements Ball.OnMoveListener {
    private final int blockSize;
    private int horizontalBlockCount;
    private int verticalBlockCount;

    private final Block[][] blockArray;

    public Map(int width, int height, int blockSize) {
        this.blockSize = blockSize;
        this.horizontalBlockCount = width / blockSize;
        this.verticalBlockCount = height / blockSize;
        this.blockArray = createMap(11);
    }

    private Block[][] createMap(int seed) {
        //Random rand = new Random(seed);
        if (horizontalBlockCount % 2 == 0)
            --horizontalBlockCount;
        if (verticalBlockCount % 2 == 0)
            --verticalBlockCount;
        Block[][] array = new Block[verticalBlockCount][horizontalBlockCount];
        int[][] map = LabyrinthGenerator.getMap(horizontalBlockCount, verticalBlockCount, seed);
        int pad = 3;
        for (int y = 0; y < verticalBlockCount; ++y) {
            for (int x = 0; x < horizontalBlockCount; ++x) {
                //int type = rand.nextInt(4);
                // type = type > 0 ? 1 : 0;
                int type = map[y][x];
                if (type == LabyrinthGenerator.INNER_WALL) type = LabyrinthGenerator.WALL;
                int left = x * blockSize + pad;
                int right = left + blockSize - 2 * pad;
                int top = y * blockSize + pad;
                int bottom = top + blockSize - 2 * pad;
                array[y][x] = new Block(type, left, top, right, bottom);
            }
        }
        return array;
    }

    void draw(Canvas canvas) {
        int yLength = blockArray.length;
        for (int y = 0; y < yLength; ++y) {
            int xLength = blockArray[y].length;
            for (int x = 0; x < xLength; ++x)
                blockArray[y][x].draw(canvas);
        }
    }

    static class Block {
        private static final int TYPE_FLOOR = 0;
        private static final int TYPE_WALL = 1;

        private final int type;
        private final Paint paint;
        final Rect rect;

        private Block(int type, int left, int top, int right, int bottom) {
            this.type = type;
            paint = new Paint();

            switch (type) {
                case TYPE_FLOOR:
                    paint.setColor(Color.GRAY);
                    break;
                case TYPE_WALL:
                    paint.setColor(Color.BLACK);
            }

            rect = new Rect(left, top, right, bottom);
        }

        private void draw(Canvas canvas) {
            // if we assume default background is black, we can skip drawing black rect
            canvas.drawRect(rect, paint);
        }
    }

    private boolean canMove(Rect movedRect) {
        int yLength = blockArray.length;
        for (int y = 0; y < yLength; ++y) {
            int xLength = blockArray[0].length;
            for (int x = 0; x < xLength; ++x) {
                Block block = blockArray[y][x];
                if (block.type == Block.TYPE_WALL && Rect.intersects(block.rect, movedRect))
                    return false;
            }
        }
        return true;
    }

    private final Rect tempBallRect = new Rect();

    @Override
    public int getCanMoveHorizontalDistance(RectF ballRect, int xOffset) {
        int result = xOffset;
        ballRect.round(tempBallRect);
        tempBallRect.offset(xOffset, 0);

        int align = xOffset < 0 ? -1 : 1;

        while (!canMove(tempBallRect)) {
            tempBallRect.offset(-align, 0);
            result -= align;
        }
        return result;
    }

    @Override
    public int getCanMoveVerticalDistance(RectF ballRect, int yOffset) {
        int result = yOffset;
        ballRect.round(tempBallRect);
        tempBallRect.offset(0, yOffset);

        int align = yOffset < 0 ? -1 : 1;

        while (!canMove(tempBallRect)) {
            tempBallRect.offset(0, -align);
            result -= align;
        }
        return result;
    }
}
