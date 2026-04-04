package menu;

import main.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TitleScreen {

    JPanel titlePanel, buttonPanel;
    JLabel titleName;
    JButton startButton, continueButton, exitButton;

    // FONTS
    Font titleFont = new Font("PixelArmy", Font.PLAIN, 60);
    Font buttonFont = new Font("Munro", Font.PLAIN, 18);

    // SCREEN DIMENSIONS
    final int SCREEN_WIDTH = 1080;
    final int SCREEN_HEIGHT = 800;

    public TitleScreen(Container con, GamePanel gamePanel){

        con.setLayout(null);

        // ==============================
        // 1. TITLE PANEL (LOGO)
        // ==============================
        int titleW = 800;
        int titleH = 200;
        int titleX = (SCREEN_WIDTH - titleW) / 2;

        titlePanel = new JPanel();
        titlePanel.setBounds(titleX, 80, titleW, titleH);
        titlePanel.setOpaque(false);

        ImageIcon titleImage = new ImageIcon("res/mmodt5.png");
        titleName = new JLabel(titleImage);
        titlePanel.add(titleName);

        // ==============================
        // 2. BUTTON PANEL
        // ==============================
        int buttonPanelW = 240;
        int buttonPanelH = 350;

        // VISUAL CENTERING ADJUSTMENT:
        // Normally (1080 - 240) / 2 = 420.
        // We use 405 to nudge the boxes left, centering them despite the leaves.
        int buttonX = 405;
        int buttonY = 380;

        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBounds(buttonX, 410, buttonPanelW, buttonPanelH);
        buttonPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Create Buttons
        startButton = createMenuButton("New Game",
                "res/ui/icon/button-not-active.png",
                "res/ui/icon/button-hover.png",
                "res/ui/icon/button-active.png");

        continueButton = createMenuButton("Continue",
                "res/ui/icon/button2-not-active.png",
                "res/ui/icon/button2-hover.png",
                "res/ui/icon/button2-active.png");

        exitButton = createMenuButton("Exit",
                "res/ui/icon/button2-not-active.png",
                "res/ui/icon/button2-hover.png",
                "res/ui/icon/button2-active.png");

        // ==============================
        // 3. ACTION HANDLERS
        // ==============================
        MenuButtonHandler handler = new MenuButtonHandler(gamePanel);

        startButton.setActionCommand("New Game");
        continueButton.setActionCommand("Continue");
        exitButton.setActionCommand("Exit");

        startButton.addActionListener(handler);
        continueButton.addActionListener(handler);
        exitButton.addActionListener(handler);

        gbc.gridy = 0; buttonPanel.add(startButton, gbc);
        gbc.gridy = 1; buttonPanel.add(continueButton, gbc);
        gbc.gridy = 2; buttonPanel.add(exitButton, gbc);

        con.add(titlePanel);
        con.add(buttonPanel);

        con.revalidate();
        con.repaint();
    }

    private JButton createMenuButton(String text, String normalPath, String hoverPath, String activePath) {
        final Image defaultImg = new ImageIcon(normalPath).getImage();
        final Image hoverImg   = new ImageIcon(hoverPath).getImage();
        final Image activeImg  = new ImageIcon(activePath).getImage();

        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                Dimension size = new Dimension(240, 90);
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

                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
                setHorizontalTextPosition(JButton.CENTER);
                setVerticalTextPosition(JButton.CENTER);

                // Text centered vertically inside the box part
                setBorder(BorderFactory.createEmptyBorder(8, 15, 0, 0));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed();
                Image currentSprite;

                if (isPressed) {
                    currentSprite = activeImg;
                } else if (hovered) {
                    currentSprite = hoverImg;
                } else {
                    currentSprite = defaultImg;
                }

                g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();

                // Tacitly push text down when clicked
                if (isPressed) {
                    g.translate(-3, 3);
                }

                super.paintComponent(g);

                if (isPressed) {
                    g.translate(0, -4);
                }
            }
        };
        return btn;
    }
}