package Interaction;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ChoiceButtonLayer extends JPanel {

    private List<ChoiceButton> choiceButtons;
    private ChoiceListener listener;

    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    private Color unlockedColor = new Color(30, 30, 40, 210);
    private Color lockedColor   = new Color(20, 20, 25, 160);
    private Color hoverColor    = new Color(60, 60, 80, 240);
    private Color borderUnlocked = new Color(180, 180, 220, 120);
    private Color borderLocked   = new Color(80, 80, 90, 100);
    private Color accentColor    = new Color(200, 160, 100);
    private Color textUnlocked  = Color.WHITE;
    private Color textLocked    = new Color(130, 130, 140);

    private Image choicesBg;

    // =======================================================
    // ⚠️ MANUAL SIZING
    // =======================================================
    private int manualPanelWidth  = 470;
    private int manualPanelHeight = 385;

    // =======================================================
    // ⚠️ MANUAL MARGINS: LEFT/RIGHT & TOP
    // =======================================================
    // INCREASE 'manualMarginX' to make buttons narrower (more side space)
    private int manualMarginX     = 30;  // <-- CHANGE THIS FOR LEFT/RIGHT MARGIN

    // INCREASE 'manualStartY' to add space at the top
    private int manualStartY      = 20;  // <-- CHANGE THIS FOR TOP MARGIN

    public interface ChoiceListener {
        void onChoiceSelected(String choiceText, String nextNode);
    }

    private class ChoiceButton extends JPanel {
        private String nextNode;
        private boolean unlocked;
        private Color normalColor;
        private JTextArea textArea;
        private String rawText;

        private float hoverAlpha    = 0f;
        private float pressAlpha    = 0f;
        private float fadeInAlpha   = 0f;
        private Timer hoverTimer;
        private Timer pressTimer;
        private Timer fadeInTimer;
        private boolean hovered     = false;

        public ChoiceButton(String text, String nextNode, boolean unlocked) {
            this.nextNode    = nextNode;
            this.unlocked    = unlocked;
            this.normalColor = unlocked ? unlockedColor : lockedColor;
            this.rawText     = unlocked ? text : "🔒 " + text;

            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(unlocked ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

            textArea = new JTextArea(rawText);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            textArea.setForeground(unlocked ? textUnlocked : textLocked);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            textArea.setHighlighter(null);
            add(textArea, BorderLayout.CENTER);

            if (unlocked) {
                MouseAdapter hover = new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; startHoverAnimation(true); }
                    public void mouseExited(MouseEvent e) { hovered = false; startHoverAnimation(false); }
                    public void mousePressed(MouseEvent e) { startPressAnimation(); }
                    public void mouseClicked(MouseEvent e) {
                        if (listener != null) listener.onChoiceSelected(rawText, nextNode);
                        ChoiceButtonLayer.this.setVisible(false);
                    }
                };
                addMouseListener(hover);
                textArea.addMouseListener(hover);
            }
        }

        private void startHoverAnimation(boolean in) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new Timer(16, e -> {
                hoverAlpha += in ? 0.08f : -0.08f;
                hoverAlpha  = Math.max(0f, Math.min(1f, hoverAlpha));
                repaint();
                if ((in && hoverAlpha >= 1f) || (!in && hoverAlpha <= 0f)) hoverTimer.stop();
            });
            hoverTimer.start();
        }

        private void startPressAnimation() {
            if (pressTimer != null) pressTimer.stop();
            pressAlpha = 1f;
            pressTimer = new Timer(16, e -> {
                pressAlpha -= 0.1f;
                pressAlpha  = Math.max(0f, pressAlpha);
                repaint();
                if (pressAlpha <= 0f) pressTimer.stop();
            });
            pressTimer.start();
        }

        public void startFadeIn(int delayMs) {
            fadeInAlpha = 0f;
            Timer delay = new Timer(delayMs, e -> {
                if (fadeInTimer != null) fadeInTimer.stop();
                fadeInTimer = new Timer(16, ev -> {
                    fadeInAlpha += 0.06f;
                    fadeInAlpha  = Math.min(1f, fadeInAlpha);
                    repaint();
                    if (fadeInAlpha >= 1f) fadeInTimer.stop();
                });
                fadeInTimer.start();
            });
            delay.setRepeats(false);
            delay.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 8;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeInAlpha));
            g2.setColor(normalColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            if (hoverAlpha > 0f) {
                g2.setColor(new Color(hoverColor.getRed(), hoverColor.getGreen(), hoverColor.getBlue(), (int)(hoverColor.getAlpha() * hoverAlpha)));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            }
            if (pressAlpha > 0f) {
                g2.setColor(new Color(255, 255, 255, (int)(60 * pressAlpha)));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            }
            Color borderColor = unlocked ? borderUnlocked : borderLocked;
            float borderAlpha = unlocked ? (0.4f + 0.6f * hoverAlpha) : 0.3f;
            g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), (int)(borderColor.getAlpha() * borderAlpha)));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }

        public void applyWrapWidth(int buttonWidth) {
            int innerW = buttonWidth - 26;
            textArea.setSize(new Dimension(innerW, Short.MAX_VALUE));
        }

        public int preferredHeightFor(int buttonWidth) {
            int innerW = buttonWidth - 26;
            FontMetrics fm = getFontMetrics(textArea.getFont());
            int lineHeight = fm.getHeight();
            int lines      = 1; int currentW   = 0;
            for (String word : rawText.split(" ")) {
                int wordW = fm.stringWidth(word + " ");
                if (currentW + wordW > innerW && currentW > 0) { lines++; currentW = wordW; } else { currentW += wordW; }
            }
            return (lines * lineHeight) + 24;
        }
    }

    public ChoiceButtonLayer() {
        setLayout(null);
        setOpaque(false);
        choiceButtons = new ArrayList<>();
        setVisible(false);
        java.io.File imgFile = new java.io.File("res/ui/panels/choices-panel.png");
        if (imgFile.exists()) { choicesBg = new ImageIcon(imgFile.getAbsolutePath()).getImage(); }
    }

    public void addChoice(String text, String nextNode) {
        ChoiceButton btn = new ChoiceButton(text, nextNode, true);
        choiceButtons.add(btn);
        add(btn);
    }

    public void clearChoices() {
        for (ChoiceButton btn : choiceButtons) remove(btn);
        choiceButtons.clear();
    }

    public void showChoices() {
        if (getWidth() == 0 || getHeight() == 0) return;

        int panelW  = getWidth();
        int panelH  = getHeight();
        int spacing = 8;

        // Uses the manual variable from the top
        int buttonWidth = panelW - (manualMarginX * 2);

        int fontSize = 14;
        int maxUsableH = panelH - manualStartY - 20;

        while (fontSize >= 10) {
            for (ChoiceButton btn : choiceButtons) { btn.textArea.setFont(new Font(bFont, Font.PLAIN, fontSize)); }
            int total = 0;
            for (ChoiceButton btn : choiceButtons) { total += btn.preferredHeightFor(buttonWidth); }
            total += (choiceButtons.size() - 1) * spacing;
            if (total <= maxUsableH) break;
            fontSize--;
        }

        int[] heights = new int[choiceButtons.size()];
        int totalHeight = 0;
        for (int i = 0; i < choiceButtons.size(); i++) {
            heights[i] = choiceButtons.get(i).preferredHeightFor(buttonWidth);
            totalHeight += heights[i];
        }
        totalHeight += (choiceButtons.size() - 1) * spacing;

        // Dynamic Height calculation
        manualPanelHeight = manualStartY + totalHeight + 25;

        int y = manualStartY;
        for (int i = 0; i < choiceButtons.size(); i++) {
            ChoiceButton btn = choiceButtons.get(i);
            // setBounds uses the manualMarginX here
            btn.setBounds(manualMarginX, y, buttonWidth, heights[i]);
            btn.applyWrapWidth(buttonWidth);
            btn.startFadeIn(i * 80);
            y += heights[i] + spacing;
        }

        setVisible(true);
        revalidate();
        repaint();
    }

    public void setChoiceListener(ChoiceListener listener) { this.listener = listener; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (choicesBg != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(choicesBg, 0, 0, manualPanelWidth, manualPanelHeight, this);
            g2.dispose();
        }
    }

    public void show5Choices(String c1,String n1,String c2,String n2,String c3,String n3,String c4,String n4,String c5,String n5) {
        clearChoices(); addChoice(c1,n1); addChoice(c2,n2); addChoice(c3,n3); addChoice(c4,n4); addChoice(c5,n5); showChoices();
    }
}