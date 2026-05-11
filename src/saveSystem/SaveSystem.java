package saveSystem;

import Characters.Character;
import Player.Gender;
import Player.Player;
import RelationshipSystem.Relationship;
import Weapon.Weapon;
import Weapon.WeaponInventory;

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
        private static final long serialVersionUID = 2L; // bumped because we added a field

        public String playerName;
        public int    playerHealth;
        public int    playerCharisma;
        public Gender playerGender;   // ← NEW: saves the gender the player chose

        public Map<String, Integer> consumableInventory;

        public int    currentLevel;
        public int    currentConversation; // 1, 2, or 3
        public String levelName;
        public String timestamp;

        // Relationship data: charName → [trust, turnOn, turnOff]
        public Map<String, int[]> relationships = new HashMap<>();

        // ← Weapons
        public List<String>  weaponNames        = new ArrayList<>();
        public List<Integer> weaponDurabilities = new ArrayList<>();

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
                               int currentLevel, int currentConversation, String levelName) {
        ensureSaveDir();

        SaveData data = new SaveData();
        data.playerName    = player.getName();
        data.playerHealth  = player.getHealth();
        data.playerCharisma = player.getCharisma();
        data.playerGender  = player.getGender();
        data.currentLevel  = currentLevel;
        data.currentConversation = currentConversation;
        data.levelName     = levelName;
        data.timestamp     = LocalDateTime.now().format(TIME_FMT);

        // Save consumables directly from the player's map via getConsumableInventory()
        data.consumableInventory = new HashMap<>(player.getConsumableInventory());

        for (Character c : characters) {
            Relationship r = player.getRelationship(c);
            data.relationships.put(c.getName(), new int[]{
                    r.getTrust(), r.getTurnOn(), r.getTurnOff()
            });
        }
        for (Weapon w : player.getWeaponInventory().getInventory()) {
            data.weaponNames.add(w.getName());
            data.weaponDurabilities.add(w.getDurability());
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
    public static void restorePlayer(Player player, SaveData data,
                                     List<Character> characters) {
        player.setHealth(data.playerHealth);
        player.increaseCharisma(data.playerCharisma);

        // Restore consumables directly — exact counts preserved
        if (data.consumableInventory != null) {
            for (Map.Entry<String, Integer> entry : data.consumableInventory.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    player.addConsumable(entry.getKey());
                }
            }
        }
        // Save weapon
        player.getWeaponInventory().clear();
        if (data.weaponNames != null) {
            for (int i = 0; i < data.weaponNames.size(); i++) {
                Weapon w = WeaponInventory.createByName(data.weaponNames.get(i));
                if (w != null) {
                    w.setDurability(data.weaponDurabilities.get(i));
                    player.getWeaponInventory().getInventory().add(w);
                }
            }
        }

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
    // LOAD ALL SLOTS (for ContinuePanel display)
    // ==============================
    public static SaveData[] loadAllSlots() {
        SaveData[] slots = new SaveData[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) slots[i] = load(i + 1);
        return slots;
    }
/*
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
    } */
}