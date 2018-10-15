package com.codingame.astarcraft.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.codingame.astarcraft.Constants.*;

public class Engine {

    public int score;
    public Cell[][] grid;
    public List<Robot> robots;
    public Set<Robot> wrecks;

    public Engine(String input) {
        score = 0;
        grid = new Cell[MAP_WIDTH][MAP_HEIGHT];
        robots = new ArrayList<>();
        wrecks = new HashSet<>();

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

                Cell cell = get(x, y);

                if (Character.isUpperCase(c)) {
                    Robot robot = new Robot();
                    robot.cell = cell;
                    robot.direction = charToType(c);

                    robots.add(robot);
                } else {
                    cell.type = charToType(c);
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

    public void registerStates() {
        for (Robot robot : robots) {
            robot.registerState();
        }
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
        wrecks.clear();

        // Increase the score
        score += robots.size();

        for (Robot robot : robots) {
            // Get the next cell
            Cell next = robot.cell.nexts[robot.direction];

            // Move the robot
            robot.cell = next;

            // This is a void cell, RIP robot
            if (next.type == VOID) {
                robot.death = DEATH_VOID;
                wrecks.add(robot);

                continue;
            }

            // Change the direction of the robot if we must
            if (next.type != NONE) {
                robot.direction = next.type;
            }

            // Register the new state and check for infinite loop
            if (!robot.registerState()) {
                robot.death = DEATH_INFINITE_LOOP;
                wrecks.add(robot);
            }
        }

        // Garbage collection
        robots.removeAll(wrecks);
    }
}
