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

    // FONT
    private String bFont = "Munro";

    private Player player;
    private List<Character> characters;

    // ==============================
    // CINEMATIC VARIABLES
    // ==============================
    private boolean inCinematic = false;
    private float profileAlpha = 0f;
    private boolean showFateImage = false;
    private Image currentEndingImg = null;
    private float imageAlpha = 0f;

    private JTextPane dialogue;

    public EndGamePanel(Player player, List<Character> characters) {
        this.player = player;
        this.characters = characters;


        MusicManager.playBGM(MusicManager.BGM_ENDING);

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
        JLabel title = new JLabel("", SwingConstants.CENTER);
        title.setFont(new Font(bFont, Font.BOLD, 46));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 70, W, 55);
        add(title);

        // Scores header
        JLabel scoresHeader = new JLabel("", SwingConstants.CENTER);
        scoresHeader.setFont(new Font(bFont, Font.PLAIN, 20));
        scoresHeader.setForeground(new Color(220, 60, 60));
        scoresHeader.setBounds(0, 130, W, 25);
        add(scoresHeader);

        // Calculate scores and find best match
        Character bestMatch = null;
        double bestScore = -1;

        Map<Character, Double> scores = new LinkedHashMap<>();
        for (Character c : characters) {
            Relationship r = player.getRelationship(c);
            double score = r.calculateFinalScore(player.getCharisma());

            if (score < 0) score = 0;
            scores.put(c, score);

            if (score >= bestScore) {
                bestScore = score;
                bestMatch = c;
            }
        }

        List<JPanel> profilePanels = new ArrayList<>();
        List<JLabel> scoreLabels = new ArrayList<>();

        // PORTRAIT GRID GENERATION
        int boxSize = 191;
        int gap = 30;
        int totalWidth = (boxSize * scores.size()) + (gap * (scores.size() - 1));
        int currentX = (W - totalWidth) / 2;
        int yPos = 180;

        for (Map.Entry<Character, Double> entry : scores.entrySet()) {
            Character c = entry.getKey();
            double score = entry.getValue();

            // 1. Portrait Frame (No borders, centered fit)
            JPanel profilePanel = new JPanel(null) {
                Image profileImg = getProfileImage(c.getName());

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (profileImg == null) return;

                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, profileAlpha));
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    int imgW = profileImg.getWidth(null);
                    int imgH = profileImg.getHeight(null);

                    // Scale to fit fully inside the 191x191 box without cropping
                    double scale = Math.min((double)getWidth() / imgW, (double)getHeight() / imgH);
                    int drawW = (int)(imgW * scale);
                    int drawH = (int)(imgH * scale);

                    int drawX = (getWidth() - drawW) / 2;
                    int drawY = (getHeight() - drawH) / 2;

                    g2.drawImage(profileImg, drawX, drawY, drawW, drawH, this);
                    g2.dispose();
                }
            };
            profilePanel.setOpaque(false);
            profilePanel.setBounds(currentX, yPos, boxSize, boxSize);
            profilePanel.setVisible(false);
            add(profilePanel);

            // 2. Percentage Label
            JLabel scoreLbl = new JLabel(String.format("%.0f%%", score), SwingConstants.CENTER);
            scoreLbl.setFont(new Font(bFont, Font.PLAIN, 16));
            scoreLbl.setForeground(Color.WHITE);
            scoreLbl.setBounds(currentX, yPos + boxSize + 15, boxSize, 25);
            scoreLbl.setVisible(false);
            add(scoreLbl);

            profilePanels.add(profilePanel);
            scoreLabels.add(scoreLbl);

            currentX += boxSize + gap;
        }

        if (bestMatch != null) {
            JButton viewEndingBtn = new JButton("View Fate") {
                Image defaultImg, hoverImg, activeImg;
                boolean hovered = false;

                {
                    try {
                        defaultImg = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-not-active.png").getImage();
                        hoverImg = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-hover.png").getImage();
                        activeImg = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-active.png").getImage();
                    } catch (Exception e) {}

                    setOpaque(false);
                    setContentAreaFilled(false);
                    setBorderPainted(false);
                    setFocusPainted(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setFont(new Font(bFont, Font.PLAIN, 16));
                    setForeground(Color.WHITE);
                    setBorder(new javax.swing.border.EmptyBorder(6, 10, 0, 0));

                    addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                        public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    boolean isPressed = getModel().isPressed();
                    Image currentSprite = isPressed ? activeImg : (hovered ? hoverImg : defaultImg);

                    if (currentSprite != null) g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();

                    if (isPressed) g.translate(-3, 3);
                    super.paintComponent(g);
                    if (isPressed) g.translate(3, -3);
                }
            };

            viewEndingBtn.setBounds((W - 215) / 2, yPos + boxSize + 90, 200, 60);
            viewEndingBtn.setVisible(false);

            final Character finalMatch = bestMatch;
            final double finalScore = bestScore;
            viewEndingBtn.addActionListener(e -> playEndingSequence(finalMatch, finalScore));
            add(viewEndingBtn);

            new Thread(() -> {
                try {
                    // 1. Reveal "YOU SURVIVED!"
                    Thread.sleep(600);
                    String titleText = "YOU SURVIVED!";
                    for (int i = 1; i <= titleText.length(); i++) {
                        final String partial = titleText.substring(0, i);
                        SwingUtilities.invokeLater(() -> title.setText(partial));
                        Thread.sleep(100);
                    }

                    // 2. Reveal "Final Relationship Scores"
                    Thread.sleep(600);
                    String headerText = "Final Relationship Scores";
                    for (int i = 1; i <= headerText.length(); i++) {
                        final String partial = headerText.substring(0, i);
                        SwingUtilities.invokeLater(() -> scoresHeader.setText(partial));
                        Thread.sleep(40);
                    }

                    Thread.sleep(1000);

                    // 3. START FADE EFFECT (This starts the alpha increasing)
                    fadeInProfiles();

                    // 4. REVEAL PORTRAITS 1, 2, 3 (The images)
                    for (int i = 0; i < profilePanels.size(); i++) {
                        final int idx = i;
                        SwingUtilities.invokeLater(() -> {
                            profilePanels.get(idx).setVisible(true);
                            repaint();
                        });
                        Thread.sleep(800); // Delay between each portrait appearing
                    }

                    // Wait a small moment for the last portrait to finish its fade
                    Thread.sleep(500);

                    // 5. REVEAL PERCENTAGES 1, 2, 3 (The text labels)
                    for (int i = 0; i < scoreLabels.size(); i++) {
                        final int idx = i;
                        SwingUtilities.invokeLater(() -> {
                            scoreLabels.get(idx).setVisible(true);
                            repaint();
                        });
                        Thread.sleep(500); // Delay between each percentage appearing
                    }

                    // 6. FINALLY SHOW BUTTON
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> {
                        viewEndingBtn.setVisible(true);
                        repaint();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private Image getProfileImage(String name) {
        // Standardize the name to lowercase and remove spaces
        String formattedName = name.toLowerCase().replace(" ", "");

        // 🛠️ FIX FILENAME MISMATCHES HERE
        if (formattedName.equals("yubie")) formattedName = "yubi";
        if (formattedName.equals("marina")) formattedName = "mariana"; // Matches your 'mariana.png' file
        // Ensure 'kim.png' exists in your folder; if it's named differently, add a check here!

        String pathF = "res/sprite/profile/female/" + formattedName + ".png";
        String pathM = "res/sprite/profile/male/" + formattedName + ".png";

        try {
            java.io.File f = null;
            if (new java.io.File(pathF).exists()) f = new java.io.File(pathF);
            else if (new java.io.File(pathM).exists()) f = new java.io.File(pathM);

            if (f != null) {
                return new ImageIcon(f.getAbsolutePath()).getImage();
            } else {
                // Log for debugging so you know which path failed
                System.out.println("Could not find profile image for: " + formattedName);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupCinematicTextPane() {
        if (dialogue == null) {
            dialogue = new JTextPane();
            int imgW = 642, imgH = 336, imgY = 80;
            int textY = imgY + imgH + 60;
            dialogue.setBounds((W - imgW) / 2+2, textY, imgW, 150);
            dialogue.setOpaque(false);
            dialogue.setBackground(new Color(0, 0, 0, 0));
            dialogue.setEditable(false);
            dialogue.setFont(new Font(bFont, Font.PLAIN, 18));
            dialogue.setForeground(Color.WHITE);

            // --- ADD THIS SECTION FOR LINE SPACING ---
            MutableAttributeSet set = new SimpleAttributeSet();
            // 0.4f adds extra space between the lines
            StyleConstants.setLineSpacing(set, 0.3f);
            dialogue.setParagraphAttributes(set, false);
            // -----------------------------------------

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

    private void playEndingSequence(Character bestMatch, double bestScore) {
        removeAll();
        inCinematic = true;
        revalidate();
        repaint();

        new Thread(() -> {
            // ==========================================
            // 1. FATE SCREEN REVELATION SEQUENCE
            // ==========================================
            showFateImage = true;
            String fateImgPath = bestScore > 60 ? "res/background/ending/fate/true-love-ending.png" : "res/background/ending/fate/parting-ending.png";
            loadImage(fateImgPath);
            fadeInImage();

            // PHASE 1: Show the Image first
            SwingUtilities.invokeLater(this::repaint);
            sleep(1000); // Wait for the player to see the image

            String endingTitle = bestScore > 60 ? "TRUE LOVE ENDING" : "PARTING WAYS ENDING";
            String endingLine1 = bestScore > 60 ? "CONGRATULATIONS! You found true love with " + bestMatch.getName() + "!" : "Too bad! Things didn't work out with " + bestMatch.getName() + ".";
            String endingLine2 = bestScore > 60 ? bestMatch.getName() + " — 'Maybe it was fate that brought us together.'" : bestMatch.getName() + " — 'Maybe we aren't meant for each other...'";
            Color titleColor = bestScore > 60 ? new Color(220, 180, 60) : new Color(220, 60, 60);

            JLabel etLabel = new JLabel(endingTitle, SwingConstants.CENTER);
            etLabel.setFont(new Font(bFont, Font.BOLD, 22));
            etLabel.setForeground(titleColor);
            etLabel.setBounds(0, 360, W, 30);
            etLabel.setVisible(false); // Start hidden

            JLabel el1 = new JLabel(endingLine1, SwingConstants.CENTER);
            el1.setFont(new Font(bFont, Font.PLAIN, 16));
            el1.setForeground(Color.WHITE);
            el1.setBounds(0, 400, W, 25);
            el1.setVisible(false); // Start hidden

            JLabel el2 = new JLabel(endingLine2, SwingConstants.CENTER);
            el2.setFont(new Font(bFont, Font.ITALIC, 14));
            el2.setForeground(new Color(180, 180, 180));
            el2.setBounds(0, 435, W, 25);
            el2.setVisible(false); // Start hidden

            // Add them all to the panel
            SwingUtilities.invokeLater(() -> {
                add(etLabel);
                add(el1);
                add(el2);
                revalidate();
            });

            // PHASE 2: Reveal the Heading (TRUE LOVE / PARTING WAYS)
            sleep(800);
            SwingUtilities.invokeLater(() -> etLabel.setVisible(true));

            // PHASE 3: Reveal the Sub-heading (Congratulations / Too bad)
            sleep(800);
            SwingUtilities.invokeLater(() -> el1.setVisible(true));

            // PHASE 4: Reveal the Last line (The Character Quote)
            sleep(800);
            SwingUtilities.invokeLater(() -> el2.setVisible(true));

            sleep(4000); // Total viewing time

            // Clean up Fate Screen
            SwingUtilities.invokeLater(() -> {
                remove(etLabel);
                remove(el1);
                remove(el2);
            });
            showFateImage = false;

            setupCinematicTextPane();
            if (bestScore > 60) {
                loadImage("res/background/ending/happy/1-happy-ending.png");
                fadeInImage();
                repaint();
                clearText();
                typeText("The helicopter lifts you away from the chaos below. Beside you, " + bestMatch.getName() + " is finally at peace.", 25);
                sleep(1500);
                clearText();
                typeText("After everything, you made it out, together.", 25);
                sleep(3500);

                loadImage("res/background/ending/happy/2-happy-ending-" + bestMatch.getName().toLowerCase() + ".png");
                fadeInImage();
                repaint();
                clearText();
                typeText("In a slowly healing world, you and " + bestMatch.getName() + " stand side by side and make a quiet promise.", 25);
                sleep(1500);
                clearText();
                typeText("To keep living, together.", 25);
                sleep(3500);
            } else {
                boolean isFemale = player.getGender() == Gender.FEMALE;
                String pronoun = isFemale ? "him" : "her";
                String imgPath = isFemale ? "res/background/ending/bad/bad-fem.png" : "res/background/ending/bad/bad-male.png";
                loadImage(imgPath);
                fadeInImage();
                repaint();
                clearText();
                typeText("As the shadows close in and teeth find their mark, you watch " + pronoun + " disappear into the fading light without a single glance back.", 25);
                sleep(3500);
                clearText();
                typeText("The promise of 'forever' dissolves into a final, lonely breath as they choose the horizon, leaving you behind to the hunger of the dark.", 25);
                sleep(3500);
            }

            loadImage("res/background/ending/happy/happy-ending-ty.png");
            fadeInImage();
            repaint();
            clearText();
            SwingUtilities.invokeLater(() -> {
                dialogue.setFont(new Font(bFont, Font.BOLD, 26));
                dialogue.setForeground(new Color(200, 200, 200));
                StyledDocument doc = dialogue.getStyledDocument();
                SimpleAttributeSet leftAlign = new SimpleAttributeSet();
                StyleConstants.setAlignment(leftAlign, StyleConstants.ALIGN_LEFT);
                doc.setParagraphAttributes(0, doc.getLength(), leftAlign, false);
            });
            typeText("                        THANK YOU FOR PLAYING!", 50);

            sleep(4000);
            sleep(4000);
            SwingUtilities.invokeLater(() -> {
                // Walk up the parent chain to find GamePanel
                Container current = getParent();
                main.GamePanel gamePanel = null;

                while (current != null) {
                    System.out.println("Checking parent: " + current.getClass().getName());
                    if (current instanceof main.GamePanel) {
                        gamePanel = (main.GamePanel) current;
                        break;
                    }
                    current = current.getParent();
                }

                if (gamePanel != null) {
                    final main.GamePanel gp = gamePanel;
                    gp.removeAll();
                    gp.setLayout(new BorderLayout());
                    menu.CreditsPanel credits = new menu.CreditsPanel(gp);
                    gp.add(credits, BorderLayout.CENTER);
                    gp.revalidate();
                    gp.repaint();
                } else {
                    // Fallback — try going through the JFrame directly
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(EndGamePanel.this);
                    if (frame != null) {
                        frame.getContentPane().removeAll();
                        main.GamePanel gp2 = new main.GamePanel("res/background/main-background.gif");
                        gp2.setLayout(new BorderLayout());
                        menu.CreditsPanel credits = new menu.CreditsPanel(gp2);
                        gp2.add(credits, BorderLayout.CENTER);
                        frame.setContentPane(gp2);
                        frame.revalidate();
                        frame.repaint();
                    }
                }
            });
        }).start();
    }
    private void fadeInProfiles() {
        profileAlpha = 0f;
        javax.swing.Timer timer = new javax.swing.Timer(30, null);
        timer.addActionListener(e -> {
            profileAlpha += 0.05f;
            if (profileAlpha >= 1.0f) {
                profileAlpha = 1.0f;
                timer.stop();
            }
            repaint(); // Refreshes portraits and labels
        });
        timer.start();
    }

    private void loadImage(String path) {
        try {
            java.io.File f = new java.io.File(path);
            if (f.exists()) currentEndingImg = new ImageIcon(f.getAbsolutePath()).getImage();
            else currentEndingImg = null;
        } catch (Exception e) {}
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }

    private void fadeInImage() {
        imageAlpha = 0f; // Reset to invisible
        javax.swing.Timer timer = new javax.swing.Timer(30, null);
        timer.addActionListener(e -> {
            imageAlpha += 0.05f; // Increase visibility
            if (imageAlpha >= 1.0f) {
                imageAlpha = 1.0f;
                timer.stop();
            }
            repaint();
        });
        timer.start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inCinematic && currentEndingImg != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, imageAlpha));

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (showFateImage) {
                int iw = currentEndingImg.getWidth(null), ih = currentEndingImg.getHeight(null);
                int imgW = (iw > 0) ? iw : 150, imgH = (ih > 0) ? ih : 150;
                g2.drawImage(currentEndingImg, (W - imgW) / 2, 120, imgW, imgH, this);
            } else {
                int imgW = 642, imgH = 336, imgX = (W - imgW) / 2, imgY = 120;
                g2.drawImage(currentEndingImg, imgX, imgY, imgW, imgH, this);
                g2.setColor(new Color(200, 140, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(imgX, imgY, imgW, imgH);
            }
            g2.dispose();
        }
    }
}