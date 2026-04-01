package menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import game.Story;
import main.GamePanel;

// We only need ActionListener now, not MouseListener!
public class MenuButtonHandler implements ActionListener {

    GamePanel gamePanel;

    public MenuButtonHandler(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }

    // Button Clicks
    @Override
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();

        if(command.equals("Exit")){
            System.exit(0);
        }

        if(command.equals("New Game")){
            // CLEAR the panel and show Story
            gamePanel.removeAll();
            gamePanel.setLayout(new BorderLayout());

            Story story = new Story(gamePanel);
            gamePanel.add(story, BorderLayout.CENTER);

            gamePanel.revalidate();
            gamePanel.repaint();

            story.requestFocusInWindow(); // needed for ENTER key to work
        }

        if(command.equals("Continue")){
            System.out.println("Continue Game");
        }
    }
}