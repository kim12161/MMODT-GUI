package game;

import Characters.Character;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CharacterScene extends JPanel {

    private Image background;
    private Image sprite;

    // ADDED: Variable to hold the panel-big image
    private Image panelBigImage;
    // ⚠️ ADDED: Variable for the name background image
    private Image nameBoxImage;

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
        background = loadResImage("main-background.gif");
        sprite     = loadResSprite(spritePath);

        // ADDED: Load the panel-big.png image
        File fPanel = new File("res/ui/panels/panel-big.png");
        if (fPanel.exists()) {
            panelBigImage = new ImageIcon(fPanel.getAbsolutePath()).getImage();
        } else {
            System.err.println("[CharacterScene] WARNING: image not found -> res/ui/panels/panel-big.png");
        }

        // ⚠️ ADDED: Load the name-characters.png image
        File fName = new File("res/ui/panels/name-characters.png");
        if (fName.exists()) {
            nameBoxImage = new ImageIcon(fName.getAbsolutePath()).getImage();
        } else {
            System.err.println("[CharacterScene] WARNING: image not found -> res/ui/panels/name-characters.png");
        }
    }

    // =========================
    // INFO PANEL (LEFT SIDE)
    // =========================
    private void createInfoPanel(Character character) {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                // 1. Draw the name-characters.png box at the very top (Y=0)
                if (nameBoxImage != null) {
                    g2.drawImage(nameBoxImage, -15, 0, 260, 55, this);
                }

                // 2. Draw panel-big.png starting exactly where the text begins (Y=65)
                if (panelBigImage != null) {
                    // We draw the big panel background starting at Y=65 to leave space for the name box
                    g2.drawImage(panelBigImage, 5, 65, 520, 480, this);
                } else {
                    g.setColor(new Color(121, 103, 103, 190));
                    g.fillRect(0, 65, 520, 480);
                }
                super.paintComponent(g);
            }
        };

        panel.setLayout(null);

        // Master Position: Centers vertically (assuming 700 height)
        int panelX = 0;
        int panelY = (700 - 580) / 2; // Increased panel height to 580 to prevent cutting off text
        panel.setBounds(panelX, panelY, 540, 580);
        panel.setOpaque(false);

        // Name Box Text: Positioned inside the nameBoxImage (Top left)
        nameBox = new JLabel(character.getName().toUpperCase());
        nameBox.setFont(new Font(mainFont, Font.BOLD, 34));
        nameBox.setForeground(Color.WHITE);
        nameBox.setBounds(20, 10, 240, 45);

        // Dialogue Box: Positioned inside the panelBigImage
        dialogue = new JTextArea();
        // Start Y at 85 (which is 20 pixels inside the panel-big starting at 65)
        dialogue.setBounds(29, 92, 470, 450);

        dialogue.setEditable(false);
        dialogue.setOpaque(false);
        dialogue.setBackground(new Color(0, 0, 0, 0));
        dialogue.setLineWrap(true);
        dialogue.setWrapStyleWord(true);
        dialogue.setFont(new Font(bFont, Font.PLAIN, 15));
        dialogue.setForeground(Color.WHITE);

        panel.add(nameBox);
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
                // Header + Spacing
                typeText("[ ROLE ]\n");
                typeText(c.getRole() + "\n\n\n"); // Triple newline for the "bigger" gap

                typeText("[ PERSONALITY ]\n");
                typeText(c.getPersonality() + "\n\n\n");

                typeText("[ FLAWS ]\n");
                typeText(c.getFlaws() + "\n\n\n");

                typeText("[ ROMANCE HOOK ]\n");
                typeText(c.getRomanceHook() + "\n\n\n");

                typeText("[ SURVIVAL SKILLS ]\n");
                typeText(c.getSurvivalSkills() + "\n\n\n\n"); // Extra gap before footer

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

        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (sprite != null) {
            //
            // Sizing: Increased from 240x360 to a larger presence
            int spriteWidth  = 340;
            int spriteHeight = 600;
//            int spriteWidth  = 240;
//            int spriteHeight = 360;

            // X remains on the right side
            int x = getWidth() - spriteWidth - 10;

            // Y is set to literally touch the bottom of the screen
            int y = getHeight() - spriteHeight;

            // DRAW IMAGE ONLY (Black shadow box code removed)
            g2.drawImage(sprite, x, y, spriteWidth, spriteHeight, this);
        }
    }
}