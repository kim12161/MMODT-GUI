package Interaction;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class SpriteLayer extends JPanel {

    private Map<String, Image> sprites = new HashMap<>();
    @Override public Dimension getMinimumSize() { return new Dimension(1280, 720); }
    @Override public Dimension getMaximumSize() { return new Dimension(1280, 720); }
    @Override public Dimension getPreferredSize() { return new Dimension(1280, 720); }

    private class SpritePosition {
        String name;
        int x, y;
        int width, height;

        SpritePosition(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private List<SpritePosition> activeSprites = new ArrayList<>();

    private final int SPRITE_WIDTH = 386;
    private final int SPRITE_HEIGHT = 648;
    private final int SPRITE_Y = 80;

    public SpriteLayer() {
        setBounds(0, 0, 1280, 720);
        setPreferredSize(new Dimension(1280, 720));
        setOpaque(false);
    }

    public void addSprite(String name, String imageName) {
        String resourcePath = "/GUI/resources/sprites/" + imageName;

        try (InputStream imgStream = getClass().getResourceAsStream(resourcePath)) {
            if (imgStream != null) {
                Image img = ImageIO.read(imgStream);
                sprites.put(name, img);
            } else {
                System.out.println("✗ Sprite not found: " + resourcePath);
            }
        } catch (IOException e) {
            System.out.println("✗ Error loading sprite: " + e.getMessage());
        }
    }

    public void showSingleSprite(String name) {
        activeSprites.clear();
        if (sprites.containsKey(name)) {
            activeSprites.add(new SpritePosition(name, 447, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        repaint();
    }

    public void showTwoSprites(String leftName, String rightName) {
        activeSprites.clear();
        if (sprites.containsKey(leftName)) {
            activeSprites.add(new SpritePosition(leftName, 150, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        if (sprites.containsKey(rightName)) {
            activeSprites.add(new SpritePosition(rightName, 744, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        repaint();
    }

    public void showThreeSprites(String leftName, String centerName, String rightName) {
        activeSprites.clear();
        if (sprites.containsKey(leftName)) {
            activeSprites.add(new SpritePosition(leftName, 50, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        if (sprites.containsKey(centerName)) {
            activeSprites.add(new SpritePosition(centerName, 447, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        if (sprites.containsKey(rightName)) {
            activeSprites.add(new SpritePosition(rightName, 844, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }

        repaint();
    }

    public void showFourSprites(String name1, String name2, String name3, String name4) {
        activeSprites.clear();

        int totalWidth = 1280;
        int spacing = (totalWidth - (4 * SPRITE_WIDTH)) / 5;

        if (sprites.containsKey(name1)) {
            activeSprites.add(new SpritePosition(name1, spacing, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        if (sprites.containsKey(name2)) {
            activeSprites.add(new SpritePosition(name2, spacing * 2 + SPRITE_WIDTH, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        if (sprites.containsKey(name3)) {
            activeSprites.add(new SpritePosition(name3, spacing * 3 + SPRITE_WIDTH * 2, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        if (sprites.containsKey(name4)) {
            activeSprites.add(new SpritePosition(name4, spacing * 4 + SPRITE_WIDTH * 3, SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT));
        }
        repaint();
    }

    public void showCharacter(String name) {
        showSingleSprite(name);
    }

    public void hideAllSprites() {
        activeSprites.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (SpritePosition sprite : activeSprites) {
            if (sprites.containsKey(sprite.name)) {
                Image img = sprites.get(sprite.name);
                g.drawImage(img, sprite.x, sprite.y, sprite.width, sprite.height, this);
            }
        }

        if (activeSprites.isEmpty()) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(255, 255, 255, 100));
        }
    }
}