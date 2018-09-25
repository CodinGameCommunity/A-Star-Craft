package com.codingame.game;

public class Cell {

    private static int globalId = 0;

    protected int id;
    protected int x;
    protected int y;
    protected Cell[] nexts;
    protected int type;

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
