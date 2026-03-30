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

        // BUTTON PANEL
        buttonPanel = new JPanel();
        buttonPanel.setBounds(300,400,200,150); // Increased height slightly to give buttons breathing room
        buttonPanel.setLayout(new GridLayout(3,1,10,8)); // Increased the gap between buttons from 10 to 15
        buttonPanel.setOpaque(false);

        // ==========================================
        // CREATE CUSTOM BUTTONS
        // Using the new helper method below
        // ==========================================
        startButton = createMenuButton("NEW GAME");
        continueButton = createMenuButton("CONTINUE");
        exitButton = createMenuButton("EXIT");

        // Set up the click handlers (Color logic removed from handler!)
        MenuButtonHandler handler = new MenuButtonHandler(gamePanel);

        startButton.addActionListener(handler);
        continueButton.addActionListener(handler);
        exitButton.addActionListener(handler);

        buttonPanel.add(startButton);
        buttonPanel.add(continueButton);
        buttonPanel.add(exitButton);

        con.add(titlePanel);
        con.add(buttonPanel);
    }

    // ==========================================
    // CUSTOM MENU BUTTON GENERATOR
    // Replaces standard buttons with your rounded,
    // color-changing frosted design!
    // ==========================================
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(buttonFont);
                setForeground(Color.WHITE); // Changed to white to match your picture

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // YOUR CUSTOM COLORS
                if (getModel().isPressed()) {
                    g2.setColor(new Color(43, 38, 35, 255));
                } else if (hovered) {
                    g2.setColor(new Color(43, 38, 35, 255));
                } else {
                    g2.setColor(new Color(62, 55, 49, 255));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // YOUR CUSTOM BORDER
                g2.setColor(new Color(30, 28, 26, 255));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        return btn;
    }
}