package com.codingame.game;

import com.codingame.astarcraft.game.Engine;
import com.codingame.astarcraft.game.Robot;
import com.codingame.astarcraft.view.TooltipModule;
import com.codingame.astarcraft.view.Viewer;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codingame.astarcraft.Constants.*;

public class Referee extends AbstractReferee {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[0-9][0-9]?$");
    private static final Pattern ACTION_PATTERN = Pattern.compile("^[URDL]$");
    private static final Pattern INPUT_PATTERN = Pattern.compile("^[.#URDLurdl]{190}$");
    private static final Pattern INPUT_ROBOTS_PATTERN = Pattern.compile("[URDL]");

    @Inject
    private SoloGameManager<Player> manager;
    @Inject
    private GraphicEntityModule graphic;
    @Inject
    private TooltipModule tooltip;

    private Engine engine;
    private Viewer viewer;
    private Player player;

    public void init() {
        String input;

        try {
            input = manager.getTestCaseInput().get(0);
        } catch (Exception exception) {
            manager.loseGame("Bad referee input: Can't read input");
            return;
        }
        
        input = input.trim();

        if (!INPUT_PATTERN.matcher(input).matches()) {
            manager.loseGame("Bad referee input: Input doesn't match the pattern");
            return;
        }

        int count = 0;
        Matcher matcher = INPUT_ROBOTS_PATTERN.matcher(input);
        while (matcher.find()) {
            count += 1;
        }

        if (count < 1 || count > 10) {
            manager.loseGame("Bad referee input: " + count + " is not an acceptable robot count");
            return;
        }

        engine = new Engine(input);
        viewer = new Viewer(graphic, engine, tooltip);

        player = manager.getPlayer();

        manager.setMaxTurns(2000);
    }

    @SuppressWarnings("unchecked")
    public void gameTurn(int turn) {
        if (turn == 0) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                StringBuilder sb = new StringBuilder();

                for (int x = 0; x < MAP_WIDTH; ++x) {
                    sb.append(typeToChar(engine.get(x, y).type));
                }

                player.sendInputLine(sb);
            }

            player.sendInputLine(engine.robots.size());
            for (Robot robot : engine.robots) {
                player.sendInputLine(robot.cell.x + " " + robot.cell.y + " " + typeToChar(robot.direction));
            }

            player.execute();

            try {
                List<String> outputs = player.getOutputs();

                if (outputs == null) {
                    outputs = Collections.EMPTY_LIST;
                }

                if (outputs.isEmpty()) {
                    manager.addToGameSummary("No output");
                } else {
                    String[] output = outputs.get(0).trim().split(" ");

                    for (int i = 0; i < output.length; i += 3) {
                        if (output.length <= i + 2) {
                            manager.addToGameSummary("Element amount in the action sequence is not a multiple of 3");
                            continue;
                        }

                        if (!INTEGER_PATTERN.matcher(output[i]).matches() || !INTEGER_PATTERN.matcher(output[i + 1]).matches() || !ACTION_PATTERN.matcher(output[i + 2]).matches()) {
                            manager.addToGameSummary(output[i] + " " + output[i + 1] + " " + output[i + 2] + " is not a valid action.");
                            continue;
                        }

                        int x = Integer.valueOf(output[i]);
                        int y = Integer.valueOf(output[i + 1]);
                        String action = output[i + 2];

                        if (x < 0 || x >= MAP_WIDTH) {
                            manager.addToGameSummary(x + " " + y + " are not valid coordinates.");
                            continue;
                        }

                        if (y < 0 || y >= MAP_HEIGHT) {
                            manager.addToGameSummary(x + " " + y + " are not valid coordinates.");
                            continue;
                        }

                        if (engine.get(x, y).type == VOID) {
                            manager.addToGameSummary(x + " " + y + " is a void cell.");
                            continue;
                        }

                        if (engine.get(x, y).type != NONE) {
                            manager.addToGameSummary(x + " " + y + " already contains an arrow");
                            continue;
                        }

                        engine.apply(x, y, charToType(action.charAt(0)));
                    }
                }

                viewer.updateMap();
            } catch (TimeoutException e) {
                manager.loseGame("You failed to provide instructions in the provided time.");
                return;
            } catch (Exception e) {
                e.printStackTrace(System.err);
                manager.loseGame("Referee error " + e.getClass().getCanonicalName() + " : " + e.getMessage());

                return;
            }

            engine.registerStates();
        } else {
            engine.play();
            viewer.update();

            for (Robot robot : engine.gones) {
                String message = "";

                if (robot.death == DEATH_INFINITE_LOOP) {
                    message = "Automaton2000 (id=" + robot.id + ") is starting an infinite loop.";
                } else if (robot.death == DEATH_VOID) {
                    message = "Automaton2000 (id=" + robot.id + ") drifted off into deep space.";
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
        if (engine != null) {
            manager.putMetadata("Points", String.valueOf(engine.score));
        } else {
            manager.putMetadata("Points", "0");
        }
    }
}
