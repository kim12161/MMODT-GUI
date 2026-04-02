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
        setBackground(new Color(15, 0, 0));

        buildUI();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(60, 0, 0),
                0, H, new Color(10, 0, 0));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setCombatEndListener(CombatEndListener listener) {
        this.combatEndListener = listener;
    }

    // ==============================
    // BUILD UI
    // ==============================
    private void buildUI() {

        // Dark red overlay background
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Gradient dark red bg
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(60, 0, 0),
                        0, H, new Color(10, 0, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setOpaque(false);
        bg.setBounds(0, 0, W, H);
        add(bg);

        // Title
        titleLabel = new JLabel("! ZOMBIE ENCOUNTER !", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        titleLabel.setForeground(new Color(220, 50, 50));
        titleLabel.setBounds(0, 30, W, 40);
        add(titleLabel);

        // HP bars panel
        JPanel hpPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(20, 20, 20, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(180, 30, 30, 150));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        hpPanel.setOpaque(false);
        hpPanel.setBounds(100, 90, 600, 80);

        zombieHpLabel = new JLabel("", SwingConstants.CENTER);
        zombieHpLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        zombieHpLabel.setForeground(new Color(220, 80, 80));
        zombieHpLabel.setBounds(0, 10, 600, 25);

        playerHpLabel = new JLabel("", SwingConstants.CENTER);
        playerHpLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        playerHpLabel.setForeground(new Color(80, 220, 120));
        playerHpLabel.setBounds(0, 42, 600, 25);

        hpPanel.add(zombieHpLabel);
        hpPanel.add(playerHpLabel);
        add(hpPanel);

        // Combat log
        logLabel = new JLabel("", SwingConstants.CENTER);
        logLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        logLabel.setForeground(Color.WHITE);
        logLabel.setBounds(50, 195, 700, 25);
        add(logLabel);

        // Second log line
        JLabel logLabel2 = new JLabel("", SwingConstants.CENTER);

        // Combat action buttons
        dodgeBtn      = makeCombatButton("DODGE",     new Color(40, 100, 160));
        fightBtn      = makeCombatButton("FIGHT",     new Color(160, 40, 40));
        inventoryBtn  = makeCombatButton("INVENTORY", new Color(60, 120, 60));

        dodgeBtn.setBounds(130, 260, 160, 50);
        fightBtn.setBounds(320, 260, 160, 50);
        inventoryBtn.setBounds(510, 260, 160, 50);

        add(dodgeBtn);
        add(fightBtn);
        add(inventoryBtn);

        // Inventory sub-panel (hidden initially)
        buildInventoryPanel();

        // Wire buttons
        dodgeBtn.addActionListener(e -> {
            synchronized (actionLock) {
                pendingAction      = "DODGE";
                pendingWeaponIndex = -1;
                actionLock.notifyAll();
            }
        });

        fightBtn.addActionListener(e -> {
            synchronized (actionLock) {
                pendingAction      = "FIGHT";
                pendingWeaponIndex = -1;
                actionLock.notifyAll();
            }
        });

        inventoryBtn.addActionListener(e ->
                showInventoryPanel()
        );

        updateHpLabels();
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
                g2.setColor(new Color(180, 30, 30));
                g2.setStroke(new BasicStroke(1.5f));
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
        sep.setForeground(new Color(180, 30, 30));
        inventoryPanel.add(sep);

        add(inventoryPanel);
    }

    private void showInventoryPanel() {

        // Clear old buttons
        inventoryPanel.removeAll();

        JLabel invTitle = new JLabel("INVENTORY", SwingConstants.CENTER);
        invTitle.setFont(new Font("Consolas", Font.BOLD, 16));
        invTitle.setForeground(new Color(220, 60, 60));
        invTitle.setBounds(0, 10, 500, 25);
        inventoryPanel.add(invTitle);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 38, 460, 2);
        sep.setForeground(new Color(180, 30, 30));
        inventoryPanel.add(sep);

        WeaponInventory wi = player.getWeaponInventory();
        int yPos = 50;

        // Weapons
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

        // --- HEALING ITEMS ---
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
                    String rawName = item.contains(" x")
                            ? item.substring(0, item.indexOf(" x"))
                            : item;
                    boolean used = player.useConsumable(rawName);
                    inventoryPanel.setVisible(false);
                    SwingUtilities.invokeLater(() -> {
                        updateHpLabels();
                        int healAmt = switch (rawName) {
                            case "Medkit"  -> 25;
                            case "Bandage" -> 15;
                            default        -> 0;
                        };
                        setLog(used
                                ? "Used " + rawName + "! +" + healAmt + " HP restored."
                                : "Your HP is already full!");
                    });
                });
                inventoryPanel.add(iBtn);
                yPos += 42;
            }
        }

        // Cancel
        JButton cancelBtn = makeCombatButton("CANCEL", new Color(60, 60, 60));
        cancelBtn.setBounds(170, yPos + 4, 160, 36);
        cancelBtn.addActionListener(e -> inventoryPanel.setVisible(false));
        inventoryPanel.add(cancelBtn);

        // Resize panel to fit content
        int newH = yPos + 56;
        inventoryPanel.setBounds(150, 600 - newH - 20, 500, newH);

        inventoryPanel.setVisible(true);
        revalidate();
        repaint();
    }

    // ==============================
    // START COMBAT LOOP
    // ==============================
    public void startCombat() {

        new Thread(() -> {

            WeaponInventory wi = player.getWeaponInventory();

            while (player.isAlive() && zombieHp > 0) {

                // Reset pending
                pendingAction      = null;
                pendingWeaponIndex = -1;

                // Enable buttons
                setButtonsEnabled(true);

                // Wait for player action
                synchronized (actionLock) {
                    while (pendingAction == null) {
                        try { actionLock.wait(); }
                        catch (InterruptedException ignored) {}
                    }
                }

                // Disable buttons while processing
                setButtonsEnabled(false);

                String action = pendingAction;
                int    wIdx   = pendingWeaponIndex;

                // Process turn
                String logMsg;

                switch (action) {

                    case "DODGE":
                        int hpBefore = zombieHp;
                        zombieHp = ZombieEncounter.processTurn(
                                level, zombieHp, player, wi, "1", -1);
                        if (zombieHp < hpBefore)
                            logMsg = "Dodge success! Counter-attacked the zombie.";
                        else
                            logMsg = "Dodge failed! Zombie caught you.";
                        break;

                    case "FIGHT":
                        zombieHp = ZombieEncounter.processTurn(
                                level, zombieHp, player, wi, "2", -1);
                        logMsg = "You fought with your fists!";
                        break;

                    case "WEAPON":
                        if (wIdx >= 0 && wIdx < wi.getSize()) {
                            Weapon w = wi.getInventory().get(wIdx);
                            zombieHp = ZombieEncounter.processTurn(
                                    level, zombieHp, player, wi, "3", wIdx);
                            logMsg = "Used " + w.getName() + "!";
                        } else {
                            logMsg = "No weapon selected.";
                        }
                        break;

                    default:
                        logMsg = "";
                }

                final int   displayZombieHp = Math.max(0, zombieHp);
                final String finalLog       = logMsg;

                SwingUtilities.invokeLater(() -> {
                    updateHpLabels();
                    setLog(finalLog);
                });

                sleep(600);
            }

            // Combat ended
            boolean playerAlive = player.isAlive();

            if (zombieHp <= 0 && playerAlive) {

                // Reward
                player.heal(10);
                Weapon found = WeaponInventory.getRandomWeapon();
                player.getWeaponInventory().addWeapon(found);

                SwingUtilities.invokeLater(() -> {
                    setButtonsEnabled(false);
                    setLog("Zombie defeated! Found: " + found.getName()
                            + "  |  Healed 10 HP.");
                });

                sleep(2500);

            } else if (!playerAlive) {

                SwingUtilities.invokeLater(() -> {
                    setButtonsEnabled(false);
                    setLog("You were devoured by zombies...");
                });

                sleep(2500);
            }

            if (combatEndListener != null)
                combatEndListener.onCombatEnd(playerAlive);

        }).start();
    }

    // ==============================
    // HELPERS
    // ==============================
    private void updateHpLabels() {
        zombieHpLabel.setText("ZOMBIE  HP  :  "
                + Math.max(0, zombieHp) + " / " + (50 + level * 10));
        playerHpLabel.setText("YOUR  HP    :  "
                + Math.max(0, player.getHealth()) + " / 100");
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

    private JButton makeCombatButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font("Consolas", Font.BOLD, 13));
                setForeground(Color.WHITE);
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true; repaint();
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false; repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = hovered ? bg.brighter() : bg;
                if (!isEnabled()) fill = new Color(40, 40, 40);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    private JButton makeInventoryItemButton(String text) {
        JButton btn = makeCombatButton(text, new Color(40, 40, 60));
        btn.setFont(new Font("Consolas", Font.PLAIN, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ignored) {}
    }
}
