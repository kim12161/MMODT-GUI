////package Interaction;
////
////import javax.swing.*;
////import java.awt.*;
////import java.awt.image.BufferedImage;
////import java.net.URL;
////import java.util.HashMap;
////import java.util.Map;
////
////public class BackgroundLayer extends JPanel {
////
////    private Icon backgroundIcon;
////    private String currentBackgroundPath;
////    private final Map<String, Icon> imageCache = new HashMap<>();
////
////    private static final int W = 900;
////    private static final int H = 700;
////
////    public BackgroundLayer() {
////        // CRITICAL: Layout must be null so we can place dialogue and sprites exactly where we want them over the GIF.
////        setLayout(null);
////        setOpaque(true);
////        setPreferredSize(new Dimension(W, H));
////    }
////
////    @Override public Dimension getMinimumSize()   { return new Dimension(W, H); }
////    @Override public Dimension getMaximumSize()   { return new Dimension(W, H); }
////    @Override public Dimension getPreferredSize() { return new Dimension(W, H); }
////
////    public void setBackgroundFromFile(String filename) {
////        if (imageCache.containsKey(filename)) {
////            backgroundIcon = imageCache.get(filename);
////            repaint();
////            return;
////        }
////        String resourcePath = "/background/" + filename;
////        setBackgroundImage(resourcePath);
////        if (backgroundIcon != null) imageCache.put(filename, backgroundIcon);
////    }
////
////    public void setBackgroundImage(String resourcePath) {
////        this.currentBackgroundPath = resourcePath;
////
////        if (resourcePath == null || resourcePath.isEmpty()) {
////            backgroundIcon = null;
////            repaint();
////            return;
////        }
////
////        URL imgUrl = getClass().getResource(resourcePath);
////        if (imgUrl != null) {
////            backgroundIcon = new ImageIcon(imgUrl);
////        } else {
////            System.out.println("Failed to load background: Resource not found at " + resourcePath);
////        }
////
////        repaint();
////    }
////
////    public void setBackgroundColor(Color color) {
////        BufferedImage solid = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
////        Graphics2D g2d = solid.createGraphics();
////        g2d.setColor(color);
////        g2d.fillRect(0, 0, W, H);
////        g2d.dispose();
////
////        backgroundIcon = new ImageIcon(solid);
////        currentBackgroundPath = "solid_color";
////        repaint();
////    }
////
////    public String getCurrentBackgroundPath() {
////        return currentBackgroundPath;
////    }
////
////    public void preload(String... filenames) {
////        for (String filename : filenames) {
////            String resourcePath = "/background/" + filename;
////            URL imgUrl = getClass().getResource(resourcePath);
////            if (imgUrl != null) {
////                imageCache.put(filename, new ImageIcon(imgUrl));
////            }
////        }
////    }
////
////    public void reloadCurrentBackground() {
////        if (currentBackgroundPath != null
////                && !currentBackgroundPath.equals("solid_color")
////                && !currentBackgroundPath.equals("gradient")) {
////            setBackgroundImage(currentBackgroundPath);
////        }
////    }
////
////    @Override
////    protected void paintComponent(Graphics g) {
////        super.paintComponent(g);
////
////        if (backgroundIcon instanceof ImageIcon) {
////            Image img = ((ImageIcon) backgroundIcon).getImage();
////            // Draws the clean image/GIF. No black overlays!
////            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
////        } else {
////            g.setColor(getBackground());
////            g.fillRect(0, 0, getWidth(), getHeight());
////        }
////    }
////}
//
//package Interaction;
//
//import javax.swing.*;
//import java.awt.*;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//
//public class BackgroundLayer extends JPanel {
//
//    private Image backgroundImage; // Changed from Icon to Image for Toolkit compatibility
//    private String currentBackgroundPath;
//    private final Map<String, Image> imageCache = new HashMap<>();
//
//    private static final int W = 900;
//    private static final int H = 700;
//
//    public BackgroundLayer() {
//        setLayout(null);
//        setOpaque(true);
//        setPreferredSize(new Dimension(W, H));
//
//        // 🛠️ ADD THESE THREE LINES:
//        this.setDoubleBuffered(true);
//        this.setIgnoreRepaint(false);
//        System.setProperty("apple.laf.useScreenMenuBar", "true"); // Helps Mac focus on the window
//    }
//
//    // Standard Size Overrides
//    @Override public Dimension getMinimumSize()   { return new Dimension(W, H); }
//    @Override public Dimension getMaximumSize()   { return new Dimension(W, H); }
//    @Override public Dimension getPreferredSize() { return new Dimension(W, H); }
//
//    public void setBackgroundFromFile(String filename) {
//        if (imageCache.containsKey(filename)) {
//            backgroundImage = imageCache.get(filename);
//            repaint();
//            return;
//        }
//
//        // 🛠️ FIX 1: Use Toolkit for better GIF handling on macOS
//        String fullPath = "res/background/" + filename;
//        Image img = Toolkit.getDefaultToolkit().createImage(fullPath);
//
//        // 🛠️ FIX 2: Use MediaTracker to ensure the GIF is loaded before painting
//        MediaTracker tracker = new MediaTracker(this);
//        tracker.addImage(img, 0);
//        try {
//            tracker.waitForAll();
//        } catch (InterruptedException ignored) {}
//
//        backgroundImage = img;
//        imageCache.put(filename, img);
//
//        // Ensure repaint happens on the Event Dispatch Thread (EDT)
//        SwingUtilities.invokeLater(this::repaint);
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        if (backgroundImage != null) {
//            // 🛠️ FIX 3: Bilinear hints help smooth out pixel art scaling on Retina displays
//            Graphics2D g2d = (Graphics2D) g;
//            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
//        } else {
//            g.setColor(getBackground());
//            g.fillRect(0, 0, getWidth(), getHeight());
//        }
//    }
//}

package Interaction;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BackgroundLayer extends JPanel {

    private Image backgroundImage;
    private final Map<String, Image> imageCache = new HashMap<>();

    private static final int W = 900;
    private static final int H = 700;

    public BackgroundLayer() {
        setLayout(null);
        setOpaque(true);
        setPreferredSize(new Dimension(W, H));

        // 🛠️ CRITICAL FOR MAC: Enable Double Buffering
        this.setDoubleBuffered(true);
//        this.setDoubleBuffered(true);
        this.setIgnoreRepaint(false);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    public void setBackgroundFromFile(String filename) {
        if (imageCache.containsKey(filename)) {
            backgroundImage = imageCache.get(filename);
            repaint();
            return;
        }

        // 🛠️ USE TOOLKIT: It handles Mac GIF streams better than ImageIcon
        String fullPath = "res/background/" + filename;
        Image img = Toolkit.getDefaultToolkit().createImage(fullPath);

        // Ensure GIF is loaded
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(img, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException ignored) {}


        backgroundImage = img;
        imageCache.put(filename, img);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g;

            // 🛠️ THE MAC FIX: Use 'this' as the observer.
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);// This tells Swing to listen to the GIF's internal clock.
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // 🛠️ ADD THIS METHOD:
    // This forces the panel to repaint ONLY when the GIF frame actually changes.
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
            repaint();
        }
        return super.imageUpdate(img, infoflags, x, y, w, h);
    }
}