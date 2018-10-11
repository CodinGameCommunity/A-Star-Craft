package com.codingame.astarcraft.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.codingame.astarcraft.game.Cell;
import com.codingame.astarcraft.game.Robot;
import com.codingame.astarcraft.game.Robot.State;
import com.codingame.game.Player;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.Entity;
import com.google.inject.Inject;

public class TooltipModule implements Module {

    SoloGameManager<Player> gameManager;
    Map<Integer, Map<String, Object>> registrations;
    Map<Integer, Map<String, Object>> newRegistrations;
    Map<Integer, String[]> extra, newExtra;
    
    Map<Integer, List<Robot.State>> paths;

    @Inject
    public TooltipModule(SoloGameManager<Player> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        registrations = new HashMap<>();
        newRegistrations = new HashMap<>();
        extra = new HashMap<>();
        newExtra = new HashMap<>();
        paths = new HashMap<>();
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
        
    }

    private void sendFrameData() {
        Object[] data = { newRegistrations, newExtra, serializePaths(paths) };
        gameManager.setViewData("tooltips", data);
        newRegistrations.clear();
        newExtra.clear();
        paths.clear();
    }

    private Map<Integer, String> serializePaths(Map<Integer, List<State>> paths) {
        return paths.entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    e -> e.getKey(),
                    e -> {
                        List<State> states = e.getValue();
                        return states
                            .stream()
                            .map(state -> {
                                Cell cell = state.getCell();
                                return cell.x + " " + cell.y + " " + state.getDirection();
                            })
                            .collect(Collectors.joining(" "));
                    }
                )
            );
    }

    public void registerEntity(Entity<?> entity) {
        registerEntity(entity, new HashMap<>());
    }

    public void registerEntity(Entity<?> entity, Map<String, Object> params) {
        int id = entity.getId();
        if (!params.equals(registrations.get(id))) {
            newRegistrations.put(id, params);
            registrations.put(id, params);
        }
    }

    boolean deepEquals(String[] a, String[] b) {
        return Arrays.deepEquals(a, b);
    }

    public void updateExtraTooltipText(Entity<?> entity, String... lines) {
        int id = entity.getId();
        if (!deepEquals(lines, extra.get(id))) {
            newExtra.put(id, lines);
            extra.put(id, lines);
        }
    }

    public void registerRobotPath(int id, List<State> states) {
        paths.put(id, states);
    }
}
