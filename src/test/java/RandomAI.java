import java.util.Random;
import java.util.Scanner;

public class RandomAI {
    public static void main(String[] args) {
        Random random = new Random();
        Scanner scanner = new Scanner(System.in);

        String output = "";

        for (int y = 0; y < 10; ++y) {
            String line = scanner.nextLine();

            for (int x = 0; x < line.length(); ++x) {
                char c = line.charAt(x);

                if (c == '.') {
                    int r = random.nextInt(4);

                    switch (r) {
                    case 0:
                        output += " " + x + " " + y + " U";
                        break;
                    case 1:
                        output += " " + x + " " + y + " R";
                        break;
                    case 2:
                        output += " " + x + " " + y + " D";
                        break;
                    case 3:
                        output += " " + x + " " + y + " L";
                        break;
                    }
                }
            }
        }

        int mouseCount = scanner.nextInt();
        for (int i = 0; i < mouseCount; ++i) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            String direction = scanner.nextLine();
        }

        System.out.println(output);
    }
}
