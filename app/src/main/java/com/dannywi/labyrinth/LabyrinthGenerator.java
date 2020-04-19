package com.dannywi.labyrinth;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

public class LabyrinthGenerator {
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int INNER_WALL = -1;

    public enum Direction {TOP, LEFT, RIGHT, BOTTOM}

    public static int[][] getMap(int horizontalBlockCount, int verticalBlockCount, int seed) {
        int[][] result = new int[verticalBlockCount][horizontalBlockCount];
        generateBase(result, horizontalBlockCount, verticalBlockCount);
        generateLabyrinth(result, horizontalBlockCount, verticalBlockCount, seed);
        return result;
    }

    private static void generateBase(int[][] result, int horizontalBlockCount, int verticalBlockCount) {
        BiPredicate<Integer, Integer> isBorder = (a, max_a) -> a == 0 || a >= max_a - 1;
        for (int y = 0; y < verticalBlockCount; ++y) {
            for (int x = 0; x < horizontalBlockCount; ++x) {
                result[y][x] = (isBorder.test(y, verticalBlockCount) || isBorder.test(x, horizontalBlockCount)) ? WALL
                        : (x % 2 == 0 && y % 2 == 0) ? INNER_WALL
                        : FLOOR;
            }
        }
    }

    private static void generateLabyrinth(int[][] result, int horizontalBlockCount, int verticalBlockCount, int seed) {
        Random rand = new Random(seed);
        for (int y = 0; y < verticalBlockCount; ++y) {
            for (int x = 0; x < horizontalBlockCount; ++x) {
                if (result[y][x] == INNER_WALL) {
                    List<Direction> directionList = Arrays.asList(Direction.LEFT, Direction.RIGHT, Direction.BOTTOM);
                    if (y == 1)
                        directionList.add(Direction.TOP);

                    do {
                        Direction direction = directionList.get(rand.nextInt(directionList.size()));
                        if (setDirection(x, y, direction, result))
                            break;
                        else
                            directionList.remove(direction);
                    } while (directionList.size() > 0);
                }
            }
        }
    }

    private static boolean setDirection(int x, int y, Direction direction, int[][] map) {
        map[y][x] = WALL;

        switch (direction) {
            case LEFT:
                x -= 1;
                break;
            case RIGHT:
                x += 1;
                break;
            case TOP:
                y -= 1;
                break;
            case BOTTOM:
                y += 1;
                break;
        }

        if (x < 0 || y < 0 || x >= map[0].length || y >= map.length)
            return false;

        if (map[y][x] == WALL)
            return false;

        map[y][x] = WALL;
        return true;
    }
}
