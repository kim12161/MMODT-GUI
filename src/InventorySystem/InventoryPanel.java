package InventorySystem;

import Player.Player;
import Weapon.Weapon;
import Weapon.WeaponInventory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryPanel extends JPanel {

    private static final int W = 800;
    private static final int H = 600;

    private Player player;


    //FONT
    private String mainFont="PixelArmy";
    private String bFont="Munro";

    public interface InventoryCloseListener {
        void onClose();
    }
    private InventoryCloseListener closeListener;

    public InventoryPanel(Player player) {

        this.player = player;

        setLayout(null);
        setPreferredSize(new Dimension(W, H));
        setOpaque(false);

        buildUI();
    }

    public void setCloseListener(InventoryCloseListener listener) {
        this.closeListener = listener;
    }

    // ==============================
    // BUILD UI
    // ==============================
    private void buildUI() {

        // white panel with black border
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // CHANGED: Background to white
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // CHANGED: Outer border to black
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBounds(100, 60, 600, 480);

        // Title
        JLabel title = new JLabel("INVENTORY", SwingConstants.CENTER);
        title.setFont(new Font(mainFont, Font.BOLD, 22));
        title.setForeground(new Color(220, 60, 60));
        title.setBounds(0, 16, 600, 30);
        card.add(title);

        JSeparator sep = new JSeparator();
        sep.setBounds(30, 52, 540, 2);
        sep.setForeground(new Color(180, 30, 30));
        card.add(sep);

        // Scroll area
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // --- WEAPONS ---
        WeaponInventory wi = player.getWeaponInventory();

        JLabel wHeader = sectionHeader("WEAPONS");
        content.add(wHeader);
        content.add(Box.createVerticalStrut(6));

        if (wi.getSize() == 0) {
            content.add(emptyLabel("No weapons in inventory."));
        } else {
            for (int i = 0; i < wi.getSize(); i++) {
                Weapon w = wi.getInventory().get(i);
                content.add(itemRow(
                        w.getName(),
                        "DMG: " + w.getDamage()
                                + "  |  HIT: " + w.getDamageSuccess() + "%"
                                + "  |  DUR: " + w.getDurability()
                                + "/" + w.getMaxDurability()
                                + "  |  " + w.getDescription(),
                        new Color(220, 180, 60)
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
            content.add(emptyLabel("No healing items."));
        } else {
            List<String> items = new ArrayList<>(player.showConsumableInventory());
            for (String item : items) {
                String rawName = item.contains(" x")
                        ? item.substring(0, item.indexOf(" x"))
                        : item;
                int healAmt = switch (rawName) {
                    case "Medkit"  -> 25;
                    case "Bandage" -> 15;
                    default        -> 0;
                };
                content.add(itemRow(
                        item,
                        healAmt > 0 ? "Heals " + healAmt + " HP" : "Unknown item",
                        new Color(80, 200, 120)
                ));
                content.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBounds(20, 60, 560, 360);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        card.add(scroll);

        // Close button
        JButton closeBtn = makeButton("CLOSE", Color.WHITE); // Set button internal color to white
        closeBtn.setBounds(220, 430, 160, 40);
        closeBtn.addActionListener(e -> {
            if (closeListener != null) closeListener.onClose();
        });
        card.add(closeBtn);

        add(card);
    }

    // ==============================
    // HELPERS
    // ==============================
    private JLabel sectionHeader(String text) {
        JLabel lbl = new JLabel("— " + text + " —");
        lbl.setFont(new Font(bFont, Font.BOLD, 14));
        lbl.setForeground(new Color(220, 60, 60));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        return lbl;
    }

    private JLabel emptyLabel(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font(bFont, Font.ITALIC, 13));
        lbl.setForeground(new Color(120, 120, 120));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel itemRow(String name, String details, Color nameColor) {

        JPanel row = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // CHANGED: Row background to white
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // ADDED: Black border for rows
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(540, 50));
        row.setPreferredSize(new Dimension(540, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font(bFont, Font.BOLD, 13));
        nameLbl.setForeground(nameColor);
        nameLbl.setBounds(10, 6, 520, 18);

        JLabel detLbl = new JLabel(details);
        detLbl.setFont(new Font(bFont, Font.PLAIN, 11));
        detLbl.setForeground(new Color(60, 60, 60)); // Darkened text for white background
        detLbl.setBounds(10, 26, 520, 18);

        row.add(nameLbl);
        row.add(detLbl);

        return row;
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font(bFont, Font.BOLD, 14));
                setForeground(Color.BLACK); // Changed text to black for white background
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true; repaint();
                    }
                    public void mouseExited (java.awt.event.MouseEvent e) {
                        hovered = false; repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // CHANGED: Background to white (slight gray on hover)
                g2.setColor(hovered ? new Color(240, 240, 240) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // CHANGED: Outer border to black
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }
}