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

    private HpBarPanel zombieHpBarPanelInstance;
    private HpBarPanel playerHpBarPanelInstance;
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

    private boolean isWeaponsTabOpen = true;
    private boolean combatOver = false;
    private JLabel effectOverlay;
    private JLabel effectOverlay2;


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
                    int chainThickness = 24;
                    int inset = 15;
                    drawDiagonalChain(g2, chainImg, 0, 0, frameX + inset, frameY + inset, chainThickness);
                    drawDiagonalChain(g2, chainImg, getWidth(), 0, frameX + frameW - inset, frameY + inset, chainThickness);
                    drawDiagonalChain(g2, chainImg, 0, getHeight(), frameX + inset, frameY + frameH - inset, chainThickness);
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
        bannerTitle.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 22f));
        bannerTitle.setForeground(Color.WHITE);
        bannerTitle.setBounds(frameX + 20, frameY + 30, frameW - 40, 30);
        bannerPanel.add(bannerTitle);

        final JLabel bannerLevelName = new JLabel("", SwingConstants.CENTER);
        bannerLevelName.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 24f));
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

        bannerSub.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 16f));
        bannerSub.setForeground(Color.WHITE);
        bannerSub.setBorder(BorderFactory.createEmptyBorder(10, 14, 0, 0));

        int btnW = 220, btnH = 60;
        int btnX = frameX + (frameW - btnW) / 2;
        int btnY = frameY + frameH - btnH - 40;
        bannerSub.setBounds(btnX, btnY, btnW, btnH);
        bannerPanel.add(bannerSub);

        add(bannerPanel);

        int startingZp = 50 + (level * 10);
        zombieHpBarPanelInstance = new HpBarPanel("Zombie HP", false, startingZp, startingZp, "res/ui/panels/hp-status-panel-zombie.png");
        zombieHpBarPanelInstance.setBounds(0, 0, 380, 80);
        add(zombieHpBarPanelInstance);

        playerHpBarPanelInstance = new HpBarPanel("Your HP", true, player.getHealth(), 100, "res/ui/panels/hp-status-panel-player.png");
        playerHpBarPanelInstance.setBounds(520, 0, 380, 80);
        add(playerHpBarPanelInstance);

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setBounds(0, 30, W, 40);
        add(titleLabel);

        logLabel = new JLabel("", SwingConstants.CENTER);
        logLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        logLabel.setForeground(Color.WHITE);
        logLabel.setBounds(0, 100, W, 30);
        add(logLabel);

        String defNormal = "res/ui/icon/normal-buttons/button-2-normal-not-active.png";
        String defHover  = "res/ui/icon/normal-buttons/button-2-normal-hover.png";
        String defActive = "res/ui/icon/normal-buttons/button-2-normal-active.png";

        String gNormal = "res/ui/icon/normal-buttons/button-green-not-active.png";
        String gHover  = "res/ui/icon/normal-buttons/button-green-hover.png";
        String gActive = "res/ui/icon/normal-buttons/button-green-active.png";

        String rNormal = "res/ui/icon/normal-buttons/button-red-not-active.png";
        String rHover  = "res/ui/icon/normal-buttons/button-red-hover.png";
        String rActive = "res/ui/icon/normal-buttons/button-red-active.png";

        dodgeBtn     = makeCombatButton("Dodge",     defNormal, defHover, defActive);
        fightBtn     = makeCombatButton("Fight",     rNormal,   rHover,   rActive);
        inventoryBtn = makeCombatButton("Inventory", gNormal,   gHover,   gActive);

        int combatBtnW = 230, combatBtnH = 77, gap = 20, startX = 70, buttonY = 550;
        dodgeBtn.setBounds(startX, buttonY, combatBtnW, combatBtnH);
        fightBtn.setBounds(startX + combatBtnW + gap, buttonY, combatBtnW, combatBtnH);
        inventoryBtn.setBounds(startX + (combatBtnW + gap) * 2, buttonY, combatBtnW, combatBtnH);
        add(dodgeBtn);
        add(fightBtn);
        add(inventoryBtn);

        // Alpha-aware zombie sprite
        zombieSprite = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Object alphaVal = getClientProperty("alpha");
                float alpha = (alphaVal instanceof Float) ? (Float) alphaVal : 1.0f;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        java.io.File f = new java.io.File("res/sprite/zombie/zombie.png");
        if (f.exists()) {
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            Image scaled = raw.getImage().getScaledInstance(560, 710, Image.SCALE_SMOOTH);
            zombieSprite.setIcon(new ImageIcon(scaled));
        }
        zombieSprite.setBounds(120, -60, 680, 730);
        add(zombieSprite);

        // Z-ORDERING
        setComponentZOrder(bannerPanel, 0);
        setComponentZOrder(dodgeBtn, 1);
        setComponentZOrder(fightBtn, 2);
        setComponentZOrder(inventoryBtn, 3);
        setComponentZOrder(zombieHpBarPanelInstance, 4);
        setComponentZOrder(playerHpBarPanelInstance, 5);
        setComponentZOrder(zombieSprite, getComponentCount() - 1);

        // HIDE EVERYTHING EXCEPT BANNER
        titleLabel.setVisible(false);
        logLabel.setVisible(false);
        dodgeBtn.setVisible(false);
        fightBtn.setVisible(false);
        inventoryBtn.setVisible(false);
        zombieSprite.setVisible(false);
        zombieHpBarPanelInstance.setVisible(false);
        playerHpBarPanelInstance.setVisible(false);

        new Thread(() -> {
            sleep(300);
            typewrite(bannerLevelName, currentLevelName, 60);
            sleep(400);
            typewrite(bannerSub, "A zombie approaches!", 45);
            sleep(1500);

            SwingUtilities.invokeLater(() -> {
                bannerPanel.setVisible(false);
                if (gameMenu != null) gameMenu.setVisible(true);
                titleLabel.setVisible(true);
                logLabel.setVisible(true);
                zombieHpBarPanelInstance.setVisible(true);
                playerHpBarPanelInstance.setVisible(true);
                dodgeBtn.setVisible(true);
                fightBtn.setVisible(true);
                inventoryBtn.setVisible(true);
                // Fade in zombie instead of just setVisible
                zombieSprite.putClientProperty("alpha", 0f);
                zombieSprite.setVisible(true);
            });
            sleep(100);
            fadeInZombie();
        }).start();

        buildEffectOverlay();
        buildInventoryPanel();
        updateHpLabels();

        dodgeBtn.addActionListener(e -> triggerAction("DODGE"));
        fightBtn.addActionListener(e -> triggerAction("FIGHT"));
        inventoryBtn.addActionListener(e -> {
            isWeaponsTabOpen = true;
            showInventoryPanel();
        });
    }

    // ==============================
    // ZOMBIE SPRITE HELPERS
    // ==============================
    private void showZombieSprite(String path) {
        java.io.File f = new java.io.File(path);
        if (!f.exists()) return;
        SwingUtilities.invokeLater(() -> {
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            Image scaled = raw.getImage().getScaledInstance(560, 710, Image.SCALE_SMOOTH);
            zombieSprite.setIcon(new ImageIcon(scaled));
            zombieSprite.setVisible(true);
            repaint();
        });
    }

    private void fadeInZombie() {
        zombieSprite.putClientProperty("alpha", 0f);
        zombieSprite.setVisible(true);
        new Thread(() -> {
            for (int i = 0; i <= 10; i++) {
                final float alpha = i / 10f;
                SwingUtilities.invokeLater(() -> {
                    zombieSprite.putClientProperty("alpha", alpha);
                    zombieSprite.repaint();
                });
                sleep(40);
            }
        }).start();
    }

    private void fadeOutZombie(Runnable onComplete) {
        new Thread(() -> {
            for (int i = 10; i >= 0; i--) {
                final float alpha = i / 10f;
                SwingUtilities.invokeLater(() -> {
                    zombieSprite.putClientProperty("alpha", alpha);
                    zombieSprite.repaint();
                });
                sleep(60);
            }
            SwingUtilities.invokeLater(() -> {
                zombieSprite.setVisible(false);
                if (onComplete != null) onComplete.run();
            });
        }).start();
    }

    private void zombieDodgeEffect(Runnable onComplete) {
        new Thread(() -> {
            for (int i = 0; i <= 15; i++) {
                final int offset = i * 5;
                SwingUtilities.invokeLater(() -> {
                    zombieSprite.setBounds(120 - offset, -60, 680, 730);
                    zombieSprite.repaint();
                });
                sleep(20);
            }

            sleep(300);

            for (int i = 15; i >= 0; i--) {
                final int offset = i * 5;
                SwingUtilities.invokeLater(() -> {
                    zombieSprite.setBounds(120 - offset, -60, 680, 730);
                    zombieSprite.repaint();
                });
                sleep(20);
            }

            // Snap back to exact position
            SwingUtilities.invokeLater(() -> {
                zombieSprite.setBounds(120, -60, 680, 730);
                zombieSprite.repaint();
            });

            sleep(200);
            if (onComplete != null) onComplete.run();
        }).start();
    }

    // ==============================
    // INVENTORY PANEL
    // ==============================
    private JPanel inventoryPanel;
    private Image closeDef, closeHov, closeAct;

    private void buildInventoryPanel() {
        try {
            java.io.File cDefF = new java.io.File("res/ui/icon/small-buttons/not-active.png");
            java.io.File cHovF = new java.io.File("res/ui/icon/small-buttons/hover.png");
            java.io.File cActF = new java.io.File("res/ui/icon/small-buttons/active.png");
            if (cDefF.exists()) closeDef = new ImageIcon(cDefF.getAbsolutePath()).getImage();
            if (cHovF.exists()) closeHov = new ImageIcon(cHovF.getAbsolutePath()).getImage();
            if (cActF.exists()) closeAct = new ImageIcon(cActF.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

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
                g2.setColor(new Color(0, 0, 0, 62));
                g2.fillRect(0, 0, getWidth(), getHeight());
                int cx = (getWidth() - boxW) / 2;
                int cy = 137;
                Image bgToDraw = isWeaponsTabOpen ? weaponsBg : medBg;
                if (bgToDraw == null) bgToDraw = baseBg;
                if (bgToDraw != null) g2.drawImage(bgToDraw, cx, cy, boxW, boxH, this);
                else {
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
        setComponentZOrder(inventoryPanel, 0);
    }

    private void showInventoryPanel() {
        inventoryPanel.removeAll();

        final int boxW = 660;
        final int boxH = 400;
        final int boxX = (W - boxW) / 2;
        final int boxY = 137;

        JButton weaponsTabBtn = new JButton("Weapons");
        weaponsTabBtn.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));
        weaponsTabBtn.setForeground(isWeaponsTabOpen ? Color.WHITE : new Color(160, 160, 160));
        weaponsTabBtn.setBounds(boxX + 40, boxY + 22, 130, 40);
        weaponsTabBtn.setOpaque(false); weaponsTabBtn.setContentAreaFilled(false);
        weaponsTabBtn.setBorderPainted(false); weaponsTabBtn.setFocusPainted(false);
        weaponsTabBtn.setFocusable(false);
        weaponsTabBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        weaponsTabBtn.addActionListener(e -> { isWeaponsTabOpen = true; inventoryPanel.repaint(); showInventoryPanel(); });
        inventoryPanel.add(weaponsTabBtn);

        JButton medTabBtn = new JButton("Healing Items");
        medTabBtn.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));
        medTabBtn.setForeground(!isWeaponsTabOpen ? Color.WHITE : new Color(160, 160, 160));
        medTabBtn.setBounds(boxX + 200, boxY + 22, 160, 40);
        medTabBtn.setOpaque(false); medTabBtn.setContentAreaFilled(false);
        medTabBtn.setBorderPainted(false); medTabBtn.setFocusPainted(false);
        medTabBtn.setFocusable(false);
        medTabBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        medTabBtn.addActionListener(e -> { isWeaponsTabOpen = false; inventoryPanel.repaint(); showInventoryPanel(); });
        inventoryPanel.add(medTabBtn);

        final Image defaultImg = closeDef;
        final Image hoverImg   = closeHov;
        final Image activeImg  = closeAct;

        JButton closeBtn = new JButton() {
            private boolean hovered = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
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
                Image currentSprite = isPressed ? activeImg : hovered ? hoverImg : defaultImg;
                if (currentSprite != null) g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();
                if (isPressed) g.translate(-1, 1);
                g.setColor(Color.WHITE);
                int lineW = 13, lineH = 2;
                g.fillRect((getWidth() - lineW) / 2, (getHeight() - lineH) / 2, lineW, lineH);
                if (isPressed) g.translate(1, -1);
            }
        };
        closeBtn.setBounds(boxX + boxW - 60, boxY + 15, 40, 40);
        closeBtn.addActionListener(e -> inventoryPanel.setVisible(false));
        inventoryPanel.add(closeBtn);

        int slotW = 180, slotH = 220, slotGap = 15;
        int totalSlotsW = slotW * 3 + slotGap * 2;
        int startX = boxX + (boxW - totalSlotsW) / 2;
        int startY = boxY + 110;

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

        setComponentZOrder(inventoryPanel, 0);
        inventoryPanel.setVisible(true);
        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    private JButton createSlotButton(Object item, boolean isWeapon) {
        boolean isEmpty = (item == null);
        String name = "";
        String stats = isWeapon ? "DMG: - | DUR: -/-" : "HP - | - Uses ";
        String imgPath = null;

        if (!isEmpty) {
            if (isWeapon) {
                Weapon w = (Weapon) item;
                name = w.getName();
                stats = "DMG: " + w.getDamage() + " | DUR: " + w.getDurability() + "/" + w.getMaxDurability();
                String check = name.toLowerCase();
                if (check.contains("wood"))     imgPath = "res/ui/icon/assets/weapons/wood.png";
                else if (check.contains("bat"))      imgPath = "res/ui/icon/assets/weapons/bat.png";
                else if (check.contains("knife"))    imgPath = "res/ui/icon/assets/weapons/knife.png";
                else if (check.contains("bottle"))   imgPath = "res/ui/icon/assets/weapons/water-bottle.png";
                else if (check.contains("crowbar"))  imgPath = "res/ui/icon/assets/weapons/crowbar.png";
            } else {
                String itemNameRaw = (String) item;
                int splitIndex = itemNameRaw.lastIndexOf(" x");
                String cleanName, useCount;
                if (splitIndex != -1) {
                    cleanName = itemNameRaw.substring(0, splitIndex);
                    useCount  = itemNameRaw.substring(splitIndex + 2);
                } else {
                    cleanName = itemNameRaw;
                    useCount  = "1";
                }
                String checkName = cleanName.toLowerCase();
                int healAmt = 0;
                if (checkName.contains("bandage")) { healAmt = 15; imgPath = "res/ui/icon/assets/items/bandage.png"; }
                else if (checkName.contains("medkit")) { healAmt = 25; imgPath = "res/ui/icon/assets/items/medkit.png"; }
                name  = cleanName;
                stats = "HEALS: " + healAmt + " HP | " + useCount + "x Uses";
            }
        }

        Image iconImg = null;
        if (imgPath != null) {
            try {
                java.io.File fi = new java.io.File(imgPath);
                if (fi.exists()) iconImg = new ImageIcon(fi.getAbsolutePath()).getImage();
            } catch (Exception e) {}
        }

        Image boxImg = null;
        try {
            java.io.File fb = new java.io.File("res/ui/panels/inventory/inventory-box.png");
            if (fb.exists()) boxImg = new ImageIcon(fb.getAbsolutePath()).getImage();
        } catch (Exception e) {}

        final Image finalIconImg = iconImg;
        final Image finalBoxImg  = boxImg;
        final String finalName   = name;
        final String finalStats  = stats;

        JButton btn = new JButton() {
            private boolean hovered = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                if (!isEmpty) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                boolean isPressed = getModel().isPressed() && !isEmpty;
                if (isPressed) g.translate(-3, 3);
                FontMetrics fm;
                g.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 19f));
                fm = g.getFontMetrics();
                if (!isEmpty) {
                    g.setColor(Color.WHITE);
                    g.drawString(finalName, (getWidth() - fm.stringWidth(finalName)) / 2, 13);
                }
                int bX = 5, bY = 25, bW = getWidth() - 10, bH = getHeight() - 55;
                if (finalBoxImg != null) g.drawImage(finalBoxImg, bX, bY, bW, bH, this);
                else { g.setColor(new Color(150, 150, 150)); g.drawRect(bX, bY, bW, bH); }
                if (finalIconImg != null) {
                    g.drawImage(finalIconImg, bX + 5, bY + 5, bW - 10, bH - 10, this);
                } else if (isEmpty) {
                    g.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));
                    g.setColor(new Color(120, 120, 120));
                    String emp = "- EMPTY -"; fm = g.getFontMetrics();
                    g.drawString(emp, bX + (bW - fm.stringWidth(emp)) / 2, bY + (bH / 2) + (fm.getAscent() / 2) - 4);
                }
                g.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 14f));
                g.setColor(Color.WHITE); fm = g.getFontMetrics();
                g.drawString(finalStats, (getWidth() - fm.stringWidth(finalStats)) / 2, getHeight() - 5);
                if (isPressed) g.translate(3, -3);
                g2.dispose();
            }
        };
        return btn;
    }

    private void showDiscardPanel(Weapon newWeapon) {
        if (inventoryPanel != null && inventoryPanel.getParent() != null) {
            inventoryPanel.getParent().remove(inventoryPanel);
        }

        inventoryPanel = new JPanel(null) {
            Image bgImg;
            {
                try {
                    java.io.File fBg = new java.io.File("res/ui/panels/save-slots-panel.png");
                    if (fBg.exists()) bgImg = new ImageIcon(fBg.getAbsolutePath()).getImage();
                } catch (Exception e) {}
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                if (bgImg != null) g2.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
                else { g2.setColor(new Color(60, 55, 50)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
                g2.setColor(new Color(150, 150, 150, 100));
                g2.drawLine(15, 75, getWidth() - 15, 75);
                g2.dispose();
            }
        };
        inventoryPanel.setOpaque(false);

        int pW = 580, pH = 380;
        int panelY = (700 - pH) / 2 - 10;
        inventoryPanel.setBounds((900 - pW) / 2, panelY, pW, pH);

        JLabel title = new JLabel("Inventory Full! Choose Weapon to Discard", SwingConstants.CENTER);
        title.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 24f));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 26, pW, 30);
        inventoryPanel.add(title);

        JLabel newLbl = new JLabel("New: " + newWeapon.getName() + "  |  DMG: " + newWeapon.getDamage() + "  |  DUR: " + newWeapon.getDurability() + "/" + newWeapon.getMaxDurability(), SwingConstants.CENTER);
        newLbl.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 17f));
        newLbl.setForeground(Color.WHITE);
        newLbl.setBounds(0, 90, pW, 25);
        inventoryPanel.add(newLbl);

        WeaponInventory wi = player.getWeaponInventory();
        int boxW = 140, boxH = 140, gap = 30;
        int totalGridW = (boxW * 3) + (gap * 2);
        int startX = (pW - totalGridW) / 2;
        int startY = 125;

        for (int i = 0; i < wi.getSize(); i++) {
            Weapon w = wi.getInventory().get(i);
            final int idx = i;

            JPanel itemContainer = new JPanel(null);
            itemContainer.setOpaque(false);
            itemContainer.setBounds(startX + (i * (boxW + gap)), startY, boxW, boxH + 60);

            JLabel nameLbl = new JLabel(w.getName(), SwingConstants.CENTER);
            nameLbl.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));
            nameLbl.setForeground(Color.WHITE);
            nameLbl.setBounds(0, 0, boxW, 20);
            itemContainer.add(nameLbl);

            JButton boxBtn = new JButton() {
                Image boxImg, weaponImg;
                {
                    try {
                        java.io.File fBox = new java.io.File("res/ui/panels/inventory/inventory-box.png");
                        if (fBox.exists()) boxImg = new ImageIcon(fBox.getAbsolutePath()).getImage();
                        String weaponName = w.getName().toLowerCase();
                        String fileName = "";
                        if (weaponName.contains("bat"))      fileName = "bat.png";
                        else if (weaponName.contains("knife"))    fileName = "knife.png";
                        else if (weaponName.contains("wood"))     fileName = "wood.png";
                        else if (weaponName.contains("bottle"))   fileName = "water-bottle.png";
                        else if (weaponName.contains("crowbar"))  fileName = "crowbar.png";
                        if (!fileName.isEmpty()) {
                            java.io.File fWpn = new java.io.File("res/ui/icon/assets/weapons/" + fileName);
                            if (fWpn.exists()) weaponImg = new ImageIcon(fWpn.getAbsolutePath()).getImage();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    setOpaque(false); setContentAreaFilled(false);
                    setBorderPainted(false); setFocusPainted(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    if (getModel().isPressed()) g2.translate(0, 2);
                    if (boxImg != null) g2.drawImage(boxImg, 0, 0, getWidth(), getHeight(), this);
                    if (weaponImg != null) {
                        int iconSize = 120;
                        g2.drawImage(weaponImg, (getWidth() - iconSize) / 2, (getHeight() - iconSize) / 2, iconSize, iconSize, this);
                    }
                    g2.dispose();
                }
            };
            boxBtn.setBounds(0, 20, boxW, boxH);
            boxBtn.addActionListener(e -> {
                wi.replaceWeapon(idx, newWeapon);
                inventoryPanel.setVisible(false);
                if (getParent() != null) getParent().remove(inventoryPanel);
                SwingUtilities.invokeLater(() -> setLog("Discarded " + w.getName() + "! Equipped " + newWeapon.getName() + "."));
                synchronized (discardLock) { discardComplete = true; discardLock.notifyAll(); }
            });
            itemContainer.add(boxBtn);

            JLabel statsLbl = new JLabel("DMG: " + w.getDamage() + " | DUR: " + w.getDurability() + "/" + w.getMaxDurability(), SwingConstants.CENTER);
            statsLbl.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 14f));
            statsLbl.setForeground(Color.WHITE);
            statsLbl.setBounds(0, 160, boxW, 20);
            itemContainer.add(statsLbl);

            inventoryPanel.add(itemContainer);
        }

        JButton skipBtn = new JButton("Skip") {
            Image defaultImg, hoverImg, activeImg;
            boolean hovered = false;
            {
                try {
                    defaultImg = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-not-active.png").getImage();
                    hoverImg   = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-hover.png").getImage();
                    activeImg  = new ImageIcon("res/ui/icon/normal-buttons/button-2-normal-active.png").getImage();
                } catch (Exception e) {}
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));
                setForeground(Color.WHITE);
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
                Image currentSprite = isPressed ? activeImg : hovered ? hoverImg : defaultImg;
                if (currentSprite != null) g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();
                g.translate(4, 4);
                if (isPressed) g.translate(-3, 3);
                super.paintComponent(g);
                if (isPressed) g.translate(3, -3);
                g.translate(0, 4);
            }
        };
        skipBtn.setBounds(((pW - 160) / 2) - 7, pH - 60 - 15, 160, 60);
        skipBtn.addActionListener(e -> {
            inventoryPanel.setVisible(false);
            if (getParent() != null) getParent().remove(inventoryPanel);
            SwingUtilities.invokeLater(() -> setLog(newWeapon.getName() + " discarded. Kept current weapons."));
            synchronized (discardLock) { discardComplete = true; discardLock.notifyAll(); }
        });
        inventoryPanel.add(skipBtn);

        this.add(inventoryPanel);
        this.setComponentZOrder(inventoryPanel, 0);
        inventoryPanel.setVisible(true);
        this.repaint();
    }

    // ==============================
    // COMBAT LOOP
    // ==============================
    public void startCombat() {
        new Thread(() -> {
            WeaponInventory wi = player.getWeaponInventory();

            while (player.isAlive() && zombieHp > 0) {
                pendingAction = null;
                setButtonsEnabled(true);

                synchronized (actionLock) {
                    while (pendingAction == null) {
                        try { actionLock.wait(); } catch (InterruptedException ignored) {}
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
                        int dodgeDmg       = playerHpBefore - player.getHealth();
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
                        int playerHpBefore = player.getHealth();
                        int zombieHpBefore = zombieHp;
                        zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "2", -1);
                        int fightDmg       = playerHpBefore - player.getHealth();
                        int fightZombieDmg = zombieHpBefore - zombieHp;
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
                        int zombieHpBefore    = zombieHp;
                        boolean hadDurability = w.getDurability() > 0;
                        zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, wi, "3", pendingWeaponIndex);
                        int weaponDmg         = playerHpBefore - player.getHealth();
                        int weaponZombieDmg   = zombieHpBefore - zombieHp;
                        boolean brokeThisTurn = hadDurability && w.isBroken();
                        boolean isWooden      = w.getName().toLowerCase().contains("wood");
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

                final String showPart1    = part1;
                final String showPart2    = part2;
                final int finalZombieHp   = zombieHp;

                boolean zombieAttacks = showPart2.contains("zombie attacks");
                boolean dodgeSuccess  = showPart1.contains("stunned");
                boolean zombieDied    = finalZombieHp <= 0;
                boolean zombieDodged  = showPart1.contains("managed to dodge");

                String effectPath = null;

                if ("FIGHT".equals(pendingAction)) {
                    effectPath = "res/ui/effects/fight.png";

                } else if ("DODGE".equals(pendingAction) && dodgeSuccess) {
                    effectPath = "res/ui/effects/fight.png";

                } else if ("WEAPON".equals(pendingAction) && pendingWeaponIndex >= 0) {
                    Weapon usedW = wi.getInventory().get(pendingWeaponIndex);
                    String wName = usedW.getName().toLowerCase();

                    boolean wasBrokenBeforeUse = showPart1.contains("is broken");
                    boolean zombieDodgedWeapon = showPart1.contains("managed to dodge");
                    boolean missedAndBroke     = showPart1.contains("but missed") && showPart1.contains("it broke");

                    if (wasBrokenBeforeUse) {
                        // weapon was already broken before use — no effect shown
                    } else if (zombieDodgedWeapon || missedAndBroke) {
                        showEffect2("res/ui/effects/slash.png");

                    } else {
                        // successful hit
                        if (wName.contains("bottle")) {
                            effectPath = "res/ui/effects/throw.png";
                        } else if (wName.contains("wood")) {
                            effectPath = "res/ui/effects/fight.png";
                        } else {
                            effectPath = "res/ui/effects/slash.png";
                        }
                    }
                }

                if (effectPath != null) {
                    final String ep = effectPath;
                    showEffect(ep);
                }

                // Stunned sprite
                if (dodgeSuccess) {
                    showZombieSprite("res/sprite/zombie/zombie_stunned.png");
                }

                // Zombie dodge animation — slides left then returns
                if (zombieDodged) {
                    final Object dodgeLock = new Object();
                    zombieDodgeEffect(() -> {
                        synchronized (dodgeLock) { dodgeLock.notifyAll(); }
                    });
                    synchronized (dodgeLock) {
                        try { dodgeLock.wait(); } catch (InterruptedException ignored) {}
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    if (zombieHpBarPanelInstance != null)
                        zombieHpBarPanelInstance.setHp(Math.max(0, finalZombieHp), (50 + level * 10));
                    setLog(showPart1);
                });
                sleep(1500);

                if (!showPart2.isEmpty()) {
                    if (zombieAttacks) {
                        showZombieSprite("res/sprite/zombie/zombie_slash.png");
                        sleep(400);
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (playerHpBarPanelInstance != null)
                            playerHpBarPanelInstance.setHp(Math.max(0, player.getHealth()), 100);
                        setLog(showPart2);
                    });
                    sleep(1500);
                }

                // Restore normal sprite
                if (!zombieDied) {
                    showZombieSprite("res/sprite/zombie/zombie.png");
                }

                // Dead — show dead sprite then fade out
                if (zombieDied) {
                    showZombieSprite("res/sprite/zombie/zombie_dead.png");
                    sleep(800);
                    fadeOutZombie(null);
                    sleep(700);
                }
            }

            boolean playerAlive = player.isAlive();

            if (zombieHp <= 0 && playerAlive) {
                player.heal(10);
                Weapon found = WeaponInventory.getRandomWeapon();

                if (level >= 4 && wi.getSize() >= 3) {
                    discardComplete = false;
                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        logLabel.setVisible(false);
                        showDiscardPanel(found);
                    });
                    synchronized (discardLock) {
                        while (!discardComplete) {
                            try { discardLock.wait(); } catch (InterruptedException ignored) {}
                        }
                    }
                } else {
                    if (wi.getSize() < 3) wi.addWeapon(found);
                    boolean healed = player.getHealth() < 100;
                    String healMsg = healed ? " Healed 10 HP." : "";

                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        logLabel.setVisible(false);
                        setLog("Victory!" + healMsg);
                    });

                    String itemName = found.getName();
                    String imgPath  = "/ui/icon/assets/weapons/wood.png";
                    String checkName = itemName.toLowerCase();
                    if (checkName.contains("bat"))         imgPath = "/ui/icon/assets/weapons/bat.png";
                    else if (checkName.contains("knife"))  imgPath = "/ui/icon/assets/weapons/knife.png";
                    else if (checkName.contains("bandage"))imgPath = "/ui/icon/assets/items/bandage.png";
                    else if (checkName.contains("medkit")) imgPath = "/ui/icon/assets/items/medkit.png";
                    else if (checkName.contains("bottle")) imgPath = "/ui/icon/assets/weapons/water-bottle.png";
                    else if (checkName.contains("crowbar"))imgPath = "/ui/icon/assets/weapons/crowbar.png";

                    final String finalImgPath = imgPath;

                    JPanel victoryPanel = new JPanel(null) {
                        Image frameImg, itemImg, invBoxImg;
                        {
                            try {
                                java.net.URL frameURL = getClass().getResource("/ui/panels/inventory/item-panel.png");
                                if (frameURL != null) frameImg = new ImageIcon(frameURL).getImage();
                                java.net.URL itemURL = getClass().getResource(finalImgPath);
                                if (itemURL != null) itemImg = new ImageIcon(itemURL).getImage();
                                else System.out.println("❌ ERROR: Could not find item image at: " + finalImgPath);
                                java.net.URL boxURL = getClass().getResource("/ui/panels/inventory/inventory-box.png");
                                if (boxURL != null) invBoxImg = new ImageIcon(boxURL).getImage();
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                            int bW = 372, bH = 310;
                            int bX = (W - bW) / 2, bY = (H - bH) / 2;
                            int cx = -10;
                            if (frameImg != null) g2.drawImage(frameImg, bX, bY, bW, bH, this);
                            g2.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 22f));
                            g2.setColor(Color.WHITE);
                            FontMetrics fm = g2.getFontMetrics();
                            g2.drawString("Item Found!", bX + (bW - fm.stringWidth("Item Found!")) / 2 + cx, bY + 50);
                            g2.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));
                            g2.drawString(itemName, bX + (bW - fm.stringWidth(itemName)) / 2 + cx, bY + 105);
                            int ibW = 140, ibH = 140;
                            int ibX = bX + (bW - ibW) / 2 + cx, ibY = bY + 115;
                            if (invBoxImg != null) g2.drawImage(invBoxImg, ibX, ibY, ibW, ibH, this);
                            if (itemImg != null) {
                                int iSize = 110;
                                g2.drawImage(itemImg, ibX + (ibW - iSize) / 2, ibY + (ibH - iSize) / 2, iSize, iSize, this);
                            }
                            g2.setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 16f));
                            String footer = "Added to inventory.";
                            g2.drawString(footer, bX + (bW - fm.stringWidth(footer)) / 2 + cx, bY + 285);
                            g2.dispose();
                        }
                    };
                    victoryPanel.setOpaque(false);
                    victoryPanel.setBounds(0, 0, W, H);

                    SwingUtilities.invokeLater(() -> {
                        add(victoryPanel);
                        setComponentZOrder(victoryPanel, 0);
                        repaint();
                    });
                    sleep(3000);
                    SwingUtilities.invokeLater(() -> {
                        remove(victoryPanel);
                        logLabel.setVisible(true);
                        if (healed && playerHpBarPanelInstance != null)
                            playerHpBarPanelInstance.setHp(Math.max(0, player.getHealth()), 100);
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

    private void buildEffectOverlay() {
        effectOverlay = new JLabel();
        effectOverlay.setOpaque(false);
        effectOverlay.setBounds((W - 350) / 2, (H - 350) / 2, 350, 350);
        effectOverlay.setVisible(false);
        add(effectOverlay);

        effectOverlay2 = new JLabel();
        effectOverlay2.setOpaque(false);
        // Shifted a bit to the right
        effectOverlay2.setBounds((W - 350) / 2 + 130, (H - 350) / 2, 350, 350);
        effectOverlay2.setVisible(false);
        add(effectOverlay2);
    }

    private void showEffect(String path) {
        java.io.File f = new java.io.File(path);
        if (!f.exists()) return;

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                ImageIcon raw = new ImageIcon(f.getAbsolutePath());
                Image scaled = raw.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
                effectOverlay.setIcon(new ImageIcon(scaled));
                effectOverlay.setVisible(true);
                setComponentZOrder(effectOverlay, 0);
                repaint();
            });
            sleep(500); // show for 500ms
            SwingUtilities.invokeLater(() -> {
                effectOverlay.setVisible(false);
                repaint();
            });
        }).start();
    }
    private void showEffect2(String path) {
        java.io.File f = new java.io.File(path);
        if (!f.exists()) return;

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                ImageIcon raw = new ImageIcon(f.getAbsolutePath());
                Image scaled = raw.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
                effectOverlay2.setIcon(new ImageIcon(scaled));
                effectOverlay2.setVisible(true);
                setComponentZOrder(effectOverlay2, 0);
                repaint();
            });
            sleep(500);
            SwingUtilities.invokeLater(() -> {
                effectOverlay2.setVisible(false);
                repaint();
            });
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
            if (zombieHpBarPanelInstance != null)
                zombieHpBarPanelInstance.setHp(Math.max(0, zombieHp), (50 + level * 10));
            if (playerHpBarPanelInstance != null)
                playerHpBarPanelInstance.setHp(Math.max(0, player.getHealth()), 100);
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

    private JButton makeCombatButton(String text, String normalPath, String hoverPath, String activePath) {
        Image normalImg = null, hoverImg = null, activeImg = null;
        try {
            java.io.File f1 = new java.io.File(normalPath);
            java.io.File f2 = new java.io.File(hoverPath);
            java.io.File f3 = new java.io.File(activePath);
            if (f1.exists()) normalImg = new ImageIcon(f1.getAbsolutePath()).getImage();
            if (f2.exists()) hoverImg  = new ImageIcon(f2.getAbsolutePath()).getImage();
            if (f3.exists()) activeImg = new ImageIcon(f3.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        final Image btnNormal = normalImg;
        final Image btnHover  = hoverImg;
        final Image btnActive = activeImg;

        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(GameFonts.MUNRO.deriveFont(Font.PLAIN, 18f));
                setForeground(Color.WHITE);
                setHorizontalTextPosition(JButton.CENTER);
                setVerticalTextPosition(JButton.CENTER);
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                boolean isPressed = getModel().isPressed();
                Image currentImg = isPressed ? btnActive : hovered ? btnHover : btnNormal;
                if (currentImg != null) g2.drawImage(currentImg, 0, 0, getWidth(), getHeight(), null);
                else { g2.setColor(new Color(62, 55, 49)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                g2.dispose();
                g.translate(4, 5);
                if (isPressed) g.translate(-3, 3);
                super.paintComponent(g);
                if (isPressed) g.translate(3, -3);
                g.translate(-4, -5);
            }
        };
        return btn;
    }

    // ==========================================
    // HP BAR PANEL
    // ==========================================
    private class HpBarPanel extends JPanel {
        private Image framePanelImg;
        private Image statusBarImg;
        private JLabel hpTitle, hpValLabel;
        private int currentHp, maxHp;
        private boolean isRightAligned;

        private final int panelW      = 380;
        private final int barW        = 265;
        private final int barH        = 24;
        private final int textW       = 65;
        private final int sideMargin  = 15;
        private final int gap         = 2;
        private final int textTitleY  = 15;
        private final int textNumbersY= 37;
        private final int mainBarY    = 32;

        public HpBarPanel(String titleText, boolean rightAligned, int startingHp, int startMaxHp, String framePath) {
            setLayout(null);
            setOpaque(false);
            this.currentHp      = startingHp;
            this.maxHp          = startMaxHp;
            this.isRightAligned = rightAligned;

            java.io.File fFrame = new java.io.File(framePath);
            if (fFrame.exists()) framePanelImg = new ImageIcon(fFrame.getAbsolutePath()).getImage();
            java.io.File fStatusBar = new java.io.File("res/ui/panels/status-bar.png");
            if (fStatusBar.exists()) statusBarImg = new ImageIcon(fStatusBar.getAbsolutePath()).getImage();

            int textX = isRightAligned ? (panelW - sideMargin - textW) : sideMargin;
            Color myTextColor = isRightAligned ? new Color(255, 220, 60) : new Color(220, 80, 80);

            hpTitle = new JLabel(titleText, isRightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
            hpTitle.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 15f));
            hpTitle.setForeground(myTextColor);
            hpTitle.setBounds(textX, textTitleY, textW, 25);
            add(hpTitle);

            hpValLabel = new JLabel(startingHp + " / " + startMaxHp, isRightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
            hpValLabel.setFont(GameFonts.MUNRO.deriveFont(Font.BOLD, 16f));
            hpValLabel.setForeground(Color.WHITE);
            hpValLabel.setBounds(textX, textNumbersY, textW, 25);
            add(hpValLabel);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if (framePanelImg != null) g2.drawImage(framePanelImg, 0, 0, getWidth(), getHeight(), this);

            int barX = isRightAligned
                    ? (panelW - sideMargin - textW - gap - barW)
                    : (sideMargin + textW + gap);
            int barY = mainBarY;

            if (statusBarImg != null) g2.drawImage(statusBarImg, barX, barY, barW, barH, this);
            else { g2.setColor(new Color(40, 40, 40)); g2.fillRoundRect(barX, barY, barW, barH, 5, 5); }

            int fillOffsetX = 4, fillOffsetY = 4;
            int fillMaxW = barW - (fillOffsetX * 2);
            int fillH    = barH - (fillOffsetY * 2);
            float percent = (float) Math.max(0, currentHp) / (float) maxHp;
            int currentFillW = (int) (fillMaxW * percent);

            if (currentFillW > 0) {
                if (hpBarTextureFill instanceof java.awt.image.BufferedImage) {
                    TexturePaint tp = new TexturePaint((java.awt.image.BufferedImage) hpBarTextureFill,
                            new Rectangle(0, 0, 32, fillH));
                    g2.setPaint(tp);
                } else {
                    g2.setColor(getHpColor(currentHp, maxHp));
                }
                g2.fillRect(barX + fillOffsetX, barY + fillOffsetY, Math.max(2, currentFillW), fillH);
            }
            g2.dispose();
        }

        public void setHp(int current, int max) {
            this.currentHp = current;
            this.maxHp     = max;
            hpValLabel.setText(Math.max(0, current) + " / " + max);
            repaint();
        }
    }

    private Color getHpColor(int hp, int maxHp) {
        float percent = (float) hp / (float) maxHp;
        if (percent >= 0.6f) return new Color(25, 83, 44);
        else if (percent >= 0.3f) return new Color(198, 174, 47);
        return new Color(120, 16, 16);
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