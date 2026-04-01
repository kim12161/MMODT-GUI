package main;

import game.Story;
import menu.TitleScreen;

public class GameLauncher {

    GameWindow gameWindow;
    GamePanel gamePanel;
    TitleScreen titleScreen;
    Story story;

    public GameLauncher(){

        gamePanel = new GamePanel("res/background/main-background.gif");
//        gamePanel = new GamePanel("res/background/mainBackground.png");



        titleScreen = new TitleScreen(gamePanel, gamePanel);

        gameWindow = new GameWindow(gamePanel);

        story = new Story(gamePanel);

    }
}