package com.codingame.astarcraft.game;

import com.codingame.astarcraft.Constants;

public class Cell {

    private static int globalId = 0;

    public int id;
    public int x;
    public int y;
    public Cell[] nexts;
    public int type;

    public Cell(int x, int y) {
        this.id = globalId++;
        this.x = x;
        this.y = y;

        nexts = new Cell[4];
        type = Constants.NONE;
    }

    public int distance(Cell cell) {
        return Math.abs(x - cell.x) + Math.abs(y - cell.y);
    }

}
