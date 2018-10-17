package com.codingame.astarcraft;

public final class Constants {

    static public final int MAP_WIDTH = 19;
    static public final int MAP_HEIGHT = 10;
    static public final int MAP_AREA = MAP_WIDTH * MAP_HEIGHT;
    static public final int UP = 0;
    static public final int RIGHT = 1;
    static public final int DOWN = 2;
    static public final int LEFT = 3;
    static public final int NONE = 4;
    static public final int VOID = 5;
    static public final int DEATH_INFINITE_LOOP = 0;
    static public final int DEATH_VOID = 1;

    private Constants() {
    }

    static public char typeToChar(int type) {
        if (type == UP) {
            return 'U';
        } else if (type == RIGHT) {
            return 'R';
        } else if (type == DOWN) {
            return 'D';
        } else if (type == LEFT) {
            return 'L';
        } else if (type == NONE) {
            return '.';
        } else if (type == VOID) {
            return '#';
        }

        throw new IllegalArgumentException(type + " is not a valid direction for dirToChar");
    }

    static public char charToType(char c) {
        c = Character.toLowerCase(c);

        if (c == 'u') {
            return UP;
        } else if (c == 'r') {
            return RIGHT;
        } else if (c == 'd') {
            return DOWN;
        } else if (c == 'l') {
            return LEFT;
        } else if (c == '.') {
            return NONE;
        } else if (c == '#') {
            return VOID;
        }

        throw new IllegalArgumentException(c + " is not a valid character for charToDir");
    }
}
