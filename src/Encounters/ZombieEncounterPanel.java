package Encounters;

import Player.Player;
import Weapon.Weapon;
import Weapon.WeaponInventory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ZombieEncounterPanel extends JPanel {

    // keeping original size as InTurn 23
    private static final int W = 800;
    private static final int H = 600;

    // ==============================
    // UI COMPONENTS
    // ==============================
    // ==============================
    // UI COMPONENTS
    // ==============================
    private JLabel  titleLabel;
    private JLabel  zombieHpLabel;
    private JLabel  playerHpLabel;
    private JLabel  logLabel;
    private JLabel  zombieSprite;

    private JButton dodgeBtn;
    private JButton fightBtn;
    private JButton inventoryBtn;

    // ⚠️ ADDED: For accessing hpBarPanel labels and bars
    private HpBarPanel zombieHpBarPanelInstance;
    private HpBarPanel playerHpBarPanelInstance;

    // ⚠️ ADDED: The texture image for the filled part of the bar
    private Image hpBarTextureFill;


    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    // ==============================
    // GAME STATE
    // ==============================
    private Player player;
    private int    level;
    private int    zombieHp;

    private volatile String pendingAction      = null;
    private volatile int    pendingWeaponIndex = -1;
    private final Object    actionLock         = new Object();
    private final Object discardLock = new Object();
    private volatile boolean discardComplete = false;

    private boolean combatOver = false;

    // Callback when combat ends
    public interface CombatEndListener {
        void onCombatEnd(boolean playerAlive);
    }
    private CombatEndListener combatEndListener;

    // ==============================
    // CONSTRUCTOR
    // ==============================
    public ZombieEncounterPanel(Player player, int level) {
        // ⚠️ FIXED: Load as BufferedImage so TexturePaint works
        try {
            java.io.File fBar = new java.io.File("res/ui/panels/hp-bar-fill.png");
            if (fBar.exists()) {
                hpBarTextureFill = javax.imageio.ImageIO.read(fBar);
            }
        } catch (Exception e) {
            System.out.println("Error loading HP texture: " + e.getMessage());
        }

        this.player   = player;
        this.level    = level;
        this.zombieHp = 50 + (level * 10);

        setLayout(null);
        setPreferredSize(new Dimension(W, H));
        setOpaque(false);
        buildUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public void setCombatEndListener(CombatEndListener listener) {
        this.combatEndListener = listener;
    }

    // ==============================
    // BUILD UI
    // ==============================
    private void buildUI() {
        setLayout(null);

        // =======================================================
        // 1. THE IMAGE-BASED BANNER INTRO (CHAINS)
        // =======================================================
        JPanel bannerPanel = new JPanel(null) {
            Image frameImg;
            Image chainImg;
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

                int frameW = 380;
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

        bannerPanel.setOpaque(false);
        bannerPanel.setBounds(0, 0, W, H);

        int frameW = 380, frameH = 300;
        int frameX = (W - frameW) / 2, frameY = (H - frameH) / 2;

        JLabel bannerTitle = new JLabel("! ZOMBIE ENCOUNTER !", SwingConstants.CENTER);
        bannerTitle.setFont(new Font(bFont, Font.BOLD, 26));
        bannerTitle.setForeground(new Color(255, 80, 80));
        bannerTitle.setBounds(frameX + 20, frameY + 70, frameW - 40, 40);
        bannerPanel.add(bannerTitle);

        JLabel bannerSub = new JLabel("A zombie approaches!", SwingConstants.CENTER) {
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
        bannerSub.setFont(new Font(bFont, Font.BOLD, 16));
        bannerSub.setForeground(Color.WHITE);
        bannerSub.setBounds(frameX + 80, frameY + 180, frameW - 160, 45);
        bannerPanel.add(bannerSub);

        add(bannerPanel);

        // =======================================================
        // 2. INITIALIZE HP BAR PANELS (Must be BEFORE setVisible calls)
        // =======================================================
        int startingZp = 50 + (level * 10);
        zombieHpBarPanelInstance = new HpBarPanel("Zombie HP", false, startingZp, startingZp, "res/ui/panels/hp-status-panel-zombie.png");
        zombieHpBarPanelInstance.setBounds(0, 0, 380, 80);
        add(zombieHpBarPanelInstance);

        playerHpBarPanelInstance = new HpBarPanel("Your HP", true, player.getHealth(), 100, "res/ui/panels/hp-status-panel-player.png");
        playerHpBarPanelInstance.setBounds(520, 0, 380, 74);
        add(playerHpBarPanelInstance);

        // =======================================================
        // 3. REMAINING COMBAT UI
        // =======================================================
        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setBounds(0, 30, W, 40);
        add(titleLabel);

        logLabel = new JLabel("", SwingConstants.CENTER);
        logLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        logLabel.setForeground(Color.WHITE);
        logLabel.setBounds(0, 185, W, 30);
        add(logLabel);

        dodgeBtn      = makeCombatButton("Dodge",     Color.WHITE);
        fightBtn      = makeCombatButton("Fight",     Color.WHITE);
        inventoryBtn  = makeCombatButton("Inventory", Color.WHITE);

        int btnW = 230, btnH = 74, gap = 20, startX = 70, buttonY = 550;
        dodgeBtn.setBounds(startX, buttonY, btnW, btnH);
        fightBtn.setBounds(startX + btnW + gap, buttonY, btnW, btnH);
        inventoryBtn.setBounds(startX + (btnW + gap) * 2, buttonY, btnW, btnH);

        add(dodgeBtn);
        add(fightBtn);
        add(inventoryBtn);

        zombieSprite = new JLabel();
        java.io.File f = new java.io.File("res/sprite/zombie.png");
        if (f.exists()) {
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            Image scaled = raw.getImage().getScaledInstance(300, 450, Image.SCALE_SMOOTH);
            zombieSprite.setIcon(new ImageIcon(scaled));
        }
        zombieSprite.setBounds(250, 100, 400, 550);
        add(zombieSprite);

        // =======================================================
        // 4. Z-ORDERING
        // =======================================================
        setComponentZOrder(bannerPanel, 0);
        setComponentZOrder(dodgeBtn, 1);
        setComponentZOrder(fightBtn, 2);
        setComponentZOrder(inventoryBtn, 3);
        setComponentZOrder(zombieHpBarPanelInstance, 4);
        setComponentZOrder(playerHpBarPanelInstance, 5);
        setComponentZOrder(zombieSprite, getComponentCount() - 1);

        // =======================================================
        // 5. HIDE EVERYTHING EXCEPT THE BANNER INITIALLY
        // =======================================================
        titleLabel.setVisible(false);
        logLabel.setVisible(false);
        dodgeBtn.setVisible(false);
        fightBtn.setVisible(false);
        inventoryBtn.setVisible(false);
        zombieSprite.setVisible(false);
        zombieHpBarPanelInstance.setVisible(false);
        playerHpBarPanelInstance.setVisible(false);

        // Timer to reveal the combat screen
        new Thread(() -> {
            sleep(2500);
            SwingUtilities.invokeLater(() -> {
                bannerPanel.setVisible(false);
                titleLabel.setVisible(true);
                logLabel.setVisible(true);
                zombieHpBarPanelInstance.setVisible(true);
                playerHpBarPanelInstance.setVisible(true);
                dodgeBtn.setVisible(true);
                fightBtn.setVisible(true);
                inventoryBtn.setVisible(true);
                zombieSprite.setVisible(true);
            });
        }).start();

        buildInventoryPanel();
        updateHpLabels();

        dodgeBtn.addActionListener(e -> triggerAction("DODGE"));
        fightBtn.addActionListener(e -> triggerAction("FIGHT"));
        inventoryBtn.addActionListener(e -> showInventoryPanel());
    }

    // ==============================
    // INVENTORY PANEL
    // ==============================
    private JPanel inventoryPanel;

    private void buildInventoryPanel() {

        inventoryPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 10, 10, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // CHANGED: Inventory Panel border to black
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        inventoryPanel.setOpaque(false);
        // original placement InTurn 23
        inventoryPanel.setBounds(150, 330, 500, 240);
        inventoryPanel.setVisible(false);

        JLabel invTitle = new JLabel("INVENTORY", SwingConstants.CENTER);
        invTitle.setFont(new Font("Consolas", Font.BOLD, 16));
        invTitle.setForeground(new Color(220, 60, 60));
        invTitle.setBounds(0, 10, 500, 25);
        inventoryPanel.add(invTitle);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 38, 460, 2);
        // CHANGED: Separator to black to match theme
        sep.setForeground(Color.BLACK);
        inventoryPanel.add(sep);

        add(inventoryPanel);
    }

    private void showInventoryPanel() {
        inventoryPanel.removeAll();

        JLabel invTitle = new JLabel("INVENTORY", SwingConstants.CENTER);
        invTitle.setFont(new Font("Consolas", Font.BOLD, 16));
        invTitle.setForeground(new Color(220, 60, 60));
        invTitle.setBounds(0, 10, 500, 25);
        inventoryPanel.add(invTitle);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 38, 460, 2);
        sep.setForeground(Color.BLACK);
        inventoryPanel.add(sep);

        WeaponInventory wi = player.getWeaponInventory();
        int yPos = 50;

        if (wi.getSize() > 0) {
            JLabel wTitle = new JLabel("WEAPONS", SwingConstants.LEFT);
            wTitle.setFont(new Font("Consolas", Font.BOLD, 13));
            wTitle.setForeground(new Color(180, 180, 60));
            wTitle.setBounds(20, yPos, 460, 20);
            inventoryPanel.add(wTitle);
            yPos += 22;

            for (int i = 0; i < wi.getSize(); i++) {
                Weapon w = wi.getInventory().get(i);
                final int idx = i;
                JButton wBtn = makeInventoryItemButton(
                        w.getName() + "  |  DMG: " + w.getDamage()
                                + "  |  DUR: " + w.getDurability() + "/" + w.getMaxDurability());
                wBtn.setBounds(20, yPos, 460, 36);
                wBtn.addActionListener(e -> {
                    inventoryPanel.setVisible(false);
                    synchronized (actionLock) {
                        pendingAction      = "WEAPON";
                        pendingWeaponIndex = idx;
                        actionLock.notifyAll();
                    }
                });
                inventoryPanel.add(wBtn);
                yPos += 42;
            }
        }

        if (player.hasConsumables()) {
            JLabel hTitle = new JLabel("HEALING ITEMS", SwingConstants.LEFT);
            hTitle.setFont(new Font("Consolas", Font.BOLD, 13));
            hTitle.setForeground(new Color(80, 200, 120));
            hTitle.setBounds(20, yPos, 460, 20);
            inventoryPanel.add(hTitle);
            yPos += 22;

            List<String> items = new ArrayList<>(player.showConsumableInventory());
            for (String item : items) {
                JButton iBtn = makeInventoryItemButton(item);
                iBtn.setBounds(20, yPos, 460, 36);
                iBtn.addActionListener(e -> {
                    String rawName = item.contains(" x") ? item.substring(0, item.indexOf(" x")) : item;
                    boolean used = player.useConsumable(rawName);
                    inventoryPanel.setVisible(false);
                    SwingUtilities.invokeLater(() -> {
                        updateHpLabels();
                        int healAmt = switch (rawName) {
                            case "Medkit"  -> 25;
                            case "Bandage" -> 15;
                            default        -> 0;
                        };
                        setLog(used ? "Used " + rawName + "! +" + healAmt + " HP restored." : "Your HP is already full!");
                    });
                });
                inventoryPanel.add(iBtn);
                yPos += 42;
            }
        }

        JButton cancelBtn = makeCombatButton("CANCEL", Color.WHITE);
        cancelBtn.setBounds(170, yPos + 4, 160, 36);
        cancelBtn.addActionListener(e -> inventoryPanel.setVisible(false));
        inventoryPanel.add(cancelBtn);

        int newH = yPos + 56;
        // original placement InTurn 23
        inventoryPanel.setBounds(150, 600 - newH - 20, 500, newH);
        inventoryPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void showDiscardPanel(Weapon newWeapon) {
        inventoryPanel.removeAll();

        JLabel title = new JLabel("INVENTORY FULL! CHOOSE WEAPON TO DISCARD",
                SwingConstants.CENTER);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        title.setForeground(new Color(220, 60, 60));
        title.setBounds(0, 10, 500, 25);
        inventoryPanel.add(title);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 38, 460, 2);
        sep.setForeground(new Color(180, 30, 30));
        inventoryPanel.add(sep);

        JLabel newLbl = new JLabel(
                "NEW:  " + newWeapon.getName()
                        + "  |  DMG: " + newWeapon.getDamage()
                        + "  |  DUR: " + newWeapon.getDurability()
                        + "/" + newWeapon.getMaxDurability(),
                SwingConstants.CENTER);
        newLbl.setFont(new Font("Consolas", Font.PLAIN, 11));
        newLbl.setForeground(new Color(80, 200, 120));
        newLbl.setBounds(20, 44, 460, 20);
        inventoryPanel.add(newLbl);

        WeaponInventory wi = player.getWeaponInventory();
        int yPos = 72;

        JLabel prompt = new JLabel("Select a weapon to replace:", SwingConstants.LEFT);
        prompt.setFont(new Font("Consolas", Font.BOLD, 12));
        prompt.setForeground(new Color(180, 180, 60));
        prompt.setBounds(20, yPos, 460, 20);
        inventoryPanel.add(prompt);
        yPos += 24;

        for (int i = 0; i < wi.getSize(); i++) {
            Weapon w = wi.getInventory().get(i);
            final int idx = i;

            JButton discardBtn = makeInventoryItemButton(
                    w.getName() + "  |  DMG: " + w.getDamage()
                            + "  |  DUR: " + w.getDurability()
                            + "/" + w.getMaxDurability());
            discardBtn.setBounds(20, yPos, 460, 36);
            discardBtn.addActionListener(e -> {
                wi.replaceWeapon(idx, newWeapon);
                inventoryPanel.setVisible(false);
                SwingUtilities.invokeLater(() ->
                        setLog("Discarded " + w.getName()
                                + "!  Equipped " + newWeapon.getName() + ".")
                );
                synchronized (discardLock) {
                    discardComplete = true;
                    discardLock.notifyAll();
                }
            });
            inventoryPanel.add(discardBtn);
            yPos += 42;
        }

        // Skip — keep current weapons
        JButton skipBtn = makeCombatButton("SKIP", new Color(60, 60, 60));
        skipBtn.setBounds(170, yPos + 4, 160, 36);
        skipBtn.addActionListener(e -> {
            inventoryPanel.setVisible(false);
            SwingUtilities.invokeLater(() ->
                    setLog(newWeapon.getName() + " discarded. Kept current weapons.")
            );
            synchronized (discardLock) {
                discardComplete = true;
                discardLock.notifyAll();
            }
        });
        inventoryPanel.add(skipBtn);

        int newH = yPos + 56;
        inventoryPanel.setBounds(150, 600 - newH - 20, 500, newH);
        inventoryPanel.setVisible(true);
        revalidate();
        repaint();
    }

    public void startCombat() {
        new Thread(() -> {
            WeaponInventory wi = player.getWeaponInventory();

            while (player.isAlive() && zombieHp > 0) {
                pendingAction = null;
                setButtonsEnabled(true);

                synchronized (actionLock) {
                    while (pendingAction == null) {
                        try { actionLock.wait(); }
                        catch (InterruptedException ignored) {}
                    }
                }

                setButtonsEnabled(false);
                String logMsg = "";

                switch (pendingAction) {
                    case "DODGE":
                        int hpBefore = zombieHp;
                        zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "1", -1);
                        if (zombieHp < hpBefore) logMsg = "Agile! You dodged and counter-attacked!";
                        else logMsg = "Too slow! The zombie caught you.";
                        break;

                    case "FIGHT":
                        zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "2", -1);
                        logMsg = "You threw a desperate punch!";
                        break;

                    case "WEAPON":
                        if (pendingWeaponIndex >= 0) {
                            Weapon w = wi.getInventory().get(pendingWeaponIndex);
                            zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "3", pendingWeaponIndex);
                            logMsg = "You used " + w.getName() + "!";
                        }
                        break;
                }

                final String finalLog = logMsg;
                SwingUtilities.invokeLater(() -> {
                    updateHpLabels();
                    setLog(finalLog);
                });
                sleep(800);
            }

            boolean playerAlive = player.isAlive();

            if (zombieHp <= 0 && playerAlive) {
                player.heal(10);
                Weapon found = WeaponInventory.getRandomWeapon();

                if (level >= 4 && wi.getSize() >= 3) {
                    // Reset discard flag
                    discardComplete = false;

                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        setLog("Inventory full! Choose a weapon to discard.");
                        showDiscardPanel(found);
                    });

                    // Wait for player to finish discarding before proceeding
                    synchronized (discardLock) {
                        while (!discardComplete) {
                            try { discardLock.wait(); }
                            catch (InterruptedException ignored) {}
                        }
                    }

                } else {
                    if (wi.getSize() < 3) {
                        wi.addWeapon(found);
                    }
                    String healMsg = player.getHealth() < 100
                            ? "  |  Healed 10 HP."
                            : "";
                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        setLog("Victory! Found: " + found.getName() + healMsg);
                    });
                }

            } else if (!playerAlive) {
                SwingUtilities.invokeLater(() -> {
                    setButtonsEnabled(false);
                    setLog("Death has claimed you...");
                });
            }

            sleep(2000);
            if (combatEndListener != null) combatEndListener.onCombatEnd(playerAlive);

        }).start();
    }

    private void triggerAction(String action) {
        synchronized (actionLock) {
            pendingAction = action;
            actionLock.notifyAll();
        }
    }

    private void updateHpLabels() {
        SwingUtilities.invokeLater(() -> {
            if (zombieHpBarPanelInstance != null) {
                zombieHpBarPanelInstance.setHp(Math.max(0, zombieHp), (50 + level * 10));
            }
            if (playerHpBarPanelInstance != null) {
                playerHpBarPanelInstance.setHp(Math.max(0, player.getHealth()), 100);
            }
            revalidate();
            repaint();
        });
    }

    private void setLog(String msg) { logLabel.setText(msg); }

    private void setButtonsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            dodgeBtn.setEnabled(enabled);
            fightBtn.setEnabled(enabled);
            inventoryBtn.setEnabled(enabled);
        });
    }

    private JButton makeCombatButton(String text, Color baseColor) {
        // Load the 3 image states for the buttons
        Image normalImg = null, hoverImg = null, activeImg = null;
        try {
            java.io.File f1 = new java.io.File("res/ui/icon/normal-buttons/button-2-normal-not-active.png");
            java.io.File f2 = new java.io.File("res/ui/icon/normal-buttons/button-2-normal-hover.png");
            java.io.File f3 = new java.io.File("res/ui/icon/normal-buttons/button-2-normal-active.png");
            if (f1.exists()) normalImg = new ImageIcon(f1.getAbsolutePath()).getImage();
            if (f2.exists()) hoverImg = new ImageIcon(f2.getAbsolutePath()).getImage();
            if (f3.exists()) activeImg = new ImageIcon(f3.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        final Image btnNormal = normalImg;
        final Image btnHover = hoverImg;
        final Image btnActive = activeImg;

        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(new Font(bFont, Font.BOLD, 16));
                setForeground(Color.WHITE); // White text so it shows up on dark images

                setHorizontalTextPosition(JButton.CENTER);
                setVerticalTextPosition(JButton.CENTER);

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
                Image currentImg;

                if (isPressed) {
                    currentImg = btnActive;
                } else if (hovered) {
                    currentImg = btnHover;
                } else {
                    currentImg = btnNormal;
                }

                // Draw the specific image based on the state
                if (currentImg != null) {
                    g2.drawImage(currentImg, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Fallback just in case images are missing
                    g2.setColor(new Color(62, 55, 49));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                g.translate(4, 5);

                // Physical button press visual effect requested
                if (isPressed) {
                    g.translate(-3, 3);
                }

                super.paintComponent(g);

                if (isPressed) {
                    g.translate(3, -3); // Revert translation
                }

                g.translate(-4, -5); // Reset Text offset
            }
        };
        return btn;
    }

    private JButton makeInventoryItemButton(String text) {
        // Inventory items now use the same white/black theme
        JButton btn = makeCombatButton(text, Color.WHITE);
        btn.setFont(new Font("Consolas", Font.PLAIN, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }

    private class HpBarPanel extends JPanel {
        private Image framePanelImg;
        private Image statusBarImg;
        private JLabel hpTitle, hpValLabel;
        private int currentHp, maxHp;

        private final int barW = 350;
        private final int barH = 24;

        public HpBarPanel(String titleText, boolean rightAligned, int startingHp, int startMaxHp, String framePath) {
            setLayout(null);
            setOpaque(false);

            this.currentHp = startingHp;
            this.maxHp = startMaxHp;

            java.io.File fFrame = new java.io.File(framePath);
            if (fFrame.exists()) framePanelImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();

            java.io.File fStatusBar = new java.io.File("res/ui/panels/status-bar.png");
            if (fStatusBar.exists()) statusBarImg = new ImageIcon(fStatusBar.getAbsolutePath()).getImage();

            int titleX = rightAligned ? (380 - 165) : 15;
            int hpValX = rightAligned ? (380 - 115) : 15;

            hpTitle = new JLabel(titleText, SwingConstants.LEFT);
            hpTitle.setFont(new Font(mainFont, Font.BOLD, 18));
            hpTitle.setForeground(new Color(255, 220, 100));
            hpTitle.setBounds(titleX, 5, 150, 25);
            add(hpTitle);

            hpValLabel = new JLabel(startingHp + " / " + startMaxHp, SwingConstants.LEFT);
            hpValLabel.setFont(new Font(bFont, Font.BOLD, 16));
            hpValLabel.setForeground(Color.WHITE);
            hpValLabel.setBounds(hpValX, 25, 100, 25);
            add(hpValLabel);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            if (framePanelImg != null) {
                g2.drawImage(framePanelImg, 0, 0, getWidth(), getHeight(), this);
            }

            int barX = 15;
            int barY = hpValLabel.getY() + 23;

            // Draw the empty status bar frame
            if (statusBarImg != null) {
                g2.drawImage(statusBarImg, barX, barY, barW, barH, this);
            } else {
                g2.setColor(new Color(40, 40, 40));
                g2.fillRoundRect(barX, barY, barW, barH, 5, 5);
            }

            int fillOffsetX = 4;
            int fillOffsetY = 4;
            int fillMaxW = barW - (fillOffsetX * 2);
            int fillH = barH - (fillOffsetY * 2);

            float percent = (float) Math.max(0, currentHp) / (float)maxHp;
            int currentFillW = (int)(fillMaxW * percent);

            if (currentFillW > 0) {
                Color dynamicColor = getHpColor(currentHp, maxHp);

                // ⚠️ FIXED: Added proper BufferedImage check for the texture
                if (hpBarTextureFill instanceof java.awt.image.BufferedImage) {
                    TexturePaint tp = new TexturePaint((java.awt.image.BufferedImage) hpBarTextureFill,
                            new Rectangle(0, 0, 32, fillH));
                    g2.setPaint(tp);
                } else {
                    g2.setColor(dynamicColor); // Fallback to solid color if texture fails
                }

                int visualW = Math.max(2, currentFillW);
                g2.fillRect(barX + fillOffsetX, barY + fillOffsetY, visualW, fillH);
            }

            g2.dispose();
        }

        public void setHp(int current, int max) {
            this.currentHp = current;
            this.maxHp = max;
            hpValLabel.setText(Math.max(0, current) + " / " + max);
            repaint();
        }
    }

    // Helper method to get color based on HP percentage (Green -> Yellow -> Red)
    private Color getHpColor(int hp, int maxHp) {
        float percent = (float) hp / (float) maxHp;
        if (percent >= 0.6f) return new Color(80, 220, 120); // Green
        else if (percent >= 0.3f) return new Color(255, 220, 60); // Yellow
        return new Color(220, 80, 80); // Red
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}