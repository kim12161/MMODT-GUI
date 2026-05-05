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

    public SpriteLayer() {
        setBounds(0, 0, 900, 700);
        setOpaque(false);
    }

    // ==============================
    // LOAD
    // ==============================
    public void loadCharacter(String name, String normalPath, String turnOnPath, String turnOffPath, String charismaPath, String trustPath) {
        loadSprite(name,            normalPath);
        loadSprite(name + "_turnOn", turnOnPath);
        loadSprite(name + "_turnOff", turnOffPath);
        loadSprite(name + "_charisma", charismaPath);
        loadSprite(name + "_trust", trustPath);
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
            case "TURN_ON"   -> name + "_turnOn";
            case "TURN_OFF",
                 "TURN_OFF2" -> name + "_turnOff";
            case "CHARISMA"   -> name + "_charisma";
            case "TRUST"   -> name + "_trust";
            default          -> name;
        };
        currentSpriteName = sprites.containsKey(key) ? key : name;
        repaint();
    }

    public void show(String name) {
        showWithEffect(name, "NEUTRAL");
    }

    public void hide() {
        currentSpriteName = null;
        repaint();
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
            g.drawImage(img, SPRITE_X, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT, this);
        }
    }
}