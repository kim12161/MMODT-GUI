package saveSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * SaveSlotPanel — full-screen overlay showing 3 save slots.
 * Matches the game's dark pixel-art aesthetic (Munro/PixelArmy fonts).
 *
 * Usage:
 *   SaveSlotPanel overlay = new SaveSlotPanel(mode, slots, listener);
 *   overlay.setBounds(0, 0, parentWidth, parentHeight);
 *   parent.add(overlay);
 *   parent.setComponentZOrder(overlay, 0);
 *   parent.repaint();
 */
public class SaveSlotPanel extends JPanel {

    public enum Mode { SAVE, LOAD }

    public interface SlotSelectedListener {
        /** Called when the user clicks a slot. slotIndex is 1-based. */
        void onSlotSelected(int slotIndex, Mode mode);
    }

    // ── fonts ──────────────────────────────────────────────────────────────
    private static final String FONT_B = "Munro";
    private static final String FONT_T = "PixelArmy";

    // ── colours ────────────────────────────────────────────────────────────
    private static final Color BG_OVERLAY  = new Color(0, 0, 0, 200);
    private static final Color SLOT_BG     = new Color(18, 14, 22, 245);
    private static final Color SLOT_BORDER = new Color(160, 40,  40, 200);
    private static final Color SLOT_HOVER  = new Color(200, 50,  50, 100);
    private static final Color ACCENT_RED  = new Color(220, 55,  55);
    private static final Color TEXT_WHITE  = new Color(240, 235, 228);
    private static final Color TEXT_DIM    = new Color(140, 130, 120);
    private static final Color TEXT_GOLD   = new Color(230, 185, 60);
    private static final Color EMPTY_TEXT  = new Color(90,  80,  75);

    // ── layout ─────────────────────────────────────────────────────────────
    private static final int SLOT_W   = 520;
    private static final int SLOT_H   = 110;
    private static final int SLOT_GAP = 18;

    private final Mode mode;
    private final SaveSystem.SaveData[] slots;   // null = empty slot
    private final SlotSelectedListener listener;

    private final JPanel[] slotPanels = new JPanel[SaveSystem.MAX_SLOTS];

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
        int totalH = SaveSystem.MAX_SLOTS * SLOT_H + (SaveSystem.MAX_SLOTS - 1) * SLOT_GAP;
        int panelW = SLOT_W + 60;   // card container width
        int panelH = totalH + 100;  // title + slots

        // ── card container (centred at runtime via componentResized) ───────
        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 8, 14, 240));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(SLOT_BORDER);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBounds(0, 0, panelW, panelH);

        // ── title ──────────────────────────────────────────────────────────
        String titleText = mode == Mode.SAVE ? "— SAVE GAME —" : "— LOAD GAME —";
        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font(FONT_B, Font.BOLD, 22));
        title.setForeground(ACCENT_RED);
        title.setBounds(0, 14, panelW, 30);
        card.add(title);

        // ── slots ──────────────────────────────────────────────────────────
        int startY = 55;
        for (int i = 0; i < SaveSystem.MAX_SLOTS; i++) {
            final int slotIdx = i + 1;
            JPanel slot = buildSlotPanel(slotIdx, slots[i], panelW);
            slot.setBounds(30, startY + i * (SLOT_H + SLOT_GAP), SLOT_W, SLOT_H);
            card.add(slot);
            slotPanels[i] = slot;
        }

        // ── cancel button ──────────────────────────────────────────────────
        JLabel cancel = new JLabel("✕  CANCEL", SwingConstants.CENTER);
        cancel.setFont(new Font(FONT_B, Font.PLAIN, 14));
        cancel.setForeground(TEXT_DIM);
        cancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancel.setBounds(panelW / 2 - 70, panelH - 32, 140, 24);
        cancel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dismiss(); }
            @Override public void mouseEntered(MouseEvent e) { cancel.setForeground(TEXT_WHITE); }
            @Override public void mouseExited(MouseEvent e)  { cancel.setForeground(TEXT_DIM); }
        });
        card.add(cancel);

        add(card);

        // ── centre card when this panel is laid out ───────────────────────
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int cx = (getWidth()  - panelW) / 2;
                int cy = (getHeight() - panelH) / 2;
                card.setBounds(cx, cy, panelW, panelH);
            }
        });

        // ── click outside to cancel ────────────────────────────────────────
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // only dismiss if click is outside card bounds
                if (!card.getBounds().contains(e.getPoint())) dismiss();
            }
        });
    }

    // ── build individual slot panel ─────────────────────────────────────────
    private JPanel buildSlotPanel(int slotIndex, SaveSystem.SaveData data, int parentW) {
        boolean empty = (data == null);

        JPanel p = new JPanel(null) {
            private boolean hovered = false;

            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
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

                // bg
                g2.setColor(hovered ? SLOT_HOVER : SLOT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // border
                g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
                g2.setColor(hovered ? ACCENT_RED : SLOT_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        // slot number badge
        JLabel badge = new JLabel("SLOT " + slotIndex, SwingConstants.CENTER);
        badge.setFont(new Font(FONT_B, Font.BOLD, 13));
        badge.setForeground(ACCENT_RED);
        badge.setBounds(10, 8, 80, 18);
        p.add(badge);

        if (empty) {
            JLabel emptyLbl = new JLabel("— EMPTY —", SwingConstants.CENTER);
            emptyLbl.setFont(new Font(FONT_B, Font.PLAIN, 17));
            emptyLbl.setForeground(EMPTY_TEXT);
            emptyLbl.setBounds(0, 0, SLOT_W, SLOT_H);
            p.add(emptyLbl);
        } else {
            // player name
            JLabel name = new JLabel(data.playerName.toUpperCase());
            name.setFont(new Font(FONT_B, Font.BOLD, 18));
            name.setForeground(TEXT_WHITE);
            name.setBounds(100, 8, SLOT_W - 120, 22);
            p.add(name);

            // level info
            JLabel level = new JLabel("Level " + data.currentLevel + "  ·  " + data.levelName);
            level.setFont(new Font(FONT_B, Font.PLAIN, 14));
            level.setForeground(TEXT_GOLD);
            level.setBounds(14, 36, SLOT_W - 20, 20);
            p.add(level);

            // HP / charisma
            JLabel stats = new JLabel("HP: " + data.playerHealth + "/" + data.playerMaxHealth
                    + "    Charisma: " + data.playerCharisma
                    + "    Items: " + (data.consumables != null ? data.consumables.size() : 0));
            stats.setFont(new Font(FONT_B, Font.PLAIN, 12));
            stats.setForeground(TEXT_DIM);
            stats.setBounds(14, 58, SLOT_W - 20, 18);
            p.add(stats);

            // timestamp
            JLabel time = new JLabel(data.timestamp, SwingConstants.RIGHT);
            time.setFont(new Font(FONT_B, Font.PLAIN, 11));
            time.setForeground(TEXT_DIM);
            time.setBounds(0, 82, SLOT_W - 12, 18);
            p.add(time);
        }

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