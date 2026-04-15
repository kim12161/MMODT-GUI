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

    // UI Components
    private JPanel storyBoxPanel;
    private JLabel nameBox;
    private JTextPane dialogue; // Swapped back to JTextPane for cinematic formatting

    // Background image
    private Image bgImage;
    private Image panelBgImage;
    private Image genderPanelImage;
    private Image textPanelImage;
    private Image chainsImage;

    // Cinematic Storyline Fields
    private Image currentStoryImage = null;
    private Image[] storylineImages = new Image[5];
    private boolean isStorylineActive = true;

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

    public Story(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        // Load the 5 storyline images
        for (int i = 0; i < 5; i++) {
            storylineImages[i] = loadResImage("res/background/storyline/storyline-" + (i + 1) + ".png");
        }

        bgImage = loadResImage("res/background/main-background.gif");
        panelBgImage = loadResImage("res/background/panel.png");
        genderPanelImage = loadResImage("res/ui/panels/frame-panel.png");
        textPanelImage = loadResImage("res/ui/panels/text-panel.png");
        chainsImage = loadResImage("res/ui/icon/assets/chains.png");

        initializeCharacters();

        setBackground(Color.BLACK);
        setLayout(null);

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
        Graphics2D g2 = (Graphics2D) g;

        if (isStorylineActive) {
            // BLACK BACKGROUND FOR STORYLINE ONLY
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // DRAW CENTERED STORY IMAGE (642 x 336)
            if (currentStoryImage != null) {
                int imgW = 642;
                int imgH = 336;
                int x = (getWidth() - imgW) / 2;
                int y = 120; // Positioned upper-middle

                // Subtle black shadow behind image for depth
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(x - 4, y - 4, imgW + 8, imgH + 8);

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(currentStoryImage, x, y, imgW, imgH, this);
            }
        } else {
            // MAIN GIF FOR GENDER SELECTION & BEYOND
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // =========================
    // NEW STORY UI PANEL
    // =========================
    private void createStoryPanel() {
        storyBoxPanel = new JPanel();
        storyBoxPanel.setLayout(null);
        storyBoxPanel.setOpaque(false);

        // Aligned perfectly with the 642px image
        int imgW = 642;
        int imgX = (getWidth() - imgW) / 2;
        int imgH = 336;
        int imgY = 120;

        int boxX = 900;

        int boxY = imgY + imgH + 20;

        storyBoxPanel.setBounds(boxX, boxY, imgW, 200);

        // Position directly under the image width (with a 20px gap)
//        storyBoxPanel.setBounds(imgX, imgY + imgH + 20, imgW, 200);

        dialogue = new JTextPane();
        dialogue.setBounds(0, 0, imgW, 200);
        dialogue.setOpaque(false);
        dialogue.setBackground(new Color(0, 0, 0, 0));
        dialogue.setEditable(false);
        dialogue.setFont(new Font(bFont, Font.PLAIN, 18));
        dialogue.setForeground(Color.WHITE);
        // Default JTextPane alignment is LEFT, no StyleConstants needed.

        storyBoxPanel.add(dialogue);
        add(storyBoxPanel);

        // Ensure text stays aligned if the window resizes
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int newX = (getWidth() - 642) / 2;
                storyBoxPanel.setBounds(newX, 120 + 336 + 20, 642, 200);
            }
        });
    }

    // =========================
    // TYPE TEXT
    // =========================
    private void typeText(String text, int delay) {
        for (char c : text.toCharArray()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Document doc = dialogue.getDocument();
                    doc.insertString(doc.getLength(), String.valueOf(c), null);
                } catch (BadLocationException ignored) {}
            });
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
        SwingUtilities.invokeLater(() -> storyBoxPanel.setVisible(true));

        new Thread(() -> {
            clearText();

            // Slide 1
            currentStoryImage = storylineImages[0];
            repaint();
            typeText("You're 28 years old, two years away from the big 3-0, and by all accounts, you've been living the good life. A stable career, your own cozy apartment, financial freedom, everything you once dreamed of, you achieved.", 20);
            pause(2000);
            clearText();

            // Slide 2
            currentStoryImage = storylineImages[1];
            repaint();
            typeText("But at your college reunion, reality hit differently... Everyone showed up with partners; some even announcing engagements or babies. Surrounded by talks of weddings and settling down, you realized something: You had built the perfect life, but never found love.", 20);
            pause(2000);
            clearText();

            // Slide 3
            currentStoryImage = storylineImages[2];
            repaint();
            typeText("That night, you decided to add one last item to your bucket list: Find love before 30. Maybe even get married. Except, fate had other plans.", 20);
            pause(2000);
            clearText();

            // Slide 4
            currentStoryImage = storylineImages[3];
            repaint();
            typeText("The very next week, the world Spira collapsed into chaos. A mysterious infection spread across the city, turning people into ravenous monsters. Society crumbled, survival became the priority... yet, in the middle of it all, your bucket list remained the same.", 20);
            pause(2000);
            clearText();

            // Slide 5
            currentStoryImage = storylineImages[4];
            repaint();
            typeText("Sure, the apocalypse has begun. But you? You're determined to find a partner before the world ends. Because love might be the thing worth surviving for.\n\nThis is where your story begins.", 20);
            pause(3000);

            // Switch to Gender Selection
            isStorylineActive = false;
            currentStoryImage = null;
            repaint();

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

    private void finalizePlayer(String genderInput, String playerName) {
        removeAll();
        repaint();

        Gender playerGender = genderInput.equals("M") ? Gender.MALE : Gender.FEMALE;
        player = new Player(playerName, 100, playerGender);

        filterRomanceable();
        playGlitchTransition(() -> {
            showMeetCharactersTitle();
        });
    }

    // =========================
    // INPUT
    // =========================
    private void showNameInput(String selectedGender) {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            setLayout(null);

            final Dimension cardSize = new Dimension(380, 260);

            JPanel card = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    if (genderPanelImage != null) {
                        g2.drawImage(genderPanelImage, 0, 0, getWidth(), getHeight(), this);
                    }
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setBounds((getWidth() - cardSize.width) / 2, (getHeight() - cardSize.height) / 2, cardSize.width, cardSize.height);

            JLabel title = new JLabel("Enter your Name:", SwingConstants.CENTER);
            title.setFont(new Font(bFont, Font.BOLD, 20));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 26, cardSize.width, 40);
            card.add(title);

            JTextField nameField = new JTextField();
            nameField.setFont(new Font(bFont, Font.BOLD, 18));
            nameField.setForeground(Color.WHITE);
            nameField.setBackground(new Color(60, 55, 50));
            nameField.setCaretColor(Color.WHITE);
            nameField.setHorizontalAlignment(JTextField.LEFT);

            nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(150, 150, 150), 2),
                    BorderFactory.createEmptyBorder(0, 15, 0, 0)
            ));

            nameField.setBounds(56, 98, 280, 46);
            card.add(nameField);

            JButton cancelBtn = createSmallButton("X");
            cancelBtn.setBounds(210, 180, 60, 60);
            card.add(cancelBtn);

            JButton confirmBtn = createSmallButton("/");
            confirmBtn.setBounds(275, 180, 60, 60);
            card.add(confirmBtn);

            add(card);

            confirmBtn.addActionListener(e -> {
                String name = nameField.getText().trim();
                if (name.isEmpty()) name = "Survivor";
                finalizePlayer(selectedGender, name);
            });

            cancelBtn.addActionListener(e -> {
                startGenderSelection();
            });

            nameField.addActionListener(e -> confirmBtn.doClick());

            revalidate();
            repaint();
            nameField.requestFocusInWindow();
        });
    }

    // ==========================================
    // SUBTLE & SLOW APOCALYPTIC GLITCH TRANSITION
    // ==========================================
    private void playGlitchTransition(Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            setLayout(null);

            JPanel glitchPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();

                    if (bgImage != null) {
                        g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    }

                    for (int i = 0; i < 3; i++) {
                        if (Math.random() > 0.5) {
                            int tearY = (int) (Math.random() * getHeight());
                            int tearHeight = (int) (Math.random() * 30 + 5);
                            int shiftX = (int) (Math.random() * 10 - 5);

                            g2.copyArea(0, tearY, getWidth(), tearHeight, shiftX, 0);
                        }
                    }

                    for (int i = 0; i < 8; i++) {
                        int x = (int) (Math.random() * getWidth());
                        int y = (int) (Math.random() * getHeight());
                        int w = (int) (Math.random() * 250 + 50);
                        int h = (int) (Math.random() * 8 + 2);

                        int colorPick = (int) (Math.random() * 3);
                        if (colorPick == 0) {
                            g2.setColor(new Color(65, 80, 50, 60));
                        } else if (colorPick == 1) {
                            g2.setColor(new Color(90, 60, 40, 60));
                        } else {
                            g2.setColor(new Color(30, 28, 25, 80));
                        }

                        g2.fillRect(x, y, w, h);
                    }

                    if (Math.random() > 0.8) {
                        g2.setColor(new Color(70, 80, 50, 15));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }

                    g2.dispose();
                }
            };
            glitchPanel.setBounds(0, 0, Math.max(getWidth(), 900), Math.max(getHeight(), 700));
            add(glitchPanel);

            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    glitchPanel.setBounds(0, 0, getWidth(), getHeight());
                }
            });

            revalidate();
            repaint();

            new Thread(() -> {
                long endTime = System.currentTimeMillis() + 900;
                while (System.currentTimeMillis() < endTime) {
                    glitchPanel.repaint();
                    pause(190);
                }
                SwingUtilities.invokeLater(onComplete);
            }).start();
        });
    }

    // ==========================================
    // MEET THE CHARACTERS (CLEAN SNAP TRANSITION)
    // ==========================================
    private void showMeetCharactersTitle() {
        removeAll();
        setLayout(null);

        int bannerW = 420;
        int bannerH = 110;

        // 1. Full Screen Overlay + CHAINS
        JPanel overlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                // DRAW THE CHAINS
                if (chainsImage != null) {
                    int currentBannerX = (getWidth() - bannerW) / 2;
                    int currentBannerY = (getHeight() - bannerH) / 2;

                    int chainW = 24;
                    int chainH = currentBannerY + 15;

                    int leftChainX = currentBannerX + 25;
                    int rightChainX = currentBannerX + bannerW - 25 - chainW;

                    g2.drawImage(chainsImage, leftChainX, -10, chainW, chainH, this);
                    g2.drawImage(chainsImage, rightChainX, -10, chainW, chainH, this);
                }
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, Math.max(getWidth(), 900), Math.max(getHeight(), 700));

        // 2. The Panel for the Image
        JPanel bannerPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                if (textPanelImage != null) {
                    g2.drawImage(textPanelImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(new Color(40, 35, 30, 220));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.setColor(new Color(200, 200, 200));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                }
                g2.dispose();
            }
        };
        bannerPanel.setOpaque(false);
        bannerPanel.setBounds((900 - bannerW) / 2, (700 - bannerH) / 2, bannerW, bannerH);

        // 3. The Text
        JLabel title = new JLabel("MEET THE CHARACTERS", SwingConstants.CENTER);
        title.setFont(new Font(mainFont, Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 0, bannerW, bannerH);

        bannerPanel.add(title);
        overlay.add(bannerPanel);
        add(overlay);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                overlay.setBounds(0, 0, getWidth(), getHeight());
                bannerPanel.setBounds((getWidth() - bannerW) / 2, (getHeight() - bannerH) / 2, bannerW, bannerH);
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

            final Dimension cardSize = new Dimension(350, 260);

            JPanel card = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    if (genderPanelImage != null) {
                        g2.drawImage(genderPanelImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g2.setColor(new Color(60, 55, 50, 220));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    }
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setPreferredSize(cardSize);
            card.setMinimumSize(cardSize);
            card.setMaximumSize(cardSize);

            int cardX = (getWidth() - cardSize.width) / 2;
            int cardY = (getHeight() - cardSize.height) / 2;
            card.setBounds(cardX, cardY, cardSize.width, cardSize.height);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.NONE;

            JLabel title = new JLabel("Choose Gender", SwingConstants.CENTER);
            title.setFont(new Font(bFont, Font.BOLD, 20));
            title.setForeground(Color.WHITE);
            gbc.gridy = 0;
            gbc.insets = new Insets(16, 0, 25, 0);
            card.add(title, gbc);

            JButton maleBtn = createGenderButton("Male");
            gbc.gridy = 1;
            gbc.insets = new Insets(6, 0, 0, 0);
            card.add(maleBtn, gbc);

            JButton femaleBtn = createGenderButton("Female");
            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            card.add(femaleBtn, gbc);

            add(card);

            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    card.setBounds((getWidth() - cardSize.width) / 2,
                            (getHeight() - cardSize.height) / 2,
                            cardSize.width, cardSize.height);
                }
            });

            maleBtn.addActionListener(e -> showNameInput("M"));
            femaleBtn.addActionListener(e -> showNameInput("F"));

            revalidate();
            repaint();
        });
    }

    // ==========================================
    // UI EDIT //GENDER BUTTON (LARGE)
    // ==========================================
    private JButton createGenderButton(String text) {
        Image btnNormal = loadResImage("res/ui/icon/normal-buttons/button-2-normal-not-active.png");
        Image btnHover  = loadResImage("res/ui/icon/normal-buttons/button-2-normal-hover.png");
        Image btnActive = loadResImage("res/ui/icon/normal-buttons/button-2-normal-active.png");

        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                Dimension size = new Dimension(200, 75);
                setPreferredSize(size);
                setMinimumSize(size);
                setMaximumSize(size);

                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(new Font(bFont, Font.BOLD, 16));
                setForeground(Color.WHITE);

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
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed();
                Image currentImg;

                if (isPressed) {
                    currentImg = btnActive;
                } else if (hovered) {
                    currentImg = btnHover;
                } else {
                    currentImg = btnNormal;
                }

                if (currentImg != null) {
                    g2.drawImage(currentImg, 0, 0, getWidth(), getHeight(), null);
                }
                g2.dispose();

                g.translate(6, 3);

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

    // ==========================================
    // UI EDIT // SMALL SQUARE BUTTON (For X and /)
    // ==========================================
    private JButton createSmallButton(String text) {
        Image btnNormal = loadResImage("res/ui/icon/small-buttons/not-active.png");
        Image btnHover  = loadResImage("res/ui/icon/small-buttons/hover.png");
        Image btnActive = loadResImage("res/ui/icon/small-buttons/active.png");

        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                Dimension size = new Dimension(60, 60);
                setPreferredSize(size);
                setMinimumSize(size);
                setMaximumSize(size);

                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(new Font(bFont, Font.BOLD, 22));
                setForeground(Color.WHITE);

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
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed();
                Image currentImg;

                if (isPressed) {
                    currentImg = btnActive;
                } else if (hovered) {
                    currentImg = btnHover;
                } else {
                    currentImg = btnNormal;
                }

                if (currentImg != null) {
                    g2.drawImage(currentImg, 0, 0, getWidth(), getHeight(), null);
                }
                g2.dispose();

                if (isPressed) {
                    g.translate(-1, 1);
                }

                super.paintComponent(g);

                if (isPressed) {
                    g.translate(0, -1);
                }
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

    /// UI EDIT //LEVEL CONFIRMATION
    private void startLevelConfirmation() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            setLayout(null);

            // ==========================================
            // ARE YOU READY? PANEL SIZING
            // ==========================================
            int cardW = 360;
            int cardH = 300; // Keep the expanded height for vertical buttons

            JPanel card = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    if (genderPanelImage != null) {
                        g2.drawImage(genderPanelImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        // Fallback rounded rect design remains the same
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(172, 172, 172, 191));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                        g2.setColor(new Color(62, 55, 49, 255));
                        g2.setStroke(new BasicStroke(5f));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    }
                    g2.dispose();
                }
            };
            card.setOpaque(false);

            int cardX = (getWidth() - cardW) / 2;
            int cardY = (getHeight() - cardH) / 2;
            card.setBounds(cardX, cardY, cardW, cardH);

            JLabel title = new JLabel("Are You Ready?", SwingConstants.CENTER);
            title.setFont(new Font(bFont, Font.BOLD, 20)); // Font same
            title.setForeground(Color.WHITE);
            // Placed at the very top, within the brown bar area.
            // setBounds(X, Y, WIDTH, HEIGHT)
            title.setBounds(0, 30, cardW, 40);

            JLabel subtitle = new JLabel("Your future lies in your hands", SwingConstants.CENTER);
            subtitle.setFont(new Font(bFont, Font.PLAIN, 15)); // Font same
            subtitle.setForeground(Color.WHITE);
            // Subtitle goes directly below the brown bar
            // and above the YES button.
            subtitle.setBounds(0, 100, cardW, 25);

            // ==========================================
            // CHANGED TO USE THE NEW createYesNoButton METHOD
            // ==========================================
            JButton yesBtn = createYesNoButton("Yes");
            JButton noBtn  = createYesNoButton("No");

            // ==========================================
            // YES / NO BUTTON POSITIONING & SIZING (VERTICAL)
            // ==========================================
            int btnW = 200;
            int btnH = 75;
            int gap = -2; // Vertical space between YES and NO buttons

            // Centers the buttons horizontally based on the width and gap!
            int startX = (cardW - btnW) / 2;

            // Placed further down than title/subtitle
            int startY = 125;

            // Stacked vertically: X stays centered, Y increases
            yesBtn.setBounds(startX, startY, btnW, btnH);
            noBtn.setBounds(startX, startY + btnH + gap, btnW, btnH);

            card.add(title);
            card.add(subtitle);
            card.add(yesBtn);
            card.add(noBtn);

            add(card);

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

    // ==========================================
    // UI EDIT // YES/NO BUTTON CREATOR
    // ==========================================
    private JButton createYesNoButton(String text) {
        // Loads the normal button images as requested
        Image btnNormal = loadResImage("res/ui/icon/normal-buttons/button-2-normal-not-active.png");
        Image btnHover  = loadResImage("res/ui/icon/normal-buttons/button-2-normal-hover.png");
        Image btnActive = loadResImage("res/ui/icon/normal-buttons/button-2-normal-active.png");

        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                Dimension size = new Dimension(200, 75);
                setPreferredSize(size);
                setMinimumSize(size);
                setMaximumSize(size);

                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(new Font(bFont, Font.BOLD, 16));
                setForeground(Color.WHITE);

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
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed();
                Image currentImg;

                if (isPressed) {
                    currentImg = btnActive;
                } else if (hovered) {
                    currentImg = btnHover;
                } else {
                    currentImg = btnNormal;
                }

                if (currentImg != null) {
                    g2.drawImage(currentImg, 0, 0, getWidth(), getHeight(), null);
                }
                g2.dispose();

                // EXACTLY AS REQUESTED:
                if (isPressed) {
                    // Pushes the text down by 4 pixels.
                    // (Change '4' to whatever matches your sprite's downward shift!)
                    g.translate(-3, 3);
                }

                // 3. Draw the text on top
                super.paintComponent(g);

                // 4. Reset the position so it doesn't mess up the next frame
                if (isPressed) {
                    g.translate(0, -4);
                }
            }
        };
        return btn;
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
            overlay.setBounds(0, 0, Math.max(getWidth(), 900), Math.max(getHeight(), 700));

            JLabel title = new JLabel("", SwingConstants.CENTER);
            title.setFont(new Font(mainFont, Font.BOLD, 48));
            title.setForeground(Color.WHITE);
            title.setBounds(0, Math.max(getHeight(), 700) / 2 - 60, Math.max(getWidth(), 900), 60);

            JLabel subtitle = new JLabel("", SwingConstants.CENTER);
            subtitle.setFont(new Font(bFont, Font.PLAIN, 25));
            subtitle.setForeground(Color.RED);
            subtitle.setBounds(0, Math.max(getHeight(), 700) / 2, Math.max(getWidth(), 900), 40);

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
}