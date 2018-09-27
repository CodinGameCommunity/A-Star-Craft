package com.codingame.astarcraft.game;

import static com.codingame.astarcraft.Constants.*;

import java.util.*;

public class Engine {

    public int score;
    public Cell[][] grid;
    public List<Robot> robots;
    public Set<Robot> gones;

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

        // Link cells
        for (int x = 0; x < MAP_WIDTH; ++x) {
            for (int y = 0; y < MAP_HEIGHT; ++y) {
                Cell cell = get(x, y);

                cell.nexts[UP] = get(x, y - 1);
                cell.nexts[RIGHT] = get(x + 1, y);
                cell.nexts[DOWN] = get(x, y + 1);
                cell.nexts[LEFT] = get(x - 1, y);
            }
        }

        // Place void cells, robots and arrows
        int index = 0;
        for (int y = 0; y < MAP_HEIGHT; ++y) {
            for (int x = 0; x < MAP_WIDTH; ++x) {
                char c = input.charAt(index);

                if (c != '.') {
                    Cell cell = get(x, y);

                    if (c == '#') {
                        cell.type = VOID;
                    } else if (c == Character.toUpperCase(c)) {
                        Robot robot = new Robot();
                        robot.cell = cell;

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
                    } else {
                        if (c == 'u') {
                            cell.type = UP;
                        } else if (c == 'r') {
                            cell.type = RIGHT;
                        } else if (c == 'd') {
                            cell.type = DOWN;
                        } else if (c == 'l') {
                            cell.type = LEFT;
                        }
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
        Cell cell = get(x, y);

        cell.type = direction;

        // Check if we need to update a robot direction
        for (Robot robot : robots) {
            if (robot.cell == cell) {
                robot.direction = direction;
            }
        }
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
