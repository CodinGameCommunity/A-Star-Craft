package com.codingame.astarcraft.view;

import static com.codingame.astarcraft.Constants.*;

import java.util.*;
import java.util.Map.Entry;

import com.codingame.astarcraft.game.*;
import com.codingame.gameengine.module.entities.*;

public class Viewer {
    private GraphicEntityModule graphic;
    private Engine engine;

    private static final int VIEWER_WIDTH = 1900;
    private static final int VIEWER_HEIGHT = 1000;
    private static final int CELL_WIDTH = VIEWER_WIDTH / MAP_WIDTH;
    private static final int CELL_HEIGHT = VIEWER_HEIGHT / MAP_HEIGHT;
    private static final double ROBOT_SIZE = Math.round(CELL_WIDTH * 1.2);
    private static final double ARROW_SIZE = Math.round(CELL_WIDTH * 0.7);
    private static final double PATH_SIZE = Math.round(CELL_WIDTH * 0.35);
    private static final double ROBOT_SCALE = ROBOT_SIZE / 500.0;
    private static final double ARROW_SCALE = ARROW_SIZE / 140.0;
    private static final double PATH_SCALE = PATH_SIZE / 140.0;
    private static final double TILE_SCALE = CELL_WIDTH / 64.0;
    private static final double BORDER_SCALE = CELL_WIDTH / 64.0;
    private static final double CORNER_SCALE = CELL_WIDTH / 64.0;
    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 68;
    private static final int Z_BACKGROUND = 0;
    private static final int Z_FLOOR = 1;
    private static final int Z_BORDER = 2;
    private static final int Z_CORNER = 3;
    private static final int Z_ARROW = 4;
    private static final int Z_GRID = 5;
    private static final int Z_ROBOT = 6;
    private static final int Z_ROBOT_MASK = 7;
    private static final int Z_PORTAL = 8;
    private static final int Z_PATH = 9;
    private static final int GRID_COLOR = 0xFFFFFF;
    private static final double GRID_ALPHA = 0.15;
    private static final double PORTAL_SCALE = CELL_WIDTH / 450.0;
    private static final int ROBOT_ANIMATION_DURATION = 2000;

    private static final int[] ROBOT_COLORS = {0xb4141e, 0x0042ff, 0x1ca7ea, 0x331ac8, 0xebe129, 0xfe8a0e, 0x168000, 0xd0a6fc, 0x1f01c9, 0x525494};
    private static final String[] ROBOT_IMAGES = new String[26];
    
    static {
        for (int i = 1; i <= 26; ++i) {
            ROBOT_IMAGES[i - 1] = "robot" + (i < 10 ? "0" : "") + i;
        }
    }

    private Map<Robot, SpriteAnimation> sprites;
    private Map<Robot, SpriteAnimation> newSprites;
    private Map<Integer, Sprite> robotMasks;
    private Map<Robot, Cell> positions;
    private Set<Cell> startArrows;
    private Text score;
    private Random random = new Random();
    private AStarCraftModule module;

    public Viewer(GraphicEntityModule graphic, Engine engine, AStarCraftModule module) {
        this.module = module;
        this.graphic = graphic;
        this.engine = engine;
        positions = new HashMap<>();
        sprites = new HashMap<>();
        newSprites = new HashMap<>();
        startArrows = new HashSet<>();
        robotMasks = new HashMap<>();

        // Background
        graphic.createSprite().setImage("background.png").setX(0).setY(0).setScale(2.0).setZIndex(Z_BACKGROUND);

        for (int x = 0; x < MAP_WIDTH + 1; ++x) {
            graphic.createLine().setLineWidth(1).setLineColor(GRID_COLOR).setAlpha(GRID_ALPHA).setX(OFFSET_X + CELL_WIDTH * x).setY(OFFSET_Y).setX2(OFFSET_X + CELL_WIDTH * x).setY2(OFFSET_Y + VIEWER_HEIGHT).setZIndex(Z_GRID);
        }

        for (int y = 0; y < MAP_HEIGHT + 1; ++y) {
            graphic.createLine().setLineWidth(1).setLineColor(GRID_COLOR).setAlpha(GRID_ALPHA).setX(OFFSET_X).setY(OFFSET_Y + CELL_HEIGHT * y).setX2(OFFSET_X + VIEWER_WIDTH).setY2(OFFSET_Y + CELL_HEIGHT * y).setZIndex(Z_GRID);
        }

        // Floor, portals and arrows
        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                Cell cell = engine.get(x, y);
                int type = cell.type;

                int cx = x * CELL_WIDTH + OFFSET_X + CELL_WIDTH / 2;
                int cy = y * CELL_HEIGHT + OFFSET_Y + CELL_HEIGHT / 2;

                boolean up = cell.nexts[UP].type != VOID;
                boolean right = cell.nexts[RIGHT].type != VOID;
                boolean down = cell.nexts[DOWN].type != VOID;
                boolean left = cell.nexts[LEFT].type != VOID;
                boolean floor = type != VOID;

                if (floor) {
                    graphic.createSprite().setImage("floor" + random.nextInt(2) + ".png").setScale(TILE_SCALE).setX(cx).setY(cy).setZIndex(Z_FLOOR).setAnchor(0.5);

                    if (type != NONE) {
                        createArrowSprite(cx, cy, type).setScale(ARROW_SCALE).setTint(0x888888);
                        startArrows.add(cell);
                    }

                    if (y == 0 && up) {
                        createPortal().setX(cx).setY(cy - CELL_HEIGHT / 2);
                    }

                    if (x == MAP_WIDTH - 1 && right) {
                        createPortal().setX(cx + CELL_WIDTH / 2).setY(cy).setRotation(Math.PI * 0.5);
                    }

                    if (y == MAP_HEIGHT - 1 && down) {
                        createPortal().setX(cx).setY(cy + CELL_HEIGHT / 2);
                    }

                    if (x == 0 && left) {
                        createPortal().setX(cx - CELL_WIDTH / 2).setY(cy).setRotation(Math.PI * 0.5);
                    }
                } else {
                    if (y != 0 && up) {
                        createBorder(cx, cy).setRotation(Math.PI * 0.5);
                    }

                    if (x != MAP_WIDTH - 1 && right) {
                        createBorder(cx, cy).setRotation(Math.PI * 1.0);
                    }

                    if (y != MAP_HEIGHT - 1 && down) {
                        createBorder(cx, cy).setRotation(Math.PI * 1.5);
                    }

                    if (x != 0 && left) {
                        createBorder(cx, cy);
                    }

                    if (y != 0 && x != MAP_WIDTH - 1 && up && right) {
                        createCorner(cx, cy).setRotation(Math.PI * 0.5);
                    }

                    if (x != MAP_WIDTH - 1 && y != MAP_HEIGHT - 1 && right && down) {
                        createCorner(cx, cy).setRotation(Math.PI * 1.0);
                    }

                    if (y != MAP_HEIGHT - 1 && x != 0 && down && left) {
                        createCorner(cx, cy).setRotation(Math.PI * 1.5);
                    }

                    if (x != 0 && y != 0 && left && up) {
                        createCorner(cx, cy);
                    }

                    if (y != 0 && x != MAP_WIDTH - 1 && !up && !right && engine.get(x + 1, y - 1).type != VOID) {
                        createCorner2(cx, cy).setRotation(Math.PI * 0.5);
                    }

                    if (x != MAP_WIDTH - 1 && y != MAP_HEIGHT - 1 && !right && !down && engine.get(x + 1, y + 1).type != VOID) {
                        createCorner2(cx, cy).setRotation(Math.PI * 1.0);
                    }

                    if (y != MAP_HEIGHT - 1 && x != 0 && !down && !left && engine.get(x - 1, y + 1).type != VOID) {
                        createCorner2(cx, cy).setRotation(Math.PI * 1.5);
                    }

                    if (x != 0 && y != 0 && !left && !up && engine.get(x - 1, y - 1).type != VOID) {
                        createCorner2(cx, cy);
                    }
                }
            }
        }

        // Robots
        for (Robot robot : engine.robots) {
            double rotation = getRobotRotation(robot.direction);
            SpriteAnimation sprite = createRobotSprite(robot.id).setRotation(rotation).setDuration(Integer.MAX_VALUE);
            robotMasks.get(sprite.getId()).setRotation(rotation);

            moveRobotSprite(sprite, robot.cell.x, robot.cell.y);

            sprites.put(robot, sprite);
        }

        // Score indicator
        graphic.createSprite().setImage("score.png").setX(10).setY(20).setScale(0.6);
        score = graphic.createText("0").setX(220).setY(10).setFillColor(0xffffff).setFontSize(50);

        storePositions();
    }

    private Sprite createCorner(int x, int y) {
        return graphic.createSprite().setImage("corner.png").setScale(CORNER_SCALE).setAnchor(0.5).setZIndex(Z_CORNER).setX(x).setY(y);
    }

    private Sprite createCorner2(int x, int y) {
        return graphic.createSprite().setImage("corner2.png").setScale(CORNER_SCALE).setAnchor(0.5).setZIndex(Z_CORNER).setX(x).setY(y);
    }

    private Sprite createBorder(int x, int y) {
        return graphic.createSprite().setImage("border.png").setScale(BORDER_SCALE).setAnchor(0.5).setZIndex(Z_BORDER).setX(x).setY(y);
    }

    private Sprite createPortal() {
        return graphic.createSprite().setImage("portal.png").setScale(PORTAL_SCALE).setZIndex(Z_PORTAL).setAnchor(0.5).setTint(0x00eeff);
    }
    
    private double getRobotRotation(int direction) {
        switch (direction) {
        case UP:
            return Math.PI * 1.0;
        case RIGHT:
            return Math.PI * 1.50;
        case DOWN:
            return 0.0;
        case LEFT:
            return Math.PI * 0.50;
        }

        return 0.0;
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

    private void moveRobotSprite(SpriteAnimation sprite, int x, int y) {
        x = CELL_WIDTH / 2 + x * CELL_WIDTH + OFFSET_X;
        y = CELL_HEIGHT / 2 + y * CELL_HEIGHT + OFFSET_Y;
        sprite.setX(x).setY(y);

        robotMasks.get(sprite.getId()).setX(x).setY(y);
    }

    private SpriteAnimation createRobotSprite(int id) {
        SpriteAnimation sprite = graphic.createSpriteAnimation().setImages(ROBOT_IMAGES).setScale(ROBOT_SCALE).setAnchor(0.5).setDuration(ROBOT_ANIMATION_DURATION).start().setLoop(true).setZIndex(Z_ROBOT);
        
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        module.addTooltip(sprite, params);

        robotMasks.put(sprite.getId(), graphic.createSprite().setImage("robot_mask.png").setScale(ROBOT_SCALE).setTint(ROBOT_COLORS[id]).setAnchor(0.5).setZIndex(Z_ROBOT_MASK));

        module.addOwnership(id, sprite);

        return sprite;
    }

    private Sprite createArrowSprite(int x, int y, int direction) {
        return graphic.createSprite().setImage("arrow.png").setX(x).setY(y).setZIndex(Z_ARROW).setRotation(getRotation(direction)).setAnchor(0.5);
    }

    private void createPath(Robot robot, SpriteAnimation sprite) {
        module.addPath(robot.id, graphic.createSprite().setImage("path.png").setScale(PATH_SCALE).setTint(ROBOT_COLORS[robot.id]).setRotation(getRotation(robot.direction)).setAnchor(0.5).setX(sprite.getX()).setY(sprite.getY()).setZIndex(Z_PATH).setAlpha(1.0).setVisible(true));
    }

    public void updateMap() {
        List<Sprite> arrows = new ArrayList<>();

        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                Cell cell = engine.get(x, y);

                if (!startArrows.contains(cell)) {
                    int type = cell.type;

                    if (type != VOID && type != NONE) {
                        arrows.add(createArrowSprite(x * CELL_WIDTH + OFFSET_X + CELL_WIDTH / 2, y * CELL_HEIGHT + OFFSET_Y + CELL_HEIGHT / 2, type).setScale(0));
                    }
                }
            }
        }

        graphic.commitWorldState(0.0);

        for (Sprite sprite : arrows) {
            sprite.setScale(ARROW_SCALE, Curve.ELASTIC);
        }

        // Update robots rotation
        for (Entry<Robot, SpriteAnimation> entry : sprites.entrySet()) {
            Robot robot = entry.getKey();
            SpriteAnimation sprite = entry.getValue();

            double rotation = getRobotRotation(robot.direction);
            sprite.setRotation(rotation);
            robotMasks.get(sprite.getId()).setRotation(rotation);
        }

        graphic.commitWorldState(1.0);

        for (Entry<Robot, SpriteAnimation> entry : sprites.entrySet()) {
            Robot robot = entry.getKey();
            SpriteAnimation sprite = entry.getValue();

            sprite.setDuration(ROBOT_ANIMATION_DURATION);

            createPath(robot, sprite);
        }
    }

    public void update() {
        newSprites.clear();

        for (Entry<Robot, SpriteAnimation> entry : sprites.entrySet()) {
            Robot robot = entry.getKey();
            SpriteAnimation sprite = entry.getValue();
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

                SpriteAnimation newSprite = createRobotSprite(robot.id).setAlpha(0).setRotation(sprite.getRotation());
                robotMasks.get(newSprite.getId()).setAlpha(0).setRotation(sprite.getRotation());

                moveRobotSprite(newSprite, x, y);

                newSprites.put(robot, newSprite);
            } else {
                newSprites.put(robot, sprite);
            }
        }

        score.setText(String.valueOf(engine.score));

        graphic.commitWorldState(0.0);

        for (Entry<Robot, SpriteAnimation> entry : sprites.entrySet()) {
            Robot robot = entry.getKey();
            SpriteAnimation sprite = entry.getValue();
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
                robotMasks.get(sprite.getId()).setAlpha(0);
                moveRobotSprite(sprite, x, y);

                SpriteAnimation newSprite = newSprites.get(robot).setAlpha(1);
                moveRobotSprite(newSprite, robot.cell.x, robot.cell.y);
                robotMasks.get(newSprite.getId()).setAlpha(1);
            } else {
                moveRobotSprite(sprite, robot.cell.x, robot.cell.y);
            }
        }

        sprites.clear();

        storePositions();

        graphic.commitWorldState(0.75);

        for (Entry<Robot, SpriteAnimation> entry : newSprites.entrySet()) {
            Robot robot = entry.getKey();
            SpriteAnimation sprite = entry.getValue();

            if (!engine.robots.contains(robot)) {
                if (robot.death == DEATH_VOID) {
                    sprite.setScale(0);
                    robotMasks.get(sprite.getId()).setScale(0);
                } else {
                    sprite.setAlpha(0);
                    robotMasks.get(sprite.getId()).setAlpha(0);
                }
            } else {
                sprites.put(robot, sprite);
                sprite.setRotation(getRobotRotation(robot.direction));
                robotMasks.get(sprite.getId()).setRotation(getRobotRotation(robot.direction));
            }
        }

        graphic.commitWorldState(1.0);

        for (Entry<Robot, SpriteAnimation> entry : newSprites.entrySet()) {
            Robot robot = entry.getKey();
            SpriteAnimation sprite = entry.getValue();

            createPath(robot, sprite);
        }
    }

    private void storePositions() {
        for (Robot robot : engine.robots) {
            positions.put(robot, robot.cell);
        }
    }

}
