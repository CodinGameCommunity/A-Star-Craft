package com.codingame.astarcraft.view;

import java.util.HashMap;
import java.util.Map;

import com.codingame.game.Player;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.Entity;
import com.google.inject.Inject;

public class AStarCraftModule implements Module {

    SoloGameManager<Player> gameManager;
    Map<Integer, Map<String, Object>> tooltips;
    Map<Integer, Integer> paths;
    Map<Integer, Integer> ownerships;
    Map<String, Map<?, ?>> data;

    @Inject
    public AStarCraftModule(SoloGameManager<Player> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        tooltips = new HashMap<>();
        paths = new HashMap<>();
        ownerships = new HashMap<>();
        data = new HashMap<>();

        data.put("tooltips", tooltips);
        data.put("paths", paths);
        data.put("ownerships", ownerships);
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
        gameManager.setViewData("astarcraft", data);

        tooltips.clear();
        paths.clear();
        ownerships.clear();
    }

    public void addTooltip(Entity<?> entity, Map<String, Object> params) {
        tooltips.put(entity.getId(), params);
    }

    public void addPath(int id, Entity<?> path) {
        paths.put(id, path.getId());
    }

    public void addOwnership(int id, Entity<?> owner) {
        ownerships.put(id, owner.getId());
    }
}
