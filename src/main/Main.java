package main;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // ── MAC-SPECIFIC GRAPHICS FIXES ───────────────────────
        // These force macOS to use the OpenGL pipeline and Quartz
        // which prevents sprite flickering and GIF lag.
        System.setProperty("apple.awt.graphics.UseQuartz", "true");
        System.setProperty("sun.java2d.opengl", "true");

        // Disable Direct3D (which is for Windows) to avoid conflicts
        System.setProperty("sun.java2d.d3d", "false");

        // ── LAUNCH THE GAME ──────────────────────────────────
        // Always wrap your launcher in invokeLater to ensure
        // thread safety on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            new GameLauncher();
        });
    }
}