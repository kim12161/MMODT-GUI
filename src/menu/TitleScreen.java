package menu;

import game.Game;
import main.GamePanel;

import javax.swing.*;
import java.awt.*;

public class TitleScreen {

    JPanel titlePanel, buttonPanel;
    JLabel titleName;

    Font titleFont = new Font("PixelArmy", Font.PLAIN, 60);
    Font buttonFont = new Font("Munro", Font.PLAIN, 15);

    JButton startButton;
    JButton continueButton;
    JButton exitButton;

    public TitleScreen(Container con, GamePanel gamePanel){

        con.setLayout(null);
        // TITLE PANEL
        titlePanel = new JPanel();
        titlePanel.setBounds(100,100,600,100);
        titlePanel.setOpaque(false);

        ImageIcon titleImage = new ImageIcon("res/mmodt5.png");
        titleName = new JLabel(titleImage);


        titlePanel.add(titleName);

        buttonPanel = new JPanel();
        buttonPanel.setBounds(300,400,200,100);
        buttonPanel.setLayout(new GridLayout(3,1,10,10));
        buttonPanel.setOpaque(false);

        startButton = new JButton("NEW GAME");
        continueButton = new JButton("CONTINUE");
        exitButton = new JButton("EXIT");

        startButton.setFont(buttonFont);
        continueButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        startButton.setForeground(Color.decode("#E8C36A"));
        continueButton.setForeground(Color.decode("#E8C36A"));
        exitButton.setForeground(Color.decode("#E8C36A"));


        Color normalRed = new Color(145,0,0);   // current red
        Color hoverRed = new Color(100,0,0);    // darker red when hovered

        MenuButtonHandler startHandler = new MenuButtonHandler(startButton, normalRed, hoverRed, gamePanel);
        MenuButtonHandler continueHandler = new MenuButtonHandler(continueButton, normalRed, hoverRed, gamePanel);
        MenuButtonHandler exitHandler = new MenuButtonHandler(exitButton, normalRed, hoverRed, gamePanel);

        startButton.addMouseListener(startHandler);
        continueButton.addMouseListener(continueHandler);
        exitButton.addMouseListener(exitHandler);

        startButton.addActionListener(startHandler);
        continueButton.addActionListener(continueHandler);
        exitButton.addActionListener(exitHandler);

        startButton.setOpaque(true);
        continueButton.setOpaque(true);
        exitButton.setOpaque(true);

        startButton.setContentAreaFilled(true);
        continueButton.setContentAreaFilled(true);
        exitButton.setContentAreaFilled(true);

        startButton.setFocusPainted(false);
        continueButton.setFocusPainted(false);
        exitButton.setFocusPainted(false);

        startButton.setBorderPainted(false);
        continueButton.setBorderPainted(false);
        exitButton.setBorderPainted(false);


        startButton.setBackground(normalRed);
        continueButton.setBackground(normalRed);
        exitButton.setBackground(normalRed);


        buttonPanel.add(startButton);
        buttonPanel.add(continueButton);
        buttonPanel.add(exitButton);

        con.add(titlePanel);
        con.add(buttonPanel);
    }
}