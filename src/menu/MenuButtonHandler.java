//tweaked for testing

package menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import Characters.Adi;
import Characters.Avy;
import Characters.Character;
import Characters.Kim;
import Characters.Marina;
import Characters.Nathan;
import Characters.Yubie;
import Player.Player;
import Player.Gender;
import game.ConversationManager;
import game.ScenePanel;
import game.Story;
import game.EndGamePanel; // 🛠️ ADDED IMPORT FOR TESTING
import main.GamePanel;
import saveSystem.SaveSystem;
import saveSystem.SaveSystem.SaveData;

import javax.swing.*;

public class MenuButtonHandler implements ActionListener {

    GamePanel gamePanel;

    public MenuButtonHandler(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();

        if(command.equals("Exit")){
            int confirm = JOptionPane.showConfirmDialog(
                    gamePanel,
                    "Are you sure you want to exit the game?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }

        if(command.equals("Credits")){
            gamePanel.removeAll();
            gamePanel.setLayout(new BorderLayout());

            CreditsPanel creditsPanel = new CreditsPanel(gamePanel);
            gamePanel.add(creditsPanel, BorderLayout.CENTER);

            gamePanel.revalidate();
            gamePanel.repaint();
        }

        if(command.equals("BackToTitle")){
            gamePanel.removeAll();

            // Re-initializes the Title Screen
            new TitleScreen(gamePanel, gamePanel);

            gamePanel.revalidate();
            gamePanel.repaint();
        }

        if(command.equals("New Game")){
            gamePanel.removeAll();
            gamePanel.setLayout(new BorderLayout());

            // ==========================================
            // 🛠️ TEST MODE: YUBIE BAD ENDING
            // (When you are done testing, just delete this
            // block and uncomment the ORIGINAL CODE below!)
            // ==========================================

            Player testPlayer = new Player("Tester", 100, Gender.MALE);
            // We removed the increaseCharisma line so the player has 0 charisma.

            List<Character> testCharacters = new ArrayList<>();
            Yubie yubie = new Yubie();
            testCharacters.add(yubie);

            // 🛠️ CHANGED: Max out Turn-Off to guarantee the Bad Ending!
            testPlayer.increaseTurnOff(yubie, 100);

            EndGamePanel testEnding = new EndGamePanel(testPlayer, testCharacters);
            gamePanel.add(testEnding, BorderLayout.CENTER);
            // ==========================================


            // ==========================================
            // ORIGINAL CODE (Commented out for now)
            // ==========================================
            // Story story = new Story(gamePanel);
            // gamePanel.add(story, BorderLayout.CENTER);
            // story.requestFocusInWindow();
            // ==========================================

            gamePanel.revalidate();
            gamePanel.repaint();
        }

        if(command.equals("Continue")){
            gamePanel.removeAll();
            gamePanel.setLayout(new BorderLayout());

            ContinuePanel continuePanel = new ContinuePanel(gamePanel, (SaveData data) -> {
                // 1. Rebuild the player using the saved gender
                Player player = new Player(data.playerName, 100, data.playerGender);

                // 2. Build the full character list
                List<Character> allCharacters = new ArrayList<>();
                allCharacters.add(new Avy());
                allCharacters.add(new Marina());
                allCharacters.add(new Kim());
                allCharacters.add(new Nathan());
                allCharacters.add(new Yubie());
                allCharacters.add(new Adi());

                // 3. Filter to opposite gender (same logic as Story.filterRomanceable)
                List<Character> romanceableCharacters = new ArrayList<>();
                for (Character c : allCharacters) {
                    if (c.getGender() != player.getGender()) {
                        romanceableCharacters.add(c);
                    }
                }

                // 4. Restore health, charisma, inventory, relationships from save
                SaveSystem.restorePlayer(player, data, romanceableCharacters);

                // 5. Launch ScenePanel at the saved level
                ScenePanel scenePanel = new ScenePanel(player, romanceableCharacters, new ConversationManager());
                scenePanel.setGamePanel(gamePanel);

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(gamePanel);
                if (frame != null) {
                    frame.getContentPane().removeAll();
                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(scenePanel, BorderLayout.CENTER);
                    frame.revalidate();
                    frame.repaint();
                }

                scenePanel.startGameFromLevel(data.currentLevel, data.currentConversation);
            });

            gamePanel.add(continuePanel, BorderLayout.CENTER);
            gamePanel.revalidate();
            gamePanel.repaint();
        }
    }
}