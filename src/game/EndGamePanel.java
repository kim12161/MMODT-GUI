package game;

import Characters.Character;
import Player.Player;
import RelationshipSystem.Relationship;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class EndGamePanel extends JPanel {

    private static final int W = 800;
    private static final int H = 600;

    //FONT
    private String mainFont="PixelArmy";
    private String bFont="Munro";

    private Player          player;
    private List<Character> characters;

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
        JLabel title = new JLabel("YOU SURVIVED.", SwingConstants.CENTER);
        title.setFont(new Font(mainFont, Font.BOLD, 42));
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
        scoresHeader.setFont(new Font(mainFont, Font.BOLD, 16));
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
}
