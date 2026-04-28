package menu;

import main.GamePanel;
import saveSystem.SaveSystem;
import saveSystem.SaveSystem.SaveData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ContinuePanel — shown when the player clicks "Continue" on the title screen.
 * Displays all 3 save slots inside a custom panel image.
 */
public class ContinuePanel extends JPanel {

    // ── Callback fired when the player picks a slot ──────────────────────────
    @FunctionalInterface
    public interface OnSlotSelected {
        void onSelected(SaveData data);
    }

    // ── Palette (Medium Brown slots to contrast with the light body) ─────────
    private static final Color SLOT_EMPTY = new Color(54, 49, 44, 120);   // Exact color from your image!
    private static final Color SLOT_FULL  = new Color(64, 59, 54);   // Slightly lighter for populated slots
    private static final Color SLOT_HOVER = new Color(54, 49, 44);
    private static final Color ACCENT     = Color.WHITE;
    private static final Color TEXT_MAIN  = Color.WHITE;
    private static final Color TEXT_DIM   = new Color(220, 220, 220);
    private static final Color DELETE_CLR = new Color(220,  80,  80);
    private static final Color BORDER_CLR = new Color(50, 40, 35); // Dark border for slots

    private String bFont = "Munro";

    // ── Images ────────────────────────────────────────────────────────────────
    private Image bgImage;
    private Image panelImg;
    private Image closeActive, closeHover, closeDefault;

    {
        try {
            java.io.File fBg = new java.io.File("res/backgrounds/main-background.gif");
            if (fBg.exists()) bgImage = new ImageIcon(fBg.getAbsolutePath()).getImage();

            java.io.File fPanel = new java.io.File("res/ui/panels/save-slots-panel.png");
            if (fPanel.exists()) panelImg = new ImageIcon(fPanel.getAbsolutePath()).getImage();

            java.io.File fCActive = new java.io.File("res/ui/icon/small-buttons/active.png");
            if (fCActive.exists()) closeActive = new ImageIcon(fCActive.getAbsolutePath()).getImage();

            java.io.File fCHover = new java.io.File("res/ui/icon/small-buttons/hover.png");
            if (fCHover.exists()) closeHover = new ImageIcon(fCHover.getAbsolutePath()).getImage();

            java.io.File fCDef = new java.io.File("res/ui/icon/small-buttons/not-active.png");
            if (fCDef.exists()) closeDefault = new ImageIcon(fCDef.getAbsolutePath()).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContinuePanel(GamePanel gamePanel, OnSlotSelected callback) {
        setLayout(null); // Absolute layout to center the panel exactly
        setOpaque(false);

        // Define the dimensions of the save slots panel
        int panelW = 560;
        int panelH = 420;
        int panelX = (900 - panelW) / 2;
        int panelY = (700 - panelH) / 2;

        // ── Main Center Panel (Draws save-slots-panel.png) ───────────────────
        JPanel centerContainer = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (panelImg != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.drawImage(panelImg, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();
                }
            }
        };
        centerContainer.setBounds(panelX, panelY, panelW, panelH);
        centerContainer.setOpaque(false);

        // ── Header Title (Centered in the upper brown part) ──────────────────
        JLabel title = new JLabel("Save Slots", SwingConstants.CENTER);
        title.setFont(new Font(bFont, Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 10, panelW, 70); // Placed specifically over the dark brown header
        centerContainer.add(title);

        // ── Custom "X" Close Button (Top Right) ──────────────────────────────
        JButton closeBtn = new JButton() {
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
                    currentSprite = closeActive != null ? closeActive : closeDefault;
                } else if (hovered) {
                    currentSprite = closeHover != null ? closeHover : closeDefault;
                } else {
                    currentSprite = closeDefault;
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

                // 3. DRAW THE "X" MANUALLY (Bypasses the "..." glitch entirely!)
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int textX = ((getWidth() - fm.stringWidth("X")) / 2) + 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString("X", textX, textY);

                // 4. Reset the position accurately to inverse the translate
                if (isPressed) {
                    g.translate(1, -1);
                }
            }
        };
        // Position the X button inside the brown header
        closeBtn.setBounds(panelW - 70, 19, 44, 44);
        closeBtn.addActionListener(e -> {
            gamePanel.removeAll();
            gamePanel.setLayout(null);
            new TitleScreen(gamePanel, gamePanel);
            gamePanel.revalidate();
            gamePanel.repaint();
        });
        centerContainer.add(closeBtn);

        // ── Slot cards ────────────────────────────────────────────────────────
        SaveData[] slots = SaveSystem.loadAllSlots();

        int startY = 100; // Start below the header
        int slotH = 90;  // Height of each slot
        int gap = 13;     // Gap between slots

        for (int i = 0; i < SaveSystem.MAX_SLOTS; i++) {
            JPanel slotCard = buildSlotCard(i + 1, slots[i], gamePanel, callback);
            slotCard.setBounds(30, startY + (i * (slotH + gap)), panelW - 60, slotH);
            centerContainer.add(slotCard);
        }

        add(centerContainer);
    }

    // ── Draw background with tint ─────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }

        // 40% opacity dark tint
        g.setColor(new Color(0, 0, 0, 102));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // ── Build one save-slot card ──────────────────────────────────────────────
    private JPanel buildSlotCard(int slotNumber, SaveData data,
                                 GamePanel gamePanel, OnSlotSelected callback) {

        boolean hasData = (data != null);

        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(hasData ? SLOT_FULL : SLOT_EMPTY);
        card.setBorder(new EmptyBorder(15, 25, 15, 20));

        // Slot number badge
        JLabel badge = new JLabel("SLOT " + slotNumber);
        badge.setFont(new Font(bFont, Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setPreferredSize(new Dimension(60, 0));
        badge.setVerticalAlignment(SwingConstants.CENTER);
        card.add(badge, BorderLayout.WEST);

        // Slot info (centre)
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        if (hasData) {
            JLabel nameLvl = new JLabel(data.playerName + "  ·  Level " + data.currentLevel + " — " + data.levelName);
            nameLvl.setFont(new Font(bFont, Font.BOLD, 18));
            nameLvl.setForeground(TEXT_MAIN);

            JLabel ts = new JLabel(data.timestamp);
            ts.setFont(new Font(bFont, Font.PLAIN, 11));
            ts.setForeground(TEXT_DIM);

            JLabel stats = new JLabel("HP: " + data.playerHealth + "   Charisma: " + data.playerCharisma);
            stats.setFont(new Font(bFont, Font.PLAIN, 14));
            stats.setForeground(TEXT_DIM);

            info.add(nameLvl);
            info.add(Box.createVerticalStrut(4));
            info.add(ts);
            info.add(Box.createVerticalStrut(4));
            info.add(stats);
        } else {
            JLabel empty = new JLabel("— Empty Slot —");
            empty.setFont(new Font(bFont, Font.ITALIC, 18));
            empty.setForeground(TEXT_DIM);
            info.add(Box.createVerticalGlue());
            info.add(empty);
            info.add(Box.createVerticalGlue());
        }

        card.add(info, BorderLayout.CENTER);

        // Right-side buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        if (hasData) {
            final SaveData captured = data;

            JButton loadBtn = styledButton("load", Color.WHITE, new Color(100, 90, 80));

            loadBtn.setPreferredSize(new Dimension(70, 30));

            loadBtn.addActionListener(e -> callback.onSelected(captured));
            btnPanel.add(loadBtn);

            JButton delBtn = styledButton("delete", DELETE_CLR, new Color(60, 40, 40));
           delBtn.setPreferredSize(new Dimension(74, 30));

            delBtn.setToolTipText("Delete save");
            delBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        gamePanel,
                        "Delete save in Slot " + slotNumber + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    SaveSystem.deleteSlot(slotNumber);
                    gamePanel.removeAll();
                    gamePanel.setLayout(new BorderLayout());
                    gamePanel.add(new ContinuePanel(gamePanel, callback), BorderLayout.CENTER);
                    gamePanel.revalidate();
                    gamePanel.repaint();
                }
            });
            btnPanel.add(delBtn);
        }

        card.add(btnPanel, BorderLayout.EAST);

        // Hover highlight (populated slots only)
        if (hasData) {
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(SLOT_HOVER); card.repaint();
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(SLOT_FULL);  card.repaint();
                }
            });
        }

        return card;
    }

    // ── Minimal styled button helper ──────────────────────────────────────────
    private static JButton styledButton(String text, Color fg, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFont(new Font("Munro", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}