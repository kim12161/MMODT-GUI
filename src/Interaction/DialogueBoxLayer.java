package Interaction;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class DialogueBoxLayer extends JPanel {

    private Font mulishFont;
    private Font unicaFont;

    private Image boxImage;
    private String speaker   = "";
    private String fullText  = "";
    private Timer typewriterTimer;
    private int charIndex       = 0;
    private int typewriterDelay = 12;

    private Font speakerFont;
    private Font textFont;

    // ---- Scaled to 800x600 ----
    private static final int PANEL_W   = 900;
    private static final int PANEL_H   = 700;

    private static final int BOX_WIDTH  = 840;
    private static final int BOX_HEIGHT = 180;
    private static final int BOX_X      = (PANEL_W - BOX_WIDTH) / 2;   // 30
    private static final int BOX_Y      = PANEL_H - BOX_HEIGHT - 20;   // 430

    private static final int SPEAKER_X = BOX_X + 20;
    private static final int SPEAKER_Y = BOX_Y + 28;
    private static final int TEXT_X    = BOX_X + 20;
    private static final int TEXT_Y    = BOX_Y + 38;
    private static final int TEXT_W    = BOX_WIDTH  - 40;
    private static final int TEXT_H    = BOX_HEIGHT - 55;

    private final Color BOX_COLOR     = new Color(255, 255, 255, 210);
    private final Color TEXT_COLOR    = Color.BLACK;
    private final Color SPEAKER_COLOR = Color.BLACK;

    private JScrollPane scrollPane;
    private JTextArea   txtDialogue;
    private JLabel      speakerLabel;

    public DialogueBoxLayer() {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));

        loadCustomFonts();
        initializeFonts();
        createComponents();
        createDefaultBox();
    }

    private void initializeFonts() {
        speakerFont = (mulishFont != null)
                ? mulishFont.deriveFont(Font.BOLD, 16f)
                : new Font("Consolas", Font.BOLD, 16);
        textFont = (mulishFont != null)
                ? mulishFont.deriveFont(14f)
                : new Font("Consolas", Font.PLAIN, 14);
    }

    private void createComponents() {

        speakerLabel = new JLabel();
        speakerLabel.setForeground(SPEAKER_COLOR);
        speakerLabel.setFont(speakerFont);
        speakerLabel.setBounds(SPEAKER_X, SPEAKER_Y - 22, 300, 24);
        speakerLabel.setOpaque(false);
        add(speakerLabel);

        txtDialogue = new JTextArea();
        txtDialogue.setEditable(false);
        txtDialogue.setLineWrap(true);
        txtDialogue.setWrapStyleWord(true);
        txtDialogue.setFocusable(false);
        txtDialogue.setHighlighter(null);
        txtDialogue.setOpaque(false);
        txtDialogue.setForeground(TEXT_COLOR);
        txtDialogue.setFont(textFont);
        txtDialogue.setCursor(Cursor.getDefaultCursor());
        txtDialogue.setBorder(null);
        txtDialogue.setMargin(new Insets(4, 4, 4, 4));

        scrollPane = new JScrollPane(txtDialogue);
        scrollPane.setBounds(TEXT_X, TEXT_Y, TEXT_W, TEXT_H);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setEnabled(false);
        scrollPane.setFocusable(false);
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane);
    }

    private void createDefaultBox() {
        BufferedImage img = new BufferedImage(
                BOX_WIDTH, BOX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(BOX_COLOR);
        g2d.fillRoundRect(0, 0, BOX_WIDTH, BOX_HEIGHT, 24, 24);

        g2d.setColor(new Color(200, 200, 200, 180));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(0, 0, BOX_WIDTH - 1, BOX_HEIGHT - 1, 24, 24);

        g2d.dispose();
        boxImage = img;
    }

    public void setSpeaker(String name) {
        this.speaker = (name == null) ? "" : name;
        speakerLabel.setText(speaker);
    }

    public void setDialogue(String text) {
        if (text == null) text = "";
        this.fullText  = text;
        this.charIndex = 0;

        if (typewriterTimer != null && typewriterTimer.isRunning())
            typewriterTimer.stop();

        txtDialogue.setText("");

        typewriterTimer = new Timer(typewriterDelay, e -> {
            if (charIndex < fullText.length()) {
                txtDialogue.append(String.valueOf(fullText.charAt(charIndex)));
                charIndex++;
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        typewriterTimer.start();
    }

    public void setDialogueInstant(String text) {
        if (text == null) text = "";
        this.fullText  = text;
        this.charIndex = fullText.length();
        if (typewriterTimer != null && typewriterTimer.isRunning())
            typewriterTimer.stop();
        txtDialogue.setText(fullText);
    }

    public void skipAnimation() {
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
            txtDialogue.setText(fullText);
            charIndex = fullText.length();
        }
    }

    public boolean isAnimating() {
        return typewriterTimer != null && typewriterTimer.isRunning();
    }

    public void setTypewriterDelay(int ms) {
        this.typewriterDelay = ms;
    }

    public void clear() {
        if (typewriterTimer != null && typewriterTimer.isRunning())
            typewriterTimer.stop();
        txtDialogue.setText("");
        speaker   = "";
        fullText  = "";
        charIndex = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (boxImage != null)
            g.drawImage(boxImage, BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT, this);
    }

    private void loadCustomFonts() {
        try {
            InputStream mulish = getClass().getResourceAsStream(
                    "/GUI/resources/font/Mulish-VariableFont_wght.ttf");
            mulishFont = (mulish != null)
                    ? Font.createFont(Font.TRUETYPE_FONT, mulish) : null;

            InputStream unica = getClass().getResourceAsStream(
                    "/GUI/resources/font/UnicaOne-Regular.ttf");
            unicaFont = (unica != null)
                    ? Font.createFont(Font.TRUETYPE_FONT, unica) : null;

        } catch (Exception e) {
            mulishFont = null;
            unicaFont  = null;
        }
    }

    @Override public Dimension getMinimumSize()   { return new Dimension(PANEL_W, PANEL_H); }
    @Override public Dimension getMaximumSize()   { return new Dimension(PANEL_W, PANEL_H); }
    @Override public Dimension getPreferredSize() { return new Dimension(PANEL_W, PANEL_H); }
}