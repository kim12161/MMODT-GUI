package Interaction;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BackgroundLayer extends JPanel {

    private Icon backgroundIcon;
    private String currentBackgroundPath;
    private final Map<String, Icon> imageCache = new HashMap<>();

    private static final int W = 900;
    private static final int H = 700;

    public BackgroundLayer() {
        // CRITICAL: Layout must be null so we can place dialogue and sprites exactly where we want them over the GIF.
        setLayout(null);
        setOpaque(true);
        setPreferredSize(new Dimension(W, H));
    }

    @Override public Dimension getMinimumSize()   { return new Dimension(W, H); }
    @Override public Dimension getMaximumSize()   { return new Dimension(W, H); }
    @Override public Dimension getPreferredSize() { return new Dimension(W, H); }

    public void setBackgroundFromFile(String filename) {
        if (imageCache.containsKey(filename)) {
            backgroundIcon = imageCache.get(filename);
            repaint();
            return;
        }
        String resourcePath = "/background/" + filename;
        setBackgroundImage(resourcePath);
        if (backgroundIcon != null) imageCache.put(filename, backgroundIcon);
    }

    public void setBackgroundImage(String resourcePath) {
        this.currentBackgroundPath = resourcePath;

        if (resourcePath == null || resourcePath.isEmpty()) {
            backgroundIcon = null;
            repaint();
            return;
        }

        URL imgUrl = getClass().getResource(resourcePath);
        if (imgUrl != null) {
            backgroundIcon = new ImageIcon(imgUrl);
        } else {
            System.out.println("Failed to load background: Resource not found at " + resourcePath);
        }

        repaint();
    }

    public void setBackgroundColor(Color color) {
        BufferedImage solid = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = solid.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, W, H);
        g2d.dispose();

        backgroundIcon = new ImageIcon(solid);
        currentBackgroundPath = "solid_color";
        repaint();
    }

    public String getCurrentBackgroundPath() {
        return currentBackgroundPath;
    }

    public void preload(String... filenames) {
        for (String filename : filenames) {
            String resourcePath = "/background/" + filename;
            URL imgUrl = getClass().getResource(resourcePath);
            if (imgUrl != null) {
                imageCache.put(filename, new ImageIcon(imgUrl));
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

        if (backgroundIcon instanceof ImageIcon) {
            Image img = ((ImageIcon) backgroundIcon).getImage();
            // Draws the clean image/GIF. No black overlays!
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}