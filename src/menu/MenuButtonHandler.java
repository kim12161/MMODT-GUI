package menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import game.Story;
import main.GamePanel;
import saveSystem.SaveSystem.SaveData;

public class MenuButtonHandler implements ActionListener {

    GamePanel gamePanel;

    public MenuButtonHandler(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();

        if(command.equals("Exit")){
            System.exit(0);
        }

        if(command.equals("New Game")){
            gamePanel.removeAll();
            gamePanel.setLayout(new BorderLayout());

            Story story = new Story(gamePanel);
            gamePanel.add(story, BorderLayout.CENTER);

            gamePanel.revalidate();
            gamePanel.repaint();

            story.requestFocusInWindow();
        }

        if(command.equals("Continue")){
            gamePanel.removeAll();
            gamePanel.setLayout(new BorderLayout());

            ContinuePanel continuePanel = new ContinuePanel(gamePanel, (SaveData data) -> {
                // ── TODO: restore game from save data ──────────────────────
                // Example:
                //   Player player = new Player(data.playerName, 100, yourGender);
                //   List<Character> chars = buildCharacterList();
                //   SaveSystem.restorePlayer(player, data, chars);
                //   // then navigate to data.currentLevel
                // ───────────────────────────────────────────────────────────
                System.out.println("Loading save: " + data);
            });

            gamePanel.add(continuePanel, BorderLayout.CENTER);
            gamePanel.revalidate();
            gamePanel.repaint();
        }
    }
}