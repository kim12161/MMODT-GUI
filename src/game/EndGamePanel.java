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
    private boolean showFateImage = false; // 🛠️ NEW: Controls if we show the Fate Hand vs Cinematic Border
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

        // Title (Starts Empty for Dramatic Reveal)
        JLabel title = new JLabel("", SwingConstants.CENTER);
        title.setFont(new Font(bFont, Font.BOLD, 46));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 70, W, 55);
        add(title);

        // Scores header (Starts Empty for Dramatic Reveal)
        JLabel scoresHeader = new JLabel("", SwingConstants.CENTER);
        scoresHeader.setFont(new Font(bFont, Font.PLAIN, 20));
        scoresHeader.setForeground(new Color(220, 60, 60));
        scoresHeader.setBounds(0, 130, W, 25);
        add(scoresHeader);

        // Calculate scores and find best match
        Character bestMatch = null;
        double    bestScore = -1;

        Map<Character, Double> scores = new LinkedHashMap<>();
        for (Character c : characters) {
            Relationship r = player.getRelationship(c);
            double score   = r.calculateFinalScore(player.getCharisma());

            if (score < 0) score = 0; // Prevent negative visual scores

            scores.put(c, score);

            if (score >= bestScore) {
                bestScore = score;
                bestMatch = c;
            }
        }

        // 🛠️ Lists to hold the components so we can reveal them later!
        List<JPanel> profilePanels = new ArrayList<>();
        List<JLabel> scoreLabels = new ArrayList<>();

        // PORTRAIT GRID GENERATION
        int boxSize = 150;
        int gap = 40;
        int totalWidth = (boxSize * scores.size()) + (gap * (scores.size() - 1));
        int currentX = (W - totalWidth) / 2;
        int yPos = 180;

        for (Map.Entry<Character, Double> entry : scores.entrySet()) {
            Character c = entry.getKey();
            double score = entry.getValue();

            // 1. Portrait Frame
            JPanel profilePanel = new JPanel(null) {
                Image profileImg = getProfileImage(c.getName());

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    // Outer border
                    g2.setColor(new Color(230, 220, 210));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Inner background
                    g2.setColor(new Color(40, 30, 25));
                    g2.fillRect(4, 4, getWidth() - 8, getHeight() - 8);

                    // Draw Profile Image
                    if (profileImg != null) {
                        g2.drawImage(profileImg, 4, 4, getWidth() - 8, getHeight() - 8, this);
                    }
                    g2.dispose();
                }
            };
            profilePanel.setOpaque(false);
            profilePanel.setBounds(currentX, yPos, boxSize, boxSize);
            profilePanel.setVisible(false); // 🛠️ Hide initially for the reveal
            add(profilePanel);

            // 2. Percentage Label below portrait
            JLabel scoreLbl = new JLabel(String.format("%.0f%%", score), SwingConstants.CENTER);
            scoreLbl.setFont(new Font(bFont, Font.PLAIN, 16));
            scoreLbl.setForeground(Color.WHITE);
            scoreLbl.setBounds(currentX, yPos + boxSize + 15, boxSize, 25);
            scoreLbl.setVisible(false); // 🛠️ Hide initially for the reveal
            add(scoreLbl);

            // Add to our lists to animate later
            profilePanels.add(profilePanel);
            scoreLabels.add(scoreLbl);

            currentX += boxSize + gap;
        }

        if (bestMatch != null) {
            // CUSTOM IMAGE "VIEW FATE" BUTTON
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
                    Image currentSprite;

                    if (isPressed) {
                        currentSprite = activeImg;
                    } else if (hovered) {
                        currentSprite = hoverImg;
                    } else {
                        currentSprite = defaultImg;
                    }

                    // 1. Draw the button image first
                    if (currentSprite != null) {
                        g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                    }
                    g2.dispose();

                    // 2. PUSH THE TEXT DOWN IF PRESSED
                    if (isPressed) {
                        g.translate(-3, 3);
                    }

                    // 3. Draw the text on top
                    super.paintComponent(g);

                    // 4. Reset the position correctly (Inverse of -3, 3 is 3, -3)
                    if (isPressed) {
                        g.translate(3, -3);
                    }
                }
            };

            viewEndingBtn.setBounds((W - 215) / 2, yPos + boxSize + 90, 200, 60);
            viewEndingBtn.setVisible(false); // 🛠️ Hide initially for the reveal

            // Passes the final match to the sequence thread
            final Character finalMatch = bestMatch;
            final double finalScore = bestScore;
            viewEndingBtn.addActionListener(e -> playEndingSequence(finalMatch, finalScore));

            add(viewEndingBtn);

            // ==========================================
            // 🛠️ THE DRAMATIC REVELATION SEQUENCE
            // ==========================================
            new Thread(() -> {
                try {
                    Thread.sleep(600); // Wait just a moment before starting

                    // 1. Dramatic Typewriter effect for "YOU SURVIVED!"
                    String titleText = "YOU SURVIVED!";
                    for (int i = 1; i <= titleText.length(); i++) {
                        final String partial = titleText.substring(0, i);
                        SwingUtilities.invokeLater(() -> title.setText(partial));
                        Thread.sleep(100); // 0.1 seconds per letter
                    }

                    Thread.sleep(600); // Brief pause for suspense

                    // 2. Dramatic Typewriter effect for "Final Relationship Scores"
                    String headerText = "Final Relationship Scores";
                    for (int i = 1; i <= headerText.length(); i++) {
                        final String partial = headerText.substring(0, i);
                        SwingUtilities.invokeLater(() -> scoresHeader.setText(partial));
                        Thread.sleep(40); // Slightly faster typing
                    }

                    Thread.sleep(1000); // Deep breath before the characters reveal...

                    // 3. Reveal characters one by one with a dramatic pause between them
                    for (int i = 0; i < profilePanels.size(); i++) {
                        final int idx = i;
                        SwingUtilities.invokeLater(() -> {
                            profilePanels.get(idx).setVisible(true);
                            scoreLabels.get(idx).setVisible(true);
                            repaint(); // Force the screen to update immediately
                        });
                        Thread.sleep(800); // Wait almost a full second between each character popping up!
                    }

                    // 4. Wait EXACTLY 1 second after the last character appears
                    Thread.sleep(1000);

                    // 5. Finally, reveal the "View Fate" button
                    SwingUtilities.invokeLater(() -> {
                        viewEndingBtn.setVisible(true);
                        repaint();
                    });

                } catch (Exception e) {}
            }).start();
        }
    }

    // ==========================================
    // HELPER: LOAD PROFILE IMAGE
    // ==========================================
    private Image getProfileImage(String name) {
        String formattedName = name.toLowerCase().replace(" ", "");
        if (formattedName.equals("yubie")) formattedName = "yubi";

        String pathF = "res/sprite/profile/female/" + formattedName + ".png";
        String pathM = "res/sprite/profile/male/" + formattedName + ".png";
        try {
            java.io.File f = null;
            if (new java.io.File(pathF).exists())      f = new java.io.File(pathF);
            else if (new java.io.File(pathM).exists()) f = new java.io.File(pathM);
            if (f == null) return null;

            Image original = new ImageIcon(f.getAbsolutePath()).getImage();

            // Crop to 2x2 ID photo style — take center square of the image
            int origW = original.getWidth(null);
            int origH = original.getHeight(null);

            int cropW = (int)(origW * 0.58);  // take center 60% of width
            int cropH = (int)(origH * 0.43);  // take 45% of height — head to mid-chest
            int cropX = (int)(origW * 0.20);  // skip 20% from left to center
            int cropY = (int)(origH * 0.2);  // skip 12% from top — cuts black space above head

            java.awt.image.BufferedImage cropped = new java.awt.image.BufferedImage(cropW, cropH, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D cg = cropped.createGraphics();
            cg.drawImage(original, 0, 0, cropW, cropH, cropX, cropY, cropX + cropW, cropY + cropH, null);
            cg.dispose();

            return cropped;
        } catch(Exception e) {}
        return null;
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

        revalidate();
        repaint();

        new Thread(() -> {

            // ==========================================
            // 1. FATE SCREEN LOGIC
            // ==========================================
            showFateImage = true; // Tell paintComponent to draw naturally centered, no copper border

            String fateImgPath = bestScore > 60 ? "res/background/ending/fate/true-love-ending.png" : "res/background/ending/fate/parting-ending.png";
            loadImage(fateImgPath);
            repaint();

            String endingTitle = bestScore > 60 ? "TRUE LOVE ENDING" : "PARTING WAYS ENDING";
            String endingLine1 = bestScore > 60 ? "CONGRATULATIONS! You found true love with " + bestMatch.getName() + "!" : "Too bad! Things didn't work out with " + bestMatch.getName() + ".";
            String endingLine2 = bestScore > 60 ? bestMatch.getName() + " — 'Maybe it was fate that brought us together.'" : bestMatch.getName() + " — 'Maybe we aren't meant for each other...'";
            Color titleColor = bestScore > 60 ? new Color(220, 180, 60) : new Color(220, 60, 60);

            JLabel etLabel = new JLabel(endingTitle, SwingConstants.CENTER);
            etLabel.setFont(new Font(bFont, Font.BOLD, 22));
            etLabel.setForeground(titleColor);
            etLabel.setBounds(0, 380, W, 30);

            JLabel el1 = new JLabel(endingLine1, SwingConstants.CENTER);
            el1.setFont(new Font(bFont, Font.PLAIN, 16));
            el1.setForeground(Color.WHITE);
            el1.setBounds(0, 425, W, 25);

            JLabel el2 = new JLabel(endingLine2, SwingConstants.CENTER);
            el2.setFont(new Font(bFont, Font.ITALIC, 14));
            el2.setForeground(new Color(180, 180, 180));
            el2.setBounds(0, 465, W, 25);

            SwingUtilities.invokeLater(() -> {
                add(etLabel);
                add(el1);
                add(el2);
                revalidate();
                repaint();
            });

            sleep(4000); // Wait 4 seconds on Fate Screen

            // Clean up Fate Screen
            SwingUtilities.invokeLater(() -> {
                remove(etLabel);
                remove(el1);
                remove(el2);
            });
            showFateImage = false; // Return to standard Cinematic Mode

            // ==========================================
            // 2. STANDARD CINEMATIC LOGIC
            // ==========================================
            // Add the typing text box to the screen
            setupCinematicTextPane();

            if (bestScore > 60) {
                // ── TRUE LOVE ENDING ──

                // Scene 1: Helicopter
                loadImage("res/background/ending/happy/1-happy-ending.png");
                repaint();

                clearText();
                typeText("The helicopter lifts you away from the chaos below. Beside you, " + bestMatch.getName() + " is finally at peace.", 25);
                sleep(1500);

                clearText();
                typeText("After everything, you made it out, together.", 25);
                sleep(3500);

                // Scene 2: Marriage
                loadImage("res/background/ending/happy/2-happy-ending-" + bestMatch.getName().toLowerCase() + ".png");
                repaint();

                clearText();
                typeText("In a slowly healing world, you and " + bestMatch.getName() + " stand side by side and make a quiet promise.", 25);
                sleep(1500);

                clearText();
                typeText("To keep living, together.", 25);
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

            // ==========================================
            // 🛠️ AUTOMATIC TRANSITION TO CREDITS
            // ==========================================
            sleep(4000); // Wait 4 seconds so the player can read the text

            SwingUtilities.invokeLater(() -> {
                // Get the main GamePanel that holds this EndGamePanel
                Container parent = getParent();
                if (parent != null && parent instanceof main.GamePanel) {
                    main.GamePanel gamePanel = (main.GamePanel) parent;

                    gamePanel.removeAll();
                    gamePanel.setLayout(new BorderLayout());

                    // Automatically load your CreditsPanel!
                    menu.CreditsPanel credits = new menu.CreditsPanel(gamePanel);
                    gamePanel.add(credits, BorderLayout.CENTER);

                    gamePanel.revalidate();
                    gamePanel.repaint();
                }
            });
            // ==========================================

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
        if (inCinematic && currentEndingImg != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (showFateImage) {
                // 🛠️ FATE SCREEN DRAWING: Smaller, centered, natural size
                int iw = currentEndingImg.getWidth(null);
                int ih = currentEndingImg.getHeight(null);

                // Defaults in case it fails to load dimensions
                int imgW = (iw > 0) ? iw : 150;
                int imgH = (ih > 0) ? ih : 150;

                int imgX = (W - imgW) / 2;
                int imgY = 160;

                g2.drawImage(currentEndingImg, imgX, imgY, imgW, imgH, this);

            } else {
                // 🛠️ STANDARD CINEMATIC DRAWING: Large with Copper Border
                int imgW = 642;
                int imgH = 336;
                int imgX = (W - imgW) / 2;
                int imgY = 120;

                g2.drawImage(currentEndingImg, imgX, imgY, imgW, imgH, this);

                // Draw Copper Border around image
                g2.setColor(new Color(200, 140, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(imgX, imgY, imgW, imgH);
            }

            g2.dispose();
        }
    }
}