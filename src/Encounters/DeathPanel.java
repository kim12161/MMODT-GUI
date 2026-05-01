package Encounters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * DeathPanel — fullscreen death screen.
 * Sequence: red flash → fades out → dark red-black gradient revealed
 *           → "DEATH HAS CLAIMED YOU" fades in → sub-text → button.
 */
public class DeathPanel extends JPanel {

    // ── dimensions ───────────────────────────────────────────────────────────
    private static final int W = 900;
    private static final int H = 700;

    // ── fonts ────────────────────────────────────────────────────────────────
    private static final Font MAIN_FONT;
    private static final Font BODY_FONT;

    static {
        Font mf = loadFont("res/fonts/PixelArmy.ttf", 62f);
        MAIN_FONT = (mf != null) ? mf : new Font("Serif", Font.BOLD, 62);

        Font bf = loadFont("res/fonts/Munro.ttf", 22f);
        BODY_FONT = (bf != null) ? bf : new Font("SansSerif", Font.PLAIN, 22);
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
    private Image btnNormal, btnHover, btnActive;

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
        JButton btn = new JButton("Return to Title") {
            private boolean hov = false;
            {
                setOpaque(false); setContentAreaFilled(false);
                setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(BODY_FONT.deriveFont(Font.BOLD, 20f));
                setForeground(Color.WHITE);
                setHorizontalTextPosition(CENTER);
                setVerticalTextPosition(CENTER);
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                if (btnAlpha <= 0f) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, btnAlpha)));
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                boolean pressed = getModel().isPressed();
                Image sprite = pressed ? btnActive : hov ? btnHover : btnNormal;
                if (sprite != null) {
                    g2.drawImage(sprite, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // fallback dark-red pill
                    g2.setColor(new Color(90, 15, 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(160, 30, 30));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();

                // text overlay
                Graphics2D tg = (Graphics2D) g.create();
                tg.setFont(getFont());
                tg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                tg.setColor(getForeground());
                FontMetrics fm = tg.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                if (getModel().isPressed()) tg.translate(-2, 2);
                tg.drawString(getText(), tx, ty);
                tg.dispose();
            }
        };

        int bW = 280, bH = 74;
        btn.setBounds((W - bW) / 2, 500, bW, bH);
        btn.addActionListener(e -> { if (onTitle != null) onTitle.goToTitleScreen(); });
        add(btn);
    }

    // =========================================================================
    // ANIMATION THREAD
    // =========================================================================
    private void startAnimation() {
        // 60-fps repaint loop
        new Timer(16, e -> repaint()).start();

        new Thread(() -> {

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

            // ── Phase 3: "DEATH HAS CLAIMED YOU" fades in ────────────────────
            for (float f = 0f; f <= 1f; f += 0.022f) {
                titleAlpha = Math.min(1f, f);
                sleep(18);
            }
            titleAlpha = 1f;
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

        // ── 3. Main title: "DEATH HAS CLAIMED YOU" ───────────────────────────
        if (titleAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));
            g2.setFont(MAIN_FONT);
            FontMetrics fm = g2.getFontMetrics();
            String title = "DEATH HAS CLAIMED YOU";
            int tx = (W - fm.stringWidth(title)) / 2;
            int ty = 310;

            // shadow
            g2.setColor(new Color(80, 0, 0, (int)(220 * titleAlpha)));
            g2.drawString(title, tx + 3, ty + 5);

            // main text
            g2.setColor(new Color(255, 255, 255, (int)(255 * titleAlpha)));
            g2.drawString(title, tx, ty);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── 4. Divider line ────────────────────────────────────────────────────
        if (subAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, subAlpha));
            g2.setColor(new Color(140, 20, 20));
            g2.setStroke(new BasicStroke(1f));
            int dw = 340;
            g2.drawLine((W - dw) / 2, 332, (W + dw) / 2, 332);

            // ── 5. Sub-text lines ──────────────────────────────────────────────
            g2.setFont(BODY_FONT);
            FontMetrics sfm = g2.getFontMetrics();

            // Primary sub-line
            String sub1 = "The world goes on without you...";
            g2.setColor(new Color(210, 80, 80, (int)(255 * subAlpha)));
            g2.drawString(sub1, (W - sfm.stringWidth(sub1)) / 2, 370);

            // Secondary flavour text
            g2.setFont(BODY_FONT.deriveFont(16f));
            sfm = g2.getFontMetrics();
            String sub2 = "Your wounds were too great to bear. Rest now, fallen one.";
            g2.setColor(new Color(160, 55, 55, (int)(200 * subAlpha)));
            g2.drawString(sub2, (W - sfm.stringWidth(sub2)) / 2, 402);

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