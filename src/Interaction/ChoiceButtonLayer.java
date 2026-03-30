package Interaction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
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

    private class ChoiceButton extends JButton {
        private String nextNode;
        private boolean unlocked;
        private Color normalColor;

        public ChoiceButton(String text, String nextNode, boolean unlocked) {
            super(text);
            this.nextNode    = nextNode;
            this.unlocked    = unlocked;
            this.normalColor = unlocked ? unlockedColor : lockedColor;

            // Small font so full choice text fits inside the narrow box
            setFont(new Font("Consolas", Font.PLAIN, 11));
            setForeground(unlocked ? textUnlocked : textLocked);
            setBackground(normalColor);
            setFocusPainted(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            unlocked ? new Color(200, 200, 200, 100)
                                    : new Color(100, 100, 100, 100), 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10) // tight padding
            ));
            setContentAreaFilled(false);
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);

            if (unlocked) {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        setBackground(hoverColor);
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        setBackground(normalColor);
                    }
                });
            } else {
                setText("🔒 " + text);
            }
        }

        public String getNextNode() { return nextNode; }
        public boolean isUnlocked() { return unlocked; }
    }

    public ChoiceButtonLayer() {
        setLayout(null);
        setOpaque(false);
        choiceButtons = new ArrayList<>();
        setVisible(false);
    }

    // No fixed preferred size — let ScenePanel control the bounds entirely
    @Override public Dimension getPreferredSize() { return getSize(); }

    public void addChoice(String text, String nextNode) {
        addChoice(text, nextNode, true);
    }

    public void addChoice(String text, String nextNode, boolean unlocked) {

        ChoiceButton btn = new ChoiceButton(text, nextNode, unlocked);

        if (unlocked) {
            btn.addActionListener(e -> {
                if (listener != null)
                    listener.onChoiceSelected(btn.getText(), btn.getNextNode());
                setVisible(false);
            });
        } else {
            btn.addActionListener(e ->
                    System.out.println("This choice is locked!")
            );
        }

        choiceButtons.add(btn);
        add(btn);
    }

    public void clearChoices() {
        for (ChoiceButton btn : choiceButtons) remove(btn);
        choiceButtons.clear();
    }

    public void showChoices() {

        int numChoices  = choiceButtons.size();
        int panelW      = getWidth();   // use actual width set by ScenePanel
        int panelH      = getHeight();  // use actual height set by ScenePanel

        int buttonWidth  = panelW - 16; // 8px margin each side
        int spacing      = 6;

        // Divide available height evenly among buttons
        int totalSpacing = (numChoices - 1) * spacing;
        int buttonHeight = (panelH - totalSpacing - 16) / numChoices; // 16px top+bottom margin
        buttonHeight     = Math.min(buttonHeight, 48); // cap so they don't get too tall

        // Recompute totalHeight with capped buttonHeight and center vertically
        int totalHeight = numChoices * buttonHeight + totalSpacing;
        int startY      = (panelH - totalHeight) / 2;

        for (int i = 0; i < choiceButtons.size(); i++) {
            ChoiceButton btn = choiceButtons.get(i);
            int x = 8;
            int y = startY + i * (buttonHeight + spacing);
            btn.setBounds(x, y, buttonWidth, buttonHeight);
        }

        setVisible(true);
        repaint();
    }

    public void hideChoices() {
        setVisible(false);
    }

    public void setChoiceListener(ChoiceListener listener) {
        this.listener = listener;
    }

    public void setChoiceLocked(int index, boolean locked) {
        if (index >= 0 && index < choiceButtons.size()) {
            ChoiceButton btn = choiceButtons.get(index);
            btn.unlocked    = !locked;
            btn.normalColor = btn.unlocked ? unlockedColor : lockedColor;
            btn.setBackground(btn.normalColor);
            btn.setForeground(btn.unlocked ? textUnlocked : textLocked);

            String currentText = btn.getText().replace("🔒 ", "");
            btn.setText(btn.unlocked ? currentText : "🔒 " + currentText);

            for (ActionListener al : btn.getActionListeners())
                btn.removeActionListener(al);

            if (btn.unlocked) {
                btn.addActionListener(e -> {
                    if (listener != null)
                        listener.onChoiceSelected(btn.getText(), btn.getNextNode());
                    setVisible(false);
                });
            }
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

    @Override
    protected void paintComponent(Graphics g) {
        // NO dark overlay — transparent so the background shows through
        super.paintComponent(g);
    }

    // Convenience methods
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