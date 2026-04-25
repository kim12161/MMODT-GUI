package saveSystem;

import Characters.Character;
import Player.Player;
import RelationshipSystem.Relationship;

import java.io.*;
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
        public int    playerHealth;   // health out of 100 (Player max is always 100)
        public int    playerCharisma;

        // consumableInventory mirrors Player's Map<String, Integer>
        // e.g. { "Medkit" -> 2, "Bandage" -> 1 }
        public Map<String, Integer> consumableInventory;

        public int    currentLevel;
        public String levelName;
        public String timestamp;

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
    public static boolean save(int slot, Player player, List<Character> characters,
                               int currentLevel, String levelName) {
        ensureSaveDir();

        SaveData data = new SaveData();
        data.playerName    = player.getName();
        data.playerHealth  = player.getHealth();       // getHealth() exists
        data.playerCharisma = player.getCharisma();    // getCharisma() exists
        data.currentLevel  = currentLevel;
        data.levelName     = levelName;
        data.timestamp     = LocalDateTime.now().format(TIME_FMT);

        // showConsumableInventory() returns a List<String> of item names that
        // still have count > 0, but we need counts too.
        // We rebuild from the player's own show method — simplest approach is
        // to ask the player for its inventory via the existing public method,
        // then count each name.  However, to keep it clean we store only the
        // name list (quantities are lost on reload — see note in LOAD below).
        // If you later add getConsumableInventory() to Player this can be
        // upgraded to preserve counts.
        List<String> itemNames = player.showConsumableInventory(); // already public
        Map<String, Integer> inventoryCopy = new HashMap<>();
        for (String name : itemNames) {
            inventoryCopy.merge(name, 1, Integer::sum);
        }
        data.consumableInventory = inventoryCopy;

        // relationships
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
    // RESTORE PLAYER FROM SAVE DATA
    // ==============================
    /**
     * Applies a SaveData snapshot back onto a freshly constructed Player.
     * Call this after new Player(save.playerName, 100, gender) in your
     * title screen Continue handler.
     *
     * Restores: health, charisma, consumable inventory, relationships.
     */
    public static void restorePlayer(Player player, SaveData data,
                                     List<Character> characters) {
        // health (Player.setHealth() exists)
        player.setHealth(data.playerHealth);

        // charisma (increaseCharisma exists; start from 0)
        player.increaseCharisma(data.playerCharisma);

        // consumables — addConsumable(name) increments count by 1 each call
        if (data.consumableInventory != null) {
            for (Map.Entry<String, Integer> entry : data.consumableInventory.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    player.addConsumable(entry.getKey());
                }
            }
        }

        // relationships
        for (Character c : characters) {
            int[] rel = data.relationships.get(c.getName());
            if (rel != null) {
                player.increaseTrust(c, rel[0]);
                player.increaseTurnOn(c, rel[1]);
                player.increaseTurnOff(c, rel[2]);
            }
        }
    }

    // ==============================
    // CHECK / DELETE SLOTS
    // ==============================
    public static boolean slotExists(int slot) { return slotFile(slot).exists(); }
    public static boolean deleteSlot(int slot)  { return slotFile(slot).delete(); }

    // ==============================
    // LOAD ALL SLOTS (for SaveSlotPanel display)
    // ==============================
    public static SaveData[] loadAllSlots() {
        SaveData[] slots = new SaveData[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) slots[i] = load(i + 1);
        return slots;
    }

    // ==============================
    // MOST RECENT SAVE (for title screen Continue)
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
    // ANY SAVE EXISTS? (enable Continue button on title screen)
    // ==============================
    public static boolean anySaveExists() {
        for (int i = 1; i <= MAX_SLOTS; i++) if (slotExists(i)) return true;
        return false;
    }
}