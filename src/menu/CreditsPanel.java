package menu;

import main.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CreditsPanel extends JPanel {

    private String bFont = "Munro";

    // Image variables for the custom button
    private Image defaultImg, hoverImg, activeImg;
    private boolean hovered = false;

    public CreditsPanel(GamePanel gamePanel) {
        setLayout(null);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(900, 700));

        // --- LOAD BUTTON ASSETS ---
        try {
            // --- LOAD IMAGES ---
            // Since 'res' is the Resources Root, we start the path AFTER 'res'
            defaultImg = loadImage("/ui/icon/normal-buttons/button-2-normal-not-active.png");
            hoverImg = loadImage("/ui/icon/normal-buttons/button-2-normal-hover.png");
            activeImg = loadImage("/ui/icon/normal-buttons/button-2-normal-active.png");} catch (Exception e) {
            System.out.println("Error loading credit button images. Check if 'ui' folder is in your resources.");
        }

        // --- TITLE ---
        JLabel title = new JLabel("CREDITS", SwingConstants.CENTER);
        title.setFont(new Font(bFont, Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 80, 900, 60);
        title.setVisible(false);
        add(title);

        // --- DIVIDER ---
        JSeparator sep = new JSeparator();
        sep.setBounds(250, 150, 400, 2);
        sep.setForeground(new Color(180, 30, 30));
        sep.setVisible(false);
        add(sep);

        // --- ROLES & NAMES ---
        String[] roles = {
                "LEAD PROGRAMMER - Mariana Icoy",
                "SPRITE DESIGNERS - Lady Divinah Sinay & Benedict Yuipco",
                "UI DESIGN & INTEGRATION - Kimmy Swain Alontaga",
                "TESTER & QA - Mariana Icoy"
        };

        Timer titleTimer = new Timer(500, e -> {
            title.setVisible(true);
            sep.setVisible(true);
        });
        titleTimer.setRepeats(false);
        titleTimer.start();

        int yPos = 220;
        for (int i = 0; i < roles.length; i++) {
            String roleText = roles[i];
            JLabel roleLabel = new JLabel("", SwingConstants.CENTER);
            roleLabel.setFont(new Font(bFont, Font.PLAIN, 24));
            roleLabel.setForeground(new Color(200, 200, 200));
            roleLabel.setBounds(0, yPos, 900, 30);
            add(roleLabel);
            startTypewriter(roleLabel, roleText, 1000 + (i * 1000));
            yPos += 70;
        }

        // --- CUSTOM BACK BUTTON ---
        JButton backBtn = new JButton("Return") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed();
                Image currentSprite = isPressed ? activeImg : (hovered ? hoverImg : defaultImg);

                if (currentSprite != null) {
                    g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                }
                g2.dispose();
                g.translate(6, 2);

                // Handle text "click" movement
                if (isPressed) {
                    g.translate(-3, 3);
                }

                super.paintComponent(g);

                if (isPressed) {
                    g.translate(3, -3);
                }
            }
        };

        // Button Configuration
        backBtn.setFont(new Font(bFont, Font.BOLD, 20));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);

        // 🛠️ FIX: Center the text by removing all internal padding
        backBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        backBtn.setVisible(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dimensions for the pixel-art button
        int btnW = 220, btnH = 68;
        int xPos = ((900 - btnW) / 2) - 5;
        int y = 520; // Or whatever your current vertical position is

        backBtn.setBounds(xPos, y, btnW, btnH);
        // Button Configuration
        backBtn.setFont(new Font(bFont, Font.BOLD, 20));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);

        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setVisible(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dimensions for the pixel-art button
//        int btnW = 220, btnH = 60;
//        backBtn.setBounds((900 - btnW) / 2, 520, btnW, btnH);

        backBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true; backBtn.repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovered = false; backBtn.repaint(); }
        });

        backBtn.setActionCommand("BackToTitle");
        backBtn.addActionListener(new MenuButtonHandler(gamePanel));
        add(backBtn);

        // Show button after credits type out
        Timer buttonTimer = new Timer(6000, e -> backBtn.setVisible(true));
        buttonTimer.setRepeats(false);
        buttonTimer.start();
    }

    private Image loadImage(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("COULD NOT FIND IMAGE AT: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    private void startTypewriter(JLabel label, String text, int initialDelay) {
        Timer timer = new Timer(40, null);
        timer.setInitialDelay(initialDelay);
        timer.addActionListener(e -> {
            int currentLength = label.getText().length();
            if (currentLength < text.length()) {
                label.setText(text.substring(0, currentLength + 1));
            } else {
                timer.stop();
            }
        });
        timer.start();
    }
}