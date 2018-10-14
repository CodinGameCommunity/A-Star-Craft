package com.codingame.astarcraft.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.codingame.game.Player;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.Entity;
import com.google.inject.Inject;

public class AStarCraftModule implements Module {

    SoloGameManager<Player> gameManager;
    Map<Integer, Map<String, Object>> registrations;

    @Inject
    public AStarCraftModule(SoloGameManager<Player> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        registrations = new HashMap<>();
    }

    @Override
    public void onGameInit() {
        sendFrameData();
    }

    @Override
    public void onAfterGameTurn() {
        sendFrameData();
    }

    @Override
    public void onAfterOnEnd() {
        sendFrameData();
    }

    private void sendFrameData() {
        Object[] data = { registrations };
        gameManager.setViewData("tooltips", data);
        registrations.clear();
    }

    public void registerEntity(Entity<?> entity, Map<String, Object> params) {
        registrations.put(entity.getId(), params);
    }
}
