package menu;

import main.GamePanel;
import javax.swing.*;
import java.awt.*;

public class CreditsPanel extends JPanel {

    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    public CreditsPanel(GamePanel gamePanel) {
        setLayout(null);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(900, 700));

        // --- TITLE ---
        JLabel title = new JLabel("CREDITS", SwingConstants.CENTER);
        title.setFont(new Font(mainFont, Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 80, 900, 60);
        title.setVisible(false); // Hidden initially
        add(title);

        // --- DIVIDER ---
        JSeparator sep = new JSeparator();
        sep.setBounds(250, 150, 400, 2);
        sep.setForeground(new Color(180, 30, 30));
        sep.setVisible(false); // Hidden initially
        add(sep);

        // --- ROLES & NAMES ---
        String[] roles = {
                "LEAD PROGRAMMER - [Your Name]",
                "LEAD ARTIST - Divinah",
                "SOUND PRODUCER - Juan Carlos",
                "WRITERS - Christopher John & Benedict",
                "TESTER & QA - John Mark"
        };

        // 1. Show Title and Divider after 500ms
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

            // Start typing after Title appears.
            // Delay is: Title Delay (500) + Sequence (i * 1000ms)
            startTypewriter(roleLabel, roleText, 1000 + (i * 1000));

            yPos += 50;
        }

        // --- BACK BUTTON ---
        JButton backBtn = new JButton("Return to Title");
        backBtn.setFont(new Font(bFont, Font.BOLD, 20));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(40, 40, 40));
        backBtn.setFocusPainted(false);
        backBtn.setVisible(false); // Hidden initially
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        int btnW = 220, btnH = 50;
        backBtn.setBounds((900 - btnW) / 2, 550, btnW, btnH);
        backBtn.setActionCommand("BackToTitle");
        backBtn.addActionListener(new MenuButtonHandler(gamePanel));
        add(backBtn);

        // Show button last (after all 5 lines have typed out)
        Timer buttonTimer = new Timer(7000, e -> backBtn.setVisible(true));
        buttonTimer.setRepeats(false);
        buttonTimer.start();
    }

    private void startTypewriter(JLabel label, String text, int initialDelay) {
        Timer timer = new Timer(40, null); // Typing speed
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