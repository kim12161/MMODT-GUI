package Weapon;
import java.util.*;

public class WeaponInventory {
    private ArrayList<Weapon> inventory = new ArrayList<>();
    private static final Random random = new Random();
    private static final int MAX_WEAPONS = 3;

    // SCAVENGE RANDOM WEAPON
    public static Weapon getRandomWeapon() {
        int weaponType = random.nextInt(5);
        switch (weaponType) {
            case 0: return new BaseballBat();
            case 1: return new KitchenKnife();
            case 2: return new Crowbar();
            case 3: return new WaterBottle();
            case 4: return new WoodenPlank();
            default: return new WoodenPlank();
        }
    }

    // ADD WEAPON TO INVENTORY
    public void addWeapon(Weapon weapon) {
        if (inventory.size() < MAX_WEAPONS) {
            inventory.add(weapon);
        } else {
            showInventory();

            Scanner sc = new Scanner(System.in);
            int choice = sc.nextInt() - 1;

            if (choice >= 0 && choice < inventory.size()) {
                inventory.set(choice, weapon);
            } else {
                System.out.println("Invalid choice! " + weapon.getName() + " was not added.");
            }
        }
    }

    // SHOW INVENTORY
    public void showInventory() {
        if (inventory.isEmpty()) {
            return;
        }
        for (int i = 0; i < inventory.size(); i++) {
            Weapon w = inventory.get(i);
            System.out.println((i + 1) + ". " + w.getName()
                    + " | Damage: " + w.getDamage()
                    + " | Hit Chance: " + w.getDamageSuccess() + "%"
                    + " | Durability: " + w.getDurability() + "/" + w.getMaxDurability()
                    + " | " + w.getDescription());
        }
    }

    // USE WEAPON
    public boolean useWeapon(int index) {
        if (index >= 0 && index < inventory.size()) {
            return inventory.get(index).use();
        } else {
            return false;
        }
    }

    // REPLACE WEAPON AT INDEX — called by discard UI
    public void replaceWeapon(int index, Weapon newWeapon) {
        if (index >= 0 && index < inventory.size()) {
            inventory.set(index, newWeapon);
        }
    }


    // GETTERS
    public ArrayList<Weapon> getInventory() { return inventory; }
    public int getSize() { return inventory.size(); }

}
