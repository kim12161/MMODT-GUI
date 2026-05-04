package game;

import Characters.Character;
import Player.Player;
import RelationshipSystem.Relationship;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import Player.Gender;

public class EndGamePanel extends JPanel {

    private static final int W = 900;
    private static final int H = 700;

    //FONT
    private String mainFont="PixelArmy";
    private String bFont="Munro";

    private Player          player;
    private List<Character> characters;

    // ==============================
    // CINEMATIC VARIABLES
    // ==============================
    private boolean inCinematic = false;
    private Image currentEndingImg = null;

    // The JTextPane that handles the typewriter effect
    private JTextPane dialogue;

    public EndGamePanel(Player player, List<Character> characters) {

        this.player     = player;
        this.characters = characters;

        setLayout(null);
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.BLACK);

        buildUI();
    }

    // ==============================
    // BUILD UI
    // ==============================
    private void buildUI() {

        // Title
        JLabel title = new JLabel("YOU SURVIVED!", SwingConstants.CENTER);
        title.setFont(new Font(bFont, Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 28, W, 55);
        add(title);

        JSeparator sep = new JSeparator();
        sep.setBounds(100, 88, 600, 2);
        sep.setForeground(new Color(180, 30, 30));
        add(sep);

        // Scores header
        JLabel scoresHeader = new JLabel(
                "FINAL RELATIONSHIP SCORES", SwingConstants.CENTER);
        scoresHeader.setFont(new Font(bFont, Font.BOLD, 16));
        scoresHeader.setForeground(new Color(220, 60, 60));
        scoresHeader.setBounds(0, 100, W, 25);
        add(scoresHeader);

        // Calculate scores and find best match
        Character bestMatch = null;
        double    bestScore = 0;

        Map<Character, Double> scores = new LinkedHashMap<>();
        for (Character c : characters) {
            Relationship r = player.getRelationship(c);
            double score   = r.calculateFinalScore(player.getCharisma());
            scores.put(c, score);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = c;
            }
        }

        // Score rows
        int yPos = 135;
        for (Map.Entry<Character, Double> entry : scores.entrySet()) {

            Character c     = entry.getKey();
            double    score = entry.getValue();
            boolean   isBest = c == bestMatch && bestScore > 0;

            JPanel row = buildScoreRow(c.getName(), score, isBest);
            row.setBounds(150, yPos, 500, 44);
            add(row);
            yPos += 52;
        }
        // Divider
        JSeparator sep2 = new JSeparator();
        sep2.setBounds(100, yPos + 4, 600, 2);
        sep2.setForeground(new Color(80, 80, 80));
        add(sep2);

        yPos += 18;

        // Ending text
        if (bestMatch != null && bestScore > 0) {

            String endingTitle;
            String endingLine1;
            String endingLine2;
            Color  endingColor;

            if (bestScore >= 80) {
                endingTitle = "TRUE LOVE ENDING";
                endingLine1 = "CONGRATULATIONS! You found true love with "
                        + bestMatch.getName() + "!";
                endingLine2 = bestMatch.getName()
                        + " — 'Maybe it was fate that brought us together.'";
                endingColor = new Color(220, 180, 60);

            } else if (bestScore <= 60) {
                endingTitle = "PARTING WAYS ENDING";
                endingLine1 = "Too bad! Things didn't work out with "
                        + bestMatch.getName() + ".";
                endingLine2 = bestMatch.getName()
                        + " — 'Maybe we aren't meant for each other...'";
                endingColor = new Color(150, 150, 150);

            } else {
                endingTitle = "UNCERTAIN ENDING";
                endingLine1 = "You hesitated between choices...";
                endingLine2 = "The heart knows no clear answer.";
                endingColor = new Color(100, 160, 220);
            }

            JLabel etLabel = new JLabel(endingTitle, SwingConstants.CENTER);
            etLabel.setFont(new Font(bFont, Font.BOLD, 18));
            etLabel.setForeground(endingColor);
            etLabel.setBounds(0, yPos, W, 28);
            add(etLabel);
            yPos += 34;

            JLabel el1 = new JLabel(endingLine1, SwingConstants.CENTER);
            el1.setFont(new Font(bFont, Font.PLAIN, 13));
            el1.setForeground(Color.WHITE);
            el1.setBounds(40, yPos, W - 80, 22);
            add(el1);
            yPos += 26;

            JLabel el2 = new JLabel(endingLine2, SwingConstants.CENTER);
            el2.setFont(new Font(bFont, Font.ITALIC, 13));
            el2.setForeground(new Color(180, 180, 180));
            el2.setBounds(40, yPos, W - 80, 22);
            add(el2);
            yPos += 30;

            // Button to view the final cinematic scenes
            JButton viewEndingBtn = new JButton("VIEW FATE");
            viewEndingBtn.setFont(new Font(bFont, Font.BOLD, 16));
            viewEndingBtn.setForeground(Color.WHITE);
            viewEndingBtn.setBackground(new Color(40, 40, 40));
            viewEndingBtn.setFocusPainted(false);
            viewEndingBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            viewEndingBtn.setBounds((W - 160) / 2, yPos + 10, 160, 40);

            // Passes the final match to the sequence thread
            final Character finalMatch = bestMatch;
            final double finalScore = bestScore;
            viewEndingBtn.addActionListener(e -> playEndingSequence(finalMatch, finalScore));

            add(viewEndingBtn);
        }
    }

    // ==============================
    // SCORE ROW
    // ==============================
    private JPanel buildScoreRow(String name, double score, boolean highlight) {

        JPanel row = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Color fill = highlight
                        ? new Color(60, 30, 10, 200)
                        : new Color(20, 20, 20, 180);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                Color border = highlight
                        ? new Color(220, 160, 40)
                        : new Color(60, 60, 60);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(highlight ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        row.setOpaque(false);

        // Name
        JLabel nameLbl = new JLabel(name.toUpperCase());
        nameLbl.setFont(new Font(bFont, Font.BOLD, 14));
        nameLbl.setForeground(highlight
                ? new Color(220, 180, 60) : Color.WHITE);
        nameLbl.setBounds(16, 10, 200, 22);

        // Score bar
        int barMaxW = 180;
        int barW    = (int) (barMaxW * (score / 100.0));
        JPanel bar  = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Track
                g2.setColor(new Color(40, 40, 40));
                g2.fillRoundRect(0, 0, barMaxW, 14, 6, 6);
                // Fill
                Color fillColor = score >= 80
                        ? new Color(60, 200, 80)
                        : score >= 50
                        ? new Color(200, 160, 40)
                        : new Color(200, 60, 60);
                g2.setColor(fillColor);
                if (barW > 0)
                    g2.fillRoundRect(0, 0, barW, 14, 6, 6);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBounds(220, 15, barMaxW, 14);

        // Score text
        JLabel scoreLbl = new JLabel(
                String.format("%.1f%%", score));
        scoreLbl.setFont(new Font(bFont, Font.BOLD, 13));
        scoreLbl.setForeground(highlight
                ? new Color(220, 180, 60)
                : new Color(180, 180, 180));
        scoreLbl.setBounds(415, 10, 70, 22);

        // Best tag
        if (highlight) {
            JLabel bestTag = new JLabel("★ BEST");
            bestTag.setFont(new Font(bFont, Font.BOLD, 11));
            bestTag.setForeground(new Color(220, 180, 60));
            bestTag.setBounds(435, 10, 70, 22);
            scoreLbl.setBounds(415, 10, 40, 22);
            row.add(bestTag);
        }

        row.add(nameLbl);
        row.add(bar);
        row.add(scoreLbl);

        return row;
    }

    // ==========================================
    // TYPEWRITER TEXT LOGIC
    // ==========================================

    private void setupCinematicTextPane() {
        if (dialogue == null) {
            dialogue = new JTextPane();

            int imgW = 642;
            int imgH = 336;
            int imgY = 120;
            // Place it exactly 20 pixels below the image bounds
            int textY = imgY + imgH + 20;

            // Width is exactly the same as imgW so it wraps cleanly at the borders
            dialogue.setBounds((W - imgW) / 2, textY, imgW, 150);
            dialogue.setOpaque(false);
            dialogue.setBackground(new Color(0, 0, 0, 0));
            dialogue.setEditable(false);
            dialogue.setFont(new Font(bFont, Font.PLAIN, 18));
            dialogue.setForeground(Color.WHITE);

            add(dialogue);
        }
        dialogue.setVisible(true);
    }

    private void clearText() {
        SwingUtilities.invokeLater(() -> dialogue.setText(""));
    }

    private void typeText(String text, int delay) {
        for (char c : text.toCharArray()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Document doc = dialogue.getDocument();
                    doc.insertString(doc.getLength(), String.valueOf(c), null);
                } catch (BadLocationException ignored) {}
            });
            try { Thread.sleep(delay); } catch (Exception ignored) {}
        }
    }


    // ==========================================
    // CINEMATIC ENDING SEQUENCES
    // ==========================================

    private void playEndingSequence(Character bestMatch, double bestScore) {
        // Clear all the scores and buttons from the screen
        removeAll();
        inCinematic = true;

        // Add the typing text box to the screen
        setupCinematicTextPane();

        revalidate();
        repaint();

        new Thread(() -> {
            if (bestScore >= 80) {
                // ── TRUE LOVE ENDING ──

                // Scene 1: Helicopter
                loadImage("res/background/ending/happy/1-happy-ending.png");
                repaint();
                clearText();
                // We merge the two sentences into one block. JTextPane wraps it automatically!
                typeText("The helicopter lifts you away from the chaos below. Beside you, " + bestMatch.getName() + " is finally at peace. After everything, you made it out, together.", 25);
                sleep(3500);

                // Scene 2: Marriage
                loadImage("res/background/ending/happy/2-happy-ending-" + bestMatch.getName().toLowerCase() + ".png");
                repaint();
                clearText();
                typeText("In a slowly healing world, you and " + bestMatch.getName() + " stand side by side and make a quiet promise. To keep living, together.", 25);
                sleep(3500);

            } else {
                // ── BAD ENDING ──
                boolean isFemale = player.getGender() == Gender.FEMALE;
                String pronoun = isFemale ? "him" : "her";
                String imgPath = isFemale ? "res/background/ending/bad/bad-fem.png" : "res/background/ending/bad/bad-male.png";

                // Scene 1
                loadImage(imgPath);
                repaint();
                clearText();
                typeText("As the shadows close in and teeth find their mark, you watch " + pronoun + " disappear into the fading light without a single glance back.", 25);
                sleep(3500);

                // Scene 2 (Same image, text continues the story)
                clearText();
                typeText("The promise of 'forever' dissolves into a final, lonely breath as they choose the horizon, leaving you behind to the hunger of the dark.", 25);
                sleep(3500);
            }

            loadImage("res/background/ending/happy/happy-ending-ty.png");
            repaint();
            clearText();

            // Format the final "THANK YOU" to be Bold and a little larger
            SwingUtilities.invokeLater(() -> {
                dialogue.setFont(new Font(bFont, Font.BOLD, 26));
                dialogue.setForeground(new Color(200, 200, 200));

                // Ensure it stays left-aligned so the typewriter effect flows left-to-right
                StyledDocument doc = dialogue.getStyledDocument();
                SimpleAttributeSet leftAlign = new SimpleAttributeSet();
                StyleConstants.setAlignment(leftAlign, StyleConstants.ALIGN_LEFT);
                doc.setParagraphAttributes(0, doc.getLength(), leftAlign, false);
            });

            // We use standard spaces to push it toward the center of the screen
            typeText("                        THANK YOU FOR PLAYING!", 50);

        }).start();
    }

    private void loadImage(String path) {
        try {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                currentEndingImg = new ImageIcon(f.getAbsolutePath()).getImage();
            } else {
                currentEndingImg = null;
            }
        } catch (Exception e) {}
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }

    // ==========================================
    // CINEMATIC PAINT COMPONENT
    // ==========================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Only draw this custom stuff if the player clicked "VIEW FATE"
        if (inCinematic) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 1. Draw Image - MATCHING THE INTRO STORYLINE EXACTLY
            int imgW = 642;
            int imgH = 336;
            int imgX = (W - imgW) / 2;
            int imgY = 120;

            if (currentEndingImg != null) {
                g2.drawImage(currentEndingImg, imgX, imgY, imgW, imgH, this);

                // Draw Copper Border around image
                g2.setColor(new Color(200, 140, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(imgX, imgY, imgW, imgH);
            }

            // NOTE: The text rendering is no longer handled in paintComponent!
            // It is completely managed by the dialogue JTextPane so the typewriter
            // effect works perfectly without flickering.

            g2.dispose();
        }
    }
}