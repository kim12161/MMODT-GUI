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

import javax.imageio.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;


public class Story extends JPanel {

    JTextPane textPane;
    StyledDocument doc;
    private GamePanel gamePanel; // ADD at top
    private JTextPane readyTextPane;
    private StyledDocument readyDoc;
    private Player player;
    private List<Characters.Character> availableCharacters;
    private int characterIndex = 0;
    private java.util.List<Characters.Character> romanceableCharacters;
    private ConversationManager conversationManager = new ConversationManager();

    // Background image
    private Image bgImage;

    //FONT
    private String mainFont="PixelArmy";
    private String bFont="Munro";

    // =========================
    // IMAGE LOADER
    // =========================
    private Image loadResImage(String filePath) {
        java.io.File f = new java.io.File(filePath);
        if (f.exists()) {
            return new ImageIcon(f.getAbsolutePath()).getImage();
        }
        // If it fails, this prints the exact path it checked so you can catch typos!
        System.err.println("[Story] WARNING: Background image not found -> " + f.getAbsolutePath());
        return null;
    }

    // =========================
    // IMAGE LOADER
    // =========================
    //ako g change to mainBackground

    public Story(GamePanel gamePanel    ) {
        this.gamePanel = gamePanel;
        // Load background once
        bgImage = loadResImage("res/background/mainBackground.png");

        initializeCharacters();

        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(0, 0, 0, 180));
        textPane.setForeground(Color.WHITE);
        textPane.setFont(new Font(bFont, Font.PLAIN, 20));
        textPane.setMargin(new Insets(20, 20, 20, 20));
        textPane.setOpaque(false);

        doc = textPane.getStyledDocument();

        SimpleAttributeSet left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
        doc.setParagraphAttributes(0, doc.getLength(), left, false);

        textPane.setPreferredSize(new Dimension(650, 380));

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.BLACK);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        add(scroll, gbc);

        SwingUtilities.invokeLater(this::startIntro);
    }

    // =========================
    // BACKGROUND
    // =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            // THIS LINE MUST BE ACTIVE TO SEE THE IMAGE:
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }


    // =========================
    // TYPE TEXT
    // =========================

    private void typeText(String text, Color color, int delay) {

        Style style = textPane.addStyle("style", null);
        StyleConstants.setForeground(style, color);

        for (char c : text.toCharArray()) {

            SwingUtilities.invokeLater(() -> {
                try {
                    doc.insertString(doc.getLength(), String.valueOf(c), style);
                } catch (Exception ignored) {}
            });

            try {
                Thread.sleep(delay);
            } catch (Exception ignored) {}
        }
    }

    private void clearText() {
        SwingUtilities.invokeLater(() -> textPane.setText(""));
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


        new Thread(() -> {


            clearText();

            typeText("\n\nYou are 28 years old, two years away from the big ", Color.WHITE, 20);
            typeText("3-0", Color.RED, 60);
            typeText(", and by all accounts, you have been living the good life.\n", Color.WHITE, 20);

            typeText("\nA stable career, your own cozy apartment, financial freedom and everything you once dreamed of, you achieved.\n", Color.WHITE, 20);

            typeText("\nBut at your college reunion,\n", Color.WHITE, 20);
            typeText("reality hit differently...", Color.WHITE, 20);

            pause(2500);
            clearText();

            typeText("\n\nEveryone showed up with partners; some even announcing engagements or babies.\n", Color.WHITE, 20);
            typeText("Surrounded by talks of weddings and settling down,\n", Color.WHITE, 20);
            typeText("you realized something:\n", Color.WHITE, 20);
            typeText("You had built the perfect life,\n", Color.WHITE, 20);
            typeText("but ", Color.WHITE, 20);
            typeText("never found love.", Color.RED, 60);

            pause(2500);
            clearText();

            typeText("\n\nThat night, you decided to add one last item to your bucket list:\n\n", Color.WHITE, 20);
            typeText("Find love before 30. Maybe even get married.\n\n", Color.RED, 60);
            typeText("Except, fate had other plans.", Color.WHITE, 20);

            pause(2500);
            clearText();

            typeText("\n\nThe very next week, the world ", Color.WHITE, 20);
            typeText("Spira", Color.RED, 60);
            typeText(" collapsed into chaos. A mysterious infection spread across the city, turning people into ravenous monsters.\n", Color.WHITE, 20);
            typeText("\nSociety crumbled, survival became the priority... yet, in the middle of it all, your bucket list remained the same.", Color.WHITE, 20);

            pause(2500);
            clearText();

            typeText("\n\nSure, the apocalypse has begun. But you?\n\n", Color.WHITE, 20);
            typeText("You're determined to ", Color.WHITE, 20);
            typeText("find a partner before the world ends", Color.RED, 60);
            typeText(". Because love might be the thing worth surviving for.", Color.WHITE, 20);

            pause(3000);

            typeText("\n\nThis is where your story begins.", Color.WHITE, 60);

            pause(3000);

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

            // Frosted glass card
            JPanel card = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.dispose();
                }
            };
            card.setOpaque(false);

            int cardW = 340, cardH = 180;
            int cardX = (getWidth() - cardW) / 2;
            int cardY = (getHeight() - cardH) / 2;
            card.setBounds(cardX, cardY, cardW, cardH);

            JLabel title = new JLabel("CHOOSE GENDER", SwingConstants.CENTER);
            title.setFont(new Font(mainFont, Font.BOLD, 22));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 25, cardW, 35);

            JButton maleBtn   = createGenderButton("MALE");
            JButton femaleBtn = createGenderButton("FEMALE");

            int btnW = 110, btnH = 38, gap = 20;
            int startX = (cardW - (btnW * 2 + gap)) / 2;

            maleBtn.setBounds(startX, 100, btnW, btnH);
            femaleBtn.setBounds(startX + btnW + gap, 100, btnW, btnH);

            card.add(title);
            card.add(maleBtn);
            card.add(femaleBtn);

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

            maleBtn.addActionListener(e -> setupPlayer("M"));
            femaleBtn.addActionListener(e -> setupPlayer("F"));

            revalidate();
            repaint();
        });
    }

    private JButton createGenderButton(String text) {

        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font(bFont, Font.BOLD, 13));
                setForeground(Color.WHITE);

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hovered)
                    g2.setColor(new Color(80, 80, 80, 210));
                else
                    g2.setColor(new Color(50, 50, 50, 190));

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2.setColor(new Color(180, 180, 180, 160));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

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
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.setStroke(new BasicStroke(1.5f));
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
            subtitle.setForeground(new Color(200, 200, 200));
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
    private void showNoScreen() {

        SwingUtilities.invokeLater(() -> {

            removeAll();
            setLayout(new GridBagLayout());
            setBackground(Color.BLACK);

            readyTextPane = new JTextPane();
            readyTextPane.setEditable(false);
            readyTextPane.setBackground(Color.BLACK);
            readyTextPane.setForeground(Color.WHITE);
            readyTextPane.setFont(new Font(bFont, Font.PLAIN, 22));
            readyTextPane.setOpaque(true);
            readyTextPane.setMargin(new Insets(20, 20, 20, 20));
            readyTextPane.setPreferredSize(new Dimension(600, 300));

            readyDoc = readyTextPane.getStyledDocument();

            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            readyDoc.setParagraphAttributes(0, readyDoc.getLength(), center, false);

            JScrollPane scroll = new JScrollPane(readyTextPane);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(Color.BLACK);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            add(scroll, gbc);

            revalidate();
            repaint();
        });

        new Thread(() -> {

            pause(500);

            typeReadyText("\n\n\nThe world awaits for no one...\n", Color.WHITE, 55);

            pause(1000);

            typeReadyText("\nYou chose to stay behind.", Color.RED, 55);

            pause(3000);

            // Go back to title screen
            SwingUtilities.invokeLater(() -> {

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

                if (frame != null) {

                    frame.getContentPane().removeAll();

                    new TitleScreen(frame.getContentPane(), gamePanel); // pass your GamePanel here if you have a reference

                    frame.revalidate();
                    frame.repaint();
                }
            });
        }).start();
    }

    private void typeReadyText(String text, Color color, int delay) {

        Style style = readyTextPane.addStyle("ready", null);
        StyleConstants.setForeground(style, color);

        // Re-apply center alignment
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