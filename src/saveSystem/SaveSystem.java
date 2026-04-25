package saveSystem;

import Characters.Character;
import Player.Player;
import RelationshipSystem.Relationship;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SaveSystem — handles saving and loading game progress to disk.
 * Save files are stored in: saves/slot_N.dat
 */
public class SaveSystem {

    public static final int MAX_SLOTS = 3;
    private static final String SAVE_DIR = "saves";
    private static final String SLOT_PREFIX = "slot_";
    private static final String SLOT_EXT = ".dat";
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy  hh:mm a");

    // ==============================
    // SAVE DATA CONTAINER
    // ==============================
    public static class SaveData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public String playerName;
        public int    playerHealth;
        public int    playerMaxHealth;
        public int    playerCharisma;
        public List<String> consumables;

        public int currentLevel;
        public String timestamp;
        public String levelName;

        // Relationship data: charName → [trust, turnOn, turnOff]
        public Map<String, int[]> relationships = new HashMap<>();

        @Override
        public String toString() {
            return playerName + " | LVL " + currentLevel + " — " + levelName + "\n" + timestamp;
        }
    }

    // ==============================
    // ENSURE SAVE DIR EXISTS
    // ==============================
    private static void ensureSaveDir() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private static File slotFile(int slot) {
        return new File(SAVE_DIR + File.separator + SLOT_PREFIX + slot + SLOT_EXT);
    }

    // ==============================
    // SAVE
    // ==============================
    public static boolean save(int slot, Player player, List<Character> characters, int currentLevel, String levelName) {
        ensureSaveDir();

        SaveData data = new SaveData();
        data.playerName    = player.getName();
        data.playerHealth  = player.getHealth();
        data.playerMaxHealth = player.getMaxHealth();
        data.playerCharisma = player.getCharisma();
        data.consumables   = new ArrayList<>(player.getConsumables());
        data.currentLevel  = currentLevel;
        data.levelName     = levelName;
        data.timestamp     = LocalDateTime.now().format(TIME_FMT);

        for (Character c : characters) {
            Relationship r = player.getRelationship(c);
            data.relationships.put(c.getName(), new int[]{
                    r.getTrust(), r.getTurnOn(), r.getTurnOff()
            });
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(slotFile(slot)))) {
            oos.writeObject(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==============================
    // LOAD
    // ==============================
    public static SaveData load(int slot) {
        File f = slotFile(slot);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (SaveData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==============================
    // CHECK IF SLOT IS USED
    // ==============================
    public static boolean slotExists(int slot) {
        return slotFile(slot).exists();
    }

    // ==============================
    // DELETE A SLOT
    // ==============================
    public static boolean deleteSlot(int slot) {
        return slotFile(slot).delete();
    }

    // ==============================
    // LOAD ALL SLOT METADATA (for display)
    // ==============================
    public static SaveData[] loadAllSlots() {
        SaveData[] slots = new SaveData[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = load(i + 1);
        }
        return slots;
    }

    // ==============================
    // FIND MOST RECENT SAVE (for "Continue")
    // ==============================
    public static SaveData getMostRecentSave() {
        SaveData best = null;
        long bestTime = -1;
        for (int i = 1; i <= MAX_SLOTS; i++) {
            File f = slotFile(i);
            if (f.exists() && f.lastModified() > bestTime) {
                bestTime = f.lastModified();
                best = load(i);
            }
        }
        return best;
    }

    // ==============================
    // ANY SAVE EXISTS? (for title screen Continue button)
    // ==============================
    public static boolean anySaveExists() {
        for (int i = 1; i <= MAX_SLOTS; i++) {
            if (slotExists(i)) return true;
        }
        return false;
    }
}