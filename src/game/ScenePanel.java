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
import java.util.List;
import java.util.Map;

public class ScenePanel extends JPanel {

    //FONT
    private String mainFont="PixelArmy";
    private String bFont="Munro";

    // ==============================
    // LAYERS
    // ==============================
    private BackgroundLayer backgroundLayer;
    private DialogueBoxLayer dialogueBoxLayer;
    private ChoiceButtonLayer choiceButtonLayer;

    // ==============================
    // GAME STATE
    // ==============================
    private Player player;
    private List<Character> characters;
    private ConversationManager conversationManager;
    private int currentLevel = 1;
    private boolean gameRunning = true;

    // Bridges GUI button click back to game loop thread
    private volatile String pendingChoice = null;
    private final Object choiceLock = new Object();

    // ==============================
    // LEVEL TITLE OVERLAY
    // ==============================
    private JPanel levelTitleOverlay;
    private JLabel levelNumberLabel;
    private JLabel levelTitleLabel;
    private JLabel levelHintLabel;

    // ==============================
    // STATUS BAR
    // ==============================
    private JLabel statusLabel;
    private JPanel statusOverlay;
    private JLabel statusCharName;
    private JLabel statusTrust;
    private JLabel statusTurnOn;
    private JLabel statusTurnOff;
    private JLabel statusCharisma;
    private JLabel statusScore;

    // ==============================
    // CONSTANTS
    // ==============================
    private static final String[] LEVEL_NAMES = {
            "Abandoned Compound",
            "Temporary Shelter",
            "City Ruins",
            "Safehouse Conflict",
            "Escape Route"
    };

    private static final String[] LEVEL_BACKGROUNDS = {
            "level1.png",
            "level2.png",
            "level3.png",
            "level4.png",
            "level5.png"
    };

    // ==============================
    // CONSTRUCTOR
    // ==============================
    public ScenePanel(Player player,
                      List<Character> characters,
                      ConversationManager conversationManager) {

        this.player = player;
        this.characters = characters;
        this.conversationManager = conversationManager;

        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        setBackground(Color.BLACK);

        buildLayers();
        buildLevelTitleOverlay();
        buildStatusBar();
        buildStatusOverlay();

        // Z-order: lower index = drawn on top
        setComponentZOrder(statusLabel,       0);
        setComponentZOrder(statusOverlay,     1); // ADD THIS
        setComponentZOrder(choiceButtonLayer, 2);
        setComponentZOrder(dialogueBoxLayer,  3);
        setComponentZOrder(levelTitleOverlay, 4);
        setComponentZOrder(backgroundLayer,   getComponentCount() - 1);


    }

    // ==============================
    // BUILD LAYERS
    // ==============================
    private void buildLayers() {

        backgroundLayer = new BackgroundLayer();
        backgroundLayer.setBounds(0, 0, 1280, 720);
        backgroundLayer.setBackgroundColor(Color.BLACK);

        dialogueBoxLayer = new DialogueBoxLayer();
        dialogueBoxLayer.setBounds(0, 0, 1280, 720);
        dialogueBoxLayer.setVisible(false);

        choiceButtonLayer = new ChoiceButtonLayer();
        choiceButtonLayer.setBounds(0, 0, 1280, 720);
        choiceButtonLayer.setVisible(false);

        add(backgroundLayer);
        add(dialogueBoxLayer);
        add(choiceButtonLayer);
    }

    private void buildLevelTitleOverlay() {

        levelTitleOverlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 190));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        levelTitleOverlay.setOpaque(false);
        levelTitleOverlay.setBounds(0, 0, 800, 600);

        levelNumberLabel = new JLabel("", SwingConstants.CENTER);
        levelNumberLabel.setFont(new Font(bFont, Font.BOLD, 20));
        levelNumberLabel.setForeground(new Color(200, 50, 50));
        levelNumberLabel.setBounds(0, 220, 800, 30);

        levelTitleLabel = new JLabel("", SwingConstants.CENTER);
        levelTitleLabel.setFont(new Font(bFont, Font.BOLD, 38));
        levelTitleLabel.setForeground(Color.WHITE);
        levelTitleLabel.setBounds(0, 260, 800, 55);

        levelHintLabel = new JLabel("— Loading —", SwingConstants.CENTER);
        levelHintLabel.setFont(new Font(bFont, Font.PLAIN, 13));
        levelHintLabel.setForeground(new Color(150, 150, 150));
        levelHintLabel.setBounds(0, 330, 800, 25);

        levelTitleOverlay.add(levelNumberLabel);
        levelTitleOverlay.add(levelTitleLabel);
        levelTitleOverlay.add(levelHintLabel);

        add(levelTitleOverlay);
        levelTitleOverlay.setVisible(false);
    }
    private void buildStatusBar() {

        statusLabel = new JLabel("", SwingConstants.RIGHT);
        statusLabel.setFont(new Font(bFont, Font.PLAIN, 13));
        statusLabel.setForeground(new Color(220, 220, 220));
        statusLabel.setBounds(880, 10, 380, 25);
        statusLabel.setOpaque(false);

        add(statusLabel);
    }

    // ==============================
    // ENTRY POINT — called from Story.java
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

    // ==============================
    // LEVEL TEMPLATE
    // ==============================
    private void playLevelTemplate(int level, String title) {

        if (!gameRunning) return;

        // Set background
        SwingUtilities.invokeLater(() -> {
            backgroundLayer.setBackgroundFromFile(LEVEL_BACKGROUNDS[level - 1]);
            dialogueBoxLayer.setVisible(false);
            choiceButtonLayer.setVisible(false);
        });

        sleep(300);

        // Show level title card
        showLevelTitle(level, title);

        sleep(3000);

        // Hide title overlay
        SwingUtilities.invokeLater(() -> levelTitleOverlay.setVisible(false));

        sleep(400);

        // 3 conversations per level
        for (int conversationNum = 1; conversationNum <= 3; conversationNum++) {

            if (!gameRunning) break;

            final int convNum = conversationNum;

            SwingUtilities.invokeLater(() ->
                    updateStatus("Level " + level
                            + "  |  Conversation " + convNum + " of 3")
            );

            // Each character gets a conversation
            for (Character character : characters) {

                if (!gameRunning) break;

                runConversationGUI(player, character, level, conversationNum);
            }

            if (!gameRunning) break;

            // Zombie encounter after conversation 3
            if (conversationNum == 3) {
                zombieEncounterGUI(level);
            }
        }
    }

    // ==============================
    // LEVEL TITLE CARD
    // ==============================
    private void showLevelTitle(int level, String title) {

        SwingUtilities.invokeLater(() -> {

            levelNumberLabel.setText("LEVEL  " + level);
            levelTitleLabel.setText(title.toUpperCase());
            levelHintLabel.setText("— Loading —");
            levelTitleOverlay.setVisible(true);

            revalidate();
            repaint();
        });

        sleep(1500);

        SwingUtilities.invokeLater(() ->
                levelHintLabel.setText("— Get Ready —")
        );
    }

    // ==============================
    // CONVERSATION GUI
    // ==============================
    private void runConversationGUI(Player player,
                                    Character character,
                                    int level,
                                    int conversationNum) {

        // Get dialogue and choices from ConversationManager
        String dialogue = conversationManager.getQuestion(
                character, level, conversationNum);

        Map<String, String> choices = conversationManager.displayChoices(
                character.getName(), level, conversationNum);

        if (dialogue == null || choices == null) return;

        // ---- Show character dialogue ----
        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.setSpeaker(character.getName());
            dialogueBoxLayer.setDialogue(dialogue);
            dialogueBoxLayer.setVisible(true);
            choiceButtonLayer.setVisible(false);
            revalidate();
            repaint();
        });

        // Wait for typewriter to finish
        sleep(dialogue.length() * 14 + 800);

        // ---- Show player choices ----
        pendingChoice = null;

        SwingUtilities.invokeLater(() -> {

            choiceButtonLayer.clearChoices();

            for (Map.Entry<String, String> entry : choices.entrySet()) {
                String key  = entry.getKey();
                String text = key + ".  " + entry.getValue();
                choiceButtonLayer.addChoice(text, key);
            }

            choiceButtonLayer.setChoiceListener((choiceText, nextNode) -> {
                synchronized (choiceLock) {
                    pendingChoice = nextNode;
                    choiceLock.notifyAll();
                }
            });

            choiceButtonLayer.showChoices();
        });

        // Wait for player to click a choice
        synchronized (choiceLock) {
            while (pendingChoice == null) {
                try {
                    choiceLock.wait();
                } catch (InterruptedException ignored) {}
            }
        }

        final String choiceMade = pendingChoice;

        // ---- Hide choices ----
        SwingUtilities.invokeLater(() -> choiceButtonLayer.setVisible(false));

        sleep(300);

        // ---- Show character response ----
        ConversationManager.ChoiceOutcome outcome =
                conversationManager.getChoiceOutcome(
                        character.getName(), level, conversationNum, choiceMade);

        if (outcome != null) {

            final String response = outcome.response;

            SwingUtilities.invokeLater(() -> {
                dialogueBoxLayer.setSpeaker(character.getName());
                dialogueBoxLayer.setDialogue(response);
                dialogueBoxLayer.setVisible(true);
            });

            sleep(response.length() * 14 + 1200);

            // Apply effect FIRST so score is correct
            conversationManager.applyEffect(player, character, outcome.effect);

            // Show status overlay — score now reflects updated values
            showStatusOverlay(character, player);
        }

        // ---- Clear dialogue ----
        SwingUtilities.invokeLater(() -> {
            dialogueBoxLayer.clear();
            dialogueBoxLayer.setVisible(false);
        });

        sleep(400);
    }
    // ==============================
    // ZOMBIE ENCOUNTER GUI
    // ==============================
    private void zombieEncounterGUI(int level) {

        final Object  combatLock = new Object();
        final boolean[] survived = {true};

        SwingUtilities.invokeLater(() -> {

            ZombieEncounterPanel zep =
                    new ZombieEncounterPanel(player, level);

            // Use null layout — set bounds manually
            zep.setBounds(0, 0, 800, 600);

            zep.setCombatEndListener(playerAlive -> {
                survived[0] = playerAlive;
                if (!playerAlive) gameRunning = false;

                SwingUtilities.invokeLater(() -> {
                    remove(zep);
                    setLayout(null); // restore null layout
                    revalidate();
                    repaint();
                });

                synchronized (combatLock) {
                    combatLock.notifyAll();
                }
            });

            // Keep null layout — just add and bring to front
            setLayout(null);
            add(zep);
            setComponentZOrder(zep, 0); // bring to top
            revalidate();
            repaint();

            zep.startCombat();
        });

        synchronized (combatLock) {
            try { combatLock.wait(); }
            catch (InterruptedException ignored) {}
        }

        sleep(400);
    }

    private void endGame() {

        SwingUtilities.invokeLater(() -> {

            removeAll();
            setLayout(new BorderLayout());

            EndGamePanel egp = new EndGamePanel(player, characters);
            add(egp, BorderLayout.CENTER);

            revalidate();
            repaint();
        });
    }

    // ==============================
    // HELPERS
    // ==============================
    private void updateStatus(String text) {
        statusLabel.setText(text + "   ");
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    private void buildStatusOverlay() {

        statusOverlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark frosted panel
                g2.setColor(new Color(10, 10, 10, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Red border
                g2.setColor(new Color(180, 30, 30, 200));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        statusOverlay.setOpaque(false);

        int w = 300, h = 200;
        int x = (800 - w) / 2;
        int y = (600 - h) / 2;
        statusOverlay.setBounds(x, y, w, h);

        // Character name header
        statusCharName = new JLabel("", SwingConstants.CENTER);
        statusCharName.setFont(new Font(bFont, Font.BOLD, 18));
        statusCharName.setForeground(new Color(220, 60, 60));
        statusCharName.setBounds(0, 12, w, 25);

        // Separator line drawn via border
        JSeparator sep = new JSeparator();
        sep.setBounds(20, 42, w - 40, 2);
        sep.setForeground(new Color(180, 30, 30));

        // Stats
        statusTrust = makeStatLabel(w);
        statusTurnOn = makeStatLabel(w);
        statusTurnOff = makeStatLabel(w);
        statusCharisma = makeStatLabel(w);
        statusScore = makeStatLabel(w);

        statusTrust.setBounds(30, 52, w - 40, 22);
        statusTurnOn.setBounds(30, 76, w - 40, 22);
        statusTurnOff.setBounds(30, 100, w - 40, 22);
        statusCharisma.setBounds(30, 124, w - 40, 22);
        statusScore.setBounds(30, 152, w - 40, 22);
        statusScore.setForeground(new Color(220, 180, 60)); // gold for final score

        statusOverlay.add(statusCharName);
        statusOverlay.add(sep);
        statusOverlay.add(statusTrust);
        statusOverlay.add(statusTurnOn);
        statusOverlay.add(statusTurnOff);
        statusOverlay.add(statusCharisma);
        statusOverlay.add(statusScore);

        add(statusOverlay);
        statusOverlay.setVisible(false);
    }

    private JLabel makeStatLabel(int panelWidth) {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font(bFont, Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private void showStatusOverlay(Character character, Player player) {

        Relationship r = player.getRelationship(character);

        // Calculate AFTER effect has been applied
        double score = r.calculateFinalScore(player.getCharisma());

        SwingUtilities.invokeLater(() -> {

            statusCharName.setText(character.getName().toUpperCase() + "  STATUS");
            statusTrust.setText("Trust        :  " + r.getTrust());
            statusTurnOn.setText("Turn-On    :  " + r.getTurnOn());
            statusTurnOff.setText("Turn-Off    :  " + r.getTurnOff());
            statusCharisma.setText("Charisma  :  " + player.getCharisma());
            statusScore.setText("Score        :  " + String.format("%.1f", score));

            statusOverlay.setVisible(true);
            revalidate();
            repaint();
        });

        sleep(2500);

        SwingUtilities.invokeLater(() -> statusOverlay.setVisible(false));

        sleep(300);
    }
}