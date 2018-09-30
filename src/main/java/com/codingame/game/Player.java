package com.codingame.game;

import com.codingame.gameengine.core.AbstractSoloPlayer;

public class Player extends AbstractSoloPlayer {
    public int getExpectedOutputLines() {
        return 1;
    }

    public void sendInputLine(Object line) {
        sendInputLine(line.toString());
    }
}