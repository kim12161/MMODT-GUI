package saveSystem;

import Characters.Character;
import Player.Player;
import game.ScenePanel;
import menu.TitleScreen;
import main.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * GameMenu — centred at the very top of ScenePanel.
 * Dropdown: Save → Exit (returns to title screen)
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
    private static final int DROP_W = 160; // 🛠️  (Makes it wider)
    private static final int DROP_H = 100; // 🛠️  (Makes it taller)
    private static final int ITEM_H = 40;

    private boolean open = false;

    private Player          player;
    private List<Character> characters;
    private int             currentLevel        = 1;
    private int             currentConversation = 1;  // ← NEW
    private String          currentLevelName    = "Abandoned Compound";

    private final JPanel    sceneRoot;
    private       GamePanel gamePanel;

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

    // ── centred-top bounds ────────────────────────────────────────────────
    public static Rectangle defaultBounds(int panelWidth) {
        int x = (panelWidth - DROP_W) / 2;
        int y = 4;
        return new Rectangle(x, y, DROP_W, BTN_H + DROP_H + 4);
    }

    // ── header pill ───────────────────────────────────────────────────────
    // ── header pill ───────────────────────────────────────────────────────
    private void buildHeader() {
        JPanel header = new JPanel(null) {
            private boolean hov = false;
            private Image menuImg;

            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);

                // ✅ Load the white hamburger icon (active only, as requested)
                try {
                    java.io.File fMenu = new java.io.File("res/ui/icon/small-buttons/active.png");
                    if (fMenu.exists()) {
                        menuImg = new ImageIcon(fMenu.getAbsolutePath()).getImage();
                    }
                } catch (Exception ignored) {}

                addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { toggle(); }
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (menuImg != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    // ✅ Draw the image perfectly centered inside the clickable area
                    int iconW = 36; // Width to match your asset
                    int iconH = 32; // Fits perfectly inside your BTN_H constant
                    int x = (getWidth() - iconW) / 2;
                    int y = (getHeight() - iconH) / 2;

                    g2.drawImage(menuImg, x, y, iconW, iconH, this);
                    g2.dispose();
                } else {
                    // Fallback just in case the image goes missing
                    g.setColor(new Color(20, 15, 20, 220));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        // Keeps your exact original bounds so it doesn't break the layout!
        header.setBounds(0, 0, DROP_W, BTN_H);

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

        String[] labels = { "Save",    "Exit"                  };
        Color[]  colors = { TXT_WHITE, new Color(220, 80, 80)  };

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

    // ── toggle ────────────────────────────────────────────────────────────
    private void toggle() { open = !open; setDropdownVisible(open); }

    private void setDropdownVisible(boolean v) {
        dropdownPanel.setVisible(v);
        repaint();
    }

    // ── item actions ──────────────────────────────────────────────────────
    private void handleItem(int idx) {
        setDropdownVisible(false);
        open = false;
        switch (idx) {
            case 0 -> openSavePanel();
            case 1 -> confirmReturnToTitle();
        }
    }

    // ── SAVE ──────────────────────────────────────────────────────────────
    private void openSavePanel() {
        if (sceneRoot == null) return;
        SaveSystem.SaveData[] slots = SaveSystem.loadAllSlots();

        SaveSlotPanel panel = new SaveSlotPanel(
                SaveSlotPanel.Mode.SAVE, slots,
                (slotIndex, mode) -> {
                    if (player == null || characters == null) return;

                    if (SaveSystem.slotExists(slotIndex)) {
                        SaveSystem.SaveData existing = SaveSystem.load(slotIndex);
                        String existingName = (existing != null) ? existing.playerName : "Unknown";
                        String existingLvl  = (existing != null) ? "Level " + existing.currentLevel : "";

                        int confirm = JOptionPane.showConfirmDialog(
                                SwingUtilities.getWindowAncestor(GameMenu.this),
                                "<html><b>Slot " + slotIndex + "</b> already has a save:<br>" +
                                        "<i>" + existingName + " — " + existingLvl + "</i><br><br>" +
                                        "Do you want to overwrite it with your current progress?</html>",
                                "Overwrite Save?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (confirm != JOptionPane.YES_OPTION) return;
                    }

                    // ── pass currentConversation to save ──────────────────
                    boolean ok = SaveSystem.save(slotIndex, player, characters,
                            currentLevel, currentConversation, currentLevelName);

                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(GameMenu.this),
                            ok ? "Progress saved to Slot " + slotIndex + "!"
                                    : "Save failed. Please try again.",
                            ok ? "Game Saved" : "Error",
                            ok ? JOptionPane.INFORMATION_MESSAGE
                                    : JOptionPane.ERROR_MESSAGE);
                });

        panel.setBounds(0, 0, sceneRoot.getWidth(), sceneRoot.getHeight());
        sceneRoot.add(panel);
        sceneRoot.setComponentZOrder(panel, 0);
        sceneRoot.repaint();
    }

    // ── EXIT ──────────────────────────────────────────────────────────────
    private void confirmReturnToTitle() {
        int confirm = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "<html>Leaving so soon?<br>" +
                        "<i>Unsaved progress will be lost.</i><br><br>" +
                        "Return to the Title Screen?</html>",
                "Return to Title",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame == null) return;

            frame.getContentPane().removeAll();
            new TitleScreen(frame.getContentPane(), gamePanel);
            frame.revalidate();
            frame.repaint();
        });
    }

    //helper
    public void setMenuVisible(boolean visible) {
        this.setVisible(visible);
        if (!visible) {
            this.open = false; // Force close dropdown if we hide it
            setDropdownVisible(false);
        }
    }

    // ── setters ───────────────────────────────────────────────────────────
    public void setPlayer(Player p)                  { this.player = p; }
    public void setCharacters(List<Character> c)     { this.characters = c; }
    public void setCurrentLevel(int level)           { this.currentLevel = level; }
    public void setCurrentLevelName(String name)     { this.currentLevelName = name; }
    public void setCurrentConversation(int convo)    { this.currentConversation = convo; } // ← NEW
    public void setGamePanel(GamePanel gp)           { this.gamePanel = gp; }
}