import com.codingame.gameengine.runner.SoloGameRunner;

public class Main {
    public static void main(String[] args) {
        SoloGameRunner gameRunner = new SoloGameRunner();

        gameRunner.setAgent(RandomAI.class);
        gameRunner.setTestCase("test19.json");

        gameRunner.start(9999);
    }
}
