package com.codingame.astarcraft;

import com.codingame.gameengine.runner.SoloGameRunner;
import com.codingame.gameengine.runner.dto.GameResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RefereeTest {

    public static final String[] MAP1 = {
            "R..................",
            "...................",
            "...................",
            "...................",
            "...................",
            "...................",
            "...................",
            "...................",
            "...................",
            "...................",
    };

    public static final String[] MAP2 = {
            "###################",
            "###################",
            "###################",
            "###################",
            "#######R....#######",
            "###################",
            "###################",
            "###################",
            "###################",
            "###################",
    };

    public static final String[] MAP_NO_ROBOT = {
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "...................",
    };

    public static final String[] MAP_TOO_MUCH_ROBOTS = {
            "###################",
            "UUUUUUUUUUUUUUUUUUU",
            "###################",
            "D..................",
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "...................",
    };

    public static final String[] BAD_MAP1 = {
            "###################",
            "OOO.............OOO",
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "...................",
    };

    public static final String[] BAD_MAP2 = {
            "###################",
            "OOO.............OOO",
            "###################",
            "...................",
            "###################"
    };

    public static final String[] BAD_MAP3 = {
            "###################",
            "RRR.............LLL",
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "...................",
            "###################",
            "..................",
    };

    @Test
    public void simples() throws Exception {
        play(19, "", MAP1);
        play(10, "0 0 D", MAP1);
        play(19, "0 0 L", MAP1);
        play(10, "0 0 U", MAP1);
        play(19, "0 0 R", MAP1);
        play(12, "2 0 U", MAP1);
        play(28, "9 0 L", MAP1);
        play(10, "0 0 U 0 0 L", MAP1);
        play(19, "0 0 L 0 0 U", MAP1);

        play(5, "", MAP2);
        play(5, "0 0 D", MAP2);
        play(5, "0 0 D 1 1 L", MAP2);
    }

    @Test
    public void badOutputs() throws Exception {
        play(19, "null", MAP1);
        play(19, "coucou", MAP1);
        play(19, "5 5", MAP1);
        play(19, "R R ", MAP1);
        play(19, "1 2 X", MAP1);
        play(19, "X X X", MAP1);
        play(19, "lol lol lol", MAP1);
        play(12, "0 0 X 2 0 U", MAP1);
    }

    @Test
    public void badInputs() throws Exception {
        play(0, "", MAP_NO_ROBOT);
        play(0, "", MAP_TOO_MUCH_ROBOTS);
        play(0, "", BAD_MAP1);
        play(0, "", BAD_MAP2);
        play(0, "", BAD_MAP3);
        play(0, "0 0 U", MAP_TOO_MUCH_ROBOTS);
        play(0, "0 0 R", MAP_TOO_MUCH_ROBOTS);
        play(0, "1 1 R", MAP_TOO_MUCH_ROBOTS);
        play(0, "2 2 L", MAP_TOO_MUCH_ROBOTS);
        play(0, "3 3 D", MAP_TOO_MUCH_ROBOTS);
    }

    private void assertScore(GameResult result, int excepted) {
        assertEquals(excepted, (int) Integer.valueOf(result.metadata.split("\"")[3]));
    }

    private void play(int expected, String output, String[] map) throws Exception {
        Files.write(Paths.get(System.getProperty("java.io.tmpdir"), "astarcraft-output"), (output + "\n").getBytes());

        SoloGameRunner runner = new SoloGameRunner();
        runner.setAgent(TestAI.class);
        runner.setTestCaseInput(String.join("", map));

        assertScore(runner.simulate(), expected);
    }
}
