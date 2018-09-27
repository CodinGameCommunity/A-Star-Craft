package com.codingame.astarcraft.view;

import static com.codingame.astarcraft.Constants.*;

import java.util.*;
import java.util.Map.Entry;

import com.codingame.astarcraft.game.*;
import com.codingame.gameengine.module.entities.*;

public class Viewer {
    private GraphicEntityModule module;
    private Engine engine;

    private static final int VIEWER_WIDTH = 1900;
    private static final int VIEWER_HEIGHT = 1000;
    private static final int CELL_WIDTH = VIEWER_WIDTH / MAP_WIDTH;
    private static final int CELL_HEIGHT = VIEWER_HEIGHT / MAP_HEIGHT;
    private static final double ROBOT_SIZE = Math.round(CELL_WIDTH * 0.75);
    private static final double ROBOT_SPRITE_SIZE = 154.0;
    private static final double ROBOT_SCALE = ROBOT_SIZE / ROBOT_SPRITE_SIZE;
    private static final double ARROW_SIZE = Math.round(CELL_WIDTH * 0.70);
    private static final double ARROW_SPRITE_SIZE = 140.0;
    private static final double ARROW_SCALE = ARROW_SIZE / ARROW_SPRITE_SIZE;
    private static final double TILE_SCALE = CELL_WIDTH / 64.0;
    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 68;
    private static final int Z_BACKGROUND = 0;
    private static final int Z_FLOOR = 1;
    private static final int Z_ARROW = 2;
    private static final int Z_GRID = 3;
    private static final int Z_ROBOT = 4;
    private static final int Z_PORTAL = 5;
    private static final int GRID_COLOR = 0xFFFFFF;
    private static final double GRID_ALPHA = 0.15;
    private static final double PORTAL_SCALE = CELL_WIDTH / 70.0;

    private Map<Robot, Sprite> sprites;
    private Map<Robot, Sprite> newSprites = new HashMap<>();
    private Map<Robot, Cell> positions;
    private Set<Cell> startArrows;
    private Text score;
    private Random random = new Random();

    public Viewer(GraphicEntityModule module, Engine engine) {
        this.module = module;
        this.engine = engine;
        positions = new HashMap<>();
        sprites = new HashMap<>();
        newSprites = new HashMap<>();
        startArrows = new HashSet<>();

        // Background
        module.createSprite().setImage("background.png").setX(0).setY(0).setScale(2.0).setZIndex(Z_BACKGROUND);

        for (int x = 0; x < MAP_WIDTH + 1; ++x) {
            module.createLine().setLineWidth(1).setLineColor(GRID_COLOR).setAlpha(GRID_ALPHA).setX(OFFSET_X + CELL_WIDTH * x).setY(OFFSET_Y).setX2(OFFSET_X + CELL_WIDTH * x).setY2(OFFSET_Y + VIEWER_HEIGHT).setZIndex(Z_GRID);
        }

        for (int y = 0; y < MAP_HEIGHT + 1; ++y) {
            module.createLine().setLineWidth(1).setLineColor(GRID_COLOR).setAlpha(GRID_ALPHA).setX(OFFSET_X).setY(OFFSET_Y + CELL_HEIGHT * y).setX2(OFFSET_X + VIEWER_WIDTH).setY2(OFFSET_Y + CELL_HEIGHT * y).setZIndex(Z_GRID);
        }

        // Floor, portals and arrows
        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                Cell cell = engine.get(x, y);
                int type = cell.type;

                if (type != VOID) {
                    module.createSprite().setImage("floor" + random.nextInt(2) + ".png").setScale(TILE_SCALE).setX(x * CELL_WIDTH + OFFSET_X).setY(y * CELL_HEIGHT + OFFSET_Y).setZIndex(Z_FLOOR);

                    if (x == 0 && engine.get(x - 1, y).type != VOID) {
                        createPortal().setX(x * CELL_WIDTH + OFFSET_X).setY(y * CELL_HEIGHT + OFFSET_Y + (CELL_HEIGHT / 2)).setRotation(Math.PI * 0.5);
                    }

                    if (x == MAP_WIDTH - 1 && engine.get(x + 1, y).type != VOID) {
                        createPortal().setX(x * CELL_WIDTH + OFFSET_X + CELL_WIDTH).setY(y * CELL_HEIGHT + OFFSET_Y + (CELL_HEIGHT / 2)).setRotation(Math.PI * 0.5);
                    }

                    if (y == 0 && engine.get(x, y - 1).type != VOID) {
                        createPortal().setX(x * CELL_WIDTH + OFFSET_X + (CELL_WIDTH / 2)).setY(y * CELL_HEIGHT + OFFSET_Y);
                    }

                    if (y == MAP_HEIGHT - 1 && engine.get(x, y + 1).type != VOID) {
                        createPortal().setX(x * CELL_WIDTH + OFFSET_X + (CELL_WIDTH / 2)).setY(y * CELL_HEIGHT + OFFSET_Y + CELL_HEIGHT);
                    }
                    
                    if (type != NONE) {
                        createArrowSprite(x, y, type).setScale(ARROW_SCALE).setTint(0x888888);
                        startArrows.add(cell);
                    }
                }
            }
        }

        // Robots
        for (Robot robot : engine.robots) {
            Sprite sprite = createRobotSprite().setRotation(getRotation(robot.direction));

            moveRobotSprite(sprite, robot.cell.x, robot.cell.y);

            sprites.put(robot, sprite);
        }

        // Score indicator
        module.createText("Score").setX(10).setY(20).setFillColor(0xffffff);
        score = module.createText("0").setX(100).setY(20).setFillColor(0xffffff);

        storePositions();
    }

    private Sprite createPortal() {
        return module.createSprite().setImage("portal.png").setScale(PORTAL_SCALE).setZIndex(Z_PORTAL).setAnchor(0.5);
    }

    private double getRotation(int direction) {
        switch (direction) {
        case UP:
            return Math.PI * 1.50;
        case RIGHT:
            return 0.0;
        case DOWN:
            return Math.PI * 0.50;
        case LEFT:
            return Math.PI * 1.00;
        }

        return 0.0;
    }

    private void moveRobotSprite(Sprite sprite, int x, int y) {
        sprite.setX(CELL_WIDTH / 2 + x * CELL_WIDTH + OFFSET_X).setY(CELL_HEIGHT / 2 + y * CELL_HEIGHT + OFFSET_Y).setZIndex(Z_ROBOT);
    }

    private Sprite createRobotSprite() {
        return module.createSprite().setImage("robot.png").setScale(ROBOT_SCALE).setAnchor(0.5);
    }

    private Sprite createArrowSprite(int x, int y, int direction) {
        return module.createSprite().setImage("arrow.png").setX(CELL_WIDTH / 2 + x * CELL_WIDTH + OFFSET_X).setY(CELL_HEIGHT / 2 + y * CELL_HEIGHT + OFFSET_Y).setZIndex(Z_ARROW).setRotation(getRotation(direction)).setAnchor(0.5);
    }

    public void updateMap() {
        List<Sprite> arrows = new ArrayList<>();

        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                Cell cell = engine.get(x, y);
                
                if (!startArrows.contains(cell)) {
                    int type = cell.type;
    
                    if (type != VOID && type != NONE) {
                        arrows.add(createArrowSprite(x, y, type).setScale(0));
                    }
                }
            }
        }

        module.commitWorldState(0.0);

        for (Sprite sprite : arrows) {
            sprite.setScale(ARROW_SCALE, Curve.ELASTIC);
        }
        
        // Update robots rotation
        for (Entry<Robot, Sprite> entries : sprites.entrySet()) {
            Robot robot = entries.getKey();
            Sprite sprite = entries.getValue();
            
            sprite.setRotation(getRotation(robot.direction));
        }

        module.commitWorldState(1.0);
    }

    public void update() {
        newSprites.clear();

        for (Entry<Robot, Sprite> entries : sprites.entrySet()) {
            Robot robot = entries.getKey();
            Sprite sprite = entries.getValue();
            Cell position = positions.get(robot);

            if (position.distance(robot.cell) > 1) {
                int x = robot.cell.x;
                int y = robot.cell.y;

                if (x != position.x) {
                    x = x == 0 ? -1 : MAP_WIDTH;
                }

                if (y != position.y) {
                    y = y == 0 ? -1 : MAP_HEIGHT;
                }

                Sprite newSprite = createRobotSprite().setAlpha(0).setRotation(sprite.getRotation()).setTint(sprite.getTint());

                moveRobotSprite(newSprite, x, y);

                newSprites.put(robot, newSprite);
            } else {
                newSprites.put(robot, sprite);
            }
        }

        score.setText(String.valueOf(engine.score));

        module.commitWorldState(0.0);

        for (Entry<Robot, Sprite> entries : sprites.entrySet()) {
            Robot robot = entries.getKey();
            Sprite sprite = entries.getValue();
            Cell position = positions.get(robot);

            if (position.distance(robot.cell) > 1) {
                int x = position.x;
                int y = position.y;

                if (x != robot.cell.x) {
                    x = x == 0 ? -1 : MAP_WIDTH;
                }

                if (y != robot.cell.y) {
                    y = y == 0 ? -1 : MAP_HEIGHT;
                }

                sprite.setAlpha(0);
                moveRobotSprite(sprite, x, y);

                Sprite newSprite = newSprites.get(robot).setAlpha(1);
                moveRobotSprite(newSprite, robot.cell.x, robot.cell.y);
            } else {
                moveRobotSprite(sprite, robot.cell.x, robot.cell.y);
            }
        }

        sprites.clear();

        storePositions();

        module.commitWorldState(0.75);

        for (Entry<Robot, Sprite> entries : newSprites.entrySet()) {
            Robot robot = entries.getKey();
            Sprite sprite = entries.getValue();

            if (!engine.robots.contains(robot)) {
                if (robot.death == DEATH_VOID) {
                    sprite.setScale(0);
                } else {
                    sprite.setAlpha(0);
                }
            } else {
                sprites.put(robot, sprite);
                sprite.setRotation(getRotation(robot.direction));
            }
        }

        module.commitWorldState(1.0);
    }

    private void storePositions() {
        for (Robot robot : engine.robots) {
            positions.put(robot, robot.cell);
        }
    }

}
