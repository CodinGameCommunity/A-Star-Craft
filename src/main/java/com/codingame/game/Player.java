package com.codingame.game;

import com.codingame.gameengine.core.AbstractSoloPlayer;

public class Player extends AbstractSoloPlayer {

    protected int expectedOutputLines = 1;

    public int getExpectedOutputLines() {
        return expectedOutputLines;
    }

    public void sendInputLine(Object line) {
        sendInputLine(line.toString());
    }
}