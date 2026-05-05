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
        add(title);

        // --- DIVIDER ---
        JSeparator sep = new JSeparator();
        sep.setBounds(250, 150, 400, 2);
        sep.setForeground(new Color(180, 30, 30));
        add(sep);

        // --- ROLES & NAMES ---
        String[] roles = {
                "LEAD PROGRAMMER - [Your Name]",
                "LEAD ARTIST - Divinah",
                "SOUND PRODUCER - Juan Carlos",
                "WRITERS - Christopher John & Benedict",
                "TESTER & QA - John Mark"
        };

        int yPos = 220;
        for (String role : roles) {
            JLabel roleLabel = new JLabel(role, SwingConstants.CENTER);
            roleLabel.setFont(new Font(bFont, Font.PLAIN, 24));
            roleLabel.setForeground(new Color(200, 200, 200));
            roleLabel.setBounds(0, yPos, 900, 30);
            add(roleLabel);
            yPos += 50; // Space between each line
        }

        // --- BACK BUTTON ---
        JButton backBtn = new JButton("Return to Title");
        backBtn.setFont(new Font(bFont, Font.BOLD, 20));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(40, 40, 40));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        int btnW = 220, btnH = 50;
        backBtn.setBounds((900 - btnW) / 2, 550, btnW, btnH);

        backBtn.setActionCommand("BackToTitle");
        backBtn.addActionListener(new MenuButtonHandler(gamePanel));

        add(backBtn);
    }
}