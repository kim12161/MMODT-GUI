package Interaction;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.InputStream;

public class DialogueBoxLayer extends JPanel {

    private Font mulishFont;
    private Font unicaFont;

    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    private Image boxImage;
    private Image chainTopImage;
    private Image chainBottomImage;

    private String speaker   = "";
    private String fullText  = "";
    private Timer typewriterTimer;
    private int charIndex       = 0;
    private int typewriterDelay = 12;

    private Font speakerFont;
    private Font textFont;

    // ---- PANEL DIMENSIONS (Your 900x700 screen) ----
    private static final int PANEL_W   = 900;
    private static final int PANEL_H   = 700;

    // =======================================================
    // ⚠️ MANUAL SIZING: CHANGE THESE TO RESIZE THE CONVO-PANEL
    // =======================================================
    // BOX_WIDTH: The width of your convo-panel.png image
    private static final int BOX_WIDTH  = 840; // <-- CHANGE WIDTH HERE

    // BOX_HEIGHT: The height of your convo-panel.png image
    private static final int BOX_HEIGHT = 180; // <-- CHANGE HEIGHT HERE

    // BOX_X & BOX_Y: Position of the panel (Auto-centered based on BOX_WIDTH)
    private static final int BOX_X      = (PANEL_W - BOX_WIDTH) / 2;
    private static final int BOX_Y      = PANEL_H - BOX_HEIGHT - 20;

    // =======================================================
    // TEXT PLACEMENT (RELATIVE TO THE BOX)
    // =======================================================
    private static final int SPEAKER_X = BOX_X + 28;
    private static final int SPEAKER_Y = BOX_Y + 47;
    private static final int TEXT_X    = BOX_X + 28;
    private static final int TEXT_Y    = BOX_Y + 74; // Pushes text into brown area
    private static final int TEXT_W    = BOX_WIDTH  - 40;
    private static final int TEXT_H    = BOX_HEIGHT - 55;

    private final Color TEXT_COLOR    = Color.WHITE;
    private final Color SPEAKER_COLOR = Color.WHITE;

    private JScrollPane scrollPane;
    private JTextPane   txtDialogue;
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
        speakerFont = new Font(mainFont, Font.BOLD, 20);
        textFont = new Font(bFont, Font.PLAIN, 16);
    }

    private void createComponents() {
        speakerLabel = new JLabel();
        speakerLabel.setForeground(SPEAKER_COLOR);
        speakerLabel.setFont(speakerFont);
        speakerLabel.setBounds(SPEAKER_X, SPEAKER_Y - 22, 300, 24);
        speakerLabel.setOpaque(false);
        add(speakerLabel);

        txtDialogue = new JTextPane();
        txtDialogue.setEditable(false);
        txtDialogue.setFocusable(false);
        txtDialogue.setHighlighter(null);
        txtDialogue.setOpaque(false);
        txtDialogue.setForeground(TEXT_COLOR);
        txtDialogue.setFont(textFont);
        txtDialogue.setCursor(Cursor.getDefaultCursor());
        txtDialogue.setBorder(null);

        // Vertical spacing control
        MutableAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(set, 0.3f);
        txtDialogue.setParagraphAttributes(set, false);

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
        java.io.File imgFile = new java.io.File("res/ui/panels/convo-panel.png");
        if (imgFile.exists()) {
            boxImage = new ImageIcon(imgFile.getAbsolutePath()).getImage();
        }

        java.io.File topChainFile = new java.io.File("res/ui/panels/chain-top.png");
        if (topChainFile.exists()) {
            chainTopImage = new ImageIcon(topChainFile.getAbsolutePath()).getImage();
        }

        java.io.File bottomChainFile = new java.io.File("res/ui/panels/chain-bottom.png");
        if (bottomChainFile.exists()) {
            chainBottomImage = new ImageIcon(bottomChainFile.getAbsolutePath()).getImage();
        }
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
                try {
                    txtDialogue.getDocument().insertString(txtDialogue.getDocument().getLength(),
                            String.valueOf(fullText.charAt(charIndex)), null);
                } catch (Exception ex) {}
                charIndex++;
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        typewriterTimer.start();
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
        if (boxImage != null) {
            // Draws convo-panel based on BOX_WIDTH and BOX_HEIGHT constants
            g.drawImage(boxImage, BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT, this);
        }
        if (chainTopImage != null) {
            int h = chainTopImage.getHeight(this);
            g.drawImage(chainTopImage, BOX_X, BOX_Y, BOX_WIDTH, h, this);
        }
        if (chainBottomImage != null) {
            int h = chainBottomImage.getHeight(this);
            g.drawImage(chainBottomImage, BOX_X, BOX_Y + BOX_HEIGHT - h, BOX_WIDTH, h, this);
        }
    }

    private void loadCustomFonts() {
        try {
            InputStream mulish = getClass().getResourceAsStream("/GUI/resources/font/Mulish-VariableFont_wght.ttf");
            mulishFont = (mulish != null) ? Font.createFont(Font.TRUETYPE_FONT, mulish) : null;
            InputStream unica = getClass().getResourceAsStream("/GUI/resources/font/UnicaOne-Regular.ttf");
            unicaFont = (unica != null) ? Font.createFont(Font.TRUETYPE_FONT, unica) : null;
        } catch (Exception e) {}
    }

    @Override public Dimension getPreferredSize() { return new Dimension(PANEL_W, PANEL_H); }
}