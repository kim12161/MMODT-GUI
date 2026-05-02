package Encounters;

import Player.Player;
import Weapon.Weapon;
import Weapon.WeaponInventory;
import Interaction.BackgroundLayer;
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
        bannerTitle.setFont(new Font(bFont, Font.BOLD, 22));
        bannerTitle.setForeground(Color.WHITE);
        bannerTitle.setBounds(frameX + 20, frameY + 30, frameW - 40, 30);
        bannerPanel.add(bannerTitle);

        final JLabel bannerLevelName = new JLabel("", SwingConstants.CENTER);
        bannerLevelName.setFont(new Font(bFont, Font.BOLD, 26));
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

        bannerSub.setFont(new Font(bFont, Font.PLAIN, 16));
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
        logLabel.setBounds(0, 185, W, 30);
        add(logLabel);

        String defNormal = "res/ui/icon/normal-buttons/button-2-normal-not-active.png";
        String defHover = "res/ui/icon/normal-buttons/button-2-normal-hover.png";
        String defActive = "res/ui/icon/normal-buttons/button-2-normal-active.png";

        String gNormal = "res/ui/icon/normal-buttons/button-green-not-active.png";
        String gHover = "res/ui/icon/normal-buttons/button-green-hover.png";
        String gActive = "res/ui/icon/normal-buttons/button-green-active.png";

        dodgeBtn = makeCombatButton("Dodge", defNormal, defHover, defActive);
        fightBtn = makeCombatButton("Fight", defNormal, defHover, defActive);
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
        inventoryBtn.addActionListener(e -> {
            isWeaponsTabOpen = true; // Always default to weapons tab when opened
            showInventoryPanel();
        });
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
        weaponsTabBtn.setFont(new Font(bFont, Font.PLAIN, 18));
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
        medTabBtn.setFont(new Font(bFont, Font.PLAIN, 18));
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

                g.setFont(new Font(bFont, Font.PLAIN, 19));
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
                    g.setFont(new Font(bFont, Font.PLAIN, 18));
                    g.setColor(new Color(120, 120, 120));
                    String emp = "- EMPTY -";
                    fm = g.getFontMetrics();
                    // Perfect center inside the box
                    int ex = boxX + (boxW - fm.stringWidth(emp)) / 2;
                    int ey = boxY + (boxH / 2) + (fm.getAscent() / 2) - 4;
                    g.drawString(emp, ex, ey);
                }

                // Draw Stats BELOW the box
                g.setFont(new Font(bFont, Font.PLAIN, 14));
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

        String gNormal = "res/ui/icon/normal-buttons/button-green-not-active.png";
        String gHover = "res/ui/icon/normal-buttons/button-green-hover.png";
        String gActive = "res/ui/icon/normal-buttons/button-green-active.png";

        // Skip — keep current weapons (Using the new green skin)
        JButton skipBtn = makeCombatButton("SKIP", gNormal, gHover, gActive);
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
                        try {
                            actionLock.wait();
                        } catch (InterruptedException ignored) {
                        }
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

                            if (w.isBroken()) {
                                zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "2", -1);
                                logMsg = w.getName() + " is broken! The zombie hits you!";
                            } else {
                                zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "3", pendingWeaponIndex);
                                logMsg = "You used " + w.getName() + "!";
                            }
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
                    String healMsg = player.getHealth() < 100
                            ? "  |  Healed 10 HP."
                            : "";
                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        setLog("Victory! Found: " + found.getName() + healMsg);
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
                        layered.revalidate();
                        layered.repaint();

                        frame.getContentPane().removeAll();
                        new menu.TitleScreen(frame.getContentPane(), null);
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

                setFont(new Font(bFont, Font.BOLD, 16));
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
            hpTitle.setFont(new Font(bFont, Font.BOLD, 15));
            hpTitle.setForeground(myTextColor);
            hpTitle.setBounds(textX, textTitleY, textW, 25);
            add(hpTitle);

            hpValLabel = new JLabel(startingHp + " / " + startMaxHp, isRightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
            hpValLabel.setFont(new Font(bFont, Font.BOLD, 16));
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