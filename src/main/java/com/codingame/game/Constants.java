package com.codingame.game;

public final class Constants {

    private Constants() {
    }

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
}
