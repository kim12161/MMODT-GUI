package saveSystem;

import Characters.Character;
import Player.Player;
import menu.TitleScreen;
import main.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.border.EmptyBorder;

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
    private static final int BTN_H  = 42;
    private static final int DROP_W = 230; // 🛠️ Made wider to fit the big image buttons
    private static final int DROP_H = 217; // 🛠️ Made taller to fit the header and buttons
    private static final int ITEM_H = 70;  // Height of the green/brown buttons

    private boolean open = false;

    private Player          player;
    private List<Character> characters;
    private int             currentLevel        = 1;
    private int             currentConversation = 1;  // ← NEW
    private String          currentLevelName    = "Abandoned Compound";

    private final JPanel    sceneRoot;
    private       GamePanel gamePanel;

    private JPanel dropdownPanel;

    // 🛠️ NEW: Image variable for your custom panel
    private Image panelImg;

    // Load the image right when the class is created
    {
        try {
            java.io.File fPanel = new java.io.File("res/ui/panels/save-exit-panel.png");
            if (fPanel.exists()) panelImg = new ImageIcon(fPanel.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}
    }

    // ── constructor ───────────────────────────────────────────────────────
    public GameMenu(JPanel sceneRoot) {
        this.sceneRoot = sceneRoot;
        setLayout(null);
        setOpaque(false);
        buildHeader();
        buildDropdown();
        setDropdownVisible(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 🛠️ Draws the 40% black background ONLY when the dropdown is open
        if (open) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 102));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // ── centred-top bounds ────────────────────────────────────────────────
    public static Rectangle defaultBounds(int panelWidth, int panelHeight) {
        return new Rectangle(0, 0, panelWidth, panelHeight);
    }
    @Override
    public boolean contains(int x, int y) {
        if (open) return super.contains(x, y); // Blocks clicks (shows background) when open

        // If closed, ONLY the small menu button is clickable
        int cx = (getWidth() - DROP_W) / 2;
        Rectangle headerBounds = new Rectangle(cx, 6, DROP_W, BTN_H);
        return headerBounds.contains(x, y);
    }

    // ── header pill ───────────────────────────────────────────────────────
    private void buildHeader() {
        // 1. Load the 3 images for the Menu button
        Image defImg = null, hovImg = null, actImg = null;
        try {
            // 🛠️ CHANGED: Now pointing to your new menu-button folder!
            java.io.File f1 = new java.io.File("res/ui/icon/menu-button/button-menu-not-active.png");
            java.io.File f2 = new java.io.File("res/ui/icon/menu-button/button-menu-hover.png");
            java.io.File f3 = new java.io.File("res/ui/icon/menu-button/button-menu-active.png");

            if (f1.exists()) defImg = new ImageIcon(f1.getAbsolutePath()).getImage();
            if (f2.exists()) hovImg = new ImageIcon(f2.getAbsolutePath()).getImage();
            if (f3.exists()) actImg = new ImageIcon(f3.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        final Image defaultImg = defImg;
        final Image hoverImg = hovImg;
        final Image activeImg = actImg;

        // 2. Build it as a JButton
        JButton header = new JButton() {
            private boolean hovered = false;

            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);

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

                // Logic for choosing which sprite to show
                if (isPressed) {
                    currentSprite = activeImg != null ? activeImg : defaultImg;
                } else if (hovered) {
                    currentSprite = hoverImg != null ? hoverImg : defaultImg;
                } else {
                    currentSprite = defaultImg;
                }

                if (currentSprite != null) {
                    // 🛠️ CHANGED: Made the icon a perfect 42x42 square so it isn't squished!
                    int iconW = 42;
                    int iconH = 42;
                    int x = (getWidth() - iconW) / 2;
                    int y = (getHeight() - iconH) / 2;

                    // Push down logic when clicked!
                    if (isPressed) g2.translate(-3, 3);
                    g2.drawImage(currentSprite, x, y, iconW, iconH, this);
                    if (isPressed) g2.translate(3, -3);
                } else {
                    // Fallback just in case images are missing
                    g2.setColor(new Color(20, 15, 20, 220));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        // 3. Trigger the toggle() method when clicked
        header.addActionListener(e -> toggle());
        // 🛠️ Auto-centers the header button at the top of the screen
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int cx = (getWidth() - DROP_W) / 2;
                header.setBounds(cx, 6, DROP_W, BTN_H);
            }
        });

        add(header);
    }

    // ── dropdown ──────────────────────────────────────────────────────────
    private void buildDropdown() {
        dropdownPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                // 🛠️ DRAW YOUR CUSTOM IMAGE INSTEAD OF CODE BOXES
                if (panelImg != null) {
                    g2.drawImage(panelImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback just in case the image fails to load
                    // 1. Draw the lighter grey/brown Body
                    g2.setColor(new Color(102, 95, 87));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // 2. Draw the dark brown Header
                    g2.setColor(new Color(59, 53, 49));
                    g2.fillRect(4, 4, getWidth() - 8, 36);

                    // 3. Draw Outer Dark Border
                    g2.setColor(new Color(60, 60, 60));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);

                    // 4. Draw Inner Light Frame
                    g2.setColor(new Color(180, 180, 180));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(4, 4, getWidth() - 8, getHeight() - 8);

                    // Header separator line
                    g2.drawLine(4, 40, getWidth() - 4, 40);
                }

                // 5. Draw "Menu" text in Header
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(FONT, Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth("Menu")) / 2;
                g2.drawString("Menu", textX, 33);

                g2.dispose();
            }
        };
        dropdownPanel.setOpaque(false);
        // 🛠️ Auto-centers the dropdown right below the header button
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int cx = (getWidth() - DROP_W) / 2;
                dropdownPanel.setBounds(cx, BTN_H + 12, DROP_W, DROP_H);
            }
        });

        // ── Adding the Custom Buttons ──────────────────────────────────────
        // 0 = Save, 1 = Exit
        JButton saveBtn = makeImageButton("Save", "res/ui/icon/normal-buttons/button-green", 0);
        JButton exitBtn = makeImageButton("Exit", "res/ui/icon/normal-buttons/button-2-normal", 1);

        int startY = 59; // Start below the header
        int gap = -1;
        int btnW = DROP_W - 40;

        saveBtn.setBounds(15, startY, btnW, ITEM_H);
        exitBtn.setBounds(15, startY + ITEM_H + gap, btnW, ITEM_H);

        dropdownPanel.add(saveBtn);
        dropdownPanel.add(exitBtn);

        add(dropdownPanel);
    }

    // ── your custom button builder ────────────────────────────────────────
    private JButton makeImageButton(String text, String basePath, int actionIdx) {
        Image defImg = null, hovImg = null, actImg = null;
        try {
            java.io.File f1 = new java.io.File(basePath + "-not-active.png");
            java.io.File f2 = new java.io.File(basePath + "-hover.png");
            java.io.File f3 = new java.io.File(basePath + "-active.png");
            if (f1.exists()) defImg = new ImageIcon(f1.getAbsolutePath()).getImage();
            if (f2.exists()) hovImg = new ImageIcon(f2.getAbsolutePath()).getImage();
            if (f3.exists()) actImg = new ImageIcon(f3.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        final Image defaultImg = defImg;
        final Image hoverImg = hovImg;
        final Image activeImg = actImg;

        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                setFont(new Font(FONT, Font.BOLD, 18));
                setForeground(Color.WHITE);

                setHorizontalTextPosition(JButton.CENTER);
                setVerticalTextPosition(JButton.CENTER);

                setBorder(new EmptyBorder(7, 15, 0, 0));

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
                    currentSprite = activeImg;
                } else if (hovered) {
                    currentSprite = hoverImg;
                } else {
                    currentSprite = defaultImg;
                }

                // 1. Draw the button image first
                if (currentSprite != null) {
                    g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();

                // 2. PUSH THE TEXT DOWN IF PRESSED
                if (isPressed) {
                    g.translate(-3, 3);
                }

                // 3. Draw the text on top
                super.paintComponent(g);

                // 4. Reset the position accurately (3, -3 is the exact opposite of -3, 3)
                if (isPressed) {
                    g.translate(3, -3);
                }
            }
        };

        btn.addActionListener(e -> handleItem(actionIdx));
        return btn;
    }

    // ── toggle ────────────────────────────────────────────────────────────
    private void toggle() {
        open = !open;
        setDropdownVisible(open);

        // 🛠️ Forces the menu to the absolute front over the zombie!
        if (open && getParent() != null) {
            getParent().setComponentZOrder(this, 0);
        }
        repaint(); // Force the dark background to draw
    }
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