package com.codingame.game;

import static com.codingame.game.Constants.*;

import java.util.*;

public class Engine {

    protected int score;
    protected Cell[][] grid;
    protected List<Robot> robots;
    protected Set<Robot> gones;

    public Engine(String input) {
        score = 0;
        grid = new Cell[MAP_WIDTH][MAP_HEIGHT];
        robots = new ArrayList<>();
        gones = new HashSet<>();

        // Initialize an empty map
        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                grid[x][y] = new Cell(x, y);
            }
        }

        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                Cell cell = get(x, y);

                cell.nexts[UP] = get(x, y - 1);
                cell.nexts[RIGHT] = get(x + 1, y);
                cell.nexts[DOWN] = get(x, y + 1);
                cell.nexts[LEFT] = get(x - 1, y);
            }
        }

        // Place walls and robots
        int index = 0;
        for (int y = 0; y < MAP_HEIGHT; ++y) {
            for (int x = 0; x < MAP_WIDTH; ++x) {
                char c = input.charAt(index);

                if (c != '.') {
                    if (c == '#') {
                        get(x, y).type = VOID;
                    } else {
                        Robot robot = new Robot();
                        robot.cell = get(x, y);

                        if (c == 'U') {
                            robot.direction = UP;
                        } else if (c == 'R') {
                            robot.direction = RIGHT;
                        } else if (c == 'D') {
                            robot.direction = DOWN;
                        } else if (c == 'L') {
                            robot.direction = LEFT;
                        }

                        robots.add(robot);
                    }
                }

                index += 1;
            }
        }
    }

    public Cell get(int x, int y) {
        if (x < 0) {
            x += MAP_WIDTH;
        } else if (x >= MAP_WIDTH) {
            x -= MAP_WIDTH;
        }

        if (y < 0) {
            y += MAP_HEIGHT;
        } else if (y >= MAP_HEIGHT) {
            y -= MAP_HEIGHT;
        }

        return grid[x][y];
    }

    public void apply(int x, int y, int direction) {
        get(x, y).type = direction;
    }

    public void play() {
        gones.clear();

        for (Robot robot : robots) {
            Cell next = robot.cell.nexts[robot.direction];
            
            robot.cell = next;

            if (next.type == VOID) {
                robot.death = DEATH_VOID;
                gones.add(robot);

                continue;
            }

            if (next.type != NONE) {
                robot.direction = next.type;
            }

            if (!robot.registerState()) {
                robot.death = DEATH_INFINITE_LOOP;
                gones.add(robot);
            }
        }
        
        robots.removeAll(gones);

        score += robots.size();
    }
}
