package Weapon;

import java.util.Random;
import java.io.Serializable;

public abstract class Weapon implements Serializable {
    private String name;
    private int damageSuccess;
    private int durability;
    private int maxDurability;
    private String description;

    public Weapon(String name, int damageSuccess, int durability, String description) {
        this.name = name;
        this.damageSuccess = damageSuccess;
        this.durability = durability;
        this.maxDurability = durability;
        this.description = description;
    }
    public void setDurability(int durability) { this.durability = durability; }

    // Each weapon subclass defines its own base damage
    public abstract int getDamage();

    // Handles hit chance and durability reduction
    public boolean use() {
        if (isBroken()) {
            System.out.println(name + " is broken!");
            return false;
        }

        Random random = new Random();
        boolean success = random.nextInt(100) < damageSuccess;
        durability--;

        if (durability <= 0) {
            System.out.println(name + " broke!");
        }

        return success;
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    // Getters
    public String getName() { return name; }
    public int getDamageSuccess() { return damageSuccess; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public String getDescription() { return description; }
}
