package Interaction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChoiceButtonLayer extends JPanel {

    private List<ChoiceButton> choiceButtons;
    private ChoiceListener listener;

    private Color unlockedColor = new Color(70, 70, 70, 220);
    private Color lockedColor   = new Color(40, 40, 40, 180);
    private Color hoverColor    = new Color(100, 100, 100, 240);
    private Color textUnlocked  = Color.WHITE;
    private Color textLocked    = new Color(150, 150, 150);

    public interface ChoiceListener {
        void onChoiceSelected(String choiceText, String nextNode);
    }

    // ==============================
    // INNER CHOICE BUTTON (JPanel-based for word wrap)
    // ==============================
    private class ChoiceButton extends JPanel {
        private String nextNode;
        private boolean unlocked;
        private Color normalColor;
        private JTextArea textArea;
        private String rawText;

        public ChoiceButton(String text, String nextNode, boolean unlocked) {
            this.nextNode    = nextNode;
            this.unlocked    = unlocked;
            this.normalColor = unlocked ? unlockedColor : lockedColor;
            this.rawText     = unlocked ? text : "🔒 " + text;

            setLayout(new BorderLayout());
            setOpaque(true);
            setBackground(normalColor);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            unlocked ? new Color(200, 200, 200, 100)
                                    : new Color(100, 100, 100, 100), 1),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)
            ));
            setCursor(unlocked
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());

            textArea = new JTextArea(rawText);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 11));
            textArea.setForeground(unlocked ? textUnlocked : textLocked);
            textArea.setBackground(normalColor);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(null);
            textArea.setHighlighter(null);
            add(textArea, BorderLayout.CENTER);

            if (unlocked) {
                java.awt.event.MouseAdapter hover = new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        setBackground(hoverColor);
                        textArea.setBackground(hoverColor);
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        setBackground(normalColor);
                        textArea.setBackground(normalColor);
                    }
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (listener != null)
                            listener.onChoiceSelected(rawText, nextNode);
                        ChoiceButtonLayer.this.setVisible(false);
                    }
                };
                addMouseListener(hover);
                textArea.addMouseListener(hover);
            } else {
                java.awt.event.MouseAdapter locked = new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        System.out.println("This choice is locked!");
                    }
                };
                addMouseListener(locked);
                textArea.addMouseListener(locked);
            }
        }

        public void applyWrapWidth(int buttonWidth) {
            int innerW = buttonWidth - 26;
            textArea.setSize(new Dimension(innerW, Short.MAX_VALUE));
        }

        public int preferredHeightFor(int buttonWidth) {
            int innerW = buttonWidth - 26;
            FontMetrics fm = getFontMetrics(textArea.getFont());
            int lineHeight = fm.getHeight();
            int lines      = 1;
            int currentW   = 0;

            for (String word : rawText.split(" ")) {
                int wordW = fm.stringWidth(word + " ");
                if (currentW + wordW > innerW && currentW > 0) {
                    lines++;
                    currentW = wordW;
                } else {
                    currentW += wordW;
                }
            }

            return (lines * lineHeight) + 16;
        }

        public String getNextNode() { return nextNode; }
        public boolean isUnlocked() { return unlocked; }
        public String getRawText()  { return rawText; }
    }

    // ==============================
    // CONSTRUCTOR
    // ==============================
    public ChoiceButtonLayer() {
        setLayout(null);
        setOpaque(false);
        choiceButtons = new ArrayList<>();
        setVisible(false);
    }

    @Override
    public Dimension getPreferredSize() { return getSize(); }

    // ==============================
    // ADD / CLEAR
    // ==============================
    public void addChoice(String text, String nextNode) {
        addChoice(text, nextNode, true);
    }

    public void addChoice(String text, String nextNode, boolean unlocked) {
        ChoiceButton btn = new ChoiceButton(text, nextNode, unlocked);
        choiceButtons.add(btn);
        add(btn);
    }

    public void clearChoices() {
        for (ChoiceButton btn : choiceButtons) remove(btn);
        choiceButtons.clear();
    }

    // ==============================
    // SHOW CHOICES — centered, auto-height, word-wrap
    // ==============================
    public void showChoices() {

        if (getWidth() == 0 || getHeight() == 0) return;

        int panelW  = getWidth();
        int panelH  = getHeight();
        int spacing = 5;
        int marginX = 20;

        int dialogueBoxHeight = 150;
        int buttonWidth = panelW - (marginX * 2);

        // Step 1: Determine max usable height for buttons
        int maxUsableH = panelH - dialogueBoxHeight - 5;


        int fontSize = 13;
        while (fontSize >= 10) {
            for (ChoiceButton btn : choiceButtons) {
                btn.textArea.setFont(new Font("Consolas", Font.PLAIN, fontSize));
            }
            int total = 0;
            for (ChoiceButton btn : choiceButtons) {
                total += btn.preferredHeightFor(buttonWidth); // no Math.max override
            }
            total += (choiceButtons.size() - 1) * spacing;
            if (total <= maxUsableH) break;
            fontSize--;
        }

        // Step 3: Final height calculation using the settled font
        int[] heights = new int[choiceButtons.size()];
        int totalHeight = 0;
        for (int i = 0; i < choiceButtons.size(); i++) {
            heights[i] = choiceButtons.get(i).preferredHeightFor(buttonWidth); // no Math.max override
            totalHeight += heights[i];
        }
        totalHeight += (choiceButtons.size() - 1) * spacing;

        // Step 4: Compute startY — center within maxUsableH
        int startY = (maxUsableH - totalHeight) / 2;
        startY = Math.max(startY, 10);

        // Step 5: Position buttons
        int y = startY;
        for (int i = 0; i < choiceButtons.size(); i++) {
            ChoiceButton btn = choiceButtons.get(i);
            btn.setBounds(marginX, y, buttonWidth, heights[i]);
            btn.applyWrapWidth(buttonWidth);
            y += heights[i] + spacing;
        }

        setVisible(true);
        revalidate();
        repaint();
    }

    public void hideChoices() {
        setVisible(false);
    }

    // ==============================
    // LISTENER
    // ==============================
    public void setChoiceListener(ChoiceListener listener) {
        this.listener = listener;
    }

    // ==============================
    // LOCK / UNLOCK
    // ==============================
    public void setChoiceLocked(int index, boolean locked) {
        if (index >= 0 && index < choiceButtons.size()) {
            ChoiceButton btn = choiceButtons.get(index);
            btn.unlocked    = !locked;
            btn.normalColor = btn.unlocked ? unlockedColor : lockedColor;
            btn.setBackground(btn.normalColor);
            btn.textArea.setForeground(btn.unlocked ? textUnlocked : textLocked);
        }
    }

    public boolean isChoiceUnlocked(int index) {
        if (index >= 0 && index < choiceButtons.size())
            return choiceButtons.get(index).isUnlocked();
        return false;
    }

    public int getChoiceCount() {
        return choiceButtons.size();
    }

    // ==============================
    // PAINT — fully transparent, no overlay
    // ==============================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    // ==============================
    // CONVENIENCE METHODS
    // ==============================
    public void show2Choices(String c1, String n1, String c2, String n2) {
        clearChoices(); addChoice(c1,n1); addChoice(c2,n2); showChoices();
    }
    public void show3Choices(String c1,String n1,String c2,String n2,String c3,String n3) {
        clearChoices(); addChoice(c1,n1); addChoice(c2,n2); addChoice(c3,n3); showChoices();
    }
    public void show4Choices(String c1,String n1,String c2,String n2,
                             String c3,String n3,String c4,String n4) {
        clearChoices(); addChoice(c1,n1); addChoice(c2,n2);
        addChoice(c3,n3); addChoice(c4,n4); showChoices();
    }
    public void show5Choices(String c1,String n1,String c2,String n2,String c3,
                             String n3,String c4,String n4,String c5,String n5) {
        clearChoices(); addChoice(c1,n1); addChoice(c2,n2); addChoice(c3,n3);
        addChoice(c4,n4); addChoice(c5,n5); showChoices();
    }

    public void setUnlockedColor(Color color) { this.unlockedColor = color; }
    public void setLockedColor(Color color)   { this.lockedColor = color; }
    public void setHoverColor(Color color)    { this.hoverColor = color; }
    public void setTextColors(Color u, Color l) { textUnlocked = u; textLocked = l; }
}