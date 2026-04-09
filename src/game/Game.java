package game;

import Characters.Adi;
import Characters.Avy;
import Characters.Character;
import Characters.Kim;
import Characters.Marina;
import Characters.Nathan;
import Characters.Yubie;
import Encounters.ZombieEncounter;
import Player.Gender;
import Player.Player;
import RelationshipSystem.Relationship;
import Weapon.WeaponInventory;
import java.util.*;

public class Game {
    /*private Player player;
    private List<Character> availableCharacters;
    private ConversationManager conversationManager;
    private Scanner scanner;
    private boolean gameRunning;

    // Executable in the MMOTD.java //
    public Game() {
        this.scanner = new Scanner(System.in);
        this.conversationManager = new ConversationManager();
        this.gameRunning = true;
        initializeCharacters();
    }

    // Initializing of Characters //
    private void initializeCharacters() {
        availableCharacters = new ArrayList<>();
        availableCharacters.add(new Avy());
        availableCharacters.add(new Marina());
        availableCharacters.add(new Kim());
        availableCharacters.add(new Nathan());
        availableCharacters.add(new Yubie());
        availableCharacters.add(new Adi());
    }

    private void startLevelConfirmation() {
        System.out.print("\nPress (Y/N) - Y if you're ready or N if not: ");
        String input;
        while (true) {
            input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y") || input.equals("N")) break;
            System.out.println("Invalid choice.");
            System.out.print("Please choose Y or N:");
        }
        if (input.equals("N")) {
            System.out.println("The world awaits for no one...");
            gameRunning = false;
            System.exit(0);
        }
    }
    private void playLevelTemplate(int level, String title) {
        System.out.println("\nLevel " + level + " - " + title);
        for (int conversationNum = 1; conversationNum <= 3; conversationNum++) {
            if (conversationNum == 1) itemDiscoveryEvent();
            for (Character character : availableCharacters) {
                if (!gameRunning) break;
                conversationManager.startConversation(player, character, level, conversationNum);
            }
            if (!gameRunning) break;
            if (conversationNum == 3) {
                zombieEncounter(level);
            }
        }
    }
    private void itemDiscoveryEvent() {
        Random random = new Random();
        System.out.println("\n\"You checked every corner and found an item!\"");
        if (random.nextBoolean()) {
            player.addConsumable("Medkit");
        } else {
            player.addConsumable("Bandage");
        }
    }

    // ===== ZOMBIE ENCOUNTER  ===== //
    private void zombieEncounter(int level) {
        int zombieHp = 50 + (level * 10);

        WeaponInventory weaponInventory = player.getWeaponInventory();

        while (player.isAlive() && zombieHp > 0) {
            ZombieEncounter.displayChoices(level, player, weaponInventory, zombieHp);
            System.out.print("Choose an action: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("1")) { // Dodge
                zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, weaponInventory, "1", -1);
            } else if (choice.equals("2")) { // Fists (sumbagon taka ron)
                zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, weaponInventory, "2", -1);
            } else if (choice.equals("3")) { // Open Inventory
                // This new shit returns the updated zombieHp after the turn
                zombieHp = openCombatInventory(level, zombieHp);
            } else { // Invalid choice
                zombieHp = ZombieEncounter.processTurn(level, zombieHp, player, weaponInventory, "INVALID", -1);
            }

            if (player.getHealth() <= 0) {
                System.out.println("You were devoured by zombies!");
                System.out.println("\nGame Over!");
                gameRunning = false;
                break;
            }
        }

        if (zombieHp <= 0 && gameRunning) {
            System.out.println();
            System.out.println("You survived the zombie encounter!");
            player.heal(10);
            Weapon.Weapon found = WeaponInventory.getRandomWeapon();
            System.out.println("You found a " + found.getName() + "!");
            player.getWeaponInventory().addWeapon(found);
        }
    }
    private int openCombatInventory(int level, int zombieHp) {
        System.out.println("\n--- INVENTORY ---");
        boolean hasWeapons = player.getWeaponInventory().getSize() > 0;
        boolean hasItems = player.hasConsumables();

        if (hasWeapons) {
            System.out.println("[1] Use Weapon");
        }
        if (hasItems) {
            System.out.println("[2] Use Healing Item");
        }
        System.out.println("[0] Cancel");
        System.out.print("Choose: ");

        String choice = scanner.nextLine().trim();


        // WEAPON VALIDATOR - //
        switch (choice) {
            case "1":
                if (hasWeapons) {
                    return handleWeaponTurn(level, zombieHp);
                } else {
                    System.out.println("Invalid choice.");
                    return zombieHp; // no turn lost
                }
            case "2":
                if (hasItems) {
                    boolean itemUsed = handleHealingTurn();
                    return zombieHp; // Return original zombie HP (healing doesn't damage it)
                } else {
                    System.out.println("Invalid choice.");
                    return zombieHp; // No change, no turn lost
                }
            case "0":
                System.out.println("You close your bag.");
                return zombieHp; // No change, no turn lost
            default:
                System.out.println("Invalid choice.");
                return zombieHp; // No change, no turn lost
        }
    }


    // Weapon Validator //
    private int handleWeaponTurn(int level, int zombieHp) {
        WeaponInventory weaponInventory = player.getWeaponInventory();


        if (weaponInventory.getSize() == 0) {
            System.out.println("You have no weapons!");
            return zombieHp; // No turn lost
        }

        weaponInventory.showInventory();
        System.out.print("Select weapon index (or 0 to cancel): ");
        try {
            int selection = Integer.parseInt(scanner.nextLine());
            if (selection == 0) {
                System.out.println("You decide to wait...");
                return zombieHp; // Canceled, no turn lost
            }

            int weaponIndex = selection - 1; // Convert 1-based to 0-based

            // Check if selection is valid
            if (weaponIndex >= 0 && weaponIndex < weaponInventory.getSize()) {
                // This is a valid attack, so we call processTurn
                return ZombieEncounter.processTurn(level, zombieHp, player, weaponInventory, "3", weaponIndex);
            } else {
                System.out.println("Invalid weapon selection.");
                // We call processTurn with "INVALID" to penalize the turn
                return ZombieEncounter.processTurn(level, zombieHp, player, weaponInventory, "INVALID", -1);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            // We call processTurn with "INVALID" to penalize the turn
            return ZombieEncounter.processTurn(level, zombieHp, player, weaponInventory, "INVALID", -1);
        }
    }

    // Headling Consumable System //
    private boolean handleHealingTurn() {
        List<String> itemList = player.showConsumableInventory();
        if (itemList.isEmpty()) {
            System.out.println("You have no items to use!");
            return false;
        }

        System.out.print("Select item (1-" + itemList.size() + ") or 0 to cancel: ");
        try {
            int itemChoice = Integer.parseInt(scanner.nextLine());
            if (itemChoice == 0) {
                System.out.println("Canceled item use.");
                return false; // Canceled, did not use a turn
            }

            if (itemChoice > 0 && itemChoice <= itemList.size()) {
                String itemToUse = itemList.get(itemChoice - 1);
                return player.useConsumable(itemToUse); // Returns true if successful
            } else {
                System.out.println("Invalid selection.");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return false;
        }
    }*/

}

