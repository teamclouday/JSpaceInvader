// This file is the starting point of the game

import java.util.Scanner;

/**
 * The entry point of the game
 */
public class Game 
{
    public static void main(String[] args)
    {
        System.out.println("Welcome to JSpaceInvader!");
        System.out.println("Please enter a level of difficulty: 0 (easy), 1 (middle), 2 (hard), 3 (extremely hard)");
        Scanner scanner = new Scanner(System.in);
        int level = -1;
        do
        {
            int input = scanner.nextInt();
            if(input < 0 || input > 3)
                System.out.println("Please enter a value in [0, 1, 2, 3]:");
            else
                level = input;
        }while(level < 0);
        scanner.close();
        Renderer myRenderer = new Renderer(level);
        myRenderer.loop();
        String summary = myRenderer.close();
        System.out.println("\nThanks for playing JSpaceInvader!");
        System.out.println(summary);
        System.out.println();
    }
}
