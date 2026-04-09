package Player;

import java.util.*;
import Characters.Character;
import RelationshipSystem.Relationship;
import Weapon.WeaponInventory;
import Weapon.Weapon;

public class Player {
    private String name;
    private int health;
    private int charisma;
    private Gender gender;
    private Map<Character, Relationship> relationships;
    private WeaponInventory weaponInventory;

    // --- NEW: Consumable Inventory ---
    private Map<String, Integer> consumableInventory;
    private static final int MEDKIT_HEAL = 25;
    private static final int BANDAGE_HEAL = 15;
    // ---------------------------------

    // ===== CONSTRUCTOR =====
    public Player(String name, int health, Gender gender) {
        this.name = name;
        this.health = health;
        this.charisma = 0;
        this.gender = gender;
        this.relationships = new HashMap<>();
        this.weaponInventory = new WeaponInventory();

        // --- NEW: Initialize consumable inventory ---
        this.consumableInventory = new HashMap<>();
        // ------------------------------------------
    }

    // ===== RELATIONSHIPS =====
    public void increaseTrust(Character character, int points) {
        getRelationship(character).increaseTrust(points);
    }

    public void increaseTurnOn(Character character, int points) {
        getRelationship(character).increaseTurnOn(points);
    }

    public void increaseTurnOff(Character character, int points) {
        getRelationship(character).increaseTurnOff(points);
    }

    public void increaseCharisma(int points) {
        this.charisma += points;
    }

    public Relationship getRelationship(Character character) {
        relationships.putIfAbsent(character, new Relationship());
        return relationships.get(character);
    }

    // ===== HEALTH =====
    public void takeDamage(int damage) {
        this.health -= damage;
    }

    public void heal(int amount) {
        this.health = Math.min(100, this.health + amount);
    }

    public boolean isAlive() {
        return this.health > 0;
    }

    // ===== WEAPON INVENTORY =====
    public WeaponInventory getWeaponInventory() {
        return weaponInventory;
    }

    public void addWeapon(Weapon weapon) {
        weaponInventory.addWeapon(weapon);
    }


    public void addConsumable(String itemName) {
        consumableInventory.put(itemName, consumableInventory.getOrDefault(itemName, 0) + 1);
   /*     System.out.println("You stored the " + itemName + " in your bag!");
        System.out.println(); */
    }


    public boolean hasConsumables() {
        for (int count : consumableInventory.values()) {
            if (count > 0) {
                return true;
            }
        }
        return false;
    }


    public List<String> showConsumableInventory() {
        System.out.println();
        System.out.println("--- Healing Items ---");
        List<String> itemList = new ArrayList<>();
        int index = 1;
        if (!hasConsumables()) {
            System.out.println("You have no healing items.");
            return itemList;
        }

        for (Map.Entry<String, Integer> entry : consumableInventory.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println("[" + index + "] " + entry.getKey() + " x" + entry.getValue());
                itemList.add(entry.getKey());
                index++;
            }
        }
        return itemList;
    }


    public boolean useConsumable(String itemName) {
        Integer count = consumableInventory.get(itemName);

        if (count == null || count == 0) {
            System.out.println("You don't have any " + itemName + ".");
            return false;
        }

        if (health == 100) {
            System.out.println("Your health is already full!");
            return false;
        }

        int healAmount = 0;
        if (itemName.equals("Medkit")) {
            healAmount = MEDKIT_HEAL;
        } else if (itemName.equals("Bandage")) {
            healAmount = BANDAGE_HEAL;
        }

        if (healAmount > 0) {
            heal(healAmount);
            consumableInventory.put(itemName, count - 1);
            System.out.println();
            System.out.println("You used a " + itemName + ". Healed " + healAmount + " HP.");
            System.out.println("Current Health: " + health + "/100");
            return true;
        }
        return false;
    }

    // ===== GETTERS =====
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getCharisma() { return charisma; }
    public Gender getGender() { return gender; }
    public Map<Character, Relationship> getRelationships() { return relationships; }

    // ===== SETTERS =====
    public void setHealth(int health) {
        this.health = health;
    }
}