package InventorySystem;

import Player.Player;
import Weapon.Weapon;
import Weapon.WeaponInventory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryPanel extends JPanel {

    private static final int W = 800;
    private static final int H = 600;

    private Player player;

    // FONT
    private String bFont = "Munro";

    // IMAGES
    private Image inventoryBoxImg;
    private Image closeDef, closeHov, closeAct;

    public interface InventoryCloseListener {
        void onClose();
    }
    private InventoryCloseListener closeListener;

    public InventoryPanel(Player player) {
        this.player = player;

        setLayout(null);
        setPreferredSize(new Dimension(W, H));
        setOpaque(false);

        // 1. Load Images
        try {
            java.io.File boxFile = new java.io.File("res/ui/panels/inventory/inventory-box.png");
            if (boxFile.exists()) inventoryBoxImg = new ImageIcon(boxFile.getAbsolutePath()).getImage();

            java.io.File cDef = new java.io.File("res/ui/icon/small-buttons/not-active.png");
            java.io.File cHov = new java.io.File("res/ui/icon/small-buttons/hover.png");
            java.io.File cAct = new java.io.File("res/ui/icon/small-buttons/active.png");

            if (cDef.exists()) closeDef = new ImageIcon(cDef.getAbsolutePath()).getImage();
            if (cHov.exists()) closeHov = new ImageIcon(cHov.getAbsolutePath()).getImage();
            if (cAct.exists()) closeAct = new ImageIcon(cAct.getAbsolutePath()).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        buildUI();

        // Close inventory if the player clicks the dark background outside the box
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (closeListener != null) closeListener.onClose();
            }
        });
    }

    public void setCloseListener(InventoryCloseListener listener) {
        this.closeListener = listener;
    }
    @Override
    public void addNotify() {
        super.addNotify();
        if (getParent() != null) {
            getParent().setComponentZOrder(this, 0);
        }
    }

    // ==============================
    // DRAW DARK BACKGROUND
    // ==============================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 102 alpha is exactly 40% opacity! (255 * 0.40)
        g.setColor(new Color(0, 0, 0, 102));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // ==============================
    // BUILD UI
    // ==============================
    private void buildUI() {
        int boxW = 670;
        int boxH = 400;

        // The main container that draws the custom inventory box image
        JPanel centerContainer = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (inventoryBoxImg != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.drawImage(inventoryBoxImg, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();
                } else {
                    // Fallback
                    g.setColor(new Color(64, 59, 54));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        centerContainer.setOpaque(false);
        // Prevent clicks on the box from closing the inventory
        centerContainer.addMouseListener(new MouseAdapter() {});

        // ── Custom "X" Close Button ──
        JButton closeBtn = new JButton("X") {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font(bFont, Font.BOLD, 18));
                setForeground(Color.WHITE);

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed();
                Image currentSprite;

                if (isPressed) {
                    currentSprite = closeAct;
                } else if (hovered) {
                    currentSprite = closeHov;
                } else {
                    currentSprite = closeDef;
                }

                // 1. Draw the button image first
                if (currentSprite != null) {
                    g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                }
                g2.dispose();

                // 2. PUSH THE TEXT DOWN IF PRESSED
                if (isPressed) {
                    g.translate(-1, 1);
                }

                // 3. Draw the text on top
                super.paintComponent(g);

                // 4. Reset the position accurately (reversing the -1, 1)
                if (isPressed) {
                    g.translate(1, -1);
                }
            }
        };
        closeBtn.setBounds(boxW - 45, 10, 35, 35); // Top right corner
        closeBtn.addActionListener(e -> {
            if (closeListener != null) closeListener.onClose();
        });
        centerContainer.add(closeBtn);

        // ── Tabs / Header Text ──
        JLabel weaponsText = new JLabel("Weapons", SwingConstants.CENTER);
        weaponsText.setFont(new Font(bFont, Font.PLAIN, 20));
        weaponsText.setForeground(Color.WHITE);
        weaponsText.setBounds(10, 5, 170, 45);
        centerContainer.add(weaponsText);

        JLabel healingText = new JLabel("Healing Items", SwingConstants.CENTER);
        healingText.setFont(new Font(bFont, Font.PLAIN, 20));
        healingText.setForeground(Color.WHITE);
        healingText.setBounds(190, 5, 190, 45);
        centerContainer.add(healingText);

        // ── Scroll Area for Items ──
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // --- WEAPONS ---
        WeaponInventory wi = player.getWeaponInventory();
        JLabel wHeader = sectionHeader("WEAPONS");
        content.add(wHeader);
        content.add(Box.createVerticalStrut(6));

        if (wi.getSize() == 0) {
            content.add(emptyLabel("— Empty —"));
        } else {
            for (int i = 0; i < wi.getSize(); i++) {
                Weapon w = wi.getInventory().get(i);
                content.add(itemRow(
                        w.getName(),
                        "DMG: " + w.getDamage() + "  |  DUR: " + w.getDurability() + "/" + w.getMaxDurability() + "  |  " + w.getDescription()
                ));
                content.add(Box.createVerticalStrut(6));
            }
        }

        content.add(Box.createVerticalStrut(16));

        // --- HEALING ITEMS ---
        JLabel hHeader = sectionHeader("HEALING ITEMS");
        content.add(hHeader);
        content.add(Box.createVerticalStrut(6));

        if (!player.hasConsumables()) {
            content.add(emptyLabel("— Empty —"));
        } else {
            List<String> items = new ArrayList<>(player.showConsumableInventory());
            for (String item : items) {
                String rawName = item.contains(" x") ? item.substring(0, item.indexOf(" x")) : item;
                int healAmt = switch (rawName) {
                    case "Medkit"  -> 25;
                    case "Bandage" -> 15;
                    default        -> 0;
                };
                content.add(itemRow(
                        item,
                        healAmt > 0 ? "Heals " + healAmt + " HP" : "Unknown item"
                ));
                content.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBounds(30, 80, boxW - 60, boxH - 110);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerContainer.add(scroll);

        add(centerContainer);

        // ── Auto-Center the Box ──
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int cx = (getWidth() - boxW) / 2;
                int cy = (getHeight() - boxH) / 2;
                centerContainer.setBounds(cx, cy, boxW, boxH);
            }
        });
    }

    // ==============================
    // HELPERS
    // ==============================
    private JLabel sectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(bFont, Font.BOLD, 16));
        lbl.setForeground(new Color(220, 220, 220)); // Light grey text
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        return lbl;
    }

    private JLabel emptyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(bFont, Font.ITALIC, 16));
        lbl.setForeground(new Color(140, 130, 120)); // Dimmed out text
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        return lbl;
    }

    private JPanel itemRow(String name, String details) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(480, 50));
        row.setPreferredSize(new Dimension(480, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel("  " + name);
        nameLbl.setFont(new Font(bFont, Font.BOLD, 15));
        nameLbl.setForeground(Color.WHITE);

        JLabel detLbl = new JLabel(details);
        detLbl.setFont(new Font(bFont, Font.PLAIN, 13));
        detLbl.setForeground(new Color(200, 200, 200));

        row.add(nameLbl, BorderLayout.NORTH);
        row.add(detLbl, BorderLayout.CENTER);

        return row;
    }
}