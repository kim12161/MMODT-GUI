package game;

import Characters.Character;
import Encounters.ZombieEncounterPanel;
import Interaction.BackgroundLayer;
import Interaction.DialogueBoxLayer;
import Interaction.ChoiceButtonLayer;
import Player.Player;
import RelationshipSystem.Relationship;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ScenePanel extends JPanel {

    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    // ==============================
    // LAYERS
    // ==============================
    private BackgroundLayer backgroundLayer;
    private DialogueBoxLayer dialogueBoxLayer;
    private ChoiceButtonLayer choiceButtonLayer;

    // SPRITES
    private Map<String, JLabel> characterSprites = new HashMap<>();

    // GAME STATE
    private Player player;
    private List<Character> characters;
    private ConversationManager conversationManager;
    private int currentLevel = 1;
    private boolean gameRunning = true;

    private volatile String pendingChoice = null;
    private final Object choiceLock = new Object();

    // STATUS BAR & OVERLAY
    private JLabel levelIndicator;
    private JLabel statusLabel;
    private JPanel statusOverlay;
    private JLabel statusCharName, statusTrust, statusTurnOn, statusTurnOff, statusCharisma, statusScore;

    // LEVEL TITLE OVERLAY COMPONENTS
    private JPanel levelTitleOverlay;
    private JLabel levelNumberLabel;
    private JLabel levelTitleLabel;
    private JLabel levelHintLabel;

    private static final String[] LEVEL_NAMES = {
            "Abandoned Compound", "Temporary Shelter", "City Ruins", "Safehouse Conflict", "Escape Route"
    };

    private static final String[] LEVEL_BACKGROUNDS = {
            "level1.gif", "level2.gif", "level3.gif", "level4.png", "level5.gif"
    };

    // ==============================
    // CONSTRUCTOR
    // ==============================
    public ScenePanel(Player player, List<Character> characters, ConversationManager conversationManager) {
        this.player = player;
        this.characters = characters;
        this.conversationManager = conversationManager;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(900, 700));
        setOpaque(false);

        buildLayers();
        buildSprites();
        buildStatusBar();
        buildStatusOverlay();
        buildLevelTitleOverlay();

        levelIndicator = new JLabel("", SwingConstants.LEFT);
        levelIndicator.setFont(new Font(bFont, Font.BOLD, 18));
        levelIndicator.setForeground(new Color(255, 255, 255, 200));
        levelIndicator.setBounds(20, 10, 500, 30);

        backgroundLayer.add(levelIndicator);
        backgroundLayer.add(statusLabel);
        backgroundLayer.add(statusOverlay);
        backgroundLayer.add(choiceButtonLayer);
        backgroundLayer.add(dialogueBoxLayer);
        backgroundLayer.add(levelTitleOverlay);

        for (JLabel sprite : characterSprites.values()) {
            backgroundLayer.add(sprite);
        }

        backgroundLayer.setComponentZOrder(levelIndicator, 0);
        backgroundLayer.setComponentZOrder(statusLabel, 1);
        backgroundLayer.setComponentZOrder(statusOverlay, 2);
        backgroundLayer.setComponentZOrder(levelTitleOverlay, 3);
        backgroundLayer.setComponentZOrder(choiceButtonLayer, 4);
        backgroundLayer.setComponentZOrder(dialogueBoxLayer, 5);

        int zIndex = 6;
        for (JLabel sprite : characterSprites.values()) {
            backgroundLayer.setComponentZOrder(sprite, zIndex++);
        }

        add(backgroundLayer, BorderLayout.CENTER);
    }

    // ==============================
    // BUILD COMPONENTS
    // ==============================
    private void buildLevelTitleOverlay() {
        levelTitleOverlay = new JPanel(null) {
            Image frameImg;
            Image chainImg;

            {
                // CHANGE: Path for the big info panel
                java.io.File fFrame = new java.io.File("res/ui/panels/frame-panel.png");
                if (fFrame.exists()) frameImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();

                java.io.File fChain = new java.io.File("res/ui/icon/assets/chains.png");
                if (fChain.exists()) chainImg = new ImageIcon(fChain.getAbsolutePath()).getImage();
            }
//
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // ==========================================
                // ⚠️ MANUAL MARGINS FOR BIG PANEL
                // ==========================================
                int frameW = 380; // Matches your Confirm Card Width
                int frameH = 300;
                int frameX = (getWidth() - frameW) / 2;
                int frameY = (getHeight() - frameH) / 2;

                if (chainImg != null) {
                    int chainW = 24;
                    g2.drawImage(chainImg, frameX + 40, 0, chainW, frameY + 15, this);
                    g2.drawImage(chainImg, frameX + frameW - 40 - chainW, 0, chainW, frameY + 15, this);
                }

                if (frameImg != null) {
                    g2.drawImage(frameImg, frameX, frameY, frameW, frameH, this);
                }

                g2.dispose();
            }
        };

        levelTitleOverlay.setOpaque(false);
        levelTitleOverlay.setBounds(0, 0, 900, 700);

        // Positioning labels with inner margins
        int frameW = 460;
        int frameH = 300;
        int frameX = (900 - frameW) / 2;
        int frameY = (700 - frameH) / 2;

        levelNumberLabel = new JLabel("", SwingConstants.CENTER);
        levelNumberLabel.setFont(new Font(bFont, Font.BOLD, 24));
        levelNumberLabel.setBounds(frameX, frameY + 30, frameW, 40);
        levelNumberLabel.setForeground(Color.WHITE);
        // Added 40px top margin inside frame
        levelNumberLabel.setBounds(frameX + 20, frameY + 40, frameW - 40, 30);

        levelTitleLabel = new JLabel("", SwingConstants.CENTER);
        levelTitleLabel.setFont(new Font(bFont, Font.BOLD, 26));
        levelTitleLabel.setForeground(Color.WHITE);
        levelTitleLabel.setBounds(frameX + 20, frameY + 120, frameW - 40, 40);

        levelHintLabel = new JLabel("", SwingConstants.CENTER) {
            Image btnImg;
            {
                java.io.File fBtn = new java.io.File("res/ui/icon/normal-buttons/button-2-normal-active.png");
                if (fBtn.exists()) btnImg = new ImageIcon(fBtn.getAbsolutePath()).getImage();
            }
            @Override
            protected void paintComponent(Graphics g) {
                if (btnImg != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.drawImage(btnImg, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        levelHintLabel.setFont(new Font(bFont, Font.PLAIN, 16));
        levelHintLabel.setForeground(Color.WHITE);

        int btnW = 220;
        int btnH = 60;
        int btnX = frameX + (frameW - btnW) / 2;
        // Added 25px bottom margin inside frame
        int btnY = frameY + frameH - btnH - 25;
        levelHintLabel.setBounds(btnX, btnY, btnW, btnH);

        levelTitleOverlay.add(levelNumberLabel);
        levelTitleOverlay.add(levelTitleLabel);
        levelTitleOverlay.add(levelHintLabel);
        levelTitleOverlay.setVisible(false);
    }

    private void buildLayers() {
        backgroundLayer = new BackgroundLayer();
        backgroundLayer.setBounds(0, 0, 900, 700);

        dialogueBoxLayer = new DialogueBoxLayer();
        dialogueBoxLayer.setBounds(0, 0, 900, 700);
        dialogueBoxLayer.setVisible(false);

        choiceButtonLayer = new ChoiceButtonLayer();
        choiceButtonLayer.setBounds(400, 50, 470, 515);
        choiceButtonLayer.setVisible(false);
    }

    private void buildSprites() {
        Map<String, String> spritePaths = new HashMap<>();
        spritePaths.put("Avy",    "res/sprite/avy.png");
        spritePaths.put("Marina", "res/sprite/marina.png");
        spritePaths.put("Kim",    "res/sprite/kim.png");
        spritePaths.put("Nathan", "res/sprite/nathan.png");
        spritePaths.put("Yubie",  "res/sprite/yubie.png");
        spritePaths.put("Adi",    "res/sprite/adi.png");

        for (Character c : characters) {
            String path = spritePaths.getOrDefault(c.getName(), "res/sprite/zombie.png");
            java.io.File f = new java.io.File(path);

            JLabel sprite = new JLabel();

            // ==========================================
            // ⚠️ SPRITE POSITIONING (REMOVED BLACK BG)
            // ==========================================
            sprite.setOpaque(false); // Remove black background
            sprite.setBackground(new Color(0,0,0,0));

            // Shifting sprite DOWN (Y = 20) and to the left (X = 10)
            sprite.setBounds(10, 20, 400, 700);
            sprite.setVisible(false);

            if (f.exists()) {
                ImageIcon raw = new ImageIcon(f.getAbsolutePath());
                Image scaled = raw.getImage().getScaledInstance(400, 700, Image.SCALE_SMOOTH);
                sprite.setIcon(new ImageIcon(scaled));
            }
            characterSprites.put(c.getName(), sprite);
        }
    }

    private void buildStatusBar() {
        statusLabel = new JLabel("", SwingConstants.RIGHT);
        statusLabel.setFont(new Font(bFont, Font.PLAIN, 13));
        statusLabel.setForeground(new Color(220, 220, 220));
        statusLabel.setBounds(500, 10, 380, 30);
    }

    // ==============================
    // ANIMATIONS
    // ==============================
    private void showLevelTitle(int level, String title) {
        SwingUtilities.invokeLater(() -> {
            levelNumberLabel.setText("LEVEL  " + level);
            levelTitleLabel.setText("");
            levelHintLabel.setText("");
            levelTitleOverlay.setVisible(true);
            backgroundLayer.repaint();
        });

        typewrite(levelTitleLabel, title.toUpperCase(), 60);
        sleep(400);
        typewrite(levelHintLabel, "— Loading —", 45);

        sleep(1500);

        SwingUtilities.invokeLater(() -> levelHintLabel.setText(""));
        typewrite(levelHintLabel, "— Get Ready —", 45);

        sleep(1500);
        SwingUtilities.invokeLater(() -> levelTitleOverlay.setVisible(false));
    }

    private void typewrite(JLabel label, String text, int delayMs) {
        for (int i = 1; i <= text.length(); i++) {
            final String partial = text.substring(0, i);
            SwingUtilities.invokeLater(() -> label.setText(partial));
            sleep(delayMs);
        }
    }

    // ==============================
    // GAME LOOP / LOGIC
    // ==============================
    public void startGame() {
        new Thread(() -> {
            for (int level = 1; level <= 5; level++) {
                if (!gameRunning) break;
                currentLevel = level;
                playLevelTemplate(level, LEVEL_NAMES[level - 1]);
            }
            if (player.isAlive()) {
                endGame();
            }
        }).start();
    }

    private void playLevelTemplate(int level, String title) {
        if (!gameRunning) return;

        SwingUtilities.invokeLater(() -> {
            backgroundLayer.setBackgroundFromFile(LEVEL_BACKGROUNDS[level - 1]);
            levelIndicator.setText("LVL " + level + ": " + title.toUpperCase());

            levelIndicator.setVisible(false);
            statusLabel.setVisible(false);

            dialogueBoxLayer.setVisible(false);
            choiceButtonLayer.setVisible(false);
            hideSpeakerSprite();
        });

        sleep(300);

        showLevelTitle(level, title);

        sleep(2000);

        SwingUtilities.invokeLater(() -> levelTitleOverlay.setVisible(false));

        if (level == 1) {
            itemDiscoveryEvent();
        }
        for (int conversationNum = 1; conversationNum <= 3; conversationNum++) {
            if (!gameRunning) break;

            final int convNum = conversationNum;
            SwingUtilities.invokeLater(() -> {
                levelIndicator.setVisible(true);
                statusLabel.setVisible(true);
                statusLabel.setText("Level " + level + "  |  Conversation " + convNum + " of 3   ");
            });

            for (Character character : characters) {
                if (!gameRunning) break;
                runConversationGUI(player, character, level, conversationNum);
            }

            if (!gameRunning) break;

            if ((level == 2 || level == 3) && conversationNum == 2) itemDiscoveryEvent();
            if ((level == 4 || level == 5) && conversationNum == 3) itemDiscoveryEvent();

            if (conversationNum == 3) {
                hideSpeakerSprite();
                SwingUtilities.invokeLater(() -> {
                    levelIndicator.setVisible(false);
                    statusLabel.setVisible(false);
                });
                zombieEncounterGUI(level);
            }
        }
    }

    // ==============================
    // SPRITE CONTROLS
    // ==============================
    private void showSpeakerSprite(String speakerName) {
        SwingUtilities.invokeLater(() -> {
            characterSprites.values().forEach(s -> s.setVisible(false));
            JLabel current = characterSprites.get(speakerName);
            if (current != null) {
                current.setVisible(true);
                backgroundLayer.setComponentZOrder(current, 6);
            }
            backgroundLayer.repaint();
        });
    }

    private void hideSpeakerSprite() {
        SwingUtilities.invokeLater(() -> {
            characterSprites.values().forEach(s -> s.setVisible(false));
            backgroundLayer.repaint();
        });
    }

    private void runConversationGUI(Player player, Character character, int level, int conversationNum) {
        String dialogue = conversationManager.getQuestion(character, level, conversationNum);
        Map<String, String> choices = conversationManager.displayChoices(character.getName(), level, conversationNum);

        if (dialogue == null || choices == null) return;

        showSpeakerSprite(character.getName());

        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.setSpeaker(character.getName());
            dialogueBoxLayer.setDialogue(dialogue);
            dialogueBoxLayer.setVisible(true);
            choiceButtonLayer.setVisible(false);
        });

        sleep(dialogue.length() * 14 + 800);
        pendingChoice = null;

        SwingUtilities.invokeLater(() -> {
            choiceButtonLayer.clearChoices();
            for (Map.Entry<String, String> entry : choices.entrySet()) {
                choiceButtonLayer.addChoice(entry.getKey() + ".  " + entry.getValue(), entry.getKey());
            }
            choiceButtonLayer.setChoiceListener((choiceText, nextNode) -> {
                synchronized (choiceLock) {
                    pendingChoice = nextNode;
                    choiceLock.notifyAll();
                }
            });
            choiceButtonLayer.showChoices();
        });

        synchronized (choiceLock) {
            while (pendingChoice == null) {
                try { choiceLock.wait(); } catch (InterruptedException ignored) {}
            }
        }

        final String choiceMade = pendingChoice;
        SwingUtilities.invokeLater(() -> choiceButtonLayer.setVisible(false));
        sleep(300);

        ConversationManager.ChoiceOutcome outcome = conversationManager.getChoiceOutcome(character.getName(), level, conversationNum, choiceMade);

        if (outcome != null) {
            SwingUtilities.invokeLater(() -> {
                dialogueBoxLayer.setSpeaker(character.getName());
                dialogueBoxLayer.setDialogue(outcome.response);
                dialogueBoxLayer.setVisible(true);
            });
            sleep(outcome.response.length() * 14 + 1200);
            conversationManager.applyEffect(player, character, outcome.effect);
            showStatusOverlay(character, player, outcome.effect);
        }

        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.clear();
            dialogueBoxLayer.setVisible(false);
        });

        sleep(400);
    }

    private void zombieEncounterGUI(int level) {
        final Object combatLock = new Object();
        final boolean[] survived = {true};
        SwingUtilities.invokeLater(() -> {
            ZombieEncounterPanel zep = new ZombieEncounterPanel(player, level);
            zep.setBounds(0, 0, getWidth(), getHeight());
            zep.setCombatEndListener(playerAlive -> {
                survived[0] = playerAlive;
                if (!playerAlive) gameRunning = false;
                SwingUtilities.invokeLater(() -> {
                    backgroundLayer.remove(zep);
                    backgroundLayer.repaint();
                });
                synchronized (combatLock) { combatLock.notifyAll(); }
            });
            backgroundLayer.add(zep);
            backgroundLayer.setComponentZOrder(zep, 0);
            backgroundLayer.repaint();
            zep.startCombat();
        });
        synchronized (combatLock) {
            try { combatLock.wait(); } catch (InterruptedException ignored) {}
        }
        sleep(400);
    }

    private void itemDiscoveryEvent() {
        Random random = new Random();
        String found = random.nextBoolean() ? "Medkit" : "Bandage";
        player.addConsumable(found);
        hideSpeakerSprite();
        sleep(300);
        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.setSpeaker("SYSTEM");
            dialogueBoxLayer.setDialogue("You checked every corner and found a " + found + "!");
            dialogueBoxLayer.setVisible(true);
        });
        sleep(2500);
        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.clear();
            dialogueBoxLayer.setVisible(false);
        });
        sleep(400);
    }

    private void endGame() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            setLayout(new BorderLayout());
            add(new EndGamePanel(player, characters), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ==============================
    // STATUS OVERLAY
    // ==============================
    private void buildStatusOverlay() {
        statusOverlay = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 10, 10, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        statusOverlay.setOpaque(false);
        int w = 300, h = 200;

        statusOverlay.setBounds(350, 200, w, h);

        statusCharName = new JLabel("", SwingConstants.CENTER);
        statusCharName.setFont(new Font(bFont, Font.BOLD, 18));
        statusCharName.setForeground(new Color(220, 60, 60));
        statusCharName.setBounds(0, 12, w, 25);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 42, w - 40, 2);
        sep.setForeground(new Color(180, 30, 30));

        statusTrust = makeStatLabel(); statusTurnOn = makeStatLabel();
        statusTurnOff = makeStatLabel(); statusCharisma = makeStatLabel(); statusScore = makeStatLabel();

        statusTrust.setBounds(30, 52, w - 40, 22);
        statusTurnOn.setBounds(30, 76, w - 40, 22);
        statusTurnOff.setBounds(30, 100, w - 40, 22);
        statusCharisma.setBounds(30, 124, w - 40, 22);
        statusScore.setBounds(30, 152, w - 40, 22);
        statusScore.setForeground(new Color(220, 180, 60));

        statusOverlay.add(statusCharName); statusOverlay.add(sep);
        statusOverlay.add(statusTrust); statusOverlay.add(statusTurnOn);
        statusOverlay.add(statusTurnOff); statusOverlay.add(statusCharisma);
        statusOverlay.add(statusScore);
        statusOverlay.setVisible(false);
    }

    private JLabel makeStatLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font(bFont, Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private void showStatusOverlay(Character character, Player player, String effect) {
        Relationship r = player.getRelationship(character);
        SwingUtilities.invokeLater(() -> {
            statusCharName.setText(character.getName().toUpperCase() + "  STATUS");
            statusTrust.setText("Trust        :  " + r.getTrust());
            statusTurnOn.setText("Turn-On    :  " + r.getTurnOn());
            statusTurnOff.setText("Turn-Off    :  " + r.getTurnOff());
            statusCharisma.setText("Charisma  :  " + player.getCharisma());

            String effectDisplay = switch (effect != null ? effect : "NEUTRAL") {
                case "CHARISMA"  -> "+2 Charisma";
                case "TRUST"     -> "+3 Trust";
                case "TURN_ON"   -> "+3 Turn-On";
                case "TURN_OFF"  -> "+3 Turn-Off";
                case "TURN_OFF2" -> "+6 Turn-Off";
                case "NEUTRAL"   -> "No change";
                default          -> "...";
            };
            statusScore.setText("Effect       :  " + effectDisplay);
            statusOverlay.setVisible(true);
        });

        sleep(2500);
        SwingUtilities.invokeLater(() -> statusOverlay.setVisible(false));
        sleep(300);
    }
}