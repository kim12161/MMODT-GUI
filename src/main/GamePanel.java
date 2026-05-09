package main;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import game.MusicManager; // Make sure to import your MusicManager

public class GamePanel extends JPanel {

    private Image backgroundImage;

    public GamePanel(String fileName) {
        this.backgroundImage = new ImageIcon(fileName).getImage();

        // 🛠️ AUTOMATIC UI SOUNDS
        setupAutomaticSounds();
    }

    private void setupAutomaticSounds() {
        // This listens to the "Focus" of the application.
        // Whenever a button is "pressed" via keyboard (Enter/Space), it triggers.
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    // Example: Play sound on Space/Enter if a button is focused
                    return false;
                });

        // 💡 THE TRICK: Use the UIManager defaults to catch global button clicks
        // Note: For custom buttons like yours, calling it in the ButtonHandler
        // as you did earlier is actually more reliable.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Nearest neighbor keeps your pixel art sharp!
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}