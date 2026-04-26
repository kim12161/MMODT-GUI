package menu;

import main.GamePanel;
import saveSystem.SaveSystem;
import saveSystem.SaveSystem.SaveData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ContinuePanel — shown when the player clicks "Continue" on the title screen.
 * Displays all 3 save slots; clicking a populated slot fires the provided
 * OnSlotSelected callback. "Back" returns to the main menu.
 *
 * Usage in MenuButtonHandler (Continue branch):
 *
 *   ContinuePanel panel = new ContinuePanel(gamePanel, (data) -> {
 *       // restore player from data, then start the game at data.currentLevel
 *   });
 *   gamePanel.removeAll();
 *   gamePanel.setLayout(new BorderLayout());
 *   gamePanel.add(panel, BorderLayout.CENTER);
 *   gamePanel.revalidate();
 *   gamePanel.repaint();
 */
public class ContinuePanel extends JPanel {

    // ── Callback fired when the player picks a slot ──────────────────────────
    @FunctionalInterface
    public interface OnSlotSelected {
        void onSelected(SaveData data);
    }

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG         = new Color(18,  18,  28);
    private static final Color SLOT_EMPTY = new Color(30,  30,  45);
    private static final Color SLOT_FULL  = new Color(28,  38,  60);
    private static final Color SLOT_HOVER = new Color(40,  55,  90);
    private static final Color ACCENT     = new Color(100, 160, 255);
    private static final Color TEXT_MAIN  = new Color(220, 225, 240);
    private static final Color TEXT_DIM   = new Color(110, 115, 140);
    private static final Color DELETE_CLR = new Color(200,  70,  70);
    private static final Color BORDER_CLR = new Color(50,  60,  90);

    public ContinuePanel(GamePanel gamePanel, OnSlotSelected callback) {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(50, 80, 50, 80));

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("CONTINUE", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 38));
        title.setForeground(ACCENT);
        title.setBorder(new EmptyBorder(0, 0, 36, 0));
        add(title, BorderLayout.NORTH);

        // ── Slot cards ────────────────────────────────────────────────────────
        SaveData[] slots = SaveSystem.loadAllSlots();   // index 0 = slot 1
        JPanel slotContainer = new JPanel();
        slotContainer.setLayout(new BoxLayout(slotContainer, BoxLayout.Y_AXIS));
        slotContainer.setOpaque(false);

        for (int i = 0; i < SaveSystem.MAX_SLOTS; i++) {
            slotContainer.add(buildSlotCard(i + 1, slots[i], gamePanel, callback));
            if (i < SaveSystem.MAX_SLOTS - 1) slotContainer.add(Box.createVerticalStrut(16));
        }

        add(slotContainer, BorderLayout.CENTER);

        // ── Back button ───────────────────────────────────────────────────────
        JButton backBtn = styledButton("← Back", TEXT_DIM, SLOT_EMPTY);
        backBtn.addActionListener(e -> {
            gamePanel.removeAll();
            gamePanel.setLayout(null); // TitleScreen uses null layout
            new TitleScreen(gamePanel, gamePanel);
            gamePanel.revalidate();
            gamePanel.repaint();
        });

        JPanel southRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        southRow.setOpaque(false);
        southRow.setBorder(new EmptyBorder(28, 0, 0, 0));
        southRow.add(backBtn);
        add(southRow, BorderLayout.SOUTH);
    }

    // ── Build one save-slot card ──────────────────────────────────────────────
    private JPanel buildSlotCard(int slotNumber, SaveData data,
                                 GamePanel gamePanel, OnSlotSelected callback) {

        boolean hasData = (data != null);

        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(hasData ? SLOT_FULL : SLOT_EMPTY);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        // Slot number badge
        JLabel badge = new JLabel("SLOT " + slotNumber);
        badge.setFont(new Font("Monospaced", Font.BOLD, 11));
        badge.setForeground(ACCENT);
        badge.setPreferredSize(new Dimension(52, 0));
        badge.setVerticalAlignment(SwingConstants.CENTER);
        card.add(badge, BorderLayout.WEST);

        // Slot info (centre)
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        if (hasData) {
            JLabel nameLvl = new JLabel(data.playerName + "  ·  Level " + data.currentLevel
                    + " — " + data.levelName);
            nameLvl.setFont(new Font("Serif", Font.BOLD, 16));
            nameLvl.setForeground(TEXT_MAIN);

            JLabel ts = new JLabel(data.timestamp);
            ts.setFont(new Font("SansSerif", Font.PLAIN, 12));
            ts.setForeground(TEXT_DIM);

            JLabel stats = new JLabel("HP " + data.playerHealth
                    + "   Charisma " + data.playerCharisma);
            stats.setFont(new Font("Monospaced", Font.PLAIN, 11));
            stats.setForeground(TEXT_DIM);

            info.add(nameLvl);
            info.add(Box.createVerticalStrut(4));
            info.add(ts);
            info.add(Box.createVerticalStrut(2));
            info.add(stats);
        } else {
            JLabel empty = new JLabel("— Empty Slot —");
            empty.setFont(new Font("Serif", Font.ITALIC, 15));
            empty.setForeground(TEXT_DIM);
            info.add(Box.createVerticalGlue());
            info.add(empty);
            info.add(Box.createVerticalGlue());
        }

        card.add(info, BorderLayout.CENTER);

        // Right-side buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        if (hasData) {
            final SaveData captured = data;

            JButton loadBtn = styledButton("Load", TEXT_MAIN, ACCENT.darker().darker());
            loadBtn.setForeground(ACCENT);
            loadBtn.addActionListener(e -> callback.onSelected(captured));
            btnPanel.add(loadBtn);

            JButton delBtn = styledButton("✕", DELETE_CLR, SLOT_EMPTY);
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
                    // Rebuild the panel to reflect deletion
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
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
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}