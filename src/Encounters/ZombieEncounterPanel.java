package Encounters;

import Player.Player;
import Weapon.Weapon;
import Weapon.WeaponInventory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ZombieEncounterPanel extends JPanel {

    private static final int W = 800;
    private static final int H = 600;

    // ==============================
    // UI COMPONENTS
    // ==============================
    private JLabel  titleLabel;
    private JLabel  zombieHpLabel;
    private JLabel  playerHpLabel;
    private JLabel  logLabel;

    private JButton dodgeBtn;
    private JButton fightBtn;
    private JButton inventoryBtn;

    // ==============================
    // GAME STATE
    // ==============================
    private Player player;
    private int    level;
    private int    zombieHp;

    private volatile String pendingAction      = null;
    private volatile int    pendingWeaponIndex = -1;
    private final Object    actionLock         = new Object();

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

        this.player   = player;
        this.level    = level;
        this.zombieHp = 50 + (level * 10);

        setLayout(null);
        setPreferredSize(new Dimension(W, H));
        // CHANGED: Background to solid black
        setBackground(Color.BLACK);

        buildUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // CHANGED: Gradient removed to keep panel solid black
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setCombatEndListener(CombatEndListener listener) {
        this.combatEndListener = listener;
    }

    // ==============================
    // BUILD UI
    // ==============================
    private void buildUI() {
        setLayout(null);

        titleLabel = new JLabel("! ZOMBIE ENCOUNTER !", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        titleLabel.setForeground(new Color(220, 50, 50));
        titleLabel.setBounds(0, 30, W, 40);
        add(titleLabel);

        JPanel hpPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(20, 20, 20, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // CHANGED: HP Panel border to black
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        hpPanel.setOpaque(false);
        hpPanel.setBounds((W - 600) / 2, 80, 600, 90);

        zombieHpLabel = new JLabel("", SwingConstants.CENTER);
        zombieHpLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        zombieHpLabel.setForeground(new Color(220, 80, 80));
        zombieHpLabel.setBounds(0, 15, 600, 25);

        playerHpLabel = new JLabel("", SwingConstants.CENTER);
        playerHpLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        playerHpLabel.setForeground(new Color(80, 220, 120));
        playerHpLabel.setBounds(0, 50, 600, 25);

        hpPanel.add(zombieHpLabel);
        hpPanel.add(playerHpLabel);
        add(hpPanel);

        logLabel = new JLabel("A zombie approaches!", SwingConstants.CENTER);
        logLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        logLabel.setForeground(Color.WHITE);
        logLabel.setBounds(0, 185, W, 30);
        add(logLabel);

        // BUTTONS CREATED WITH WHITE COLOR (Opacity handled in makeCombatButton)
        dodgeBtn      = makeCombatButton("DODGE",     Color.WHITE);
        fightBtn      = makeCombatButton("FIGHT",     Color.WHITE);
        inventoryBtn  = makeCombatButton("INVENTORY", Color.WHITE);

        int btnW = 160;
        int startX = (W - (btnW * 3 + 60)) / 2;

        dodgeBtn.setBounds(startX, 240, btnW, 50);
        fightBtn.setBounds(startX + btnW + 30, 240, btnW, 50);
        inventoryBtn.setBounds(startX + (btnW + 30) * 2, 240, btnW, 50);

        add(dodgeBtn);
        add(fightBtn);
        add(inventoryBtn);

        setComponentZOrder(dodgeBtn, 0);
        setComponentZOrder(fightBtn, 0);
        setComponentZOrder(inventoryBtn, 0);

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
                        try { actionLock.wait(); } catch (InterruptedException ignored) {}
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
                SwingUtilities.invokeLater(() -> { updateHpLabels(); setLog(finalLog); });
                sleep(800);
            }
            boolean playerAlive = player.isAlive();
            if (zombieHp <= 0 && playerAlive) {
                player.heal(10);
                Weapon found = WeaponInventory.getRandomWeapon();
                if (wi.getSize() < 3) {
                    // space available — just add it
                    wi.addWeapon(found);
                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        setLog("Victory! Found: " + found.getName() + "  |  Healed 10 HP.");
                    });
                } else {
                    // only shows discard UI when inventory is full (size == 3)
                    SwingUtilities.invokeLater(() -> {
                        setButtonsEnabled(false);
                        setLog("Inventory full! Choose a weapon to discard.");
                        showDiscardPanel(found);
                    });
                }
            } else if (!playerAlive) {
                SwingUtilities.invokeLater(() -> setLog("Death has claimed you..."));
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
            zombieHpLabel.setText("ZOMBIE  HP  :  " + Math.max(0, zombieHp) + " / " + (50 + level * 10));
            playerHpLabel.setText("YOUR  HP    :  " + Math.max(0, player.getHealth()) + " / 100");
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
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font("Consolas", Font.BOLD, 13));
                // CHANGED: Foreground to black for contrast on white background
                setForeground(Color.BLACK);
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // CHANGED: White background with 60% opacity (153/255)
                // Hover increases opacity slightly for visual feedback
                int alpha = hovered ? 200 : 153;
                if (!isEnabled()) alpha = 80;

                g2.setColor(new Color(255, 255, 255, alpha));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // CHANGED: Button outline to solid black
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);

                g2.dispose();
                super.paintComponent(g);
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

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}