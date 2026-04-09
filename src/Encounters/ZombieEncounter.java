package Encounters;

import java.util.*;
import Player.Player;
import Weapon.*;

public class ZombieEncounter {
    public static final String RED    = "\u001B[31m";
    private static final Random random = new Random();
    private static final int MAX_HEALTH = 100;
    private static final double DODGE_SUCCESS_RATE = 0.6;

    // Combat Choices!
    public static void displayChoices(int level, Player player, WeaponInventory weaponInventory, int zombieHP) {
        System.out.println("\nA zombie emerged from the shadows!");
        System.out.println("Zombie HP: " + zombieHP);
        System.out.println("What do you want to do? (Health: " + player.getHealth() + ")");
        System.out.println("[1] Dodge");
        System.out.println("[2] Fight (Fists)");
        System.out.println("[3] Open Inventory");
    }

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
     //               System.out.println("Invalid weapon choice. The zombie lunges at you!");
                    player.takeDamage(randomDamage(level));
                }
                break;
            default:
                // This will catch "INVALID"
 //               System.out.println("Invalid choice. The zombie attacks while you hesitate!");
                player.takeDamage(randomDamage(level));
        }

        System.out.println("Zombie HP: " + Math.max(0, newZombieHP));
        System.out.println("Player HP: " + Math.max(0, player.getHealth()));

        return newZombieHP;
    }

    private static int handleFight(int level, int zombieHP, Player player, WeaponInventory inventory, int weaponIndex) {
        int newZombieHP = zombieHP;


        if (weaponIndex == -1) {
            int fistDamage = random.nextInt(31) + 10;
            System.out.println();
        //    System.out.println("You punch the zombie! Dealt " + fistDamage + " damage.");
            newZombieHP -= fistDamage;
        } else {
            Weapon weapon = inventory.getInventory().get(weaponIndex);
            if (inventory.useWeapon(weaponIndex)) {
                newZombieHP -= weapon.getDamage();
       //         System.out.println("" + weapon.getName() + " hits! Dealt " + weapon.getDamage() + " damage.");
            } else {
        //        System.out.println("The " + weapon.getName() + " broke mid-fight!");
                player.takeDamage(randomDamage(level));
                return newZombieHP;
            }
        }

        if (newZombieHP > 0) {
            int damageTaken = randomDamage(level);
       //     System.out.println("The zombie strikes back! You took " + damageTaken + " damage!");
            System.out.println();
            player.takeDamage(damageTaken);
        } /*else {
            System.out.println("The zombie collapses, defeated!");
        } */

        return newZombieHP;
    }


    // HANDLES DODGE DAMAGE //
    private static int handleDodgeDamage(int currentZombieHP, Player player) {
        if (random.nextDouble() < DODGE_SUCCESS_RATE) {
            int updatedZombieHP = currentZombieHP;
            int rollDmg = random.nextInt(21) + 10;

        /*    System.out.println("\n>> You perform a dodge roll!");
            System.out.println(">> The Zombie lunges at you but MISSES completely!");
            System.out.println(">> The Zombie is off-balance! You have 2 FREE turns to attack!"); */

            // Executes the 2 free turns immediately
            for (int i = 1; i <= 2; i++) {
                System.out.println("\n--- Counter - Attack Turn " + i + " ---");

                System.out.println("You struck the zombie for " + rollDmg + " damage!");

                updatedZombieHP -= rollDmg;
            }

            return Math.max(0, updatedZombieHP);

        } else {

            // FAILED DODGE //
            int damage = random.nextInt(16) + 5;
            System.out.println("\n>> You tried to dodge, but you stumbled!");
            System.out.println(">> The Zombie caught you mid-roll!");
            System.out.println(">> You took " + damage + " damage.");

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