package Interaction;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.*;

public class BackgroundLayer extends JPanel {

    private Image backgroundImage;
    private String currentBackgroundPath;
    private final Map<String, Image> imageCache = new HashMap<>();

    private static final int W = 800;
    private static final int H = 600;

    public BackgroundLayer() {
        setLayout(null);
        setOpaque(true);
        setPreferredSize(new Dimension(W, H));
    }

    @Override public Dimension getMinimumSize()   { return new Dimension(W, H); }
    @Override public Dimension getMaximumSize()   { return new Dimension(W, H); }
    @Override public Dimension getPreferredSize() { return new Dimension(W, H); }

    public void setBackgroundFromFile(String filename) {
        if (imageCache.containsKey(filename)) {
            backgroundImage = imageCache.get(filename);
            repaint();
            return;
        }
        String resourcePath = "/GUI/resources/backgrounds/" + filename;
        setBackgroundImage(resourcePath);
        if (backgroundImage != null) imageCache.put(filename, backgroundImage);
    }

    public void setBackgroundImage(String resourcePath) {
        this.currentBackgroundPath = resourcePath;

        if (resourcePath == null || resourcePath.isEmpty()) {
            repaint();
            return;
        }

        try (InputStream imgStream = getClass().getResourceAsStream(resourcePath)) {
            if (imgStream != null) {
                backgroundImage = ImageIO.read(imgStream);
            }
        } catch (IOException e) {
            System.out.println("Failed to load background: " + e.getMessage());
        }

        repaint();
    }

    public void setBackgroundColor(Color color) {
        BufferedImage solid = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = solid.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, W, H);
        g2d.dispose();
        backgroundImage = solid;
        currentBackgroundPath = "solid_color";
        repaint();
    }

    public String getCurrentBackgroundPath() {
        return currentBackgroundPath;
    }

    public void preload(String... filenames) {
        for (String filename : filenames) {
            String resourcePath = "/GUI/resources/backgrounds/" + filename;
            try (InputStream imgStream = getClass().getResourceAsStream(resourcePath)) {
                if (imgStream != null) {
                    imageCache.put(filename, ImageIO.read(imgStream));
                }
            } catch (IOException e) {
                System.out.println("Failed to preload: " + filename);
            }
        }
    }

    public void reloadCurrentBackground() {
        if (currentBackgroundPath != null
                && !currentBackgroundPath.equals("solid_color")
                && !currentBackgroundPath.equals("gradient")) {
            setBackgroundImage(currentBackgroundPath);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}