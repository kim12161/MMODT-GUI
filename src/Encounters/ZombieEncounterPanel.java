package Encounters;

import Player.Player;
import Weapon.Weapon;
import Weapon.WeaponInventory;
import Interaction.BackgroundLayer;
import game.GameFonts;
import main.GamePanel;
import saveSystem.GameMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ZombieEncounterPanel extends JPanel {

    // keeping original size as InTurn 23
    private static final int W = 900;
    private static final int H = 700;

    // ==============================
    // UI COMPONENTS
    // ==============================
    private GameMenu gameMenu;
    private JLabel titleLabel;
    private JLabel zombieHpLabel;
    private JLabel playerHpLabel;
    private JLabel logLabel;
    private JLabel zombieSprite;

    private JButton dodgeBtn;
    private JButton fightBtn;
    private JButton inventoryBtn;

    // ⚠️ For accessing hpBarPanel labels and bars
    private HpBarPanel zombieHpBarPanelInstance;
    private HpBarPanel playerHpBarPanelInstance;

    // ⚠️ The texture image for the filled part of the bar
    private Image hpBarTextureFill;

    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    // ==============================
    // GAME STATE
    // ==============================
    private Player player;
    private int level;
    private int zombieHp;

    private volatile String pendingAction = null;
    private volatile int pendingWeaponIndex = -1;
    private final Object actionLock = new Object();
    private final Object discardLock = new Object();
    private volatile boolean discardComplete = false;

    // ⚠️ ADDED: State to track which inventory tab is open
    private boolean isWeaponsTabOpen = true;

    private boolean combatOver = false;

    // Callback when combat ends
    public interface CombatEndListener {
        void onCombatEnd(boolean playerAlive);
    }

    private CombatEndListener combatEndListener;

    // ==============================
    // CONSTRUCTOR
    // ==============================
    public ZombieEncounterPanel(Player player, int level, GameMenu gameMenu) {
        this.gameMenu = gameMenu;
        try {
            java.io.File fBar = new java.io.File("res/ui/panels/hp-bar-fill.png");
            if (fBar.exists()) {
                hpBarTextureFill = javax.imageio.ImageIO.read(fBar);
            }
        } catch (Exception e) {
            System.out.println("Error loading HP texture: " + e.getMessage());
        }

        this.player = player;
        this.level = level;
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
                java.io.File fFrame = new java.io.File("res/ui/panels/main-panel-no-leaves.png");
                if (fFrame.exists()) frameImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();

                java.io.File fChain = new java.io.File("res/ui/icon/assets/chains-zombie.png");
                if (fChain.exists()) chainImg = new ImageIcon(fChain.getAbsolutePath()).getImage();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, getWidth(), getHeight());


                int frameW = 340, frameH = 250;
                int frameX = (W - frameW) / 2;
                int frameY = (H - frameH) / 2;

                if (chainImg != null) {
                    int chainThickness = 24; // How wide the chain is
                    int inset = 15; // How far into the box the chains connect

                    // Top-Left corner of screen -> Top-Left of panel
                    drawDiagonalChain(g2, chainImg, 0, 0, frameX + inset, frameY + inset, chainThickness);

                    // Top-Right corner of screen -> Top-Right of panel
                    drawDiagonalChain(g2, chainImg, getWidth(), 0, frameX + frameW - inset, frameY + inset, chainThickness);

                    // Bottom-Left corner of screen -> Bottom-Left of panel
                    drawDiagonalChain(g2, chainImg, 0, getHeight(), frameX + inset, frameY + frameH - inset, chainThickness);

                    // Bottom-Right corner of screen -> Bottom-Right of panel
                    drawDiagonalChain(g2, chainImg, getWidth(), getHeight(), frameX + frameW - inset, frameY + frameH - inset, chainThickness);
                }
                if (frameImg != null) {
                    g2.drawImage(frameImg, frameX, frameY, frameW, frameH, this);
                }
                g2.dispose();
            }


            private void drawDiagonalChain(Graphics2D g2, Image img, int startX, int startY, int endX, int endY, int thickness) {
                int dx = endX - startX;
                int dy = endY - startY;
                double length = Math.sqrt(dx * dx + dy * dy);


                double angle = Math.atan2(dy, dx) - (Math.PI / 2);

                java.awt.geom.AffineTransform oldTransform = g2.getTransform();

                g2.translate(startX, startY);
                g2.rotate(angle);


                g2.drawImage(img, -thickness / 2, 0, thickness, (int) length, this);

                g2.setTransform(oldTransform);
            }
        };

        bannerPanel.setOpaque(false);
        bannerPanel.setBounds(0, 0, W, H);

        int frameW = 340, frameH = 250;
        int frameX = (W - frameW) / 2, frameY = (H - frameH) / 2;


        final String[] LEVEL_NAMES = {
                "Abandoned Compound", "Temporary Shelter", "City Ruins", "Safehouse Conflict", "Escape Route"
        };
        final String currentLevelName = (level >= 1 && level <= 5) ? LEVEL_NAMES[level - 1].toUpperCase() : "UNKNOWN AREA";


        JLabel bannerTitle = new JLabel(" ! ZOMBIE ENCOUNTER !", SwingConstants.CENTER);
        bannerTitle.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 22f));  // GameFonttt 1
        bannerTitle.setForeground(Color.WHITE);
        bannerTitle.setBounds(frameX + 20, frameY + 30, frameW - 40, 30);
        bannerPanel.add(bannerTitle);

        final JLabel bannerLevelName = new JLabel("", SwingConstants.CENTER);
        bannerLevelName.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 26f));  // GameFonttt 2
        bannerLevelName.setForeground(Color.WHITE);
        bannerLevelName.setBounds(frameX + 23, frameY + 110, frameW - 40, 40);
        bannerPanel.add(bannerLevelName);


        final JLabel bannerSub = new JLabel("", SwingConstants.CENTER) {
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

        bannerSub.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 22f)); // GameFonttt  3
        bannerSub.setForeground(Color.WHITE);
        bannerSub.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        int btnW = 220, btnH = 60;
        int btnX = frameX + (frameW - btnW) / 2;
        int btnY = frameY + frameH - btnH - 40;
        bannerSub.setBorder(BorderFactory.createEmptyBorder(10, 14, 0, 0)); // Pushes the text down 10px inside the button


        bannerSub.setBounds(btnX, btnY, btnW, btnH);
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
        playerHpBarPanelInstance.setBounds(520, 0, 380, 80);
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
        logLabel.setBounds(0, 100, W, 30);
        add(logLabel);

        String defNormal = "res/ui/icon/normal-buttons/button-2-normal-not-active.png";
        String defHover = "res/ui/icon/normal-buttons/button-2-normal-hover.png";
        String defActive = "res/ui/icon/normal-buttons/button-2-normal-active.png";

        String gNormal = "res/ui/icon/normal-buttons/button-green-not-active.png";
        String gHover = "res/ui/icon/normal-buttons/button-green-hover.png";
        String gActive = "res/ui/icon/normal-buttons/button-green-active.png";

        String rNormal = "res/ui/icon/normal-buttons/button-red-not-active.png";
        String rHover = "res/ui/icon/normal-buttons/button-red-hover.png";
        String rActive = "res/ui/icon/normal-buttons/button-red-active.png";

        // 🛠️ ADD THE YELLOW PATHS HERE IN BUILD UI!
        String yNormal = "res/ui/icon/normal-buttons/button-yellow-not-active.png";
        String yHover  = "res/ui/icon/normal-buttons/button-yellow-hover.png";
        String yActive = "res/ui/icon/normal-buttons/button-yellow-active.png";

        dodgeBtn = makeCombatButton("Dodge", rNormal, rHover, rActive);
        fightBtn = makeCombatButton("Fight", yNormal, yHover, yActive); // 🛠️ NOW IT WILL BE YELLOW!
        inventoryBtn = makeCombatButton("Inventory", gNormal, gHover, gActive);

        int combatBtnW = 230, combatBtnH = 74, gap = 20, startX = 70, buttonY = 550;
        dodgeBtn.setBounds(startX, buttonY, combatBtnW, combatBtnH);
        fightBtn.setBounds(startX + combatBtnW + gap, buttonY, combatBtnW, combatBtnH);
        inventoryBtn.setBounds(startX + (combatBtnW + gap) * 2, buttonY, combatBtnW, combatBtnH);
        add(dodgeBtn);
        add(fightBtn);
        add(inventoryBtn);

        zombieSprite = new JLabel();
        java.io.File f = new java.io.File("res/sprite/zombie.png");
        if (f.exists()) {
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            Image scaled = raw.getImage().getScaledInstance(660, 820, Image.SCALE_SMOOTH);
            zombieSprite.setBounds(60, -40, 780, 830);
            zombieSprite.setIcon(new ImageIcon(scaled));
        }

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
        // Timer to reveal the combat screen with typing effects
        new Thread(() -> {
            sleep(300); // Small pause before typing starts

            // 🛠️ Typewrite the level name (e.g. ABANDONED COMPOUND)
            typewrite(bannerLevelName, currentLevelName, 60);
            sleep(400);

            // 🛠️ Typewrite the zombie warning
            typewrite(bannerSub, "A zombie approaches!", 45);
            sleep(1500); // Let the player read it

            SwingUtilities.invokeLater(() -> {
                bannerPanel.setVisible(false);

                if (gameMenu != null) {
                    gameMenu.setVisible(true);
                }

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

//og
        inventoryBtn.addActionListener(e -> {
            isWeaponsTabOpen = true; // Always default to weapons tab when opened
            showInventoryPanel();
        });


//        //tweaked
//        inventoryBtn.addActionListener(e -> {
//            // ==========================================
//            // 🛠️ TEMPORARY TEST FOR DISCARD PANEL
//            // ==========================================
//            // We are hijacking this button to show the discard panel.
//            // It generates a random weapon to simulate finding a new one!
//            Weapon testWeapon = WeaponInventory.getRandomWeapon();
//            showDiscardPanel(testWeapon);
//        });
    }

    // ==============================
    // INVENTORY PANEL (TABBED)
    // ==============================
    private JPanel inventoryPanel;

    // Small button images for the X close button
    private Image closeDef, closeHov, closeAct;

    private void buildInventoryPanel() {
        // Load the small "X" button images
        try {
            java.io.File cDefF = new java.io.File("res/ui/icon/small-buttons/not-active.png");
            java.io.File cHovF = new java.io.File("res/ui/icon/small-buttons/hover.png");
            java.io.File cActF = new java.io.File("res/ui/icon/small-buttons/active.png");
            if (cDefF.exists()) closeDef = new ImageIcon(cDefF.getAbsolutePath()).getImage();
            if (cHovF.exists()) closeHov = new ImageIcon(cHovF.getAbsolutePath()).getImage();
            if (cActF.exists()) closeAct = new ImageIcon(cActF.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        // Box dimensions
        final int boxW = 660;
        final int boxH = 400;

        inventoryPanel = new JPanel(null) {
            Image weaponsBg, medBg, baseBg;
            {
                try {
                    java.io.File fw = new java.io.File("res/ui/panels/inventory/weapons-panel.png");
                    if (fw.exists()) weaponsBg = new ImageIcon(fw.getAbsolutePath()).getImage();

                    java.io.File fm = new java.io.File("res/ui/panels/inventory/med-panel.png");
                    if (fm.exists()) medBg = new ImageIcon(fm.getAbsolutePath()).getImage();

                    java.io.File fb = new java.io.File("res/ui/panels/inventory/inventory-box.png");
                    if (fb.exists()) baseBg = new ImageIcon(fb.getAbsolutePath()).getImage();
                } catch (Exception e) {}
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                // 40% BLACK OPACITY OVER FULL SCREEN
                g2.setColor(new Color(0, 0, 0, 62));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // CENTERED INVENTORY BOX
                int cx = (getWidth() - boxW) / 2;
                int cy = 137;

                Image bgToDraw = isWeaponsTabOpen ? weaponsBg : medBg;
                if (bgToDraw == null) bgToDraw = baseBg;

                if (bgToDraw != null) {
                    g2.drawImage(bgToDraw, cx, cy, boxW, boxH, this);
                } else {
                    g2.setColor(new Color(40, 40, 40, 240));
                    g2.fillRoundRect(cx, cy, boxW, boxH, 16, 16);
                }
                g2.dispose();
            }
        };
        inventoryPanel.setOpaque(false);
        inventoryPanel.setBounds(0, 0, W, H);
        inventoryPanel.setVisible(false);
        add(inventoryPanel);

        // Z-order: inventory panel at the very front (index 0)
        setComponentZOrder(inventoryPanel, 0);
    }

    private void showInventoryPanel() {
        inventoryPanel.removeAll();

        // Box dimensions — must match paintComponent above
        final int boxW = 660;
        final int boxH = 400;
        final int boxX = (W - boxW) / 2;
        final int boxY = 137;


        // ── Tab Buttons (Text included, properly aligned, no white borders!) ──
        JButton weaponsTabBtn = new JButton("Weapons");
        weaponsTabBtn.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));  // GameFonttt 4
        weaponsTabBtn.setForeground(isWeaponsTabOpen ? Color.WHITE : new Color(160, 160, 160));
        weaponsTabBtn.setBounds(boxX + 40, boxY + 22, 130, 40); // 🛠️ Shifted right to align!
        weaponsTabBtn.setOpaque(false);
        weaponsTabBtn.setContentAreaFilled(false);
        weaponsTabBtn.setBorderPainted(false);
        weaponsTabBtn.setFocusPainted(false); // 🛠️ Kills the white border
        weaponsTabBtn.setFocusable(false);    // 🛠️ Kills the focus outline completely
        weaponsTabBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        weaponsTabBtn.addActionListener(e -> {
            isWeaponsTabOpen = true;
            inventoryPanel.repaint();
            showInventoryPanel();
        });
        inventoryPanel.add(weaponsTabBtn);

        JButton medTabBtn = new JButton("Healing Items");
        medTabBtn.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));  // GameFonttt 5
        medTabBtn.setForeground(!isWeaponsTabOpen ? Color.WHITE : new Color(160, 160, 160));
        medTabBtn.setBounds(boxX + 200, boxY + 22, 160, 40); // 🛠️ Shifted right to align!
        medTabBtn.setOpaque(false);
        medTabBtn.setContentAreaFilled(false);
        medTabBtn.setBorderPainted(false);
        medTabBtn.setFocusPainted(false); // 🛠️ Kills the white border
        medTabBtn.setFocusable(false);    // 🛠️ Kills the focus outline completely
        medTabBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        medTabBtn.addActionListener(e -> {
            isWeaponsTabOpen = false;
            inventoryPanel.repaint();
            showInventoryPanel();
        });
        inventoryPanel.add(medTabBtn);

        // ── Invisible tab-switch buttons (over the labels) ──


        // ── Image-based X Close Button (top-right of box) ──
        final Image defaultImg = closeDef;
        final Image hoverImg   = closeHov;
        final Image activeImg  = closeAct;

        // ── Custom "-" Close Button ──
        // 🛠️ 1. Leave this empty so Java stops trying to format it!
        JButton closeBtn = new JButton() {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
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

                if (isPressed) {
                    g.translate(-1, 1);
                }

                // 🛠️ 3. DRAW A PERFECT SLIM MINUS SIGN!
                g.setColor(Color.WHITE);
                int lineW = 13; // How wide the line is
                int lineH = 2;  // How thick the line is (2 = very slim!)

                // This math perfectly auto-centers it no matter what!
                int lineX = (getWidth() - lineW) / 2;
                int lineY = (getHeight() - lineH) / 2;

                g.fillRect(lineX, lineY, lineW, lineH);

                // 4. Reset position
                if (isPressed) {
                    g.translate(1, -1);
                }
            }
        };
        closeBtn.setBounds(boxX + boxW - 60, boxY + 15, 40, 40);
        closeBtn.addActionListener(e -> inventoryPanel.setVisible(false));
        inventoryPanel.add(closeBtn);

        // ── Slot grid — centered inside the box ──
        int slotW = 180;   // 🛠️ INCREASE this to make the box WIDER (was 140)
        int slotH = 220;   // 🛠️ INCREASE this to make the box TALLER (was 190)
        int slotGap = 15;  // 🛠️ DECREASE this to make the gap SMALLER (was 30)

        int totalSlotsW = slotW * 3 + slotGap * 2;

        // This math automatically keeps everything perfectly centered left/right!
        int startX = boxX + (boxW - totalSlotsW) / 2;

        // 🛠️ To move the boxes further DOWN manually, increase the '+ 80' to a bigger number
        int startY = boxY + 110; // e.g., + 120 or + 140 moves them lower

        if (isWeaponsTabOpen) {
            WeaponInventory wi = player.getWeaponInventory();
            for (int i = 0; i < 3; i++) {
                Weapon w = (i < wi.getSize()) ? wi.getInventory().get(i) : null;
                final int idx = i;
                JButton slotBtn = createSlotButton(w, true);
                slotBtn.setBounds(startX + (slotW + slotGap) * i, startY, slotW, slotH);
                if (w != null) {
                    slotBtn.addActionListener(e -> {
                        inventoryPanel.setVisible(false);
                        synchronized (actionLock) {
                            pendingAction = "WEAPON";
                            pendingWeaponIndex = idx;
                            actionLock.notifyAll();
                        }
                    });
                }
                inventoryPanel.add(slotBtn);
            }
        } else {
            // Healing Items Tab
            List<String> items = new ArrayList<>(player.showConsumableInventory());
            for (int i = 0; i < 3; i++) {
                String itemName = (i < items.size()) ? items.get(i) : null;
                JButton slotBtn = createSlotButton(itemName, false);
                slotBtn.setBounds(startX + (slotW + slotGap) * i, startY, slotW, slotH);
                if (itemName != null) {
                    slotBtn.addActionListener(e -> {
                        String rawName = itemName.contains(" x") ? itemName.substring(0, itemName.indexOf(" x")) : itemName;
                        boolean used = player.useConsumable(rawName);
                        inventoryPanel.setVisible(false);
                        SwingUtilities.invokeLater(() -> {
                            updateHpLabels();
                            int healAmt = switch (rawName) {
                                case "Medkit" -> 25;
                                case "Bandage" -> 15;
                                default -> 0;
                            };
                            setLog(used ? "Used " + rawName + "! +" + healAmt + " HP restored." : "Your HP is already full!");
                        });
                    });
                }
                inventoryPanel.add(slotBtn);
            }
        }

        // Bring inventory panel to the very front before showing
        setComponentZOrder(inventoryPanel, 0);
        inventoryPanel.setVisible(true);
        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    // ⚠️ ADDED: Specific invisible button method for the Inventory Slots!
    private JButton createSlotButton(Object item, boolean isWeapon) {
        boolean isEmpty = (item == null);
        String name = "";
        String stats = "DMG: - | DUR: -/-";
        String imgPath = null;

        if (!isEmpty) {
            if (isWeapon) {
                Weapon w = (Weapon) item;
                name = w.getName();
                stats = "DMG: " + w.getDamage() + " | DUR: " + w.getDurability() + "/" + w.getMaxDurability();

                // Matches the file paths from your image
                if (name.toLowerCase().contains("wood")) imgPath = "res/ui/icon/weapons/wood.jpg";
                else if (name.toLowerCase().contains("bat")) imgPath = "res/ui/icon/weapons/bat.jpg";
                else if (name.toLowerCase().contains("knife")) imgPath = "res/ui/icon/weapons/knife.jpg";
            } else {
                name = (String) item;
                stats = "HEALS HP";
                if (name.toLowerCase().contains("medkit")) imgPath = "res/ui/icon/weapons/medkit.png";
            }
        }

        Image iconImg = null;
        if (imgPath != null) {
            try {
                java.io.File f = new java.io.File(imgPath);
                if (f.exists()) iconImg = new ImageIcon(f.getAbsolutePath()).getImage();
            } catch (Exception e) {}
        }

        // 🛠️ LOAD THE INVENTORY BOX IMAGE FOR THE BACKGROUND
        Image boxImg = null;
        try {
            java.io.File fb = new java.io.File("res/ui/panels/inventory/inventory-box.png");
            if (fb.exists()) boxImg = new ImageIcon(fb.getAbsolutePath()).getImage();
        } catch (Exception e) {}

        final Image finalIconImg = iconImg;
        final Image finalBoxImg = boxImg;
        final String finalName = name;
        final String finalStats = stats;

        JButton btn = new JButton() {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                if (!isEmpty) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed() && !isEmpty;

                // 2. PUSH DOWN IF PRESSED (Uses your exact requested logic!)
                if (isPressed) {
                    g.translate(-3, 3);
                }

                FontMetrics fm;

                g.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 19f));  // GameFonttt 6
                fm = g.getFontMetrics();

                // 1. Draw Name ABOVE the box
                if (!isEmpty) {
                    g.setColor(Color.WHITE);
                    int nx = (getWidth() - fm.stringWidth(finalName)) / 2;
                    // 🛠️ PLACEMENT: Changed from 15 to 13 to push the text UP by 2 pixels!
                    g.drawString(finalName, nx, 12);
                }

                // 🛠️ 2. DRAW THE INVENTORY BOX BACKGROUND
                int boxX = 5;
                int boxY = 25;
                int boxW = getWidth() - 10;
                int boxH = getHeight() - 55;

                if (finalBoxImg != null) {
                    g.drawImage(finalBoxImg, boxX, boxY, boxW, boxH, this);
                } else {
                    // Fallback border just in case
                    g.setColor(new Color(150, 150, 150));
                    g.drawRect(boxX, boxY, boxW, boxH);
                }

                // 🛠️ 3. DRAW IMAGE OR EMPTY TEXT INSIDE THE BOX
                if (finalIconImg != null) {
                    // Make the weapon big! Fill most of the box
                    g.drawImage(finalIconImg, boxX + 5, boxY + 5, boxW - 10, boxH - 10, this);
                } else if (isEmpty) {
                    g.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));  // GameFonttt 7
                    g.setColor(new Color(120, 120, 120));
                    String emp = "- EMPTY -";
                    fm = g.getFontMetrics();
                    // Perfect center inside the box
                    int ex = boxX + (boxW - fm.stringWidth(emp)) / 2;
                    int ey = boxY + (boxH / 2) + (fm.getAscent() / 2) - 4;
                    g.drawString(emp, ex, ey);
                }

                // Draw Stats BELOW the box
                g.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 14f));  // GameFonttt 8
                g.setColor(Color.WHITE);
                fm = g.getFontMetrics();
                int sx = (getWidth() - fm.stringWidth(finalStats)) / 2;
                g.drawString(finalStats, sx, getHeight() - 5);

                super.paintComponent(g);

                // 4. Reset
                if (isPressed) {
                    g.translate(3, -3);
                }
                g2.dispose();
            }
        };
        return btn;
    }

    private void showDiscardPanel(Weapon newWeapon) {
        // 1. Redefine inventoryPanel to use the custom background image
        if (inventoryPanel != null && inventoryPanel.getParent() != null) {
            inventoryPanel.getParent().remove(inventoryPanel);
        }

        inventoryPanel = new JPanel(null) {
            Image bgImg;

            {
                try {
                    java.io.File fBg = new java.io.File("res/ui/panels/save-slots-panel.png");
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
                    g2.setColor(new Color(60, 55, 50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                // 🛠️ CHANGED: Moved the separator line down from 60 to 75
                g2.setColor(new Color(150, 150, 150, 100));
                g2.drawLine(15, 75, getWidth() - 15, 75);
                g2.dispose();
            }
        };
        inventoryPanel.setOpaque(false);

        int pW = 580;
        int pH = 380;
        inventoryPanel.setBounds((900 - pW) / 2, (700 - pH) / 2, pW, pH);

        float titleSize = 24f;
        float newWeaponSize = 17f;
        float promptSize = 18f;
        float boxWeaponNameSize = 18f;
        float boxStatsSize = 14f;
        float skipButtonSize = 18f;

        // 🛠️ CHANGED: Moved title down from 20 to 32
        JLabel title = new JLabel("Inventory Full! Choose Weapon to Discard", SwingConstants.CENTER);
        title.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, titleSize));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 26, pW, 30);
        inventoryPanel.add(title);

        // 🛠️ CHANGED: Moved New Weapon Info down from 75 to 90
        JLabel newLbl = new JLabel("New: " + newWeapon.getName() + "  |  DMG: " + newWeapon.getDamage() + "  |  DUR: " + newWeapon.getDurability() + "/" + newWeapon.getMaxDurability(), SwingConstants.CENTER);
        newLbl.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, newWeaponSize));
        newLbl.setForeground(Color.WHITE);
        newLbl.setBounds(0, 90, pW, 25);
        inventoryPanel.add(newLbl);

        // 🛠️ CHANGED: Moved Prompt down from 100 to 115 (Hidden in your photo, but adjusting just in case)
        JLabel prompt = new JLabel("Select a weapon to replace:", SwingConstants.CENTER);
        prompt.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, promptSize));
        prompt.setForeground(Color.WHITE);
        prompt.setBounds(0, 115, pW, 25);
        // inventoryPanel.add(prompt); // You can uncomment this if you want the prompt back!

        WeaponInventory wi = player.getWeaponInventory();
        int boxW = 140;
        int boxH = 140;
        int gap = 30;

        int totalGridW = (boxW * 3) + (gap * 2);
        int startX = (pW - totalGridW) / 2;

        // 🛠️ CHANGED: Moved the entire grid down to 145
        int startY = 125;

        for (int i = 0; i < wi.getSize(); i++) {
            Weapon w = wi.getInventory().get(i);
            final int idx = i;

            JPanel itemContainer = new JPanel(null);
            itemContainer.setOpaque(false);
            itemContainer.setBounds(startX + (i * (boxW + gap)), startY, boxW, boxH + 60);

            // 1. Weapon Name (Top)
            JLabel nameLbl = new JLabel(w.getName(), SwingConstants.CENTER);
            nameLbl.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, boxWeaponNameSize));
            nameLbl.setForeground(Color.WHITE);
            // 🛠️ CHANGED: Text sits at the top (Y: 0)
            nameLbl.setBounds(0, 0, boxW, 20);
            itemContainer.add(nameLbl);

            // 2. Clickable Inventory Box
            JButton boxBtn = new JButton() {
                Image boxImg, weaponImg;

                {
                    try {
                        java.io.File fBox = new java.io.File("res/ui/panels/inventory/inventory-box.png");
                        if (fBox.exists()) boxImg = new ImageIcon(fBox.getAbsolutePath()).getImage();

                        String wNamePath = w.getName().toLowerCase().replace(" ", "-");
                        java.io.File fWpn = new java.io.File("res/ui/icon/weapons/" + wNamePath + ".png");
                        if (!fWpn.exists()) fWpn = new java.io.File("res/ui/icon/weapons/" + wNamePath + ".jpg");
                        if (fWpn.exists()) weaponImg = new ImageIcon(fWpn.getAbsolutePath()).getImage();
                    } catch (Exception e) {
                    }

                    setOpaque(false);
                    setContentAreaFilled(false);
                    setBorderPainted(false);
                    setFocusPainted(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    if (getModel().isPressed()) g2.translate(0, 2);

                    if (boxImg != null) g2.drawImage(boxImg, 0, 0, getWidth(), getHeight(), this);

                    if (weaponImg != null) {
                        int iconSize = 90;
                        int ix = (getWidth() - iconSize) / 2;
                        int iy = (getHeight() - iconSize) / 2;
                        g2.drawImage(weaponImg, ix, iy, iconSize, iconSize, this);
                    }
                    g2.dispose();
                }
            };
            // 🛠️ CHANGED: Pushed box down to Y=28 to give the Name more space
            boxBtn.setBounds(0, 20, boxW, boxH);
            boxBtn.addActionListener(e -> {
                wi.replaceWeapon(idx, newWeapon);
                inventoryPanel.setVisible(false);
                if (getParent() != null) getParent().remove(inventoryPanel);
                SwingUtilities.invokeLater(() -> setLog("Discarded " + w.getName() + "! Equipped " + newWeapon.getName() + "."));
                synchronized (discardLock) {
                    discardComplete = true;
                    discardLock.notifyAll();
                }
            });
            itemContainer.add(boxBtn);

            // 3. Stats label (Bottom)
            JLabel statsLbl = new JLabel("DMG: " + w.getDamage() + " | DUR: " + w.getDurability() + "/" + w.getMaxDurability(), SwingConstants.CENTER);
            statsLbl.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, boxStatsSize));
            statsLbl.setForeground(Color.WHITE);
            // 🛠️ CHANGED: Moved stats up closer to the box (Y=boxH+30)
            statsLbl.setBounds(0, 160, boxW, 20);
            itemContainer.add(statsLbl);

            inventoryPanel.add(itemContainer);
        }

        // ==============================================
        // 🛠️ SKIP BUTTON
        // ==============================================
        JButton skipBtn = new JButton("Skip") {
            Image defaultImg, hoverImg, activeImg;
            boolean hovered = false;

            {
                try {
                    defaultImg = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-not-active.png").getImage();
                    hoverImg = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-hover.png").getImage();
                    activeImg = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-active.png").getImage();
                } catch (Exception e) {
                }

                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, skipButtonSize));
                setForeground(Color.WHITE);

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
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

                if (currentSprite != null) g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();

                // 🛠️ CHANGED: Shifts the text UP permanently by 4 pixels to center it inside the graphic
                g.translate(4, 4);

                // 2. PUSH THE TEXT DOWN IF PRESSED
                if (isPressed) {
                    g.translate(-3, 3);
                }

                // 3. Draw the text on top
                super.paintComponent(g);

                // 4. Reset the positions to prevent graphical glitching
                if (isPressed) {
                    g.translate(3, -3);
                }

                // 🛠️ CHANGED: Revert the permanent shift
                g.translate(0, 4);
            }
        };

        // Center the Skip button at the bottom
        int btnW = 160;
        int btnH = 60;
        int btnX = ((pW - btnW) / 2) - 7;

        skipBtn.setBounds(btnX, pH - btnH - 15, btnW, btnH);

        skipBtn.addActionListener(e -> {
            inventoryPanel.setVisible(false);
            if (getParent() != null) getParent().remove(inventoryPanel); // Cleanup
            SwingUtilities.invokeLater(() -> setLog(newWeapon.getName() + " discarded. Kept current weapons."));
            synchronized (discardLock) {
                discardComplete = true;
                discardLock.notifyAll();
            }
        });
        inventoryPanel.add(skipBtn);

        // Display panel
        this.add(inventoryPanel);
        this.setComponentZOrder(inventoryPanel, 0);
        inventoryPanel.setVisible(true);
        this.repaint();
    }
    public void startCombat() {
        new Thread(() -> {
            WeaponInventory wi = player.getWeaponInventory();

            while (player.isAlive() && zombieHp > 0) {
                pendingAction = null;
                setButtonsEnabled(true);

                synchronized (actionLock) {
                    while (pendingAction == null) {
                        try {
                            actionLock.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }

                setButtonsEnabled(false);
                String part1 = "";
                String part2 = "";

                switch (pendingAction) {

                    case "DODGE": {
                        int hpBefore = zombieHp;
                        int playerHpBefore = player.getHealth();
                        zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "1", -1);
                        int dodgeDmg     = playerHpBefore - player.getHealth();
                        int dodgeZombieDmg = hpBefore - zombieHp;

                        if (dodgeZombieDmg > 0) {
                            part1 = "Agile! You dodged and struck twice! The zombie is stunned!";
                            part2 = "You dealt " + dodgeZombieDmg + " damage in two rapid hits!";
                        } else {
                            part1 = "Too slow! You failed to dodge.";
                            part2 = zombieHp > 0 && dodgeDmg > 0
                                    ? "The zombie attacks and dealt " + dodgeDmg + " damage!"
                                    : zombieHp > 0 && dodgeDmg == 0
                                    ? "The zombie lunges but you barely slip away, taking no damage!"
                                    : "";
                        }
                        break;
                    }

                    case "FIGHT": {
                        int playerHpBefore  = player.getHealth();
                        int zombieHpBefore  = zombieHp;
                        zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "2", -1);
                        int fightDmg        = playerHpBefore - player.getHealth();
                        int fightZombieDmg  = zombieHpBefore - zombieHp;

                        part1 = "You threw a desperate punch and dealt " + fightZombieDmg + " damage!";
                        part2 = zombieHp > 0 && fightDmg > 0
                                ? "The zombie attacks back and dealt " + fightDmg + " damage!"
                                : zombieHp > 0 && fightDmg == 0
                                ? "The zombie attacks back but falls short, dealing no damage!"
                                : "";
                        break;
                    }

                    case "WEAPON": {
                        if (pendingWeaponIndex < 0) break;
                        Weapon w = wi.getInventory().get(pendingWeaponIndex);
                        int playerHpBefore = player.getHealth();

                        if (w.isBroken()) {
                            zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "4", -1);
                            int brokenDmg = playerHpBefore - player.getHealth();
                            part1 = "The " + w.getName() + " is broken! You couldn't do anything.";
                            part2 = "The zombie manages to attack and dealt " + brokenDmg + " damage!";
                            break;
                        }

                        int zombieHpBefore     = zombieHp;
                        boolean hadDurability  = w.getDurability() > 0;
                        zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "3", pendingWeaponIndex);
                        int weaponDmg          = playerHpBefore - player.getHealth();
                        int weaponZombieDmg    = zombieHpBefore - zombieHp;
                        boolean brokeThisTurn  = hadDurability && w.isBroken();
                        boolean isWooden       = w.getName().toLowerCase().contains("wood");

                        String zombieCounterMsg = zombieHp > 0 && weaponDmg > 0
                                ? "The zombie attacks back and dealt " + weaponDmg + " damage!"
                                : zombieHp > 0 && weaponDmg == 0
                                ? "The zombie attacks back but falls short, dealing no damage!"
                                : "";

                        if (weaponZombieDmg == 0 && !brokeThisTurn) {
                            part1 = "You swung the " + w.getName() + ", but the zombie managed to dodge!";
                            part2 = zombieCounterMsg;
                        } else if (weaponZombieDmg == 0 && brokeThisTurn) {
                            part1 = "You swung the " + w.getName() + " but missed — and it broke!";
                            part2 = zombieCounterMsg;
                        } else if (brokeThisTurn && isWooden) {
                            part1 = "The Wooden Plank broke mid-fight! You were stunned for a moment...";
                            part2 = "The zombie seized the chance and dealt " + weaponDmg + " damage!";
                        } else if (brokeThisTurn) {
                            part1 = "You hit with " + w.getName() + " and dealt " + weaponZombieDmg + " damage, but it broke!";
                            part2 = zombieCounterMsg;
                        } else {
                            part1 = "You used " + w.getName() + " and dealt " + weaponZombieDmg + " damage!";
                            part2 = zombieCounterMsg;
                        }
                        break;
                    }
                }

// ── Phase 1: show player action + update HP bars ──
                final String showPart1 = part1;
                final String showPart2 = part2;
                final int finalZombieHp = zombieHp;

                SwingUtilities.invokeLater(() -> {
                    if (zombieHpBarPanelInstance != null) {
                        zombieHpBarPanelInstance.setHp(Math.max(0, finalZombieHp), (50 + level * 10));
                    }
                    setLog(showPart1);
                });
                sleep(1500);

                if (!showPart2.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        if (playerHpBarPanelInstance != null) {
                            playerHpBarPanelInstance.setHp(Math.max(0, player.getHealth()), 100);
                        }
                        setLog(showPart2);
                    });
                    sleep(1500);
                }
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
                            try {
                                discardLock.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }

                } else {
                    if (wi.getSize() < 3) {
                        wi.addWeapon(found);
                    }
                    boolean healed = player.getHealth() < 100;
                    String healMsg = healed ? " Healed 10 HP." : "";

                    // Hide buttons/logs to focus on the popup
                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        logLabel.setVisible(true); // 🛠️ Keep it visible!
                        setLog("Victory!" + healMsg); // 🛠️ Set the text here!
                    });

                    // 1. Determine image path for the weapon
                    String foundName = found.getName();
                    String imgPath = "res/ui/icon/weapons/wood.jpg"; // Default
                    if (foundName.toLowerCase().contains("bat")) imgPath = "res/ui/icon/weapons/bat.jpg";
                    else if (foundName.toLowerCase().contains("knife")) imgPath = "res/ui/icon/weapons/knife.jpg";

                    final String finalImgPath = imgPath;

                    // 2. Build the graphical popup (Using your exact Item Discovery math!)
                    JPanel victoryPanel = new JPanel(null) {
                        Image frameImg, itemImg, invBoxImg;
                        {
                            try {
                                java.io.File fFrame = new java.io.File("res/ui/panels/inventory/item-panel.png");
                                if (fFrame.exists()) frameImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();

                                java.io.File fItem = new java.io.File(finalImgPath);
                                if (fItem.exists()) itemImg = new ImageIcon(fItem.getAbsolutePath()).getImage();

                                java.io.File fInvBox = new java.io.File("res/ui/panels/inventory/inventory-box.png");
                                if (fInvBox.exists()) invBoxImg = new ImageIcon(fInvBox.getAbsolutePath()).getImage();
                            } catch (Exception e) {}
                        }
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                            // Darken background slightly
//                            g2.setColor(new Color(0, 0, 0, 100));
//                            g2.fillRect(0, 0, getWidth(), getHeight());

                            int boxW = 372;
                            int boxH = 310;
                            int boxX = (900 - boxW) / 2;
                            int boxY = (700 - boxH) / 2;

                            // 🛠️ NUDGES
                            boxX += 5;
                            int contentX = boxX + 12;

                            if (frameImg != null) {
                                g2.drawImage(frameImg, boxX, boxY, boxW, boxH, this);
                            } else {
                                g2.setColor(new Color(60, 55, 50));
                                g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
                            }

                            // Draw Top Title
                            g2.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 22f)); // GameFonttt 9
                            g2.setColor(Color.WHITE);
                            FontMetrics fm = g2.getFontMetrics();
                            String topText = "Item Found!";
                            int tx = contentX + (boxW - fm.stringWidth(topText)) / 2;
                            g2.drawString(topText, tx, boxY + 50);

                            // Draw Item Name
                            g2.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));  // GameFonttt 10
                            int nx = contentX + (boxW - fm.stringWidth(foundName)) / 2;
                            g2.drawString(foundName, nx, boxY + 105);

                            // Draw Inner Inventory Box
                            int invBoxW = 140;
                            int invBoxH = 140;
                            int invBoxX = contentX + (boxW - invBoxW) / 2 - 10;
                            int invBoxY = boxY + 115;
                            if (invBoxImg != null) {
                                g2.drawImage(invBoxImg, invBoxX, invBoxY, invBoxW, invBoxH, this);
                            }

                            // Draw Item Sprite
                            int itemSize = 130;
                            int itemX = invBoxX + (invBoxW - itemSize) / 2;
                            int itemY = invBoxY + (invBoxH - itemSize) / 2;
                            if (itemImg != null) {
                                g2.drawImage(itemImg, itemX, itemY, itemSize, itemSize, this);
                            }

                            // 🛠️ DRAW STATS (Damage & Durability)
                            g2.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 14f));  // GameFonttt 12
                            String statsText = "DMG: " + found.getDamage() + " | DUR: " + found.getDurability() + "/" + found.getMaxDurability();
                            fm = g2.getFontMetrics();
                            int sx = contentX + (boxW - fm.stringWidth(statsText)) / 2-10;
                            g2.drawString(statsText, sx, invBoxY + invBoxH + 25);

                            // 🛠️ DRAW ADDED TO INVENTORY & HEAL TEXT

                            g2.dispose();
                        }
                    };

                    victoryPanel.setOpaque(false);
                    victoryPanel.setBounds(0, 0, W, H);

                    // 3. Display the panel
                    SwingUtilities.invokeLater(() -> {
                        add(victoryPanel);
                        setComponentZOrder(victoryPanel, 0); // Put it at the very front
                        repaint();
                    });

                    sleep(3000); // Show it for 3 seconds

                    // 4. Remove panel & update player HP if they healed
                    SwingUtilities.invokeLater(() -> {
                        remove(victoryPanel);
                        logLabel.setVisible(true);
                        if (healed && playerHpBarPanelInstance != null) {
                            playerHpBarPanelInstance.setHp(Math.max(0, player.getHealth()), 100);
                        }
                        repaint();
                    });
                }

            } else if (!playerAlive) {
                sleep(300);
                SwingUtilities.invokeLater(() -> {

                    dodgeBtn.setVisible(false);
                    fightBtn.setVisible(false);
                    inventoryBtn.setVisible(false);
                    zombieSprite.setVisible(false);
                    zombieHpBarPanelInstance.setVisible(false);
                    playerHpBarPanelInstance.setVisible(false);
                    logLabel.setVisible(false);

                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(ZombieEncounterPanel.this);
                    JLayeredPane layered = frame.getLayeredPane();

                    DeathPanel dp = new DeathPanel(() -> {

                        layered.removeAll();

                        frame.getContentPane().removeAll();

                        GamePanel gamePanel = new GamePanel("res/background/main-background.gif");
                        gamePanel.setLayout(null);
                        gamePanel.setPreferredSize(new Dimension(900, 700));

                        frame.setContentPane(gamePanel);
                        new menu.TitleScreen(gamePanel, gamePanel);

                        frame.revalidate();
                        frame.repaint();
                    });
                    dp.setBounds(0, 0, frame.getWidth(), frame.getHeight());
                    layered.add(dp, JLayeredPane.POPUP_LAYER);
                    layered.revalidate();
                    layered.repaint();
                    dp.onShow();
                });
                return;
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

    private void setLog(String msg) {
        logLabel.setText(msg);
    }

    private void setButtonsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            dodgeBtn.setEnabled(enabled);
            fightBtn.setEnabled(enabled);
            inventoryBtn.setEnabled(enabled);
        });
    }

    private JButton makeCombatButton(String text, String normalPath, String hoverPath, String activePath) {
        // Load the 3 image states for the buttons
        Image normalImg = null, hoverImg = null, activeImg = null;
        try {
            java.io.File f1 = new java.io.File(normalPath);
            java.io.File f2 = new java.io.File(hoverPath);
            java.io.File f3 = new java.io.File(activePath);

            if (f1.exists()) normalImg = new ImageIcon(f1.getAbsolutePath()).getImage();
            if (f2.exists()) hoverImg = new ImageIcon(f2.getAbsolutePath()).getImage();
            if (f3.exists()) activeImg = new ImageIcon(f3.getAbsolutePath()).getImage();
        } catch (Exception ignored) {
        }

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

                setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 16f));  // GameFonttt 13
                setForeground(Color.WHITE);

                setHorizontalTextPosition(JButton.CENTER);
                setVerticalTextPosition(JButton.CENTER);

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
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
                } else {
                    g2.setColor(new Color(62, 55, 49));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                g.translate(4, 5);

                if (isPressed) {
                    g.translate(-3, 3);
                }

                super.paintComponent(g);

                if (isPressed) {
                    g.translate(3, -3);
                }

                g.translate(-4, -5);
            }
        };
        return btn;
    }

    private JButton makeInventoryItemButton(String text) {
        JButton btn = makeCombatButton(
                text, "res/ui/icon/normal-buttons/button-green-not-active.png",
                "res/ui/icon/normal-buttons/button-green-hover.png",
                "res/ui/icon/normal-buttons/button-green-active.png"
        );

        btn.setFont(new Font("Consolas", Font.PLAIN, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }

    // ==========================================
    // UPDATED HP BAR PANEL FOR PERFECT ALIGNMENT
    // ==========================================
    private class HpBarPanel extends JPanel {
        private Image framePanelImg;
        private Image statusBarImg;
        private JLabel hpTitle, hpValLabel;
        private int currentHp, maxHp;
        private boolean isRightAligned;

        private final int panelW = 380;

        // ==========================================
        // 🛠️ MANUAL EDIT: SIZES & GAPS
        // ==========================================
        private final int barW = 265;
        private final int barH = 24;

        private final int textW = 65;
        private final int sideMargin = 15;
        private final int gap = 2;

        // ==========================================
        // 🛠️ MANUAL EDIT: UP/DOWN POSITIONS
        // ==========================================
        private final int textTitleY   = 15;
        private final int textNumbersY = 37;
        private final int mainBarY     = 32;

        public HpBarPanel(String titleText, boolean rightAligned, int startingHp, int startMaxHp, String framePath) {
            setLayout(null);
            setOpaque(false);

            this.currentHp = startingHp;
            this.maxHp = startMaxHp;
            this.isRightAligned = rightAligned;

            java.io.File fFrame = new java.io.File(framePath);
            if (fFrame.exists()) framePanelImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();

            java.io.File fStatusBar = new java.io.File("res/ui/panels/status-bar.png");
            if (fStatusBar.exists()) statusBarImg = new ImageIcon(fStatusBar.getAbsolutePath()).getImage();

            int textX = isRightAligned ? (panelW - sideMargin - textW) : sideMargin;

            Color myTextColor = isRightAligned ? new Color(255, 220, 60)  // 🟡 Player: Yellow
                    : new Color(220, 80, 80);  // 🔴 Zombie: Red

            hpTitle = new JLabel(titleText, isRightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
            hpTitle.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 15f));
            hpTitle.setForeground(myTextColor);
            hpTitle.setBounds(textX, textTitleY, textW, 25);
            add(hpTitle);

            hpValLabel = new JLabel(startingHp + " / " + startMaxHp, isRightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
            hpValLabel.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 16f));  // GameFonttt 14
            hpValLabel.setForeground(Color.WHITE);
            hpValLabel.setBounds(textX, textNumbersY, textW, 25);
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

            int barX = isRightAligned
                    ? (panelW - sideMargin - textW - gap - barW)
                    : (sideMargin + textW + gap);

            int barY = mainBarY;

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

                if (hpBarTextureFill instanceof java.awt.image.BufferedImage) {
                    TexturePaint tp = new TexturePaint((java.awt.image.BufferedImage) hpBarTextureFill,
                            new Rectangle(0, 0, 32, fillH));
                    g2.setPaint(tp);
                } else {
                    g2.setColor(dynamicColor);
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

    private Color getHpColor(int hp, int maxHp) {
        float percent = (float) hp / (float) maxHp;

        Color customGreen  = new Color(80, 220, 120);
        Color customYellow = new Color(255, 220, 60);
        Color customRed    = new Color(220, 80, 80);

        if (percent >= 0.6f) return customGreen;
        else if (percent >= 0.3f) return customYellow;
        return customRed;
    }

    private void typewrite(JLabel label, String text, int delayMs) {
        for (int i = 1; i <= text.length(); i++) {
            final String partial = text.substring(0, i);
            SwingUtilities.invokeLater(() -> label.setText(partial));
            sleep(delayMs);
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}