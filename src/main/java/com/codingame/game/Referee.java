package com.codingame.game;

import static com.codingame.game.Constants.*;

import java.util.List;
import java.util.regex.Pattern;

import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[0-9][0-9]?$");
    private static final Pattern ACTION_PATTERN = Pattern.compile("^[URDL]$");

    @Inject
    private SoloGameManager<Player> manager;
    @Inject
    private GraphicEntityModule module;

    private Engine engine;
    private Viewer viewer;
    private Player player;

    public void init() {
        engine = new Engine(manager.getTestCaseInput().get(0));
        viewer = new Viewer(module, engine);

        player = manager.getPlayer();

        manager.setMaxTurns(2000);
    }

    public void gameTurn(int turn) {
        if (turn == 0) {
            player.sendInputLine(MAP_WIDTH);
            player.sendInputLine(MAP_HEIGHT);
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                StringBuilder sb = new StringBuilder();

                for (int x = 0; x < MAP_WIDTH; ++x) {
                    if (engine.get(x, y).type == VOID) {
                        sb.append("#");
                    } else {
                        sb.append(".");
                    }
                }

                player.sendInputLine(sb);
            }

            player.sendInputLine(engine.robots.size());
            for (Robot robot : engine.robots) {
                String direction = "";
                switch (robot.direction) {
                case UP:
                    direction = "U";
                    break;
                case RIGHT:
                    direction = "R";
                    break;
                case DOWN:
                    direction = "D";
                    break;
                case LEFT:
                    direction = "L";
                    break;
                }

                player.sendInputLine(robot.cell.x + " " + robot.cell.y + " " + direction);
            }

            player.execute();

            try {
                List<String> outputs = player.getOutputs();

                if (outputs.isEmpty()) {
                    manager.addToGameSummary("No output");
                    return;
                }

                String[] output = outputs.get(0).trim().split(" ");

                if (output.length % 3 != 0) {
                    manager.loseGame("Element amount in the action sequence is not a multiple of 3");
                    return;
                }

                for (int i = 0; i < output.length; i += 3) {
                    if (!INTEGER_PATTERN.matcher(output[i]).matches() || !INTEGER_PATTERN.matcher(output[i + 1]).matches() || !ACTION_PATTERN.matcher(output[i + 2]).matches()) {
                        manager.addToGameSummary(output[i] + " " + output[i + 1] + " " + output[i + 2] + " is not a valid action.");
                        continue;
                    }

                    int x = Integer.valueOf(output[i]);
                    int y = Integer.valueOf(output[i + 1]);
                    String action = output[i + 2];
                    int direction;

                    if (x < 0 || x >= MAP_WIDTH) {
                        manager.addToGameSummary(x + " " + y + " are not valid coordinates.");
                        continue;
                    }

                    if (y < 0 || y >= MAP_HEIGHT) {
                        manager.addToGameSummary(x + " " + y + " are not valid coordinates.");
                        return;
                    }

                    if (engine.get(x, y).type == VOID) {
                        manager.addToGameSummary(x + " " + y + " is a wall.");
                        continue;
                    }

                    switch (action) {
                    case "U":
                        direction = UP;
                        break;
                    case "R":
                        direction = RIGHT;
                        break;
                    case "D":
                        direction = DOWN;
                        break;
                    case "L":
                        direction = LEFT;
                        break;
                    default:
                        manager.addToGameSummary(action + " is not a valid action.");
                        continue;
                    }

                    engine.apply(x, y, direction);
                }

                viewer.updateMap();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                manager.loseGame("Referee error");
            }
        } else {
            engine.play();
            viewer.update();

            for (Robot robot : engine.gones) {
                String message = "";
               
                if (robot.death == DEATH_INFINITE_LOOP) {
                    message = "Robot " + robot.id + " is starting an infinite loop.";
                } else if (robot.death == DEATH_VOID) {
                    message = "Robot " + robot.id + " crashed into a wall.";
                }
                
                manager.addTooltip(player, message);
                manager.addToGameSummary(message);
            }

            if (engine.robots.isEmpty()) {
                manager.winGame("Score: " + engine.score);
            }

            forceNewFrame();
        }
    }

    public void forceNewFrame() {
        player.expectedOutputLines = 0;
        manager.setTurnMaxTime(1);
        player.execute();
        try {
            player.getOutputs();
        } catch (Exception e) {

        }
    }

    public void onEnd() {
        manager.putMetadata("Score", String.valueOf(engine.score));
    }
}