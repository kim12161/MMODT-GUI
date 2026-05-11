package game;

import Characters.Character;
import Encounters.ZombieEncounterPanel;
import Interaction.BackgroundLayer;
import Interaction.DialogueBoxLayer;
import Interaction.ChoiceButtonLayer;
import Interaction.SpriteLayer;
import Player.Player;
import RelationshipSystem.Relationship;
import saveSystem.GameMenu;
import main.GamePanel;


import javax.swing.*;
import java.awt.*;
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
    private SpriteLayer spriteLayer;

    // GAME STATE
    private Player player;
    private List<Character> characters;
    private ConversationManager conversationManager;
    private int currentLevel = 1;
    private int currentConversation = 1;  // ← tracked so GameMenu can save it
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

    // GAME MENU
    private GameMenu gameMenu;
    private GamePanel gamePanel;

    private static final int Z_GAME_MENU = 0;
    private static final int Z_LEVEL_IND = 1;
    private static final int Z_STATUS_LABEL = 2;
    private static final int Z_STATUS_OVERLAY = 3;
    private static final int Z_LEVEL_TITLE = 4;
    private static final int Z_CHOICES = 5;
    private static final int Z_DIALOGUE = 6;
    private static final int Z_SPRITES_START = 7;

    private static final String[] LEVEL_NAMES = {
            "Abandoned Compound", "Temporary Shelter", "City Ruins", "Safehouse Conflict", "Escape Route"
    };

    private static final String[] LEVEL_BACKGROUNDS = {
            "level1.gif", "level2.gif", "level3.gif", "level4.gif", "level5.gif"
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
        buildGameMenu();

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
        add(backgroundLayer, BorderLayout.CENTER);
        backgroundLayer.add(gameMenu);

        backgroundLayer.add(spriteLayer);

        backgroundLayer.setComponentZOrder(gameMenu, Z_GAME_MENU);
        backgroundLayer.setComponentZOrder(levelIndicator, Z_LEVEL_IND);
        backgroundLayer.setComponentZOrder(statusLabel, Z_STATUS_LABEL);
        backgroundLayer.setComponentZOrder(statusOverlay, Z_STATUS_OVERLAY);
        backgroundLayer.setComponentZOrder(levelTitleOverlay, Z_LEVEL_TITLE);
        backgroundLayer.setComponentZOrder(choiceButtonLayer, Z_CHOICES);
        backgroundLayer.setComponentZOrder(dialogueBoxLayer, Z_DIALOGUE);

        backgroundLayer.setComponentZOrder(spriteLayer, Z_SPRITES_START);

        add(backgroundLayer, BorderLayout.CENTER);
    }

    // ==============================
    // BUILD GAME MENU
    // ==============================
    private void buildGameMenu() {
        gameMenu = new GameMenu(backgroundLayer);
        gameMenu.setPlayer(player);
        gameMenu.setCharacters(characters);
        gameMenu.setCurrentLevel(currentLevel);
        gameMenu.setCurrentLevelName(LEVEL_NAMES[currentLevel - 1]);
        gameMenu.setCurrentConversation(currentConversation); // ← pass conversation
        if (gamePanel != null) gameMenu.setGamePanel(gamePanel);
        gameMenu.setBounds(GameMenu.defaultBounds(900, 700));
    }

    public void setGamePanel(GamePanel gp) {
        this.gamePanel = gp;
        if (gameMenu != null) gameMenu.setGamePanel(gp);
    }

    // ==============================
    // GAME LOOP
    // ==============================
    public void showReadyPrompt() {
        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.setSpeaker("SYSTEM");
            dialogueBoxLayer.setDialogue("Are you ready, " + player.getName() + "? The dead don't wait...");
            dialogueBoxLayer.setVisible(true);
        });
        sleep(2800);
        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.clear();
            dialogueBoxLayer.setVisible(false);
        });
        sleep(400);
    }

    /**
     * Start from level 1, conversation 1 (new game).
     */
    public void startGame() {
//        MusicManager.playBGM(MusicManager.BGM_GAME);
        startGameFromLevel(1, 1);
    }

    /**
     * Resume from a specific level AND conversation.
     * Shows the level title screen first, then skips straight to startConversation.
     */
    public void startGameFromLevel(int startLevel, int startConversation) {
        this.currentLevel = startLevel;
        this.currentConversation = startConversation;

        SwingUtilities.invokeLater(() -> {
            gameMenu.setCurrentLevel(currentLevel);
            gameMenu.setCurrentLevelName(LEVEL_NAMES[Math.max(0, currentLevel - 1)]);
            gameMenu.setCurrentConversation(currentConversation);
        });

        new Thread(() -> {

            if (startLevel > 1 || startConversation > 1) {
//                MusicManager.playBGM(MusicManager.BGM_GAME); // resuming save
            }


            for (int level = startLevel; level <= 5; level++) {
                if (!gameRunning) break;
                currentLevel = level;
                // On the first level we resume from saved conversation,
                // all subsequent levels start from conversation 1
                int resumeFrom = (level == startLevel) ? startConversation : 1;
                playLevelTemplate(level, LEVEL_NAMES[level - 1], resumeFrom);
            }
            if (player.isAlive()) endGame();
        }).start();
    }

    // Keep the old single-arg version working (new game path in Story.java)

    private void playLevelTemplate(int level, String title, int startConversation) {
        if (!gameRunning) return;

        final String levelName = title;
        SwingUtilities.invokeLater(() -> {
            levelIndicator.setVisible(false);
            statusLabel.setVisible(false);

            gameMenu.setVisible(false);
            gameMenu.setCurrentLevel(level);
            gameMenu.setCurrentLevelName(levelName);
            backgroundLayer.setBackgroundFromFile(LEVEL_BACKGROUNDS[level - 1]);
            levelIndicator.setText("LVL " + level + ": " + title.toUpperCase());
            levelIndicator.setVisible(false);
            statusLabel.setVisible(false);
            dialogueBoxLayer.setVisible(false);
            choiceButtonLayer.setVisible(false);
            hideSpeakerSprite();
        });

        sleep(300);
        // Always show the level title screen even when resuming mid-level
        showLevelTitle(level, title);
        sleep(2000);
        SwingUtilities.invokeLater(() -> levelTitleOverlay.setVisible(false));

        if (level == 1 && startConversation == 1) itemDiscoveryEvent();

        for (int conversationNum = startConversation; conversationNum <= 3; conversationNum++) {
            if (!gameRunning) break;

            // Update tracked conversation and sync to GameMenu for saving
            currentConversation = conversationNum;
            final int convNum = conversationNum;
            SwingUtilities.invokeLater(() -> {
                gameMenu.setVisible(true);
                gameMenu.setCurrentConversation(convNum);
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
                if (gameRunning) {
                    sleep(500); // ← let the panel clean up first
                    MusicManager.playBGM(MusicManager.BGM_GAME);
                }
            }
        }
    }

    // ==============================
    // BUILD COMPONENTS
    // ==============================
    private void buildLevelTitleOverlay() {
        levelTitleOverlay = new JPanel(null) {
            Image frameImg, chainImg;
            {
                java.io.File fFrame = new java.io.File("res/ui/panels/frame-panel.png");
                if (fFrame.exists()) frameImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();
                java.io.File fChain = new java.io.File("res/ui/icon/assets/chains.png");
                if (fChain.exists()) chainImg = new ImageIcon(fChain.getAbsolutePath()).getImage();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, getWidth(), getHeight());

                int frameW = 360, frameH = 220;
                int centeredX = (900 - frameW) / 2;
                int frameY = (700 - frameH) / 2;

                // 1. Chains stay perfectly centered
                if (chainImg != null) {
                    int chainW = 24;
                    g2.drawImage(chainImg, centeredX + 40, 0, chainW, frameY + 15, this);
                    g2.drawImage(chainImg, centeredX + frameW - 40 - chainW, 0, chainW, frameY + 15, this);
                }

                // 2. Panel moves 4 pixels to the left
                if (frameImg != null) {
                    g2.drawImage(frameImg, centeredX - 4, frameY, frameW, frameH, this);
                }
                g2.dispose();
            }
        };

        levelTitleOverlay.setOpaque(false);
        levelTitleOverlay.setBounds(0, 0, 900, 700);

        // Standard measurements
        int frameW = 380, frameH = 260;
        int shiftedX = ((900 - frameW) / 2) - 4; // Shifted 4px left
        int frameY = (700 - frameH) / 2;

        // --- Level Number ---
        levelNumberLabel = new JLabel("", SwingConstants.CENTER);
        levelNumberLabel.setFont(new Font(bFont, Font.BOLD, 22));
        levelNumberLabel.setForeground(Color.WHITE);
        levelNumberLabel.setBounds(shiftedX + 20, frameY + 42, frameW - 40, 30);

        // --- Level Title ---
        levelTitleLabel = new JLabel("", SwingConstants.CENTER);
        levelTitleLabel.setFont(new Font(bFont, Font.BOLD, 24));
        levelTitleLabel.setForeground(Color.WHITE);
        levelTitleLabel.setBounds(shiftedX + 23, frameY + 110, frameW - 40, 40);

        // --- Loading / Get Ready Button ---
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
        levelHintLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        int btnW = 220, btnH = 60;
        int btnX = shiftedX + (frameW - btnW) / 2;
        int btnY = frameY + frameH - btnH - 45;
        levelHintLabel.setBounds(btnX, btnY, btnW, btnH);

        // Add to overlay
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
        spriteLayer = new SpriteLayer();
        spriteLayer.setBounds(0, 0, 900, 700);

        spriteLayer.loadCharacter("Avy",
                "res/sprite/charac/avy/avy.png",
                "res/sprite/charac/avy/avy_turnOn.png",
                "res/sprite/charac/avy/avy_turnOff.png",
                "res/sprite/charac/avy/avy_charisma.png");

        spriteLayer.loadCharacter("Marina",
                "res/sprite/charac/marina/marina.png",
                "res/sprite/charac/marina/marina_turnOn.png",
                "res/sprite/charac/marina/marina_turnOff.png",
                "res/sprite/charac/marina/marina_charisma.png");

        spriteLayer.loadCharacter("Kim",
                "res/sprite/charac/kim/kim.png",
                "res/sprite/charac/kim/kim_turnOn.png",
                "res/sprite/charac/kim/kim_turnOff.png",
                "res/sprite/charac/kim/kim_charisma.png");

        spriteLayer.loadCharacter("Nathan",
                "res/sprite/charac/nathan/nathan.png",
                "res/sprite/charac/nathan/nathan_turnOn.png",
                "res/sprite/charac/nathan/nathan_turnOff.png",
                "res/sprite/charac/nathan/nathan_charisma.png");

        spriteLayer.loadCharacter("Yubie",
                "res/sprite/charac/yubie/yubie.png",
                "res/sprite/charac/yubie/yubie_turnOn.png",
                "res/sprite/charac/yubie/yubie_turnOff.png",
                "res/sprite/charac/yubie/yubie_charisma.png");

        spriteLayer.loadCharacter("Adi",
                "res/sprite/charac/adi/adi.png",
                "res/sprite/charac/adi/adi_turnOn.png",
                "res/sprite/charac/adi/adi_turnOff.png",
                "res/sprite/charac/adi/adi_charisma.png");
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
            levelNumberLabel.setText("Level " + level);
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
        MusicManager.loopSFX(MusicManager.TYPEWRITER);
        for (int i = 1; i <= text.length(); i++) {
            final String partial = text.substring(0, i);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                label.setText(partial);
                latch.countDown();
            });
            try {
                latch.await();
                Thread.sleep(delayMs);
            } catch (Exception ignored) {}
        }
        MusicManager.stopSFX(MusicManager.TYPEWRITER);
    }

    // ==============================
    // SPRITE CONTROLS
    // ==============================
    private void showSpeakerSprite(String name) {
        SwingUtilities.invokeLater(() -> {
            spriteLayer.show(name);

            backgroundLayer.setComponentZOrder(gameMenu, Z_GAME_MENU);
            backgroundLayer.setComponentZOrder(levelIndicator, Z_LEVEL_IND);
            backgroundLayer.setComponentZOrder(statusLabel, Z_STATUS_LABEL);
            backgroundLayer.setComponentZOrder(statusOverlay, Z_STATUS_OVERLAY);
            backgroundLayer.setComponentZOrder(levelTitleOverlay, Z_LEVEL_TITLE);
            backgroundLayer.setComponentZOrder(choiceButtonLayer, Z_CHOICES);
            backgroundLayer.setComponentZOrder(dialogueBoxLayer, Z_DIALOGUE);
            backgroundLayer.setComponentZOrder(spriteLayer, Z_SPRITES_START);

            backgroundLayer.repaint();
        });
    }

    private void showSpeakerSprite(String name, String effect) {
        SwingUtilities.invokeLater(() -> {
            spriteLayer.showWithEffect(name, effect);
            // ── play SFX when sprite loads ────────────────
            if (effect != null) {
                switch (effect) {
                    case "CHARISMA" -> MusicManager.playSFX(MusicManager.CHARM);
                    case "TRUST"    -> MusicManager.playSFX(MusicManager.TRUST);
                    case "TURN_ON"  -> MusicManager.playSFX(MusicManager.TURN_ON);
                    case "TURN_OFF",
                         "TURN_OFF2"-> MusicManager.playSFX(MusicManager.TURN_OFF);
                }
            }

            backgroundLayer.setComponentZOrder(gameMenu, Z_GAME_MENU);
            backgroundLayer.setComponentZOrder(levelIndicator, Z_LEVEL_IND);
            backgroundLayer.setComponentZOrder(statusLabel, Z_STATUS_LABEL);
            backgroundLayer.setComponentZOrder(statusOverlay, Z_STATUS_OVERLAY);
            backgroundLayer.setComponentZOrder(levelTitleOverlay, Z_LEVEL_TITLE);
            backgroundLayer.setComponentZOrder(choiceButtonLayer, Z_CHOICES);
            backgroundLayer.setComponentZOrder(dialogueBoxLayer, Z_DIALOGUE);
            backgroundLayer.setComponentZOrder(spriteLayer, Z_SPRITES_START);

            backgroundLayer.repaint();
        });
    }

    private void hideSpeakerSprite() {
        SwingUtilities.invokeLater(() -> {
            spriteLayer.hide();
            backgroundLayer.repaint();
        });
    }

    // ==============================
    // CONVERSATION
    // ==============================
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
                try {
                    choiceLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }

        final String choiceMade = pendingChoice;
        SwingUtilities.invokeLater(() -> choiceButtonLayer.setVisible(false));
        sleep(300);

        ConversationManager.ChoiceOutcome outcome = conversationManager.getChoiceOutcome(
                character.getName(), level, conversationNum, choiceMade);

        if (outcome != null) {
            showSpeakerSprite(character.getName(), outcome.effect);
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

    // ==============================
    // ZOMBIE ENCOUNTER
    // ==============================
    private void zombieEncounterGUI(int level) {
        final Object combatLock = new Object();
        final boolean[] combatEnded = {false};

        SwingUtilities.invokeLater(() -> {
            if (gameMenu != null) gameMenu.setVisible(false);
            ZombieEncounterPanel zep = new ZombieEncounterPanel(player, level, gameMenu);
            zep.setBounds(0, 0, getWidth(), getHeight());
            zep.setCombatEndListener(playerAlive -> {
                if (!playerAlive) gameRunning = false;
                MusicManager.stopBGM();
                SwingUtilities.invokeLater(() -> {
                    backgroundLayer.remove(zep);
                    backgroundLayer.repaint();
                });
                synchronized (combatLock) {
                    combatEnded[0] = true;
                    combatLock.notifyAll();
                }
            });
            gameMenu.setVisible(true);
            backgroundLayer.add(zep);
            backgroundLayer.setComponentZOrder(zep, 0);
            backgroundLayer.repaint();
            zep.startCombat();
        });

        // Block until combat truly ends
        synchronized (combatLock) {
            while (!combatEnded[0]) {
                try { combatLock.wait(); } catch (InterruptedException ignored) {}
            }
        }

        sleep(500);
        if (gameRunning) {
            MusicManager.playBGM(MusicManager.BGM_GAME);
        }
    }
    // ==============================

// ITEM DISCOVERY

// ==============================

    private void itemDiscoveryEvent() {

        String foundName = new Random().nextBoolean() ? "Medkit" : "Bandage";

        player.addConsumable(foundName);



// 1. Hide normal UI

        if (gameMenu != null) gameMenu.setVisible(false);

        levelIndicator.setVisible(false);

        statusLabel.setVisible(false);

        hideSpeakerSprite();

        sleep(300);
        MusicManager.playSFX(MusicManager.DISCOVERY);



// 2. Determine path for the found item image

        String imgPath = "res/ui/icon/assets/items/medkit.png"; // Default fallback

        if (foundName.equalsIgnoreCase("Medkit")) imgPath = "res/ui/icon/assets/items/medkit.png";

        else if (foundName.equalsIgnoreCase("Bandage"))

            imgPath = "res/ui/icon/assets/items/bandage.png"; // Change this if you have a bandage image!



        final String finalImgPath = imgPath;



// 3. Build the custom popup panel using frame-panel.png

        JPanel foundPanel = new JPanel(null) {

            Image frameImg, itemImg, invBoxImg;



            {

                try {

                    java.io.File fFrame = new java.io.File("res/ui/panels/inventory/item-panel.png");

                    if (fFrame.exists()) frameImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();



                    java.io.File fItem = new java.io.File(finalImgPath);

                    if (fItem.exists()) itemImg = new ImageIcon(fItem.getAbsolutePath()).getImage();



// 🛠️ LOAD THE INVENTORY BOX BACKGROUND

                    java.io.File fInvBox = new java.io.File("res/ui/panels/inventory/inventory-box.png");

                    if (fInvBox.exists()) invBoxImg = new ImageIcon(fInvBox.getAbsolutePath()).getImage();

                } catch (Exception e) {

                }

            }



            @Override

            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);



// Darken background slightly

                g2.setColor(new Color(0, 0, 0, 100));

                g2.fillRect(0, 0, getWidth(), getHeight());



// ==========================================

// 🛠️ 1. MAIN POPUP BOX DIMENSIONS & PLACEMENT

// ==========================================

                int boxW = 372;

                int boxH = 315;



// 🛠️ Forced exact 900x700 math so it centers perfectly on the screen!

                int boxX = (900 - boxW) / 2;

                int boxY = (700 - boxH) / 2;



// 🛠️ NUDGE THE ENTIRE PANEL:

// Change these if the WHOLE box needs to move!

                boxX += 5; // Example: 10 moves it right, -10 moves it left

                boxY += 0; // Example: 10 moves it down, -10 moves it up



                if (frameImg != null) {

                    g2.drawImage(frameImg, boxX, boxY, boxW, boxH, this);

                } else {

                    g2.setColor(new Color(60, 55, 50));

                    g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

                }



// ==========================================

// 🛠️ 2. INNER CONTENT OFFSET

// ==========================================

// 🛠️ NUDGE THE TEXT & INNER BOX:

// If the panel has a thicker border on one side, change this to shift

// the inner box and text left or right to make it visually centered!

                int contentX = boxX + 9; // Try +5 or -5 to shift things perfectly inside the frame!



// Draw Top Title

                g2.setFont(new Font(bFont, Font.BOLD, 22));

                g2.setColor(Color.WHITE);

                FontMetrics fm = g2.getFontMetrics();

                String topText = "Item Found!";

                int tx = contentX + (boxW - fm.stringWidth(topText)) / 2;

                g2.drawString(topText, tx, boxY + 50);



// Draw Item Name

                g2.setFont(new Font(bFont, Font.PLAIN, 18));

                String nameText = foundName;

                int nx = contentX + (boxW - fm.stringWidth(nameText)) / 2;

                g2.drawString(nameText, nx, boxY + 105);



// ==========================================

// 🛠️ 3. INNER INVENTORY BOX DIMENSIONS

// ==========================================

                int invBoxW = 140;

                int invBoxH = 140;

                int invBoxX = contentX + (boxW - invBoxW) / 2 - 5;

                int invBoxY = boxY + 115; // Change this to move the box UP/DOWN



                if (invBoxImg != null) {

                    g2.drawImage(invBoxImg, invBoxX, invBoxY, invBoxW, invBoxH, this);

                }



// ==========================================

// 🛠️ 4. ITEM SPRITE DIMENSIONS

// ==========================================

                int itemSize = 130; // Change this to make the WEAPON/ITEM bigger or smaller



// This math perfectly auto-centers the item inside the inventory box

                int itemX = invBoxX + (invBoxW - itemSize) / 2;

                int itemY = invBoxY + (invBoxH - itemSize) / 2;



                if (itemImg != null) {

                    g2.drawImage(itemImg, itemX, itemY, itemSize, itemSize, this);

                }



// Draw description below the box

                g2.setFont(new Font(bFont, Font.PLAIN, 14));

                String descText = "Added to inventory.";

                fm = g2.getFontMetrics();

                int dx = contentX + (boxW - fm.stringWidth(descText)) / 2;

// Move text down dynamically based on where the box ends

                g2.drawString(descText, dx, invBoxY + invBoxH + 25);



                g2.dispose();

            }

        };



        foundPanel.setOpaque(false);

        foundPanel.setBounds(0, 0, getWidth(), getHeight());



// 4. Show the panel, wait, then clean up

        SwingUtilities.invokeLater(() -> {

            backgroundLayer.add(foundPanel);

            backgroundLayer.setComponentZOrder(foundPanel, 0);

            backgroundLayer.repaint();

        });



        sleep(3000); // Show popup for 3 seconds



        SwingUtilities.invokeLater(() -> {

            backgroundLayer.remove(foundPanel);

            if (gameMenu != null) gameMenu.setVisible(true);

            levelIndicator.setVisible(true);

            statusLabel.setVisible(true);

            backgroundLayer.repaint();

        });

        sleep(400);

    }



// ==============================

// END GAME

// ==============================

    private void endGame() {
//        MusicManager.fadeOut(2000);

        SwingUtilities.invokeLater(() -> {

            removeAll();

            setLayout(new BorderLayout());

            add(new EndGamePanel(player, characters), BorderLayout.CENTER);

            revalidate();

            repaint();

        });

    }



    private void sleep(int ms) {

        try {

            Thread.sleep(ms);

        } catch (InterruptedException ignored) {

        }

    }
    // ==============================
    // STATUS OVERLAY
    // ==============================
    private void buildStatusOverlay() {
        statusOverlay = new JPanel(null) {
            Image bgImg;

            {
                try {
                    // 🛠️ LOAD THE CUSTOM OVERLAY BACKGROUND
                    java.io.File fBg = new java.io.File("res/ui/panels/overlay-status-panel.png");
                    if (fBg.exists()) bgImg = new ImageIcon(fBg.getAbsolutePath()).getImage();
                } catch (Exception e) {
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                if (bgImg != null) {
                    g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback just in case
                    g2.setColor(new Color(10, 10, 10, 220));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                }
                g2.dispose();
            }
        };
        statusOverlay.setOpaque(false);

        // 🛠️ Adjusted dimensions to match the taller mockup ratio
        int w = 240, h = 260;
        int xPosition = ((900 - w) / 2) ;

        int yPosition = ((700 - h) / 2) - 10;

        statusOverlay.setBounds(xPosition, yPosition, w, h);


        statusCharName = new JLabel("", SwingConstants.CENTER);
        statusCharName.setFont(new Font(bFont, Font.PLAIN, 18));
        statusCharName.setForeground(Color.WHITE);
        statusCharName.setBounds(0, 13, w, 30);

        // Note: Removed the JSeparator since your new image already has a divider line!

        statusTrust = makeStatLabel();
        statusTurnOn = makeStatLabel();
        statusTurnOff = makeStatLabel();
        statusCharisma = makeStatLabel();

        // 🛠️ CREATE THE BUTTON LABEL (Visual only, no click logic)
        statusScore = new JLabel("", SwingConstants.CENTER) {
            Image btnImg;

            {
                try {
                    // Loads the button active image to wrap the text
                    java.io.File fBtn = new java.io.File("res/ui/icon/normal-buttons/button-2-normal-active.png");
                    if (fBtn.exists()) btnImg = new ImageIcon(fBtn.getAbsolutePath()).getImage();
                } catch (Exception e) {
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (btnImg != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.drawImage(btnImg, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();
                }
                g.translate(0, 4);
                super.paintComponent(g); // Draws the text on top of the button image
            }
        };
        statusScore.setFont(new Font(bFont, Font.PLAIN, 13));
        statusScore.setForeground(Color.WHITE);


        int statX = 74;
        int startY = 63;
        int gap = 32;

        statusTrust.setBounds(statX, startY, w - statX, 25);
        statusTurnOn.setBounds(statX, startY + gap, w - statX, 25);
        statusTurnOff.setBounds(statX, startY + gap * 2, w - statX, 25);
        statusCharisma.setBounds(statX, startY + gap * 3, w - statX, 25);


        int btnW = 200, btnH = 50;

        int btnY = 190;

        statusScore.setBounds((w - btnW) / 2, btnY, btnW, btnH);
        statusOverlay.add(statusCharName);
        statusOverlay.add(statusTrust);
        statusOverlay.add(statusTurnOn);
        statusOverlay.add(statusTurnOff);
        statusOverlay.add(statusCharisma);
        statusOverlay.add(statusScore);
        statusOverlay.setVisible(false);
    }

    private JLabel makeStatLabel() {
        // 🛠️ Custom label that aligns the colons perfectly using a fixed pixel offset
        JLabel lbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = fm.getAscent() + (getHeight() - fm.getHeight()) / 2;

                String text = getText();

                // If it's a stat with a colon, split it to align perfectly!
                if (text.contains(":")) {
                    String[] parts = text.split(":");
                    String leftText = parts[0].trim();
                    String rightText = ":" + (parts.length > 1 ? parts[1].trim() : "");

                    // 1. Draw Left Side (e.g., "Turn-Off")
                    g2.setColor(Color.BLACK);
                    g2.drawString(leftText, 2, y + 2);
                    g2.setColor(Color.WHITE);
                    g2.drawString(leftText, 0, y);

                    // 🛠️ CHANGE THIS NUMBER TO MOVE THE COLONS LEFT OR RIGHT!
                    // Increase it to push the ":0" further to the right.
                    int colonPositionX = 85;

                    // 2. Draw Right Side (e.g., ":0") perfectly aligned
                    g2.setColor(Color.BLACK);
                    g2.drawString(rightText, colonPositionX + 2, y + 2);
                    g2.setColor(Color.WHITE);
                    g2.drawString(rightText, colonPositionX, y);

                } else {
                    // Normal drawing if there is no colon
                    g2.setColor(Color.BLACK);
                    g2.drawString(text, 2, y + 2);
                    g2.setColor(Color.WHITE);
                    g2.drawString(text, 0, y);
                }
                g2.dispose();
            }
        };
        lbl.setFont(new Font(bFont, Font.PLAIN, 18)); // 🛠️ Bumped slightly to match the image
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private void showStatusOverlay(Character character, Player player, String effect) {
        // ── play SFX based on effect ──────────────────────
        switch (effect != null ? effect : "NEUTRAL") {
            case "CHARISMA" -> MusicManager.playSFX(MusicManager.CHARM);
            case "TRUST"    -> MusicManager.playSFX(MusicManager.TRUST);
            case "TURN_ON"  -> MusicManager.playSFX(MusicManager.TURN_ON);
            case "TURN_OFF",
                 "TURN_OFF2"-> MusicManager.playSFX(MusicManager.TURN_OFF);
        }

        Relationship r = player.getRelationship(character);
        SwingUtilities.invokeLater(() -> {
            statusCharName.setText(character.getName() + "'s Status");

            // 🛠️ Because of the new makeStatLabel logic, we don't need spaces anymore!
            // Just write "Word:Number" and the code will align it perfectly for you.
            statusTrust.setText("Trust:" + r.getTrust());
            statusTurnOn.setText("Turn-On:" + r.getTurnOn());
            statusTurnOff.setText("Turn-Off:" + r.getTurnOff());
            statusCharisma.setText("Charisma:" + player.getCharisma());

            String effectDisplay = switch (effect != null ? effect : "NEUTRAL") {
                case "CHARISMA" -> "+2 Charisma";
                case "TRUST" -> "+3 Trust";
                case "TURN_ON" -> "+3 Turn-On";
                case "TURN_OFF" -> "+3 Turn-Off";
                case "TURN_OFF2" -> "+6 Turn-Off";
                case "NEUTRAL" -> "No change";
                default -> "...";
            };

            // Note: effect score is using standard spacing because it is centered on a button image
            statusScore.setText("Effect :  " + effectDisplay);
            statusOverlay.setVisible(true);
            MusicManager.playSFX(MusicManager.STATS);
        });

        sleep(2500);
        SwingUtilities.invokeLater(() -> statusOverlay.setVisible(false));
        sleep(300);
    }
}