package menu;

import main.GamePanel;
import javax.swing.*;
import java.awt.*;

public class TitleScreen {

    JPanel titlePanel, buttonPanel;
    JLabel titleName;

    // FONTS
    Font titleFont = new Font("PixelArmy", Font.PLAIN, 60);
    Font buttonFont = new Font("Munro", Font.PLAIN, 15);

    JButton startButton, continueButton, exitButton;

    public TitleScreen(Container con, GamePanel gamePanel){

        con.setLayout(null);

        // ==============================
        // TITLE PANEL
        // ==============================
        titlePanel = new JPanel();
        titlePanel.setBounds(100, 100, 600, 100);
        titlePanel.setOpaque(false);

        // Your game logo/title image
        ImageIcon titleImage = new ImageIcon("res/mmodt5.png");
        titleName = new JLabel(titleImage);
        titlePanel.add(titleName);

        // ==============================
        // BUTTON PANEL
        // GridBagLayout prevents stretching
        // ==============================
        buttonPanel = new JPanel(new GridBagLayout());
        // Set width to exactly 160 to match your 160x50px sprite
        //button placement
        buttonPanel.setBounds(300, 300, 200, 270);
        buttonPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE; // DO NOT STRETCH
        //gap of placement
        gbc.insets = new Insets(4, 0, 2, 0); // Spacing between buttons


        startButton = createMenuButton("New Game",
                "res/ui/icon/menu-button-not-active.png",
                "res/ui/icon/menu-button-active.png");

        // Continue and Exit use the standard menu-button2 files
        continueButton = createMenuButton("Continue",
                "res/ui/icon/menu-button2-not-active.png",
                "res/ui/icon/menu-button2-active.png");

        exitButton = createMenuButton("Exit",
                "res/ui/icon/menu-button2-not-active.png",
                "res/ui/icon/menu-button2-active.png");

        // Set up the click handlers
        MenuButtonHandler handler = new MenuButtonHandler(gamePanel);
        startButton.addActionListener(handler);
        continueButton.addActionListener(handler);
        exitButton.addActionListener(handler);

        // Add buttons to panel with vertical positioning
        gbc.gridy = 0; buttonPanel.add(startButton, gbc);
        gbc.gridy = 1; buttonPanel.add(continueButton, gbc);
        gbc.gridy = 2; buttonPanel.add(exitButton, gbc);

        con.add(titlePanel);
        con.add(buttonPanel);
    }

    // ==========================================
    // CUSTOM MENU BUTTON GENERATOR
    // ==========================================
    private JButton createMenuButton(String text, String normalPath, String activePath) {
        final Image defaultImg = new ImageIcon(normalPath).getImage();
        final Image activeImg  = new ImageIcon(activePath).getImage();

        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                // LOCK THE SIZE TO 160x50 (Matches your sprite)
                //sizing placemnt button
                Dimension size = new Dimension(190, 60);
                setPreferredSize(size);
                setMinimumSize(size);
                setMaximumSize(size);

                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(buttonFont);
                setForeground(Color.WHITE);

                // Keep text perfectly centered on the sprite
                setHorizontalTextPosition(JButton.CENTER);
                setVerticalTextPosition(JButton.CENTER);

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                // RENDERING HINTS: Pixel-perfect for Mac Retina
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                boolean isActive = hovered || getModel().isPressed();
                Image currentSprite = isActive ? activeImg : defaultImg;

                // Draws the image at the button's actual 160x50 size
                g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        return btn;
    }
}