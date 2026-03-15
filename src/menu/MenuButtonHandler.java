package menu;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import game.Game;
import game.Story;
import main.GamePanel;

public class MenuButtonHandler implements MouseListener, ActionListener {

    JButton button;
    Color normalColor;
    Color hoverColor;
    GamePanel gamePanel;

    public MenuButtonHandler(JButton button, Color normalColor, Color hoverColor, GamePanel gamePanel){
        this.button = button;
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.gamePanel = gamePanel;

    }

    // Hover Effect
    public void mouseEntered(MouseEvent e){
        button.setBackground(hoverColor); // turn red when hovered
        button.repaint();
    }

    public void mouseExited(MouseEvent e){
        button.setBackground(normalColor); // go back to original color
        button.repaint();
    }

    public void mouseClicked(MouseEvent e){
    }
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    // Button Clicks
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();

        if(command.equals("EXIT")){
            System.exit(0);
        }

        if(command.equals("NEW GAME")){
            // CLEAR the panel and show Story
            gamePanel.removeAll();
            gamePanel.setLayout(new BorderLayout()); // ADD layout manager

            Story story = new Story(gamePanel);
            gamePanel.add(story, BorderLayout.CENTER);

            gamePanel.revalidate();
            gamePanel.repaint();

            story.requestFocusInWindow(); // needed for ENTER key to work
        }

        if(command.equals("CONTINUE")){
            System.out.println("Continue Game");
        }
    }
}