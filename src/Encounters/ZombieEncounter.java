package Encounters;

import java.util.*;
import Player.Player;
import Weapon.*;

public class ZombieEncounter {
    private static final Random random = new Random();
    private static final double DODGE_SUCCESS_RATE = 0.6;

    public static int processTurn(int level, int zombieHp, Player player, WeaponInventory inventory, String userChoice, int weaponIndex) {
        int newZombieHP = zombieHp;

        switch (userChoice) {
            case "1":
                newZombieHP = handleDodgeDamage(newZombieHP, player);
                break;
            case "2":
                newZombieHP = handleFight(level, newZombieHP, player, inventory, -1); // -1 for fist fight
                break;
            case "3":
                if (inventory.getSize() > 0 && weaponIndex >= 0 && weaponIndex < inventory.getSize()) {
                    newZombieHP = handleFight(level, newZombieHP, player, inventory, weaponIndex);
                } else {
                    player.takeDamage(randomDamage(level));
                }
                break;

            case "4":
                //broken weapon
                player.takeDamage(randomDamage(level));
                break;
            default:
                player.takeDamage(randomDamage(level));
        }

        return newZombieHP;
    }

    private static int handleFight(int level, int zombieHP, Player player, WeaponInventory inventory, int weaponIndex) {
        int newZombieHP = zombieHP;


        if (weaponIndex == -1) {
            int fistDamage = random.nextInt(31) + 10;
            System.out.println();
            newZombieHP -= fistDamage;
        } else {
            Weapon weapon = inventory.getInventory().get(weaponIndex);
            if (inventory.useWeapon(weaponIndex)) {
                newZombieHP -= weapon.getDamage();
            }
        }

        newZombieHP = Math.max(0, newZombieHP);

        if (newZombieHP > 0 && random.nextDouble() < 0.90) {
            int damageTaken = randomDamage(level);
            player.takeDamage(damageTaken);
        }

        return newZombieHP;
    }

    private static int handleDodgeDamage(int currentZombieHP, Player player) {
        if (random.nextDouble() < DODGE_SUCCESS_RATE) {
            int updatedZombieHP = currentZombieHP;
            int rollDmg = random.nextInt(21) + 10;

            // Executes the 2 free turns immediately
            for (int i = 1; i <= 2; i++) {

                updatedZombieHP -= rollDmg;
            }

            return Math.max(0, updatedZombieHP);

        } else {

            // FAILED DODGE //
            int damage = random.nextInt(16) + 5;

            player.takeDamage(damage);

            return currentZombieHP; // Return the damage to subtract from player health
        }
    }
    public static int randomDamage(int level) {
        int attackDamage = random.nextInt(11) + 0;
        attackDamage = (level * 5) + attackDamage;

        return attackDamage;
    }
}