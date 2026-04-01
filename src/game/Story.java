package game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import Characters.Avy;
import Characters.Marina;
import Characters.Kim;
import Characters.Nathan;
import Characters.Yubie;
import Characters.Adi;
import Player.Gender;
import Player.Player;
import main.GamePanel;
import menu.TitleScreen;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class Story extends JPanel {

    private GamePanel gamePanel;
    private JTextPane readyTextPane;
    private StyledDocument readyDoc;
    private Player player;
    private List<Characters.Character> availableCharacters;
    private int characterIndex = 0;
    private java.util.List<Characters.Character> romanceableCharacters;
    private ConversationManager conversationManager = new ConversationManager();

    // UI Components for the new Story Box
    private JPanel storyBoxPanel;
    private JLabel nameBox;
    private JTextArea dialogue;

    // Background image
    private Image bgImage;
    private Image panelBgImage; // ADDED: Variable for the storyline panel image

    // FONTS
    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    // =========================
    // IMAGE LOADER
    // =========================
    private Image loadResImage(String filePath) {
        java.io.File f = new java.io.File(filePath);
        if (f.exists()) {
            return new ImageIcon(f.getAbsolutePath()).getImage();
        }
        System.err.println("[Story] WARNING: Background image not found -> " + f.getAbsolutePath());
        return null;
    }
    private Image genderPanelImage;

    public Story(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        bgImage = loadResImage("res/background/mainBackground.png");
        // ADDED: Load panel.png from your res/background folder
        panelBgImage = loadResImage("res/background/panel.png");
        genderPanelImage = loadResImage("res/ui/panels/frame-panel.png");

        initializeCharacters();

        setBackground(Color.BLACK);
        setLayout(null); // Use null layout for precise positioning like the example

        // Build the new Story Panel
        createStoryPanel();

        SwingUtilities.invokeLater(this::startIntro);
    }

    // =========================
    // BACKGROUND
    // =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
    // =========================
    // NEW STORY UI PANEL
    // =========================


    //UI EDIT
    //STORYLINE
    private void createStoryPanel() {
        storyBoxPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // CHANGED: Instead of fillRect, we draw your panel.png
                if (panelBgImage != null) {
                    g2.drawImage(panelBgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback if image is missing
                    g2.setColor(new Color(172, 172, 172, 191));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }


                // FIXED: Changed to drawRect for the sharp border
//                g2.setColor(new Color(30, 28, 26, 255));
//                g2.setStroke(new BasicStroke(5f));
//                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                g2.dispose();
                // super.paintComponent(g); // Removed to prevent double painting issues
            }
        };
        storyBoxPanel.setLayout(null);
        storyBoxPanel.setOpaque(false); // CRUCIAL: Tells Swing not to auto-paint the background

        // Positioned at the bottom
        storyBoxPanel.setBounds(40, 380, 700, 160);

        nameBox = new JLabel("STORYLINE");
        nameBox.setFont(new Font(mainFont, Font.BOLD, 18));
        nameBox.setForeground(Color.WHITE);
//        nameBox.setBounds(20, 10, 420, 25);
        nameBox.setBounds(40, 60, 420, 25);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 40, 660, 2);
        sep.setForeground(Color.WHITE);

        dialogue = new JTextArea();
        dialogue.setBounds(20, 90, 660, 100);

        // Ensure the text area itself is completely invisible so the background shows through
        dialogue.setOpaque(false);
        dialogue.setBackground(new Color(0, 0, 0, 0));
        dialogue.setEditable(false);
        dialogue.setLineWrap(true);
        dialogue.setWrapStyleWord(true);
        dialogue.setFont(new Font(bFont, Font.PLAIN, 16));
        dialogue.setForeground(Color.WHITE);

        // Hide the panel initially until startIntro is called
        storyBoxPanel.setVisible(false);

        storyBoxPanel.add(nameBox);
        storyBoxPanel.add(sep);
        storyBoxPanel.add(dialogue);

        add(storyBoxPanel);

        // Resize listener to keep the box centered at the bottom
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int boxW = Math.min(getWidth() - 80, 800);
//                int boxH = 160;
                int boxH = 250;
//                int boxX = (getWidth() - boxW) / 2;
//                int boxY = getHeight() - boxH - 40;

                // CENTER HORIZONTALLY
                int boxX = (getWidth() - boxW) / 2;

                // ==========================================
                // CENTER VERTICALLY
                // Changed from (getHeight() - boxH - 40) to just divide by 2
                // ==========================================
                int boxY = (getHeight() - boxH) / 2;
                storyBoxPanel.setBounds(boxX, boxY, boxW, boxH);

//                sep.setBounds(20, 40, boxW - 40, 2);
//                dialogue.setBounds(20, 50, boxW - 40, boxH - 60);


                sep.setBounds(40, 60, boxW - 80, 2);
                //dialogue placement
                dialogue.setBounds(40, 120, boxW - 80, boxH - 110);
            }
        });
    }

    // ... (rest of the code is unchanged) ...
    // =========================
    // TYPE TEXT (Updated for JTextArea)
    // =========================
    private void typeText(String text, int delay) {
        for (char c : text.toCharArray()) {
            SwingUtilities.invokeLater(() -> dialogue.append(String.valueOf(c)));
            try {
                Thread.sleep(delay);
            } catch (Exception ignored) {}
        }
    }

    private void clearText() {
        SwingUtilities.invokeLater(() -> dialogue.setText(""));
    }

    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }

    // =========================
    // INTRO STORY
    // =========================
    public void startIntro() {

        // Make the panel visible when the story starts
        SwingUtilities.invokeLater(() -> storyBoxPanel.setVisible(true));

        new Thread(() -> {

            clearText();

            // Note: JTextArea doesn't support multiple colors in the same document easily like JTextPane does.
            // If the color changes (like making "3-0" red) are crucial, you will need to revert back to JTextPane inside the new box layout.
            // For now, it types it all in white to match the screenshot style.

            typeText("You're 28 years old, two years away from the big 3-0, and by all accounts, you've been living the good life. ", 20);
            typeText("A stable career, your own cozy apartment, financial freedom, everything you once dreamed of, you achieved. ", 20);

            pause(1500);
            clearText();

            typeText("But at your college reunion, reality hit differently... \n", 20);
            pause(1000);
            typeText("Everyone showed up with partners; some even announcing engagements or babies. ", 20);
            typeText("Surrounded by talks of weddings and settling down, you realized something: ", 20);
            pause(800);
            typeText("\nYou had built the perfect life, but never found love.", 60);

            pause(2500);
            clearText();

            typeText("That night, you decided to add one last item to your bucket list: ", 20);
            typeText("Find love before 30. Maybe even get married. \n\n", 40);
            typeText("Except, fate had other plans.", 20);

            pause(2500);
            clearText();

            typeText("The very next week, the world Spira collapsed into chaos. ", 20);
            typeText("A mysterious infection spread across the city, turning people into ravenous monsters. \n", 20);
            typeText("Society crumbled, survival became the priority... yet, in the middle of it all, your bucket list remained the same.", 20);

            pause(2500);
            clearText();

            typeText("Sure, the apocalypse has begun. But you? \n", 20);
            typeText("You're determined to find a partner before the world ends. ", 40);
            typeText("Because love might be the thing worth surviving for.", 20);

            pause(3000);
            clearText();

            typeText("This is where your story begins.", 60);

            pause(3000);

            // Hide the story box before moving to gender selection
            SwingUtilities.invokeLater(() -> storyBoxPanel.setVisible(false));
            startGenderSelection();

        }).start();
    }

    // =========================
    // PLAYER SETUP
    // =========================

    private void initializeCharacters() {

        availableCharacters = new ArrayList<>();

        availableCharacters.add(new Avy());
        availableCharacters.add(new Marina());
        availableCharacters.add(new Kim());
        availableCharacters.add(new Nathan());
        availableCharacters.add(new Yubie());
        availableCharacters.add(new Adi());
    }

    private void setupPlayer(String genderInput) {

        removeAll();
        repaint();

        String name = JOptionPane.showInputDialog(this, "Enter Your Name:");

        if (name == null || name.isEmpty())
            name = "Survivor";

        Gender playerGender = genderInput.equals("M") ? Gender.MALE : Gender.FEMALE;

        player = new Player(name, 100, playerGender);

        JOptionPane.showMessageDialog(this, "Welcome, " + name + "!");

        filterRomanceable();
        showMeetCharactersTitle();
    }

    // =========================
    // MEET THE CHARACTERS
    // =========================

    private void showMeetCharactersTitle() {

        removeAll();
        setLayout(null);

        // Dark overlay
        JPanel overlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, Math.max(getWidth(), 800), Math.max(getHeight(), 600));

        JLabel title = new JLabel("MEET THE CHARACTERS", SwingConstants.CENTER);
        title.setFont(new Font(mainFont, Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 0, Math.max(getWidth(), 800), Math.max(getHeight(), 600));

        overlay.add(title);
        add(overlay);

        // Resize listener so title stays centered
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                overlay.setBounds(0, 0, getWidth(), getHeight());
                title.setBounds(0, 0, getWidth(), getHeight());
                revalidate();
                repaint();
            }
        });

        revalidate();
        repaint();

        new Thread(() -> {
            pause(2500);
            showNextCharacter();
        }).start();
    }

    private void showNextCharacter() {

        if (characterIndex >= romanceableCharacters.size()) {
            return;
        }

        Characters.Character c = romanceableCharacters.get(characterIndex);

        SwingUtilities.invokeLater(() -> {
            removeAll();
            setLayout(new BorderLayout());

            CharacterScene scene = new CharacterScene(c, getCharacterImage(c.getName()));
            add(scene, BorderLayout.CENTER);

            revalidate();
            repaint();
        });

        characterIndex++;
        waitForEnter();
    }

    private String getCharacterImage(String name) {

        switch (name) {
            case "Avy":    return "sprite/avy.png";
            case "Marina": return "sprite/marina.png";
            case "Kim":    return "sprite/kim.png";
            case "Nathan": return "sprite/nathan.png";
            case "Yubie":  return "sprite/yubie.png";
            case "Adi":    return "sprite/adi.png";
            default:       return "sprite/zombie.png";
        }
    }

    private void filterRomanceable() {

        romanceableCharacters = new ArrayList<>();

        for (Characters.Character c : availableCharacters) {
            if (c.getGender() != player.getGender()) {
                romanceableCharacters.add(c);
            }
        }

        characterIndex = 0;
    }

    // =========================
    // GENDER SELECTION
    // =========================

    private void startGenderSelection() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            setLayout(null);

            // 1. LOCK THE DIMENSIONS to prevent stretching
            final Dimension cardSize = new Dimension(430, 290);

            JPanel card = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    // Keeps pixel art crisp on your laptop
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    if (genderPanelImage != null) {
                        // 2. DRAW at the fixed dimension, not the stretched panel size
                        g2.drawImage(genderPanelImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g2.setColor(new Color(60, 55, 50, 220));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    }
                    g2.dispose();
                }
            };
            card.setOpaque(false);

            // 3. APPLY the fixed dimensions to the panel
            card.setPreferredSize(cardSize);
            card.setMinimumSize(cardSize);
            card.setMaximumSize(cardSize);

            // Centering logic
            int cardX = (getWidth() - cardSize.width) / 2;
            int cardY = (getHeight() - cardSize.height) / 2;
            card.setBounds(cardX, cardY, cardSize.width, cardSize.height);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.NONE;

            // CHOOSE GENDER Title
            JLabel title = new JLabel("CHOOSE GENDER", SwingConstants.CENTER);
            title.setFont(new Font(mainFont, Font.BOLD, 26));
            title.setForeground(Color.WHITE);
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 15, 0); // Larger gap to match the square look
            card.add(title, gbc);

            // MALE Button (Matches your 190x60 TitleScreen size)
            JButton maleBtn = createGenderButton("MALE");
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 10, 0);
            card.add(maleBtn, gbc);

            // FEMALE Button
            JButton femaleBtn = createGenderButton("FEMALE");
            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            card.add(femaleBtn, gbc);

            add(card);

            // Resize listener to keep it centered
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    card.setBounds((getWidth() - cardSize.width) / 2,
                            (getHeight() - cardSize.height) / 2,
                            cardSize.width, cardSize.height);
                }
            });

            maleBtn.addActionListener(e -> setupPlayer("M"));
            femaleBtn.addActionListener(e -> setupPlayer("F"));

            revalidate();
            repaint();
        });
    }

    // UI EDIT //GENDER BUTTON
    // UI EDIT //GENDER BUTTON
    // ==========================================
    // UI EDIT //GENDER BUTTON (MODIFIED FOR TEXTURES)
//     ==========================================
    // ==========================================
    // UI EDIT //GENDER BUTTON (FIXED FILENAMES)
    // ==========================================
    private JButton createGenderButton(String text) {
        // Exact paths from your screenshot
        Image btnNormal = loadResImage("res/ui/icon/button2-not-active.png");
        Image btnActive = loadResImage("res/ui/icon/button2-active.png");

        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                // LOCK THE SIZE to 200x60 just like your TitleScreen
                Dimension size = new Dimension(190, 60);
                setPreferredSize(size);
                setMinimumSize(size);
                setMaximumSize(size);

                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(new Font(bFont, Font.BOLD, 13));
                setForeground(Color.WHITE);

                // Center text on the sprite
                setHorizontalTextPosition(JButton.CENTER);
                setVerticalTextPosition(JButton.CENTER);

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Pixel-perfect rendering for your laptop
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                Image currentImg = (hovered || getModel().isPressed()) ? btnActive : btnNormal;

                if (currentImg != null) {
                    // Stretch the 80x24 sprite to fit the 200x60 button area perfectly
                    g2.drawImage(currentImg, 0, 0, getWidth(), getHeight(), null);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    // =========================
    // WAIT FOR ENTER
    // =========================

    private void waitForEnter() {

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {

                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e) {

                        if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {

                            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                                    .removeKeyEventDispatcher(this);

                            if (characterIndex < romanceableCharacters.size()) {
                                showNextCharacter();
                            } else {
                                startLevelConfirmation();
                            }
                            return true;
                        }

                        return false;
                    }
                });
    }
    // UI EDIT //LEVEL CONFIRMATION
    private void startLevelConfirmation() {

        SwingUtilities.invokeLater(() -> {

            removeAll();
            setLayout(null);

            // Frosted glass card
            JPanel card = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
                    g2.setColor(new Color(172, 172, 172, 191));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(62, 55, 49, 255));
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.dispose();
                }
            };
            card.setOpaque(false);

            int cardW = 380, cardH = 200;
            int cardX = (getWidth() - cardW) / 2;
            int cardY = (getHeight() - cardH) / 2;
            card.setBounds(cardX, cardY, cardW, cardH);

            JLabel title = new JLabel("ARE YOU READY?", SwingConstants.CENTER);
            title.setFont(new Font(mainFont, Font.BOLD, 26));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 30, cardW, 40);

            JLabel subtitle = new JLabel("Your future lies in your hands", SwingConstants.CENTER);
            subtitle.setFont(new Font(bFont, Font.PLAIN, 13));
            subtitle.setForeground(Color.WHITE);
            subtitle.setBounds(0, 75, cardW, 25);

            JButton yesBtn = createGenderButton("YES");
            JButton noBtn  = createGenderButton("NO");

            int btnW = 110, btnH = 38, gap = 20;
            int startX = (cardW - (btnW * 2 + gap)) / 2;

            yesBtn.setBounds(startX, 130, btnW, btnH);
            noBtn.setBounds(startX + btnW + gap, 130, btnW, btnH);

            card.add(title);
            card.add(subtitle);
            card.add(yesBtn);
            card.add(noBtn);

            add(card);

            // Keep card centered on resize
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    card.setBounds(
                            (getWidth()  - cardW) / 2,
                            (getHeight() - cardH) / 2,
                            cardW, cardH
                    );
                    revalidate();
                    repaint();
                }
            });

            yesBtn.addActionListener(e -> {

                ScenePanel scenePanel = new ScenePanel(
                        player,
                        romanceableCharacters,
                        conversationManager
                );

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

                if (frame != null) {
                    frame.getContentPane().removeAll();
                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(scenePanel, BorderLayout.CENTER);
                    frame.revalidate();
                    frame.repaint();
                }

                scenePanel.startGame();
            });

            noBtn.addActionListener(e -> showNoScreen());

            revalidate();
            repaint();
        });
    }
    private void typewriteInner(JLabel label, String text, int delayMs) {
        for (int i = 1; i <= text.length(); i++) {
            final String partial = text.substring(0, i);
            SwingUtilities.invokeLater(() -> label.setText(partial));
            pause(delayMs);
        }
    }
    private void showNoScreen() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            setLayout(null);

            JPanel overlay = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            overlay.setOpaque(false);
            overlay.setBounds(0, 0, Math.max(getWidth(), 800), Math.max(getHeight(), 600));

            JLabel title = new JLabel("", SwingConstants.CENTER);
            title.setFont(new Font(mainFont, Font.BOLD, 48));
            title.setForeground(Color.WHITE);
            title.setBounds(0, Math.max(getHeight(), 600) / 2 - 60, Math.max(getWidth(), 800), 60);

            JLabel subtitle = new JLabel("", SwingConstants.CENTER);
            subtitle.setFont(new Font(bFont, Font.PLAIN, 25));
            subtitle.setForeground(Color.RED);
            subtitle.setBounds(0, Math.max(getHeight(), 600) / 2, Math.max(getWidth(), 800), 40);

            overlay.add(title);
            overlay.add(subtitle);
            add(overlay);

            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    overlay.setBounds(0, 0, getWidth(), getHeight());
                    title.setBounds(0, getHeight() / 2 - 60, getWidth(), 60);
                    subtitle.setBounds(0, getHeight() / 2, getWidth(), 40);
                    revalidate();
                    repaint();
                }
            });

            revalidate();
            repaint();

            new Thread(() -> {
                pause(500);
                typewriteInner(title, "The world awaits for no one...", 55);
                pause(1000);
                typewriteInner(subtitle, "You chose to stay behind.", 55);
                pause(3000);
                SwingUtilities.invokeLater(() -> {
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    if (frame != null) {
                        frame.getContentPane().removeAll();
                        new TitleScreen(frame.getContentPane(), gamePanel);
                        frame.revalidate();
                        frame.repaint();
                    }
                });
            }).start();
        });
    }

    private void typeReadyText(String text, Color color, int delay) {

        Style style = readyTextPane.addStyle("ready", null);
        StyleConstants.setForeground(style, color);

        SwingUtilities.invokeLater(() -> {
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            readyDoc.setParagraphAttributes(0, readyDoc.getLength(), center, false);
        });

        for (char c : text.toCharArray()) {

            SwingUtilities.invokeLater(() -> {
                try {
                    readyDoc.insertString(readyDoc.getLength(), String.valueOf(c), style);
                } catch (Exception ignored) {}
            });

            try {
                Thread.sleep(delay);
            } catch (Exception ignored) {}
        }
    }

}