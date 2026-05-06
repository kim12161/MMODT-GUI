package Interaction;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SpriteLayer extends JPanel {

    private Map<String, Image> sprites = new HashMap<>();

    private static final int SPRITE_WIDTH  = 400;
    private static final int SPRITE_HEIGHT = 700;
    private static final int SPRITE_X      = 10;
    private static final int SPRITE_Y      = 20;

    private String currentSpriteName = null;
    private String nextSpriteName    = null;

    // Fade fields
    private float alpha        = 1.0f;
    private boolean isFading   = false;

    private static final int FADE_STEPS    = 10;
    private static final int FADE_DELAY_MS = 20;

    public SpriteLayer() {
        setBounds(0, 0, 900, 700);
        setOpaque(false);
    }

    // ==============================
    // LOAD
    // ==============================
    public void loadCharacter(String name, String normalPath, String turnOnPath, String turnOffPath, String charismaPath) {
        loadSprite(name,               normalPath);
        loadSprite(name + "_turnOn",   turnOnPath);
        loadSprite(name + "_turnOff",  turnOffPath);
        loadSprite(name + "_charisma", charismaPath);
        loadSprite(name + "_trust",    charismaPath);
    }

    private void loadSprite(String key, String path) {
        if (path == null) return;
        java.io.File f = new java.io.File(path);
        if (f.exists()) {
            Image img = new ImageIcon(f.getAbsolutePath())
                    .getImage()
                    .getScaledInstance(SPRITE_WIDTH, SPRITE_HEIGHT, Image.SCALE_SMOOTH);
            sprites.put(key, img);
        } else {
            System.out.println("✗ Sprite not found: " + path);
        }
    }

    // ==============================
    // SHOW / HIDE
    // ==============================
    public void showWithEffect(String name, String effect) {
        String key = switch (effect != null ? effect : "NEUTRAL") {
            case "TURN_ON"            -> name + "_turnOn";
            case "TURN_OFF",
                 "TURN_OFF2"          -> name + "_turnOff";
            case "CHARISMA"           -> name + "_charisma";
            case "TRUST"              -> name + "_trust";
            default                   -> name;
        };
        String resolved = sprites.containsKey(key) ? key : name;
        fadeToSprite(resolved);
    }

    public void show(String name) {
        fadeToSprite(name);
    }

    public void hide() {
        fadeOut(() -> {
            currentSpriteName = null;
            alpha = 1.0f;
            repaint();
        });
    }

    // ==============================
    // FADE LOGIC
    // ==============================
    private void fadeToSprite(String newSpriteName) {
        if (isFading) return; // prevent overlap

        // If no sprite shown yet, just fade in directly
        if (currentSpriteName == null) {
            currentSpriteName = newSpriteName;
            alpha = 0f;
            fadeIn();
            return;
        }

        // If same sprite, do nothing
        if (newSpriteName.equals(currentSpriteName)) return;

        nextSpriteName = newSpriteName;

        // Fade out current, then swap and fade in new
        fadeOut(() -> {
            currentSpriteName = nextSpriteName;
            nextSpriteName = null;
            fadeIn();
        });
    }

    private void fadeOut(Runnable onComplete) {
        isFading = true;
        new Thread(() -> {
            for (int i = FADE_STEPS; i >= 0; i--) {
                alpha = i / (float) FADE_STEPS;
                repaint();
                sleep(FADE_DELAY_MS);
            }
            alpha = 0f;
            isFading = false;
            SwingUtilities.invokeLater(onComplete);
        }).start();
    }

    private void fadeIn() {
        isFading = true;
        new Thread(() -> {
            for (int i = 0; i <= FADE_STEPS; i++) {
                alpha = i / (float) FADE_STEPS;
                repaint();
                sleep(FADE_DELAY_MS);
            }
            alpha = 1.0f;
            isFading = false;
            repaint();
        }).start();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ==============================
    // PAINT
    // ==============================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentSpriteName == null) return;

        Image img = sprites.get(currentSpriteName);
        if (img != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
            g2.drawImage(img, SPRITE_X, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT, this);
            g2.dispose();
        }
    }
}