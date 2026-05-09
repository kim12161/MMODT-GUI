package Encounters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import game.MusicManager;

/**
 * DeathPanel — fullscreen death screen.
 * Sequence: red flash → fades out → dark red-black gradient revealed
 *           → "DEATH HAS CLAIMED YOU" fades in → sub-text → button.
 */
public class DeathPanel extends JPanel {

    // ── dimensions ───────────────────────────────────────────────────────────
    private static final int W = 900;
    private static final int H = 700;

    private float imageAlpha = 0f;
    //FONT
    private String mainFont = "PixelArmy";
    private String bFont = "Munro";

    static {
        loadFont("/font/PixelArmy.ttf", 62f);
        loadFont("/font/Munro.ttf", 22f);
    }

    private static Font loadFont(String path, float size) {
        try {
            java.io.File f = new java.io.File(path);
            if (!f.exists()) return null;
            Font font = Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(size);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception ignored) { return null; }
    }

    // ── button sprites ───────────────────────────────────────────────────────
    private Image btnNormal, btnHover, btnActive, deathImg;

    // ── animation state ──────────────────────────────────────────────────────
    private int   redFlashAlpha = 255;   // 255 → 0 (red flash fades away)
    private float bgAlpha       = 0f;    // 0 → 1  (gradient background fades in)
    private float titleAlpha    = 0f;    // 0 → 1
    private float subAlpha      = 0f;    // 0 → 1
    private float btnAlpha      = 0f;    // 0 → 1

    // ── callback ─────────────────────────────────────────────────────────────
    public interface TitleScreenCallback { void goToTitleScreen(); }
    private final TitleScreenCallback onTitle;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public DeathPanel(TitleScreenCallback onTitle) {
        this.onTitle = onTitle;

        MusicManager.loadAllBGM();  // ← add this
        MusicManager.loadAllSFX();
        setLayout(null);
        setPreferredSize(new Dimension(W, H));
        setOpaque(true);
        setBackground(Color.BLACK);


        loadImages();
        buildButton();
        // Do NOT start animation here — call onShow() after adding to parent
    }

    /**
     * Call this AFTER adding DeathPanel to its parent container.
     * Starts the animation sequence once the panel is in the hierarchy.
     */
    public void onShow() {
        startAnimation();
    }

    // =========================================================================
    // IMAGE LOADING
    // =========================================================================
    private void loadImages() {
        tryLoad("res/ui/icon/normal-buttons/button-2-normal-not-active.png", img -> btnNormal = img);
        tryLoad("res/ui/icon/normal-buttons/button-2-normal-hover.png",      img -> btnHover  = img);
        tryLoad("res/ui/icon/normal-buttons/button-2-normal-active.png",     img -> btnActive = img);

        // 🛠️ ADDED: Loading the death image
        tryLoad("res/background/ending/death.png",                           img -> deathImg  = img);
    }

    @FunctionalInterface private interface ImageConsumer { void accept(Image img); }
    private void tryLoad(String path, ImageConsumer c) {
        try {
            java.io.File f = new java.io.File(path);
            if (f.exists()) c.accept(new ImageIcon(f.getAbsolutePath()).getImage());
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // BUTTON
    // =========================================================================
    private void buildButton() {
        // 🛠️ CHANGED: Text updated to "Try Again?"
        JButton btn = new JButton("Try Again?") {
            private boolean hovered = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font(bFont, Font.BOLD, 20));
                setForeground(Color.WHITE);
                setHorizontalTextPosition(CENTER);
                setVerticalTextPosition(CENTER);
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (btnAlpha <= 0f) return; // Keeps the fade-in animation working!

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, btnAlpha)));
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                boolean isPressed = getModel().isPressed();
                Image currentSprite;

                if (isPressed) {
                    currentSprite = btnActive;
                } else if (hovered) {
                    currentSprite = btnHover;
                } else {
                    currentSprite = btnNormal;
                }

                // 1. Draw the button image first
                if (currentSprite != null) {
                    g2.drawImage(currentSprite, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // fallback dark-red pill
                    g2.setColor(new Color(90, 15, 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(160, 30, 30));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();

                // 2. PUSH THE TEXT DOWN IF PRESSED
                if (isPressed) {
                    g.translate(-3, 3);
                }

                // 3. Draw the text on top
                // (Wrapped in gText to ensure the text fades in smoothly with the button)
                Graphics2D gText = (Graphics2D) g.create();
                gText.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, btnAlpha)));
                gText.translate(9, 5);
                super.paintComponent(gText);
                gText.dispose();

                // 4. Reset the position so it doesn't mess up the next frame
                if (isPressed) {
                    g.translate(3, -3);
                }
            }
        };

        // 🛠️ CHANGED: Shifted down to Y = 600 so it sits nicely below the image and text
        int bW = 240, bH = 70;
        btn.setBounds((W - bW) / 2-10, 570, bW, bH);
        btn.addActionListener(e -> {
//            MusicManager.playSoundEffect("res/audio/effects/btn-click.wav");
            if (onTitle != null) onTitle.goToTitleScreen(); });
        add(btn);
    }

    // =========================================================================
    // ANIMATION THREAD
    // =========================================================================
    private void startAnimation() {
        // 60-fps repaint loop
        new Timer(16, e -> repaint()).start();

        new Thread(() -> {
//            MusicManager.play("res/audio/bgm/death-bgm.wav");

            // ── Phase 1: red flash holds briefly ────────────────────────────
            sleep(500);

            // ── Phase 2: red flash fades out while gradient fades in ─────────
            for (int i = 255; i >= 0; i -= 4) {
                redFlashAlpha = Math.max(0, i);
                bgAlpha = 1f - (i / 255f);   // inverse: bg appears as flash leaves
                sleep(16);
            }
            redFlashAlpha = 0;
            bgAlpha = 1f;
            sleep(400);

            // ── Phase 3: "DEATH HAS CLAIMED YOU" & IMAGE fade in ─────────────
            for (float f = 0f; f <= 1f; f += 0.022f) {
                titleAlpha = Math.min(1f, f);
                imageAlpha = Math.min(1f, f);
                sleep(18);
            }
            titleAlpha = 1f;
            imageAlpha = 1f;
            sleep(350);

            // ── Phase 4: sub-text fades in ────────────────────────────────────
            for (float f = 0f; f <= 1f; f += 0.028f) {
                subAlpha = Math.min(1f, f);
                sleep(18);
            }
            subAlpha = 1f;
            sleep(450);

            // ── Phase 5: button fades in ──────────────────────────────────────
            for (float f = 0f; f <= 1f; f += 0.028f) {
                btnAlpha = Math.min(1f, f);
                sleep(18);
            }
            btnAlpha = 1f;

        }, "DeathPanel-Anim").start();
    }

    // =========================================================================
    // PAINT
    // =========================================================================
    @Override
    protected void paintComponent(Graphics g) {
        // solid black base — fully covers whatever is underneath
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── 1. Dark red-black gradient background ─────────────────────────────
        if (bgAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha));
            GradientPaint gp = new GradientPaint(
                    0, 0,   new Color(18, 0, 0),
                    0, H,   new Color(60, 8, 8)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, W, H);

            // vignette: dark edges
            RadialGradientPaint vignette = new RadialGradientPaint(
                    new java.awt.geom.Point2D.Float(W / 2f, H / 2f),
                    Math.max(W, H) * 0.68f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 210)}
            );
            g2.setPaint(vignette);
            g2.fillRect(0, 0, W, H);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── 2. Red flash overlay (on top of gradient) ─────────────────────────
        if (redFlashAlpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, redFlashAlpha / 255f));
            g2.setColor(new Color(180, 0, 0));
            g2.fillRect(0, 0, W, H);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── 3. Main title & Image center ─────────────────────────────────────
        if (titleAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));
            g2.setFont(new Font(bFont, Font.BOLD, 52));
            FontMetrics fm = g2.getFontMetrics();
            String title = "DEATH HAS CLAIMED YOU";
            int tx = (W - fm.stringWidth(title)) / 2;
            int ty = 120; // 🛠️ CHANGED: Moved text up to the top!

            // shadow
            g2.setColor(new Color(80, 0, 0, (int)(220 * titleAlpha)));
            g2.drawString(title, tx + 3, ty + 5);

            // main text
            g2.setColor(new Color(255, 255, 255, (int)(255 * titleAlpha)));
            g2.drawString(title, tx, ty);

            // 🛠️ NEW: Draw the death image underneath the title
            if (deathImg != null) {
                int imgW = 580;
                int imgH = 320;
                int imgX = (W - imgW) / 2;
                int imgY = 160;

                Composite oldComp = g2.getComposite();

                // Set alpha specifically for the image (using imageAlpha or titleAlpha)
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, imageAlpha));

                g2.drawImage(deathImg, imgX, imgY, imgW, imgH, this);

                // Draw the thin copper border around the image like in the reference
                g2.setColor(new Color(200, 140, 120, (int)(255 * imageAlpha)));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(imgX, imgY, imgW, imgH);

                // Restore composite so next elements aren't messed up
                g2.setComposite(oldComp);
            }

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── 4. Sub-text lines ────────────────────────────────────────────────
        if (subAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, subAlpha));

            // Note: Removed the red divider line to match your reference image

            g2.setFont(new Font(bFont, Font.PLAIN, 24));
            FontMetrics sfm = g2.getFontMetrics();

            // Primary sub-line
            String sub1 = "The world goes on without you...";
            // 🛠️ CHANGED: Color updated to white and moved below the image
            g2.setColor(new Color(255, 255, 255, (int)(255 * subAlpha)));
            g2.drawString(sub1, (W - sfm.stringWidth(sub1)) / 2, 520);

            // Secondary flavour text
            g2.setFont(new Font(bFont, Font.PLAIN, 16));
            sfm = g2.getFontMetrics();
            String sub2 = "Your wounds were too great to bear. Rest now, fallen one.";
            // 🛠️ CHANGED: Color updated to light grey and moved below the image
            g2.setColor(new Color(200, 200, 200, (int)(255 * subAlpha)));
            g2.drawString(sub2, (W - sfm.stringWidth(sub2)) / 2, 550);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        g2.dispose();
    }

    private void fadeInImage() {
        imageAlpha = 0f; // Reset to invisible
        javax.swing.Timer timer = new javax.swing.Timer(30, null);
        timer.addActionListener(e -> {
            imageAlpha += 0.05f; // Increase visibility
            if (imageAlpha >= 1.0f) {
                imageAlpha = 1.0f;
                timer.stop();
            }
            repaint();
        });
        timer.start();
    }

    // =========================================================================
    // HELPER
    // =========================================================================
    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}