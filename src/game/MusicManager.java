package game;

import javax.sound.sampled.*;
import java.io.File;

public class MusicManager {

    private static Clip bgmClip;
    private static String currentTrack = "";

    // ========================
    // PLAY (loops forever)
    // ========================
    public static void play(String filePath) {
        try {
            // 🛠️ CHECK: If we are already playing this exact file, do NOT restart it.
            // This prevents the music from stopping/resetting if called twice.
            if (currentTrack.equals(filePath) && bgmClip != null && bgmClip.isRunning()) {
                return;
            }

            if (bgmClip != null) {
                bgmClip.stop();
                bgmClip.flush(); // Clears any buffered data
                bgmClip.close();
            }

            currentTrack = filePath;

            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(filePath));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audio);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // ← loops forever automatically
            bgmClip.start();

        } catch (Exception e) {
            System.err.println("[MusicManager] Could not play: " + filePath);
        }
    }

    public static void playSoundEffect(String filePath) {
        new Thread(() -> {
            try {
                File soundFile = new File(filePath);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                // Optional: close the clip after it finishes playing
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.err.println("[MusicManager] Error playing effect: " + filePath);
            }
        }).start();
    }

    // ========================
    // STOP
    // ========================
    public static void stop() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    // ========================
    // FADE IN
    // ========================
    public static void fadeIn(String filePath, int durationMs) {
        try {
            if (bgmClip != null && bgmClip.isRunning()) {
                bgmClip.stop();
                bgmClip.close();
            }

            currentTrack = filePath;
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(filePath));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audio);

            FloatControl volume = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-40f);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();

            new Thread(() -> {
                float start = -40f, end = 0f;
                int steps = 40, delay = durationMs / steps;
                float step = (end - start) / steps;
                for (int i = 0; i < steps; i++) {
                    start += step;
                    volume.setValue(Math.min(start, end));
                    try { Thread.sleep(delay); } catch (Exception ignored) {}
                }
            }).start();

        } catch (Exception e) {
            System.err.println("[MusicManager] Could not fade in: " + filePath);
        }
    }

    // ========================
    // FADE OUT
    // ========================
    public static void fadeOut(int durationMs) {
        if (bgmClip == null || !bgmClip.isRunning()) return;

        new Thread(() -> {
            try {
                FloatControl volume = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                float start = volume.getValue(), end = -40f;
                int steps = 40, delay = durationMs / steps;
                float step = (end - start) / steps;

                for (int i = 0; i < steps; i++) {
                    start += step;
                    volume.setValue(Math.max(start, end));
                    try { Thread.sleep(delay); } catch (Exception ignored) {}
                }
                bgmClip.stop();
                bgmClip.close();
            } catch (Exception ignored) {}
        }).start();
    }

    // ========================
    // CROSSFADE (switch tracks smoothly)
    // ========================
    public static void crossfade(String newTrack, int durationMs) {
        fadeOut(durationMs);
        try { Thread.sleep(durationMs); } catch (Exception ignored) {}
        fadeIn(newTrack, durationMs);
    }

    // ========================
    // SET VOLUME (0.0 = silent, 1.0 = full)
    // ========================
    public static void setVolume(float level) {
        if (bgmClip == null) return;
        try {
            FloatControl volume = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = volume.getMinimum();
            float max = volume.getMaximum();
            float gain = min + (max - min) * level;
            volume.setValue(gain);
        } catch (Exception ignored) {}
    }

    public static boolean isPlaying() {
        return bgmClip != null && bgmClip.isRunning();
    }
}