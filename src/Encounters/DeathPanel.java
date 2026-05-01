package Encounters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * DeathPanel — fullscreen overlay shown when the player dies.
 *
 * Drop this into your project (same package as ZombieEncounterPanel).
 * Trigger it by replacing the "Death has claimed you..." log line with:
 *
 *   DeathPanel dp = new DeathPanel(() -> {
 *       // navigate back to title screen here
 *       cardLayout.show(mainContainer, "TITLE");   // or however you do it
 *   });
 *   parentContainer.add(dp, "DEATH");
 *   cardLayout.show(parentContainer, "DEATH");
 *
 * The panel is 900 × 700 to match ZombieEncounterPanel.
 */
public class DeathPanel extends JPanel {

    // ── sizes to match ZombieEncounterPanel ─────────────────────────────────
    private static final int W = 900;
    private static final int H = 700;

    // ── fonts (same names used in ZombieEncounterPanel) ─────────────────────
    private static final String MAIN_FONT  = "PixelArmy";
    private static final String BODY_FONT  = "Munro";

    // ── images ───────────────────────────────────────────────────────────────
    private Image bgImage;          // optional custom death-bg
    private Image btnNormal, btnHover, btnActive;

    // ── animation state ──────────────────────────────────────────────────────
    private float overlayAlpha   = 0f;   // 0 → 1   (fade-in dark overlay)
    private float titleAlpha     = 0f;   // 0 → 1   (fade-in main text)
    private float subAlpha       = 0f;   // 0 → 1   (fade-in sub text)
    private float btnAlpha       = 0f;   // 0 → 1   (fade-in button)
    private int   redFlashAlpha  = 180;  // starts opaque, fades away
    private boolean animDone     = false;

    // ── blood drip particles ─────────────────────────────────────────────────
    private final java.util.List<BloodDrip> drips = new java.util.ArrayList<>();

    // ── callback ─────────────────────────────────────────────────────────────
    public interface TitleScreenCallback { void goToTitleScreen(); }
    private final TitleScreenCallback onTitle;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public DeathPanel(TitleScreenCallback onTitle) {
        this.onTitle = onTitle;
        setLayout(null);
        setPreferredSize(new Dimension(W, H));
        setOpaque(true);
        setBackground(Color.BLACK);

        loadImages();
        spawnDrips();
        buildButton();
        startAnimation();
    }

    // =========================================================================
    // IMAGE LOADING
    // =========================================================================
    private void loadImages() {
        // Optional custom death background — falls back to pure black
        tryLoad("res/ui/panels/death-bg.png",            img -> bgImage   = img);

        // Reuse the same button sprites from ZombieEncounterPanel
        tryLoad("res/ui/icon/normal-buttons/button-2-normal-not-active.png", img -> btnNormal = img);
        tryLoad("res/ui/icon/normal-buttons/button-2-normal-hover.png",      img -> btnHover  = img);
        tryLoad("res/ui/icon/normal-buttons/button-2-normal-active.png",     img -> btnActive = img);
    }

    @FunctionalInterface private interface ImageConsumer { void accept(Image img); }
    private void tryLoad(String path, ImageConsumer c) {
        try {
            java.io.File f = new java.io.File(path);
            if (f.exists()) c.accept(new ImageIcon(f.getAbsolutePath()).getImage());
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // BLOOD DRIP PARTICLES
    // =========================================================================
    private static class BloodDrip {
        float x, y, speed, length;
        int alpha;
        BloodDrip(float x, float y, float speed, float length, int alpha) {
            this.x = x; this.y = y; this.speed = speed;
            this.length = length; this.alpha = alpha;
        }
    }

    private void spawnDrips() {
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 18; i++) {
            drips.add(new BloodDrip(
                    rnd.nextInt(W),
                    -rnd.nextInt(H / 2),          // start above screen
                    1.2f + rnd.nextFloat() * 2.4f, // speed
                    20 + rnd.nextFloat() * 70,     // tail length
                    0                              // alpha starts at 0
            ));
        }
    }

    // =========================================================================
    // BUTTON
    // =========================================================================
    private void buildButton() {
        JButton btn = new JButton("Return to Title") {
            private boolean hov = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font(BODY_FONT, Font.BOLD, 20));
                setForeground(Color.WHITE);
                setHorizontalTextPosition(CENTER); setVerticalTextPosition(CENTER);
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                if (btnAlpha <= 0f) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, btnAlpha)));
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                boolean pressed = getModel().isPressed();
                Image sprite = pressed ? btnActive : hov ? btnHover : btnNormal;
                if (sprite != null) g2.drawImage(sprite, 0, 0, getWidth(), getHeight(), this);
                else {
                    g2.setColor(new Color(80, 20, 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                // draw text on top
                Graphics tg = g.create();
                tg.setFont(getFont());
                tg.setColor(getForeground());
                FontMetrics fm = tg.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                tg.translate(4, 5);
                if (getModel().isPressed()) tg.translate(-3, 3);
                tg.drawString(getText(), tx, ty);
                tg.dispose();
            }
        };

        int bW = 280, bH = 74;
        btn.setBounds((W - bW) / 2, 490, bW, bH);
        btn.addActionListener(e -> {
            if (onTitle != null) onTitle.goToTitleScreen();
        });
        add(btn);
    }

    // =========================================================================
    // ANIMATION THREAD
    // =========================================================================
    private void startAnimation() {
        // Repaint timer — 60 fps
        Timer repaintTimer = new Timer(16, e -> repaint());
        repaintTimer.start();

        new Thread(() -> {
            sleep(200);

            // 1. Red flash fades out
            for (int i = 180; i >= 0; i -= 6) {
                redFlashAlpha = i;
                sleep(16);
            }
            redFlashAlpha = 0;
            sleep(100);

            // 2. Dark overlay fades in
            for (float f = 0f; f <= 1f; f += 0.02f) {
                overlayAlpha = Math.min(1f, f);
                sleep(16);
            }
            sleep(200);

            // 3. Blood drips become visible
            for (BloodDrip d : drips) d.alpha = 255;
            sleep(600);

            // 4. Title fades in
            for (float f = 0f; f <= 1f; f += 0.025f) {
                titleAlpha = Math.min(1f, f);
                sleep(20);
            }
            sleep(300);

            // 5. Sub-text fades in
            for (float f = 0f; f <= 1f; f += 0.03f) {
                subAlpha = Math.min(1f, f);
                sleep(20);
            }
            sleep(400);

            // 6. Button fades in
            for (float f = 0f; f <= 1f; f += 0.03f) {
                btnAlpha = Math.min(1f, f);
                sleep(20);
            }
            animDone = true;

            // 7. Drips keep falling — handled in paintComponent
        }).start();
    }

    // =========================================================================
    // PAINT
    // =========================================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── background ──────────────────────────────────────────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, W, H, this);
        } else {
            // Fallback: gradient black → very dark red
            GradientPaint gp = new GradientPaint(0, 0, new Color(0, 0, 0),
                    0, H, new Color(28, 4, 4));
            g2.setPaint(gp);
            g2.fillRect(0, 0, W, H);
        }

        // ── dark overlay ────────────────────────────────────────────────────
        if (overlayAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha * 0.72f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── blood drips ─────────────────────────────────────────────────────
        for (BloodDrip d : drips) {
            if (d.alpha <= 0) continue;
            d.y += d.speed;
            if (d.y > H + d.length) {
                // reset to top
                d.y = -d.length - 10;
                d.x = (float)(Math.random() * W);
            }
            Color bloodColor = new Color(160, 10, 10, Math.min(255, d.alpha));
            g2.setColor(bloodColor);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int)d.x, (int)(d.y - d.length), (int)d.x, (int)d.y);
            // teardrop tip
            g2.fillOval((int)d.x - 3, (int)d.y - 3, 6, 6);
        }

        // ── initial red flash ────────────────────────────────────────────────
        if (redFlashAlpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, redFlashAlpha / 255f));
            g2.setColor(new Color(180, 0, 0));
            g2.fillRect(0, 0, W, H);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── vignette ────────────────────────────────────────────────────────
        RadialGradientPaint vignette = new RadialGradientPaint(
                new java.awt.geom.Point2D.Float(W / 2f, H / 2f),
                Math.max(W, H) * 0.72f,
                new float[]{0f, 1f},
                new Color[]{new Color(0,0,0,0), new Color(0,0,0,200)}
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, W, H);

        // ── "DEATH HAS CLAIMED YOU" title ────────────────────────────────────
        if (titleAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));

            // Glow / shadow
            g2.setFont(new Font(MAIN_FONT, Font.PLAIN, 62));
            FontMetrics fm = g2.getFontMetrics();
            String title = "DEATH HAS CLAIMED YOU";
            int tx = (W - fm.stringWidth(title)) / 2;
            int ty = 320;

            // dark red shadow
            g2.setColor(new Color(100, 0, 0, (int)(200 * titleAlpha)));
            g2.drawString(title, tx + 3, ty + 4);

            // main white text
            g2.setColor(new Color(255, 255, 255, (int)(255 * titleAlpha)));
            g2.drawString(title, tx, ty);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── sub-text ─────────────────────────────────────────────────────────
        if (subAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, subAlpha));
            g2.setFont(new Font(BODY_FONT, Font.PLAIN, 22));
            FontMetrics fm = g2.getFontMetrics();
            String sub = "The world goes on without you...";
            int sx = (W - fm.stringWidth(sub)) / 2;
            g2.setColor(new Color(200, 80, 80, (int)(255 * subAlpha)));
            g2.drawString(sub, sx, 370);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── thin horizontal divider ──────────────────────────────────────────
        if (subAlpha > 0.5f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (subAlpha - 0.5f) * 2f));
            g2.setColor(new Color(160, 20, 20));
            g2.setStroke(new BasicStroke(1f));
            int dw = 300;
            g2.drawLine((W - dw) / 2, 395, (W + dw) / 2, 395);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        g2.dispose();
    }

    // =========================================================================
    // HELPER
    // =========================================================================
    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}