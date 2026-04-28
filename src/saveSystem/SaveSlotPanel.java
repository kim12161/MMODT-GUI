package saveSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * SaveSlotPanel — full-screen overlay showing 3 save slots.
 * Updated to use the custom 'save-slots-panel.png' and match ContinuePanel's aesthetics.
 */
public class SaveSlotPanel extends JPanel {

    public enum Mode { SAVE, LOAD }

    public interface SlotSelectedListener {
        /** Called when the user clicks a slot. slotIndex is 1-based. */
        void onSlotSelected(int slotIndex, Mode mode);
    }

    // ── Palette (Matching the new ContinuePanel look) ────────────────────────
    private static final Color BG_OVERLAY  = new Color(0, 0, 0, 180); // Dim the game behind it
    private static final Color SLOT_EMPTY  = new Color(54, 49, 44, 120);
    private static final Color SLOT_FULL   = new Color(64, 59, 54);
    private static final Color SLOT_HOVER  = new Color(54, 49, 44);
    private static final Color TEXT_MAIN   = Color.WHITE;
    private static final Color TEXT_DIM    = new Color(220, 220, 220);

    private String bFont = "Munro";

    private final Mode mode;
    private final SaveSystem.SaveData[] slots;
    private final SlotSelectedListener listener;

    // ── Images ────────────────────────────────────────────────────────────────
    private Image panelImg;
    private Image closeActive, closeHover, closeDefault;

    {
        try {
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

    public SaveSlotPanel(Mode mode,
                         SaveSystem.SaveData[] slots,
                         SlotSelectedListener listener) {
        this.mode     = mode;
        this.slots    = slots;
        this.listener = listener;

        setLayout(null);
        setOpaque(false);
        setCursor(Cursor.getDefaultCursor());

        buildUI();

        // Close if the player clicks outside the popup box
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dismiss(); }
        });
    }

    // ── paint dim background ────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(BG_OVERLAY);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // ── build ───────────────────────────────────────────────────────────────
    private void buildUI() {
        int panelW = 560;
        int panelH = 420;

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
        centerContainer.setOpaque(false);

        // Prevent clicks on the panel from triggering the "close" background click
        centerContainer.addMouseListener(new MouseAdapter() {});

        // ── Header Title ─────────────────────────────────────────────────────
        String titleText = mode == Mode.SAVE ? "Save Game" : "Load Game";
        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font(bFont, Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 10, panelW, 70);
        centerContainer.add(title);

        // ── Custom "X" Close Button ──────────────────────────────────────────
        JButton closeBtn = new JButton() {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font(bFont, Font.BOLD, 18));

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

                if (currentSprite != null) {
                    g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                }
                g2.dispose();

                if (isPressed) g.translate(-3, 3);

                g.setColor(Color.WHITE);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int textX = ((getWidth() - fm.stringWidth("X")) / 2) + 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString("X", textX, textY);

                if (isPressed) g.translate(3, -3);
            }
        };
        closeBtn.setBounds(panelW - 70, 19, 44, 44);
        closeBtn.addActionListener(e -> dismiss());
        centerContainer.add(closeBtn);

        // ── Slot cards ────────────────────────────────────────────────────────
        int startY = 100;
        int slotH = 90;
        int gap = 13;

        for (int i = 0; i < SaveSystem.MAX_SLOTS; i++) {
            JPanel slotCard = buildSlotPanel(i + 1, slots[i]);
            slotCard.setBounds(30, startY + (i * (slotH + gap)), panelW - 60, slotH);
            centerContainer.add(slotCard);
        }

        add(centerContainer);

        // ── Auto-center when panel resizes ───────────────────────────────────
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int cx = (getWidth()  - panelW) / 2;
                int cy = (getHeight() - panelH) / 2;
                centerContainer.setBounds(cx, cy, panelW, panelH);
            }
        });
    }

    // ── build individual slot panel ─────────────────────────────────────────
    private JPanel buildSlotPanel(int slotIndex, SaveSystem.SaveData data) {
        boolean hasData = (data != null);

        JPanel p = new JPanel(new BorderLayout(16, 0)) {
            private boolean hovered = false;

            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);
                setBackground(hasData ? SLOT_FULL : SLOT_EMPTY);
                setBorder(new EmptyBorder(15, 40, 15, 40));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; setBackground(SLOT_HOVER); repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; setBackground(hasData ? SLOT_FULL : SLOT_EMPTY); repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (listener != null) listener.onSlotSelected(slotIndex, mode);
                        dismiss();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };

        // Slot number badge
        JLabel badge = new JLabel("SLOT " + slotIndex);
        badge.setFont(new Font(bFont, Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setPreferredSize(new Dimension(60, 0));
        badge.setVerticalAlignment(SwingConstants.CENTER);
        p.add(badge, BorderLayout.WEST);

        // Slot info (centre)
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        if (hasData) {
            JLabel nameLvl = new JLabel(data.playerName + "  ·  Level " + data.currentLevel + " — " + data.levelName);
            nameLvl.setFont(new Font(bFont, Font.BOLD, 18));
            nameLvl.setForeground(TEXT_MAIN);

            JLabel ts = new JLabel(data.timestamp);
            ts.setFont(new Font(bFont, Font.PLAIN, 13));
            ts.setForeground(TEXT_DIM);

            // 🛠️ RESTORED THE "ITEMS" LOGIC HERE
            JLabel stats = new JLabel("HP: " + data.playerHealth + "/100"
                    + "    Charisma: " + data.playerCharisma
                    + "    Items: " + (data.consumableInventory != null ? data.consumableInventory.values().stream().mapToInt(Integer::intValue).sum() : 0));
            stats.setFont(new Font(bFont, Font.PLAIN, 14));
            stats.setForeground(TEXT_DIM);

            info.add(nameLvl);
            info.add(Box.createVerticalStrut(4));
            info.add(ts);
            info.add(Box.createVerticalStrut(4));
            info.add(stats);
        } else {
            JLabel emptyLbl = new JLabel("— Empty Slot —");
            emptyLbl.setFont(new Font(bFont, Font.ITALIC, 18));
            emptyLbl.setForeground(TEXT_DIM);
            info.add(Box.createVerticalGlue());
            info.add(emptyLbl);
            info.add(Box.createVerticalGlue());
        }

        p.add(info, BorderLayout.CENTER);

        return p;
    }

    // ── remove self from parent ─────────────────────────────────────────────
    private void dismiss() {
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.repaint();
        }
    }
}