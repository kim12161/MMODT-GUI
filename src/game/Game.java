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
    private Player player;
    private List<Character> availableCharacters;
    private ConversationManager conversationManager;
    private Scanner scanner;
    private boolean gameRunning;

    // COLOR CODING //
    public static final String RESET  = "\u001B[0m";
    public static final String RED    = "\u001B[31m";
    public static final String PINK   = "\u001B[38;2;255;105;180m";
    public static final String PURPLE = "\u001B[35m";

    private static void typewriter(String text, int delayMillis) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
    }

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
    public void startGame() {
        displayWelcome();
        setupPlayer();
        displayCast();
        startLevelConfirmation();
        if (gameRunning) { playLevelTemplate(1, "Abandoned Compound"); }
        if (gameRunning) { playLevelTemplate(2, "Temporary Shelter"); }
        if (gameRunning) { playLevelTemplate(3, "City Ruins"); }
        if (gameRunning) { playLevelTemplate(4, "Safehouse Conflict"); }
        if (gameRunning) { playLevelTemplate(5, "Escape Route"); }
        if (player.isAlive()) { endGame(); }
    }
    private void displayWelcome() {
        String blood = "\u001B[38;2;138;3;3m";
        String reset = "\u001B[0m";
        String lightPink = "\u001B[38;2;255;182;193m";

        System.out.println();
        String art = """ 
 
`.            `.   `.`..                     `.`              `             _ _   `    `
`    `.``              .``.   `.`             `..    `.        .`        . (_\\_)   . ` .
.`       .`.                                   `     ``.`         .     ` (__<__)  . . ` 
 . o`.      `.                                     .`        `.  `.        (_/_)  .   . 
` .| .███    ███  █████  ██████  ██████  ██    ██     ███    ███ ███████   |\\ |     ` .                              
. .| `████  ████ ██   ██ ██   ██ ██   ██  ██  ██  `.  ████  ████ ██         \\\\| /| ` 
` .| `██ ████ ██ ███████ ██████  ██████    ████       ██ ████ ██ █████  `.   \\|//  . 
. `| .██  ██  ██ ██   ██ ██   ██ ██   ██    ██  ``.   ██  ██  ██ ██           |/ `  . 
. ` .`██      ██ ██   ██ ██   ██ ██   ██    ██    `.. ██   `. ██ ███████ ,.,.,|.,.,. """;
        printCentered(art, lightPink);
        System.out.println();
        String art1 = """   
▄████▄ █████▄  
██  ██ ██▄▄██▄ 
▀████▀ ██   ██ """;
        printCentered(art1, reset);
        System.out.println();

        String art2 = """
        ▓█████▄  ██▓▓█████    ▄▄▄█████▓ ██▀███ ▓██   ██▓ ██▓ ███▄    █   ▄████ 
        ▒██▀ ██▌▓██▒▓█   ▀    ▓  ██▒ ▓▒▓██ ▒ ██▒▒██  ██▒▓██▒ ██ ▀█   █  ██▒ ▀█▒
        ░██   █▌▒██▒▒███      ▒ ▓██░ ▒░▓██ ░▄█ ▒ ▒██ ██░▒██▒▓██  ▀█ ██▒▒██░▄▄▄░
        ░▓█▄   ▌░██░▒▓█  ▄    ░ ▓██▓ ░ ▒██▀▀█▄   ░ ▐██▓░░██░▓██▒  ▐▌██▒░▓█  ██▓
        ░▒████▓ ░██░░▒████▒     ▒██▒ ░ ░██▓ ▒██▒ ░ ██▒▓░░██░▒██░   ▓██░░▒▓███▀▒
         ▒▒▓  ▒ ░▓  ░░ ▒░ ░     ▒ ░░   ░ ▒▓ ░▒▓░  ██▒▒▒ ░▓  ░ ▒░   ▒ ▒  ░▒   ▒ 
         ░ ▒  ▒  ▒ ░ ░ ░  ░       ░      ░▒ ░ ▒░▓██ ░▒░  ▒ ░░ ░░   ░ ▒░  ░   ░ 
         ░ ░  ░  ▒ ░   ░        ░        ░░   ░ ▒ ▒ ░░   ▒ ░   ░   ░ ░ ░ ░   ░ 
           ░     ░     ░  ░               ░     ░ ░      ░           ░       ░ 
         ░                                      ░ ░                            
        """;
        printCentered(art2, blood);

        String input;
        boolean skip = false;
        do {
            System.out.print(">| Press ENTER to start or 'S' to skip story animation: ");
            input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("S")) {
                skip = true;
                break;
            }
        } while (!input.isEmpty());
        System.out.println();
        // STORY INTRODUCTION //
        displayLine("You are 28 years old, two years away from the big" + RED + " 3-0" + RESET +
                ", and by all accounts, you have been living the good life.", 30, skip);
        displayLine("A stable career, your own cozy apartment, financial freedom and everything you once dreamed of, you achieved.", 45, skip);
        displayLine("But at your college reunion,", 65, skip);
        displayLine("reality hit differently...", 150, skip);
        displayLine("Everyone showed up with partners; some even announcing engagements or babies.", 90, skip);
        displayLine("Surrounded by talks of weddings and settling down,", 70, skip);
        displayLine("you realized something:", 150, skip);
        displayLine("You had built the perfect life,", 100, skip);
        displayLine("but" + RED + " never found love." + RESET, 150, skip);
        System.out.println();
        displayLine("That night, you decided to add one last item to your bucket list:", 70, skip);
        displayLine(RED + "Find love before 30. Maybe even get married." + RESET, 100, skip);
        displayLine("Except, fate had other plans.", 80, skip);
        displayLine("The very next week, the world " + RED + "Spira" + RESET + " collapsed into chaos. A mysterious infection spread across the city, turning people into ravenous monsters. Society crumbled, survival became the priority... yet, in the middle of it all, your bucket list remained the same.", 80, skip);
        displayLine("Sure, the apocalypse has begun. But you?", 150, skip);
        System.out.println();
        displayLine("You're determined to" + RED + " find a partner before the world ends." + RESET + " Because love might be the thing worth surviving for.", 110, skip);
        System.out.println();
        displayLine("This is where your story begins.", 70, skip);
        System.out.println();
        System.out.print("Press ENTER to start your journey...");
        scanner.nextLine();
    }


    // --------------- CENTERED TITLE --------------//
    private void printCentered(String art, String colorCode) {
        String reset = "\u001B[0m";
        int consoleWidth = 120;

        String[] lines = art.split("\n");


        int artWidth = 0;
        for (String line : lines) {
            if (line.length() > artWidth) {
                artWidth = line.length();
            }
        }


        int padding = (consoleWidth - artWidth) / 2;
        String spaces = " ".repeat(Math.max(0, padding)); //

        System.out.print(colorCode);
        for (String line : lines) {
            System.out.println(spaces + line);
        }
        System.out.print(reset);
    }
    // ---------------------------------------------- //


    private void displayLine(String text, int delay, boolean skip) {
        if (skip) { System.out.println(text); } else { typewriter(text, delay); }
    }

    // GENDER SELECTION //
    private void setupPlayer() {
        System.out.print("\nChoose your gender Male(M) or Female(F): ");
        String genderInput;
        while (true) {
            genderInput = scanner.nextLine().trim().toUpperCase();
            if (genderInput.equals("M") || genderInput.equals("F")) break;
            System.out.print("Invalid choice.");
            System.out.print("\nChoose your gender Male(M) or Female(F): ");
        }
        System.out.print("Enter your character name: ");
        String name = scanner.nextLine();
        Gender playerGender = genderInput.equals("M") ? Gender.MALE : Gender.FEMALE;
        this.player = new Player(name, 100, playerGender);
        typewriter("\nWelcome, " + name + "!", 60);
    }

    // CHARACTERS INTRO
    private void displayCast() {
        System.out.println("");
        typewriter("\n==== MEET THE CHARACTERS ====", 40);
        List<Character> romanceableCharacters = new ArrayList<>();
        for (Character character : availableCharacters) {
            if (character.getGender() != player.getGender()) {
                romanceableCharacters.add(character);
                System.out.println();
                System.out.println("---- " + character.getName() + " ---- ");
                typewriter("* " + character.getRole(), 20);
                typewriter("* " + character.getFlaws(), 20);
                typewriter("* " + character.getPersonality(), 20);
                typewriter("* " + character.getRomanceHook(), 20);
                typewriter("* " + character.getSurvivalSkills(), 20);
                System.out.println();
            }
        }
        availableCharacters = romanceableCharacters;
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
    }

    private void displayLoveEnding() {
        String blood = "\u001B[38;2;138;3;3m";
        String reset = "\u001B[0m";
        String lightPink = "\u001B[38;2;255;182;193m";
        System.out.println();

        String art = """
                                            .  * .    .    .  * .    .  * .    .    .  * .    .  * .
                                        .   * .    .    .   * .  .   * .    .    .   * .  .   * .
                                    .     ♡     .    .     ♡     .     ♡     .    .     ♡     .     ♡
                                        .   * .    .    .   * .  .   * .    .    .   * .  .   * .
                                        .  * .    .    .  * .    .  * .    .    .  * .    .  * .


 █████          ███████    █████   █████ ██████████    ██████████ ██████   █████ ██████████   █████ ██████   █████   █████████ 
░░███         ███░░░░░███ ░░███   ░░███ ░░███░░░░░█   ░░███░░░░░█░░██████ ░░███ ░░███░░░░███ ░░███ ░░██████ ░░███   ███░░░░░███
 ░███        ███     ░░███ ░███    ░███  ░███  █ ░     ░███  █ ░  ░███░███ ░███  ░███   ░░███ ░███  ░███░███ ░███  ███     ░░░ 
 ░███       ░███      ░███ ░███    ░███  ░██████       ░██████    ░███░░███░███  ░███    ░███ ░███  ░███░░███░███ ░███         
 ░███       ░███      ░███ ░░███   ███   ░███░░█       ░███░░█    ░███ ░░██████  ░███    ░███ ░███  ░███ ░░██████ ░███    █████
 ░███      █░░███     ███   ░░░█████░    ░███ ░   █    ░███ ░   █ ░███  ░░█████  ░███    ███  ░███  ░███  ░░█████ ░░███  ░░███ 
 ███████████ ░░░███████░      ░░███      ██████████    ██████████ █████  ░░█████ ██████████   █████ █████  ░░█████ ░░█████████ 
░░░░░░░░░░░    ░░░░░░░         ░░░      ░░░░░░░░░░    ░░░░░░░░░░ ░░░░░    ░░░░░ ░░░░░░░░░░   ░░░░░ ░░░░░    ░░░░░   ░░░░░░░░░  



                                    .  * .    .    .  * .    .  * .    .    .  * .    .  * .
                                    .   * .    .    .   * .  .   * .    .    .   * .  .   * .
                                .     ♡     .    .     ♡     .     ♡     .    .     ♡     .     ♡
                                    .   * .    .    .   * .  .   * .    .    .   * .  .   * .
                                    .  * .    .    .  * .    .  * .    .    .  * .    .  * .
    """;

        printCentered(art, lightPink);
        System.out.println();
    }

    private void displayBadEnding() {
        String blood = "\u001B[38;2;138;3;3m";
        System.out.println();

        String art = """
                ╔══════════════════════════════════════════════════════════════════════════════╗
                ║                                                                              ║
                ║                                                                              ║
                ║ ▄▄▄▄    ▄▄▄      ▓█████▄    ▓█████  ███▄    █ ▓█████▄  ██▓ ███▄    █   ▄████ ║
                ║▓█████▄ ▒████▄    ▒██▀ ██▌   ▓█   ▀  ██ ▀█   █ ▒██▀ ██▌▓██▒ ██ ▀█   █  ██▒ ▀█▒║
                ║▒██▒ ▄██▒██  ▀█▄  ░██   █▌   ▒███   ▓██  ▀█ ██▒░██   █▌▒██▒▓██  ▀█ ██▒▒██░▄▄▄░║
                ║▒██░█▀  ░██▄▄▄▄██ ░▓█▄   ▌   ▒▓█  ▄ ▓██▒  ▐▌██▒░▓█▄   ▌░██░▓██▒  ▐▌██▒░▓█  ██▓║
                ║░▓█  ▀█▓ ▓█   ▓██▒░▒████▓    ░▒████▒▒██░   ▓██░░▒████▓ ░██░▒██░   ▓██░░▒▓███▀▒║
                ║░▒▓███▀▒ ▒▒   ▓▒█░ ▒▒▓  ▒    ░░ ▒░ ░░ ▒░   ▒ ▒  ▒▒▓  ▒ ░▓  ░ ▒░   ▒ ▒  ░▒   ▒ ║
                ║▒░▒   ░   ▒   ▒▒ ░ ░ ▒  ▒     ░ ░  ░░ ░░   ░ ▒░ ░ ▒  ▒  ▒ ░░ ░░   ░ ▒░  ░   ░ ║
                ║ ░    ░   ░   ▒    ░ ░  ░       ░      ░   ░ ░  ░ ░  ░  ▒ ░   ░   ░ ░ ░ ░   ░ ║
                ║ ░            ░  ░   ░          ░  ░         ░    ░     ░           ░       ░ ║
                ║      ░            ░                            ░                             ║
                ║                                                                              ║
                ║                                                                              ║
                ╚══════════════════════════════════════════════════════════════════════════════╝
                """;


        printCentered(art, blood);
        System.out.println();
    }

// private void displayFriendshipEnding() {
//     String green = "\u001B[38;2;0;200;0m";
//     String yellow = "\u001B[38;2;255;215;0m";
//     System.out.println();

//     String art = green + yellow + """
//                    _      _      _      _      _      _      _      _      _      _      _      _      _      _
//                  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_
//                 (_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)
//                  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)
//                    _                                                                                          _
//                  _( )_                                                                                      _( )_
//                 (_ o _)                                                                                    (_ o _)
//                  (_,_)       ▄████████    ▄████████  ▄█     ▄████████ ███▄▄▄▄   ████████▄     ▄████████     (_,_)
//                    _        ███    ███   ███    ███ ███    ███    ███ ███▀▀▀██▄ ███   ▀███   ███    ███       _
//                  _( )_      ███    █▀    ███    ███ ███▌   ███    █▀  ███   ███ ███    ███   ███    █▀      _( )_
//                 (_ o _)    ▄███▄▄▄      ▄███▄▄▄▄██▀ ███▌  ▄███▄▄▄     ███   ███ ███    ███   ███           (_ o _)
//                  (_,_)    ▀▀███▀▀▀     ▀▀███▀▀▀▀▀   ███▌ ▀▀███▀▀▀     ███   ███ ███    ███ ▀███████████     (_,_)
//                    _        ███        ▀███████████ ███    ███    █▄  ███   ███ ███    ███          ███       _
//                  _( )_      ███          ███    ███ ███    ███    ███ ███   ███ ███   ▄███    ▄█    ███     _( )_
//                 (_ o _)     ███          ███    ███ █▀     ██████████  ▀█   █▀  ████████▀   ▄████████▀     (_ o _)
//                  (_,_)                   ███    ███                                                         (_,_)
//                    _              ▄████████ ███▄▄▄▄   ████████▄   ▄█  ███▄▄▄▄      ▄██████▄                   _
//                  _( )_           ███    ███ ███▀▀▀██▄ ███   ▀███ ███  ███▀▀▀██▄   ███    ███                _( )_
//                 (_ o _)          ███    █▀  ███   ███ ███    ███ ███▌ ███   ███   ███    █▀                (_ o _)
//                  (_,_)          ▄███▄▄▄     ███   ███ ███    ███ ███▌ ███   ███  ▄███                       (_,_)
//                    _           ▀▀███▀▀▀     ███   ███ ███    ███ ███▌ ███   ███ ▀▀███ ████▄                   _
//                  _( )_           ███    █▄  ███   ███ ███    ███ ███  ███   ███   ███    ███                _( )_
//                 (_ o _)          ███    ███ ███   ███ ███   ▄███ ███  ███   ███   ███    ███               (_ o _)
//                  (_,_)           ██████████  ▀█   █▀  ████████▀  █▀    ▀█   █▀    ████████▀                 (_,_)
//                    _                                                                                          _
//                  _( )_                                                                                      _( )_
//                 (_ o _)                                                                                    (_ o _)
//                  (_,_)                                                                                      (_,_)
//                    _      _      _      _      _      _      _      _      _      _      _      _      _      _
//                  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_  _( )_
//                 (_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)(_ o _)
//                  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)  (_,_)

//         """;

//     printCentered(art,yellow);
//     System.out.println();
// }

    private void displayGameFinished() {
        String blood = "\u001B[38;2;138;3;3m";
        String reset = "\u001B[0m";
        String lightPink = "\u001B[38;2;255;182;193m";
        System.out.println();


        String art = """   

════════════════════════════════════════════════════════════════════════════════════════════════
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓


 ██████╗  █████╗ ███╗   ███╗███████╗    ███████╗██╗███╗   ██╗██╗███████╗██╗  ██╗███████╗██████╗ 
██╔════╝ ██╔══██╗████╗ ████║██╔════╝    ██╔════╝██║████╗  ██║██║██╔════╝██║  ██║██╔════╝██╔══██╗
██║  ███╗███████║██╔████╔██║█████╗      █████╗  ██║██╔██╗ ██║██║███████╗███████║█████╗  ██║  ██║
██║   ██║██╔══██║██║╚██╔╝██║██╔══╝      ██╔══╝  ██║██║╚██╗██║██║╚════██║██╔══██║██╔══╝  ██║  ██║
╚██████╔╝██║  ██║██║ ╚═╝ ██║███████╗    ██║     ██║██║ ╚████║██║███████║██║  ██║███████╗██████╔╝
 ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝    ╚═╝     ╚═╝╚═╝  ╚═══╝╚═╝╚══════╝╚═╝  ╚═╝╚══════╝╚═════╝ 
                                                                                                

▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
═════════════════════════════════════════════════════════════════════════════════════════════════
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
.................................................................................................

    
    """;

        printCentered(art, lightPink);
        System.out.println();
    }




    // END SYSTEM //
    private void endGame() {
        typewriter("\n Calculating the scores....", 200);
        typewriter("████████████████████████████████████", 200);
        System.out.println("\nFinal Relationship Scores:");
        Character bestMatch = null;
        double bestScore = 0;
        for (Character character : availableCharacters) {
            Relationship relationship = player.getRelationship(character);
            double score = relationship.calculateFinalScore(player.getCharisma());
            System.out.println(character.getName() + ": " + String.format("%.1f", score) + "%");
            if (score > bestScore) {
                bestScore = score;
                bestMatch = character;
            }
        }
        if (bestMatch != null && bestScore > 0) {
            if (bestScore >= 80) {
                displayLoveEnding();
                System.out.println("CONGRATULATIONS! You found true love with " + bestMatch.getName() + "!");
                typewriter(bestMatch.getName() + ": It might've been fate that has managed to met with us together in the first place!", 100);
                typewriter("You confessed to " + bestMatch.getName() + " and married each other a year later..", 100);
                typewriter("Both of you had lived a good life and a family that were there for each other...", 100);
            } else if (bestScore <= 60) {
                displayBadEnding();
                System.out.println("Too bad! You made a mess with " + bestMatch.getName() + "!");
                typewriter(bestMatch.getName() + ": Maybe we aren't for each other, fate has mistaken as to be together.", 100);
                typewriter(bestMatch.getName() + ": This is where we part our ways now, it was nice knowing you...", 100);
                typewriter("Both of you has parted ways and never bumped into each other again..", 100);
            } else if (bestScore == bestScore){
                typewriter("You've hesitated to choose which one, so you decided to SPIN THE WHEEL!", 50);
            }
        }
        displayGameFinished();
    }
}

