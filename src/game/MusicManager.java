package game;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MusicManager {

    // ── BGM ─────────────────────────────────────────────
    private static Clip bgmClip;
    private static FloatControl bgmVolume;
    private static final Map<String, String> bgmPaths = new HashMap<>();

    // ── BGM Keys ─────────────────────────────────────────
    public static final String BGM_DEATH             = "death-bgm";
    public static final String BGM_ENDING            = "ending-bgm";
    public static final String BGM_GAME              = "game-bgm";
    public static final String BGM_STORYLINE         = "storyline-bgm";
    public static final String BGM_ZOMBIE_ENCOUNTER  = "zombie-encounter";

    // ── SFX ─────────────────────────────────────────────
    private static final Map<String, Clip> sfxClips = new HashMap<>();

    // ── SFX Keys ─────────────────────────────────────────
    public static final String TYPEWRITER        = "typewriter";
    public static final String STATS             = "status-add";
    public static final String DISCOVERY         = "discovery-item";
    public static final String BTN_CLICK         = "btn-click";
    public static final String BLOOD_SPLASH      = "blood-splash";
    public static final String CHARM          = "charm";
    public static final String DAMAGE            = "damage";
    public static final String DODGE             = "dodge";
    public static final String FIGHT             = "fight";
    public static final String OPENING_INVENTORY = "opening-inventory";
    public static final String STUNED            = "stuned";
    public static final String DEC_HP            = "dec-hp";
    public static final String HEARTBEAT         = "heartbeat";
    public static final String HP_RESTORE        = "hp-restores";
    public static final String ITEM              = "item";
    public static final String TRUST             = "trust";
    public static final String TURN_ON           = "turn-on";
    public static final String TURN_OFF          = "turn-off";
    public static final String KNIFE             = "knife-slash";
    public static final String CROWBAR             = "crowbar";
    public static final String BAT             = "bat";
    public static final String WOOD           = "wood";
    public static final String WATER          = "water-bottle";

    // ── Load all BGM ─────────────────────────────────────
    public static void loadAllBGM() {
        bgmPaths.put(BGM_DEATH,            "res/audio/death-bgm.wav");
        bgmPaths.put(BGM_ENDING,           "res/audio/ending-bgm.wav");
        bgmPaths.put(BGM_GAME,             "res/audio/game-bgm.wav");
        bgmPaths.put(BGM_STORYLINE,        "res/audio/storyline-bgm.wav");
        bgmPaths.put(BGM_ZOMBIE_ENCOUNTER, "res/audio/zombie-encoun.wav");
    }

    // ── Load all SFX ─────────────────────────────────────
    public static void loadAllSFX() {
        loadSFX(TYPEWRITER,        "res/audio/effects/typewriter.wav");
        loadSFX(STATS,             "res/audio/effects/status-add.wav");
        loadSFX(DISCOVERY,         "res/audio/effects/discovery-item.wav");
        loadSFX(BTN_CLICK,         "res/audio/effects/btn-click.wav");
        loadSFX(BLOOD_SPLASH,      "res/audio/effects/zombie/blood-splash.wav");
        loadSFX(CHARM,          "res/audio/effects/charm.wav");
        loadSFX(DAMAGE,            "res/audio/effects/zombie/damage.wav");
        loadSFX(DODGE,             "res/audio/effects/zombie/dodge.wav");
        loadSFX(FIGHT,             "res/audio/effects/zombie/fight.wav");
        loadSFX(OPENING_INVENTORY, "res/audio/effects/zombie/opening-inventory.wav");
        loadSFX(STUNED,            "res/audio/effects/zombie/stuned.wav");
        loadSFX(DEC_HP,            "res/audio/effects/zombie/dec-hp.wav");
        loadSFX(HEARTBEAT,         "res/audio/effects/zombie/heartbeat.wav");
        loadSFX(HP_RESTORE,        "res/audio/effects/zombie/hp-restores.wav");
        loadSFX(ITEM,              "res/audio/effects/zombie/item.wav");
        loadSFX(TRUST,             "res/audio/effects/trust.wav");
        loadSFX(TURN_ON,           "res/audio/effects/turn-on.wav");
        loadSFX(TURN_OFF,          "res/audio/effects/turn-off.wav");

        loadSFX(KNIFE,          "res/audio/effects/zombie/knife-slash.wav");
        loadSFX(CROWBAR,           "res/audio/effects/zombie/crowbar.wav");
        loadSFX(BAT,           "res/audio/effects/zombie/bat.wav");
        loadSFX(WOOD,          "res/audio/effects/zombie/wood.wav");
        loadSFX(WATER,          "res/audio/effects/zombie/water-bottle.wav");
    }

    private static void loadSFX(String key, String path) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            sfxClips.put(key, clip);
        } catch (Exception e) {
            System.err.println("[MusicManager] Failed to load SFX: " + path);
        }
    }

    // ── BGM: Play by key ─────────────────────────────────
    public static void playBGM(String key) {
        String path = bgmPaths.get(key);
        if (path == null) {
            System.err.println("[MusicManager] Unknown BGM key: " + key);
            return;
        }
        try {
            stopBGM();
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                bgmVolume = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            }
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            System.err.println("[MusicManager] Failed to play BGM: " + path);
        }
    }

    // ── BGM: Stop ────────────────────────────────────────
    public static void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    // ── BGM: Fade out ────────────────────────────────────
    public static void fadeOut(int durationMs) {
        if (bgmClip == null || !bgmClip.isRunning() || bgmVolume == null) return;
        new Thread(() -> {
            float start = bgmVolume.getValue();
            float end   = bgmVolume.getMinimum();
            int   steps = 40;
            float step  = (end - start) / steps;
            int   delay = durationMs / steps;
            for (int i = 0; i < steps; i++) {
                bgmVolume.setValue(Math.max(bgmVolume.getValue() + step, end));
                try { Thread.sleep(delay); } catch (Exception ignored) {}
            }
            stopBGM();
        }).start();
    }

    // ── SFX: Play once ───────────────────────────────────
    public static void playSFX(String key) {
        Clip clip = sfxClips.get(key);
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    // ── SFX: Loop continuously ───────────────────────────
    public static void loopSFX(String key) {
        Clip clip = sfxClips.get(key);
        if (clip == null) return;
        if (clip.isRunning()) return;
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    // ── SFX: Stop specific ───────────────────────────────
    public static void stopSFX(String key) {
        Clip clip = sfxClips.get(key);
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    // ── SFX: Stop all ────────────────────────────────────
    public static void stopAllSFX() {
        sfxClips.values().forEach(c -> {
            if (c.isRunning()) { c.stop(); c.setFramePosition(0); }
        });
    }
}