package game;

import Characters.Character;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CharacterScene extends JPanel {

    private Image background;
    private Image sprite;

    private JTextArea dialogue;
    private JLabel nameBox;

    //FONT
    private String mainFont="PixelArmy";
    private String bFont="Munro";

    public CharacterScene(Character character, String spritePath) {

        setLayout(null);

        loadImages(spritePath);
        createInfoPanel(character);
        startDialogue(character);
    }

    // =========================
    // LOAD IMAGES
    // =========================

    private Image loadResImage(String filename) {
        File f = new File("res/background/" + filename);
        if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
        System.err.println("[CharacterScene] WARNING: image not found -> res/" + filename);
        return null;
    }
    private Image loadResSprite(String filename) {
        File f = new File("res/" + filename);
        if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
        System.err.println("[CharacterScene] WARNING: image not found -> res/" + filename);
        return null;
    }

    private void loadImages(String spritePath) {
        background = loadResImage("mainBackground.png");
        sprite     = loadResSprite(spritePath);
    }

    // =========================
    // INFO PANEL (LEFT SIDE)
    // =========================
    // UI EDIT //INFOPANEL
    private void createInfoPanel(Character character) {


        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        panel.setLayout(null);
        panel.setBounds(40, 60, 540, 400);

        panel.setOpaque(false);

        // BACKGROUND FILL
        panel.setBackground(new Color(121, 103, 103, 190));

        // FILL BORDER
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 2));

        nameBox = new JLabel(character.getName().toUpperCase());
        nameBox.setFont(new Font(mainFont, Font.BOLD, 34));
        nameBox.setForeground(Color.WHITE); // Changed to white to match the pic
        nameBox.setBounds(20, 15, 420, 40);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 58, 420, 2);
        sep.setForeground(new Color(180, 30, 30));

        dialogue = new JTextArea();
        dialogue.setBounds(20, 70, 420, 400);

        dialogue.setEditable(false);

        dialogue.setOpaque(false);
        dialogue.setBackground(new Color(0, 0, 0, 0));

        dialogue.setLineWrap(true);
        dialogue.setWrapStyleWord(true);
        dialogue.setFont(new Font(bFont, Font.PLAIN, 15));
        dialogue.setForeground(Color.WHITE);

        panel.add(nameBox);
        panel.add(sep);
        panel.add(dialogue);

        add(panel);
    }
    // =========================
    // DIALOGUE SYSTEM
    // =========================

    private volatile boolean dialogueStarted = false;
    private Thread dialogueThread;

    private void startDialogue(Character c) {

        if (dialogueStarted) return;
        dialogueStarted = true;

        if (dialogueThread != null && dialogueThread.isAlive()) {
            dialogueThread.interrupt();
        }

        dialogueThread = new Thread(() -> {
            try {
                typeText("[ ROLE ]\n");
                typeText(c.getRole() + "\n\n");

                typeText("[ PERSONALITY ]\n");
                typeText(c.getPersonality() + "\n\n");

                typeText("[ FLAWS ]\n");
                typeText(c.getFlaws() + "\n\n");

                typeText("[ ROMANCE HOOK ]\n");
                typeText(c.getRomanceHook() + "\n\n");

                typeText("[ SURVIVAL SKILLS ]\n");
                typeText(c.getSurvivalSkills() + "\n\n");

                typeText("— Press ENTER to continue —");

            } catch (Exception ignored) {}
        });

        dialogueThread.setDaemon(true);
        dialogueThread.start();
    }

    private void typeText(String text) {

        for (char ch : text.toCharArray()) {

            if (Thread.currentThread().isInterrupted()) return;

            SwingUtilities.invokeLater(() -> dialogue.append(String.valueOf(ch)));

            try {
                Thread.sleep(18);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // =========================
    // DRAW GRAPHICS
    // =========================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (background != null) {
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(15, 15, 15));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (sprite != null) {
            int spriteWidth  = 240;
            int spriteHeight = 360;
            int x = getWidth() - spriteWidth - 40;
            int y = (getHeight() - spriteHeight) / 2;

            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRoundRect(x + 10, y + 10, spriteWidth, spriteHeight, 12, 12);
            g2.drawImage(sprite, x, y, spriteWidth, spriteHeight, this);
        }
    }
}