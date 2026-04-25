package saveSystem;

import Characters.Character;
import Player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * GameMenu — centred at the very top of ScenePanel, between the level
 * label (left) and the conversation counter (right).
 *
 * Dropdown order: Continue → Save → Exit
 *
 * Usage in ScenePanel (add import saveSystem.GameMenu):
 *   menuButton = new GameMenu(backgroundLayer);
 *   menuButton.setPlayer(player);
 *   menuButton.setCharacters(characters);
 *   menuButton.setCurrentLevel(1);
 *   menuButton.setCurrentLevelName(LEVEL_NAMES[0]);
 *   Rectangle b = GameMenu.defaultBounds(900);
 *   menuButton.setBounds(b);
 *   backgroundLayer.add(menuButton);
 *   backgroundLayer.setComponentZOrder(menuButton, 0);
 */
public class GameMenu extends JPanel {

    // colours
    private static final Color BG_CLOSED = new Color(20, 15, 20, 220);
    private static final Color BG_OPEN   = new Color(20, 15, 20, 240);
    private static final Color BORDER    = new Color(160, 40, 40, 210);
    private static final Color ACCENT    = new Color(220, 55, 55);
    private static final Color TXT_WHITE = new Color(240, 235, 228);
    private static final Color HOVER_BG  = new Color(200, 50, 50, 80);
    private static final String FONT     = "Munro";

    // dimensions
    private static final int BTN_H  = 32;
    private static final int DROP_W = 140;
    private static final int DROP_H = 115;
    private static final int ITEM_H = 30;

    private boolean open = false;

    private Player     player;
    private List<Character> characters;
    private int    currentLevel     = 1;
    private String currentLevelName = "Abandoned Compound";

    private final JPanel sceneRoot;
    private JPanel dropdownPanel;

    // ── constructor ───────────────────────────────────────────────────────
    public GameMenu(JPanel sceneRoot) {
        this.sceneRoot = sceneRoot;
        setLayout(null);
        setOpaque(false);
        buildHeader();
        buildDropdown();
        setDropdownVisible(false);
    }

    // ── centred-top positioning ───────────────────────────────────────────
    /**
     * Returns bounds that centre the MENU button horizontally at the top
     * of the panel — between the level label (left) and the conversation
     * counter (right).
     *
     * @param panelWidth width of the parent panel (typically 900)
     */
    public static Rectangle defaultBounds(int panelWidth) {
        int x = (panelWidth - DROP_W) / 2;  // horizontal centre
        int y = 4;                            // top margin
        return new Rectangle(x, y, DROP_W, BTN_H + DROP_H + 4);
    }

    // ── header pill ───────────────────────────────────────────────────────
    private void buildHeader() {
        JPanel header = new JPanel(null) {
            private boolean hov = false;
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);
                addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { toggle(); }
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(40, 25, 30, 230) : BG_CLOSED);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setBounds(0, 0, DROP_W, BTN_H);

        // three hamburger bars
        JPanel bars = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ACCENT);
                for (int i = 0; i < 3; i++)
                    g2.fillRoundRect(0, i * 6, 14, 2, 2, 2);
                g2.dispose();
            }
        };
        bars.setOpaque(false);
        bars.setBounds(10, (BTN_H - 14) / 2, 16, 14);
        header.add(bars);

        JLabel lbl = new JLabel("MENU", SwingConstants.LEFT);
        lbl.setFont(new Font(FONT, Font.BOLD, 14));
        lbl.setForeground(TXT_WHITE);
        lbl.setBounds(32, 0, 90, BTN_H);
        header.add(lbl);

        add(header);
    }

    // ── dropdown ──────────────────────────────────────────────────────────
    private void buildDropdown() {
        dropdownPanel = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_OPEN);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        dropdownPanel.setOpaque(false);
        dropdownPanel.setBounds(0, BTN_H + 2, DROP_W, DROP_H);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(100, 30, 30, 160));
        sep.setBounds(10, 2, DROP_W - 20, 2);
        dropdownPanel.add(sep);

        String[] labels = { "Continue", "Save", "Exit" };
        Color[]  colors = { TXT_WHITE, TXT_WHITE, new Color(220, 80, 80) };
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JPanel item = buildItem(labels[i], colors[i], idx);
            item.setBounds(0, 4 + i * ITEM_H, DROP_W, ITEM_H);
            dropdownPanel.add(item);
        }

        add(dropdownPanel);
    }

    private JPanel buildItem(String text, Color fg, int idx) {
        JPanel item = new JPanel(null) {
            private boolean hov = false;
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);
                addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { handleItem(idx); }
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            protected void paintComponent(Graphics g) {
                if (hov) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(HOVER_BG);
                    g2.fillRoundRect(4, 2, getWidth()-8, getHeight()-4, 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font(FONT, Font.PLAIN, 15));
        lbl.setForeground(fg);
        lbl.setBounds(0, 0, DROP_W, ITEM_H);
        item.add(lbl);
        return item;
    }

    // ── logic ─────────────────────────────────────────────────────────────
    private void toggle() { open = !open; setDropdownVisible(open); }

    private void setDropdownVisible(boolean v) {
        dropdownPanel.setVisible(v);
        repaint();
    }

    private void handleItem(int idx) {
        setDropdownVisible(false);
        open = false;
        switch (idx) {
            case 0 -> { /* Continue — just close */ }
            case 1 -> openSaveOverlay();
            case 2 -> {
                int r = JOptionPane.showConfirmDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "Exit the game? Unsaved progress will be lost.",
                        "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r == JOptionPane.YES_OPTION) System.exit(0);
            }
        }
    }

    private void openSaveOverlay() {
        if (sceneRoot == null) return;
        SaveSystem.SaveData[] slots = SaveSystem.loadAllSlots();
        SaveSlotPanel overlay = new SaveSlotPanel(
                SaveSlotPanel.Mode.SAVE, slots,
                (slotIndex, mode) -> {
                    if (player != null && characters != null) {
                        boolean ok = SaveSystem.save(slotIndex, player, characters,
                                currentLevel, currentLevelName);
                        JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(GameMenu.this),
                                ok ? "Game saved to Slot " + slotIndex + "!" : "Save failed.",
                                ok ? "Saved" : "Error",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    }
                });
        overlay.setBounds(0, 0, sceneRoot.getWidth(), sceneRoot.getHeight());
        sceneRoot.add(overlay);
        sceneRoot.setComponentZOrder(overlay, 0);
        sceneRoot.repaint();
    }

    // ── setters ───────────────────────────────────────────────────────────
    public void setPlayer(Player p)          { this.player = p; }
    public void setCharacters(List<Character> c) { this.characters = c; }
    public void setCurrentLevel(int level)       { this.currentLevel = level; }
    public void setCurrentLevelName(String name) { this.currentLevelName = name; }
}