package game;

import Characters.Character;
import Player.Player;
import RelationshipSystem.Relationship;
import java.util.*;

public class ConversationManager {
    private final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);


    // Typewriter Effect //
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

    // -- DECLARATIONS OF HASHMAPS --
    private final Map<String, Map<Integer, Map<Integer, String>>> dialogueMap = new HashMap<>();
    private final Map<String, Map<Integer, Map<Integer, Map<String, String>>>> choiceMap = new HashMap<>();
    private final Map<String, Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>>> responseMap = new HashMap<>();


    private static final String[] LEVEL_NAMES = {
            "Abandoned Compound",
            "Temporary Shelter",
            "City Ruins",
            "Safehouse Conflict",
            "Escape Route"
    };

    // FOR CHOICE PATTERNS //
    public class ChoiceOutcome {
        public final String response;
        public final String effect;

        public ChoiceOutcome(String response, String effect) {
            this.response = response;
            this.effect = effect;
        }
    }

    // ====== Initialization ====== // CONSTRUCTOR

    public ConversationManager() {
        initializeDialogues();
        initializeChoices();
        initializeResponses();
    }

    // ====== Start Conversation ======
    public void startConversation(Player player, Character character, int level, int conversationNumber) {

        String levelName = "Unknown Level";

        if (level > 0 && level <= LEVEL_NAMES.length) {
            levelName = LEVEL_NAMES[level - 1];
        }

        System.out.println("=== Level " + level + " " + levelName + " ===");
        typewriter("=== Conversation " + conversationNumber + " with " + character.getName() + " ===", 50);
        System.out.println();

        // Get dialogue
        String question = getQuestion(character, level, conversationNumber);
        String dialogueLine = character.getName() + ": \"" + question + "\"";
        typewriter(dialogueLine, 30);

        // Display choices
        displayChoices(character.getName(), level, conversationNumber);

        // Get player choice
        String choice = getPlayerChoice();

        // Process effect
        processChoice(player, character, level, conversationNumber, choice);

        // Show updated relationship
        displayRelationshipStatus(player, character);
    }

    // ====== Dialogue Getter ======
    public String getQuestion(Character character, int level, int conversationNumber) {
        String name = character.getName();
        if (dialogueMap.containsKey(name)) {
            Map<Integer, Map<Integer, String>> levelMap = dialogueMap.get(name);
            if (levelMap.containsKey(level) && levelMap.get(level).containsKey(conversationNumber)) {
                return levelMap.get(level).get(conversationNumber);
            }
        }
        return "The silence is heavy... there's nothing to say right now.";
    }

    // ====== Choice Display ======
    public Map<String, String> displayChoices(String charName, int level, int conversationNumber) {
        Map<String, String> choices = null;
        if (choiceMap.containsKey(charName)) {
            Map<Integer, Map<Integer, Map<String, String>>> levels = choiceMap.get(charName);
            if (levels.containsKey(level) && levels.get(level).containsKey(conversationNumber)) {
                choices = levels.get(level).get(conversationNumber);
            }
        }

        System.out.println("\nChoices:");
        for (Map.Entry<String, String> entry : choices.entrySet()) {
            System.out.println(entry.getKey() + ". " + entry.getValue());
        }
        return choices;
    }

    // ====== Player Input ======
    private String getPlayerChoice() {
        String choice;
        while (true) {
            System.out.print("\nChoose your response(A-E): ");
            choice = scanner.nextLine().trim().toUpperCase();
            if (choice.matches("[A-E]")) return choice;
            System.out.println("Invalid Choice. Please choose A-E only.");
        }
    }

    // ====== Process Choice ====== //
    private void processChoice(Player player, Character character, int level, int conversationNumber, String choice) {

        // 1. Gets the entire outcome object
        ChoiceOutcome outcome = getChoiceOutcome(character.getName(), level, conversationNumber, choice);

        if (outcome == null) {

            System.out.println(character.getName() + ": \"...\"");
            applyEffect(player, character, "NEUTRAL");
            return;
        }

        // 2. Print the response text from the object
        typewriter(outcome.response, 30);

        // 3. Apply the effect from the object
        applyEffect(player, character, outcome.effect);
    }



    public void applyEffect(Player player, Character character, String effect) {
        if (effect == null) {
            effect = "NEUTRAL";
        }
        switch (effect) {
            case "CHARISMA":
                player.increaseCharisma(2);
                System.out.println();
                System.out.println("+2 Charisma");
                break;
            case "TRUST":
                player.increaseTrust(character, 3);
                System.out.println();
                System.out.println("+3 Trust with " + character.getName());
                break;
            case "TURN_ON":
                player.increaseTurnOn(character, 3);
                System.out.println();
                System.out.println("+3 Turn-On with " + character.getName());
                break;
            case "NEUTRAL":
                System.out.println();
                System.out.println("No change in relationship.");
                break;
            case "TURN_OFF":
                player.increaseTurnOff(character, 3);
                System.out.println();
                System.out.println("+3 Turn-Off with " + character.getName());
                break;
            case "TURN_OFF2":
                player.increaseTurnOff(character, 6);
                System.out.println();
                System.out.println("+6 Turn-Off with " + character.getName());
                break; // <-- ADDED BREAK
            default:
                System.out.println(character.getName() + ": \"...Let's move on.\"");
        }
    }

    // ====== Status Display ======
    private void displayRelationshipStatus(Player player, Character character) {
        Relationship r = player.getRelationship(character);
        System.out.println("\n---------------- " + character.getName() + " Status ---------------");
        System.out.println("Trust: " + r.getTrust());
        System.out.println("Turn-On: " + r.getTurnOn());
        System.out.println("Turn-Off: " + r.getTurnOff());
        System.out.println("Your Charisma: " + player.getCharisma());
        System.out.println("-------------------------------------------");
        System.out.println();
    }

    // ====== Initialize Dialogue ======
    private void initializeDialogues() {
        // Example for AVY ,  add others like Marina, Kim, etc.
        dialogueMap.put("Avy", new HashMap<>());
        dialogueMap.put("Marina", new HashMap<>());
        dialogueMap.put("Kim", new HashMap<>());
        dialogueMap.put("Nathan", new HashMap<>());
        dialogueMap.put("Yubie", new HashMap<>());
        dialogueMap.put("Adi", new HashMap<>());

        Map<Integer, Map<Integer, String>> avyLevels = new HashMap<>();
        Map<Integer, Map<Integer, String>> marinaLevels = new HashMap<>();
        Map<Integer, Map<Integer, String>> kimLevels = new HashMap<>();
        Map<Integer, Map<Integer, String>> nathanLevels = new HashMap<>();
        Map<Integer, Map<Integer, String>> yubieLevels = new HashMap<>();
        Map<Integer, Map<Integer, String>> adiLevels = new HashMap<>();

        // ====== Avy ======

        // Level 1: Abandoned Compound
        Map<Integer, String> avylevel1 = new HashMap<>();
        avylevel1.put(1, "Finally! Another survivor! What kind of life did you have before all this?");
        avylevel1.put(2, "Man, my legs are killing me. Funny, right? I used to run for fun, now I'm running for my life. So tell me, how do you handle the pressure when it's do-or-die?");
        avylevel1.put(3, "You'd think an abandoned compound would feel safe, but nah, it's like a horror movie set. If we had popcorn, I'd almost enjoy it. What about you, scared?");
        avyLevels.put(1, avylevel1);

        // Level 2: Temporary Shelter
        Map<Integer, String> avylevel2 = new HashMap<>();
        avylevel2.put(1, "This place feels almost safe... almost. Sitting still makes me itchy. Back in varsity, I hated being benched. Tell me, how do you keep your head up in a world like this?");
        avylevel2.put(2, "Back in college, I was always running, sweating, chasing the ball. I thought I'd stay that way forever. Then real life hit, desk job, deadlines, endless emails. Funny how the apocalypse makes me miss even the office. What do you miss most from the old world?");
        avylevel2.put(3, "(leaning back against the wall): When I worked at a corporation, sitting in endless meetings made me itch. Before that, sitting on the bench during a game gave me the same feeling. Guess I've never liked being told to just sit and wait. Do you ever feel useless, like you're just... stuck?");
        avyLevels.put(2, avylevel2);

        // Level 3: City Ruins
        Map<Integer, String> avylevel3 = new HashMap<>();
        avylevel3.put(1, "(catching her breath after fighting zombies): This city... feels like a graveyard. I'm trying to laugh it off, but it's heavy, you know? What's keeping you going out here?");
        avylevel3.put(2, "(kicking aside debris, glancing up at broken skyscrapers): Crazy, right? I used to walk these streets in heels, rushing to meetings. Now I'm running in sneakers, rushing from zombies. Life's got a messed-up sense of humor. What do you think this city will be like in another year?");
        avylevel3.put(3, "(after smashing a zombie with a pipe, panting): Whew... not my best form. I used to train for hours every day in college. Now? My workouts are just swinging at corpses. Do you think we're getting stronger out here, or just... breaking apart slower?");
        avyLevels.put(3, avylevel3);

        // Level 4: Safehouse Conflict
        Map<Integer, String> avylevel4 = new HashMap<>();
        avylevel4.put(1, "(snapping after an argument with another survivor): Ugh! People drive me crazy. I say we push forward, they say we wait. What do you think? And don't sugarcoat it.");
        avylevel4.put(2, "(restless while keeping watch): This place feels too quiet. Makes me wonder if we're sitting ducks... or if I'm just being paranoid again.");
        avylevel4.put(3, "(counting rations on the table): Damn it... this won't last long. Someone's got to make the hard calls, and I don't think the others have it in them.");
        avyLevels.put(4, avylevel4);

        //Level 5: Escape Route
        Map<Integer, String> avylevel5 = new HashMap<>();
        avylevel5.put(1, "(panting, hand trembling as she looks at you): This is it, the last stretch. If we mess up now, we die. So tell me straight: why should I keep running with you?");
        avylevel5.put(2, "(gritting her teeth, hearing footsteps closing in): If we fight here, we risk being trapped. If we run, we risk being chased. No safe option. What's your call?");
        avylevel5.put(3, "(wind whipping her hair as the helicopter hovers above, the roof groaning under their feet): One wrong move and the whole building comes down. The rope's swaying too much... Tell me how we do this.");
        avyLevels.put(5, avylevel5);

        dialogueMap.get("Avy").putAll(avyLevels);

        // ====== Marina ======

        // Level 1: Abandoned Compound
        Map<Integer, String> marinalevel1 = new HashMap<>();
        marinalevel1.put(1, "...You're not one of them. (lets out a small breath, her eyes softening) I almost gave up thinking anyone else was out here. If you made it this far, then maybe I'm not as alone as I thought. You know... they say people survive longer when they've got someone beside them. S-so... what keeps you going?”");
        marinalevel1.put(2, "(paused for a second) ...Wait. Someone's definitely been here not too long ago.” (she crouches, brushing her hand lightly over the dust where footprints remain) ...We should stay by the walls, it lowers the chance of being spotted, since zombies usually notice direct movement first. I... I think it's worth trying.");
        marinalevel1.put(3, " (observing the place) ...It's... weird thinking about how quiet it is now. D-Did you know, humans aren't really used to silence? In cities, background noise is almost constant... even at night. Now it's just... gone. Do you think anyone else is still out there... or are we really alone?");
        marinaLevels.put(1, marinalevel1);

        // Level 2: Temporary Shelter
        Map<Integer, String> marinalevel2 = new HashMap<>();
        marinalevel2.put(1, "...I-I think we could rest for a bit here. From what I observed earlier, the broken windows don't have fresh cracks, and the dust near the entrance hasn't been disturbed... that usually means no one's been through here in a while. W-we could... maybe make this a hideout, don't you think?");
        marinalevel2.put(2, "(twirling a pebble in her hand) I-I was thinking... maybe we should take turns keeping watch. Like... four hours each or five? Studies said the average person starts losing focus after about 90 minutes, so splitting shifts means we'd stay sharper... and, um, less likely to miss anything. That way no one gets too tired... and we'd notice if... anything comes.");
        marinalevel2.put(3, "...I-I sometimes wonder... before all this, did you ever think about what you'd be doing? Your dreams, or what you wanted to become... before the world ended up like this? For me... I always thought it'd be fun to just... keep learning things. Collecting little facts, sharing them with people... like a walking book, I guess. (she clutches her cracked notebook tightly)");
        marinaLevels.put(2, marinalevel2);

        // Level 3: City Ruins
        Map<Integer, String> marinalevel3 = new HashMap<>();
        marinalevel3.put(1, "(panting, clutching her knees) I... I thought... we weren't gonna make it...t-the human brain releases more cortisol in moments like that... it's supposed to help us survive, but... it almost felt like it froze me instead.\n(Her hands shake, but her eyes are sharp, she's already scanning the street for danger.)");
        marinalevel3.put(2, "(worrying after smacking the zombie with a pipe) Wait, hold still. Are you hurt? A zombie bite transmits faster than rabies, and the incubation rate can vary depending on where it breaks the skin...Please tell me it didn't bite you...?");
        marinalevel3.put(3, "(fidgeting slightly, voice quiet but determined) I... I know this area. If we take this alley and then turn left at that telephone booth, we can avoid the bigger streets... I think that's where the zombies usually gather. If we plan carefully... we might avoid running into even one, or hundreds.");
        marinaLevels.put(3, marinalevel3);

        // Level 4: Safehouse Conflict
        Map<Integer, String> marinalevel4 = new HashMap<>();
        marinalevel4.put(1, "(observing another clash from the survivors) ...I-I'm not sure we should trust them fully. Groups like this... they usually break down under stress, power struggles, fights over food, that kind of thing. My gut says we need to be careful... but part of me still wants to believe in them. W-what do you think?");
        marinalevel4.put(2, " (restless while keeping watch) I... I've been keeping an eye on the streets. It's quiet, too quiet. You know, in nature, when everything goes silent, it usually means a predator is nearby. S-so... I kept watching. I just want to make sure we're safe. Am I... overthinking it?");
        marinalevel4.put(3, "...I've been thinking. The food might last a few more days if we ration it. There's a store nearby that could have supplies... I-I want to trust the others, but it still feels risky.");
        marinaLevels.put(4, marinalevel4);

        //Level 5: Escape Route
        Map<Integer, String> marinalevel5 = new HashMap<>();
        marinalevel5.put(1, "(softly, nudging you as you walk) ...H-Hey... we got the signal. Looks like the rescue team's nearby. It's... the final stretch. I-I just hope we make it without any surprises... I'm glad they followed my advice to take the longer route instead of risking our lives through the shorter path with zombies...");
        marinalevel5.put(2, "I...I think I've figured out a way through the tunnel without alerting the larger zombie groups. They can't see or hear well, so if we move quietly, use the side paths, and match our steps with the echoes, we might slip past the ones near the exit. Can you let them know?");
        marinalevel5.put(3, "M- maybe should drop the heavy bags and only carry what's essential. Less weight means less noise and faster movement. Even leaving a weapon behind might help. It's risky, but it could keep us safe. I- I'm making sense, r- right?");
        marinaLevels.put(5, marinalevel5);

        dialogueMap.get("Marina").putAll(marinaLevels);


        // ====== Kim ======
        // Level 1: Abandoned Compound
        Map<Integer, String> kimlevel1 = new HashMap<>();
        kimlevel1.put(1, "Oh... you're not one of them. You look exhausted and worn down. Please sit, if you'd like. I don't have much left, but you can rest here for a while. No one should face the dark alone.");
        kimlevel1.put(2, "...Before all of this, I was a nursing student. I thought I'd spend my days walking through hospital halls, carrying charts... not hiding in ruins, carrying a medkit. Strange, isn't it?");
        kimlevel1.put(3, "It's frightening, isn't it? The silence between their cries. Like waiting for a storm you can't stop. But having someone here beside me, it makes the night feel less endless.");
        kimLevels.put(1, kimlevel1);

        // Level 2: Temporary Shelter
        Map<Integer, String> kimlevel2 = new HashMap<>();
        kimlevel2.put(1, "This isn't near perfect, but these walls, this roof... it feels almost like safety... If tomorrow was certain what would you want it to look like?");
        kimlevel2.put(2, "You're hurt? Why didn't you say something? Please... hold still. That cut's deeper than you think.");
        kimlevel2.put(3, "Being still, no running and no hiding. But when the quiet settles... the memories feel louder. Almost like waiting for a storm you can't stop. Do you ever feel that way?”");
        kimLevels.put(2, kimlevel2);

        // Level 3: City Ruins
        Map<Integer, String> kimlevel3 = new HashMap<>();
        kimlevel3.put(1, "This is so overwhelming. But panicking won't save us. We have to decide, how do you want to get through this?");
        kimlevel3.put(2, "I thought we'd have a bit more... but it's almost gone. I don't want to sound weak, but... I'm really worried. Can we... figure this out together?");
        kimlevel3.put(3, "We have just enough for now... but I can't stop thinking... What if it runs out tomorrow? I don't want either of us to get hurt. Can I ask you something?");
        kimLevels.put(3, kimlevel3);

        // Level 4: Safehouse Conflict
        Map<Integer, String> kimlevel4 = new HashMap<>();
        kimlevel4.put(1, "This place... it could be shelter. But what if it's not safe? I'm so tired of running. What do we do?");
        kimlevel4.put(2, "One of the survivors offered to share supplies, but... I don't know if we can trust them. What should we do?”");
        kimlevel4.put(3, "Everyone's on edge... food, space, everything. I hate fighting with people when we should be helping each other. How do you think we should handle this?");
        kimLevels.put(4, kimlevel4);

        //Level 5: Escape Route
        Map<Integer, String> kimlevel5 = new HashMap<>();
        kimlevel5.put(1, "We've made it this far... but it's hard to ignore everything we've seen. I... I don't know how I'd do this without you. Can I ask you something?");
        kimlevel5.put(2, "If this breaks under us… it's over. I'm terrified, but I'll follow you. Just, tell me how we get along.");
        kimlevel5.put(3, "Kim (voice shaking as the water rises, clutching your arm): I can't do this alone… not this time. Stay with me, don't let go, please.");
        kimLevels.put(5, kimlevel5);

        dialogueMap.get("Kim").putAll(kimLevels);



        // ====== Nathan ======

        // Level 1: Abandoned Compound
        Map<Integer, String> nathanlevel1 = new HashMap<>();
        nathanlevel1.put(1, "...Never thought I'd see you here, of all places. Guess fate's got a sense of humor. How've you been holding up?");
        nathanlevel1.put(2, "..This place... reminds me of late nights studying, back when things were normal. Do you ever miss those days?");
        nathanlevel1.put(3, "...I'm quite tired for tonight. We'll need to take turns keeping watch tonight. I'll go first unless... you want to.");
        nathanLevels.put(1, nathanlevel1);

        // Level 2: Temporary Shelter
        Map<Integer, String> nathanlevel2 = new HashMap<>();
        nathanlevel2.put(1, "...Sitting by a fire again feels almost normal. Almost.");
        nathanlevel2.put(2, "(Reminiscing the past)...Burned my hands cooking once, back at camp. Guess I never learned to stay out of the kitchen.");
        nathanlevel2.put(3, "(Anxious)...Can't sleep. Every time I close my eyes, I hear them outside..");
        nathanLevels.put(2, nathanlevel2);

        // Level 3: City Ruins
        Map<Integer, String> nathanlevel3 = new HashMap<>();
        nathanlevel3.put(1, "Damn, almost didn't make it out. You okay?");
        nathanlevel3.put(2, "(Sweating and panting) It's never easy. Doesn't matter how many of them I kill, they won't stop coming.");
        nathanlevel3.put(3, "(Exhausted) We barely made it... I thought, I thought I'd lost you.");
        nathanLevels.put(3, nathanlevel3);

        // Level 4: Safehouse Conflict
        Map<Integer, String> nathanlevel4 = new HashMap<>();
        nathanlevel4.put(1, "(Tense, voice low) You went against my call out there. You put us both at risk. Why?");
        nathanlevel4.put(2, "(Exhausted) There's something I never told you. Back in college... I wanted to ask you out. But I never did.");
        nathanlevel4.put(3, "(Hoarse) Every call I make costs someone their life. Sometimes I wonder if keeping people alive just means delaying the inevitable.");
        nathanLevels.put(4, nathanlevel4);

        //Level 5: Escape Route
        Map<Integer, String> nathanlevel5 = new HashMap<>();
        nathanlevel5.put(1, "(Exhaling shakily) We're almost there, but everyone's hanging by a thread... including me. Tell me, why should I still believe we'll make it?");
        nathanlevel5.put(2, "(Whimpering) ...If we make it through this... I don't just want to survive. I want you. Tell me... do you feel the same?");
        nathanlevel5.put(3, "(Shouting over the wind) We've only got one chance at this! You first or me?");
        nathanLevels.put(5, nathanlevel5);

        dialogueMap.get("Nathan").putAll(nathanLevels);

        // ====== Yubie ======

        // Level 1: Abandoned Compound
        Map<Integer, String> yubielevel1 = new HashMap<>();
        yubielevel1.put(1, "Ahh! Don't eat me! Wait, uh... you're not a zombie. Right? Please say you're not a zombie.");
        yubielevel1.put(2, "Wait... I know you. Arcade by the mall, right? I was there all the time, usually losing to kids half my size.");
        yubielevel1.put(3, "So, uh... haven't eaten in two days. If I pass out, don't loot my body, okay?");
        yubieLevels.put(1, yubielevel1);

        // Level 2: Temporary Shelter
        Map<Integer, String> yubielevel2 = new HashMap<>();
        yubielevel2.put(1, "Okay, so... rule one of apocalypse survival: never trust the quiet. Quiet means something's lurking. Unless it's a safe zone... which this obviously isn't. Heh.");
        yubielevel2.put(2, "Soo, before all this... what were you like? Normal life, I mean.");
        yubielevel2.put(3, "...You know what I miss the most? Not games, not movies... but just... laughing. Like, stupid, pointless laughter. I'd give anything to have that again. Maybe that's why I'm trying my best to stay positive.");
        yubieLevels.put(2, yubielevel2);

        // Level 3: City Ruins
        Map<Integer, String> yubielevel3 = new HashMap<>();
        yubielevel3.put(1, "...Holy, okay. That was nothing like the movies. Way more screaming, way less cool headshots and who knew there were other survivors here? My hands are still shaking");
        yubielevel3.put(2, "...Uh... hi? We, uh... helped with the zombies. Not that we're heroes or anything... just, you know, nerds who memorized every possible apocalypse scenario.");
        yubielevel3.put(3, "Well, this is officially worse than any zombie guidebook scenario I studied. My plans are... mostly theoretical. Guess we'll find out if theory survives reality!");
        yubieLevels.put(3, yubielevel3);

        // Level 4: Safehouse Conflict
        Map<Integer, String> yubielevel4 = new HashMap<>();
        yubielevel4.put(1, "Look, I'm not trying to boss anyone around. I just... studied this stuff. Maps, escape routes, zombie behavior... This is literally what I've been doing my whole life. I... just want everyone safe.");
        yubielevel4.put(2, "Okay, maybe we can reorganize the rations. If we distribute carefully, everyone gets something without fighting.");
        yubielevel4.put(3, "I didn't panic as much as I thought I would. And having you here... it makes all this chaos a little easier to handle. Weird... right?");
        yubieLevels.put(4, yubielevel4);

        //Level 5: Escape Route
        Map<Integer, String> yubielevel5 = new HashMap<>();
        yubielevel5.put(1, "Last chance! My insides are screaming, adrenaline is screaming, but... this could be fun! Maybe terrifying, maybe epic... either way, let's go!");
        yubielevel5.put(2, "Okay, last stretch. My instincts say we should go left, sneak behind cover, then sprint to the exit. I'm terrified but if we do this, we're out. We...we really could make it.");
        yubielevel5.put(3, "We actually made it... I mean, I didn't think we'd survive all that chaos. But here we are...");
        yubieLevels.put(5, yubielevel5);

        dialogueMap.get("Yubie").putAll(yubieLevels);

        // == ADI == //

        // Level 1: Abandoned Compound
        Map<Integer, String> adilevel1 = new HashMap<>();
        adilevel1.put(1, "You're hurt. Sit down, I'll clean that up. Supplies are scarce, but infection spreads faster. I can't let you risk that.");
        adilevel1.put(2, "Here. Take it. I'll be fine for now. You need your strength more.");
        adilevel1.put(3, "Sometimes I wonder if I should've left when I had the chance. But then... who would've been here to help?");
        adiLevels.put(1, adilevel1);

        // Level 2: Temporary Shelter
        Map<Integer, String> adilevel2 = new HashMap<>();
        adilevel2.put(1, "...I'll take the first watch. And if anyone needs stitches, I'll handle it. Sleep while you can.");
        adilevel2.put(2, "It's not much, but it'll keep the infection away. Trust me, I've done more with less.");
        adilevel2.put(3, "If I can just figure out how to make this last longer... maybe we'll all survive a little longer too.");
        adiLevels.put(2, adilevel2);

        // Level 3: City Ruins
        Map<Integer, String> adilevel3 = new HashMap<>();
        adilevel3.put(1, "I trained here. I thought one day I'd save lives inside these walls. Now... it feels like this place died before I did.");
        adilevel3.put(2, "If there's morphine in there, it could save someone down the line. I'll climb up.");
        adilevel3.put(3, "I... I can't just walk away. But if I give them what little we have, we might not make it through the night.");
        adiLevels.put(3, adilevel3);

        // Level 4: Safehouse Conflict
        Map<Integer, String> adilevel4 = new HashMap<>();
        adilevel4.put(1, "We can't survive if we're turning on each other. Please, enough.");
        adilevel4.put(2, "I can't even remember the last time I slept properly... but someone always needs something.");
        adilevel4.put(3, "People talk. They think I'm distracted. Maybe... they're right.");
        adiLevels.put(4, adilevel4);

        //Level 5: Escape Route
        Map<Integer, String> adilevel5 = new HashMap<>();
        adilevel5.put(1, "If we go now, there's no coming back. But if we stay... this place won't last.");
        adilevel5.put(2, "If this is it... I just want to say, you made me believe again. In something worth saving.");
        adilevel5.put(3, "Go! I can hold them off, just get to the truck!");
        adiLevels.put(5, adilevel5);

        dialogueMap.get("Adi").putAll(adiLevels);
    }

    // INITIALIZES RESPONSES //
    private void initializeResponses() {
        responseMap.put("Avy", new HashMap<>());
        responseMap.put("Marina", new HashMap<>());
        responseMap.put("Kim", new HashMap<>());
        responseMap.put("Nathan", new HashMap<>());
        responseMap.put("Yubie", new HashMap<>());
        responseMap.put("Adi", new HashMap<>());

        // ====== NATHAN ======
        Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>> nathanLevels = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> nathanLevel1 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> nathanLevel2 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> nathanLevel3 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> nathanLevel4 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> nathanLevel5 = new HashMap<>();

        // --- NATHAN LEVEL 1 ---
        Map<String, ChoiceOutcome> nathanconvo1_l1 = new LinkedHashMap<>(); // Pattern Case 4
        nathanconvo1_l1.put("A", new ChoiceOutcome("Nathan: \"Heh... stubbornness looks good on you. I guess I can count on that.\"", "CHARISMA"));
        nathanconvo1_l1.put("B", new ChoiceOutcome("Nathan: \"...That sounds like you. Always putting others first, even when it hurts. I can respect that.\"", "TRUST"));
        nathanconvo1_l1.put("C", new ChoiceOutcome("Nathan: \"...Tch. Don't get cocky. But yeah... I'm glad you made it.\"", "TURN_ON"));
        nathanconvo1_l1.put("D", new ChoiceOutcome("Nathan: \"...Fair enough. Guess that's all we can do right now.\"", "NEUTRAL"));
        nathanconvo1_l1.put("E", new ChoiceOutcome("Nathan: \"...Damn. Didn't think I'd hear that from you. Don't lose hope, not now.\"", "TURN_OFF"));
        nathanLevel1.put(1, nathanconvo1_l1);

        Map<String, ChoiceOutcome> nathanconvo2_l1 = new LinkedHashMap<>(); // Pattern Case 3
        nathanconvo2_l1.put("A", new ChoiceOutcome("Nathan: \"...Yeah. The past doesn't keep you alive. Not anymore.\"", "TURN_ON"));
        nathanconvo2_l1.put("B", new ChoiceOutcome("Nathan: \"...You really know how to mess with my head. Don't... don't joke about that.\"", "CHARISMA"));
        nathanconvo2_l1.put("C", new ChoiceOutcome("Nathan: \"...Careful. You're gonna make me wonder where I stood in that.\"", "TRUST"));
        nathanconvo2_l1.put("D", new ChoiceOutcome("Nathan: \"...Strong answer. You've changed... but in a good way.\"", "NEUTRAL"));
        nathanconvo2_l1.put("E", new ChoiceOutcome("Nathan: \"...That's one way to see it. Doesn't mean I like it.\"", "TURN_OFF"));
        nathanLevel1.put(2, nathanconvo2_l1);

        Map<String, ChoiceOutcome> nathanconvo3_l1 = new LinkedHashMap<>(); // Pattern Case 1
        nathanconvo3_l1.put("A", new ChoiceOutcome("Nathan: \"...Tch. Don't push it. But... maybe.\"", "CHARISMA"));
        nathanconvo3_l1.put("B", new ChoiceOutcome("Nathan: \"...You really know how to mess with my head. Don't... don't joke about that.\"", "TRUST"));
        nathanconvo3_l1.put("C", new ChoiceOutcome("Nathan: \"...Thanks. I owe you one.\"", "TURN_ON"));
        nathanconvo3_l1.put("D", new ChoiceOutcome("Nathan: \"...Alright.\"", "NEUTRAL"));
        nathanconvo3_l1.put("E", new ChoiceOutcome("Nathan: \"...Right. Guess I'll handle it myself, then.\"", "TURN_OFF"));
        nathanLevel1.put(3, nathanconvo3_l1);

        nathanLevels.put(1, nathanLevel1);

        // --- NATHAN LEVEL 2 ---
        // Dialogue 1: "...almost normal."
        Map<String, ChoiceOutcome> nathanconvo3_l2 = new LinkedHashMap<>(); // Pattern Case 1
        nathanconvo3_l2.put("A", new ChoiceOutcome("Nathan: \"You protecting me? Guess I'll hold you to it.\"", "CHARISMA"));
        nathanconvo3_l2.put("B", new ChoiceOutcome("Nathan: \"...That helps. More than you know.\"", "TRUST"));
        nathanconvo3_l2.put("C", new ChoiceOutcome("Nathan: \"...Dangerous offer. But quite tempting.\"", "TURN_ON"));
        nathanconvo3_l2.put("D", new ChoiceOutcome("Nathan: \"...You're right. It always does. Thanks.\"", "NEUTRAL"));
        nathanconvo3_l2.put("E", new ChoiceOutcome("Nathan: \"...That's low. Even for you..\"", "TURN_OFF"));
        nathanLevel2.put(1, nathanconvo3_l2);

        // Dialogue 2: "...cooking..."
        Map<String, ChoiceOutcome> nathanconvo2_l2 = new LinkedHashMap<>(); // Pattern Case 4
        nathanconvo2_l2.put("A", new ChoiceOutcome("Nathan: \"...You'll burn out quickly thinking like that.\"", "TURN_OFF"));
        nathanconvo2_l2.put("B", new ChoiceOutcome("Nathan: \"I cook, I lead, I keep people alive.... Anyways, help me out as well\"", "TRUST"));
        nathanconvo2_l2.put("C", new ChoiceOutcome("Nathan: \"...Yeah. Guess every mark tells a story now.\"", "NEUTRAL"));
        nathanconvo2_l2.put("D", new ChoiceOutcome("Nathan: \"...Didn't know you had a type.\"", "TURN_ON"));
        nathanconvo2_l2.put("E", new ChoiceOutcome("Nathan: \"Careful. I might spoil you.\"", "CHARISMA"));
        nathanLevel2.put(2, nathanconvo2_l2);

        // Dialogue 3: "...Can't sleep..."
        Map<String, ChoiceOutcome> nathanconvo1_l2 = new LinkedHashMap<>(); // Pattern Case 3
        nathanconvo1_l2.put("A", new ChoiceOutcome("Nathan: \"...Don't tell anyone. You'll ruin my image.\"", "TURN_ON"));
        nathanconvo1_l2.put("B", new ChoiceOutcome("Nathan: \"Heh. Yeah... it does.\"", "CHARISMA"));
        nathanconvo1_l2.put("C", new ChoiceOutcome("Nathan: \"...Maybe you're right. Maybe we can.\"", "TRUST"));
        nathanconvo1_l2.put("D", new ChoiceOutcome("Nathan: \"...Yeah. Just for tonight, let's pretend it's peaceful.\"", "NEUTRAL"));
        nathanconvo1_l2.put("E", new ChoiceOutcome("Nathan: \"...That's harsh, you might be right.\"", "TURN_OFF"));
        nathanLevel2.put(3, nathanconvo1_l2);

        nathanLevels.put(2, nathanLevel2);

        // --- NATHAN LEVEL 3 ---
        Map<String, ChoiceOutcome> nathanconvo1_l3 = new LinkedHashMap<>(); // Pattern Case 5
        nathanconvo1_l3.put("A", new ChoiceOutcome("Nathan: \"...You think I don't know that?\"", "TURN_OFF2"));
        nathanconvo1_l3.put("B", new ChoiceOutcome("Nathan: \"...Don't test me.\"", "TURN_OFF"));
        nathanconvo1_l3.put("C", new ChoiceOutcome("Nathan: \"Yeah. You've always been dependable.\"", "TRUST"));
        nathanconvo1_l3.put("D", new ChoiceOutcome("Nathan: \"Heh. Maybe I'm rubbing off on you.\"", "CHARISMA"));
        nathanconvo1_l3.put("E", new ChoiceOutcome("Nathan: \"...Don't push it. Just... stay alive.\"", "TURN_ON"));
        nathanLevel3.put(1, nathanconvo1_l3);

        Map<String, ChoiceOutcome> nathanconvo2_l3 = new LinkedHashMap<>(); // Pattern Case 5
        nathanconvo2_l3.put("A", new ChoiceOutcome("Nathan: \"...You don't understand.\"", "TURN_OFF2"));
        nathanconvo2_l3.put("B", new ChoiceOutcome("Nathan: \"...You sound colder than the dead out there.\"", "TURN_OFF"));
        nathanconvo2_l3.put("C", new ChoiceOutcome("Nathan: \"...I needed to hear that.\"", "TRUST"));
        nathanconvo2_l3.put("D", new ChoiceOutcome("Nathan: \"...Guess I needed that reminder.\"", "CHARISMA"));
        nathanconvo2_l3.put("E", new ChoiceOutcome("Nathan: \"Careful. You don't know what you're wishing for.\"", "TURN_ON"));
        nathanLevel3.put(2, nathanconvo2_l3);

        Map<String, ChoiceOutcome> nathanconvo3_l3 = new LinkedHashMap<>(); // Pattern Case 6
        nathanconvo3_l3.put("A", new ChoiceOutcome("Nathan: \"I'll cash that in someday.\"", "CHARISMA"));
        nathanconvo3_l3.put("B", new ChoiceOutcome("Nathan: \"...Yeah. Together. That's how we survive.\"", "TRUST"));
        nathanconvo3_l3.put("C", new ChoiceOutcome("Nathan: \"Don't...make me say it.\"", "TURN_ON"));
        nathanconvo3_l3.put("D", new ChoiceOutcome("Nathan: \"Ouch, I'm sorry, okay?\"", "TURN_OFF2"));
        nathanconvo3_l3.put("E", new ChoiceOutcome("Nathan: \"Really? After the efforts I've made, you're gonna act like this?\"", "TURN_OFF"));
        nathanLevel3.put(3, nathanconvo3_l3);

        nathanLevels.put(3, nathanLevel3);

        // --- NATHAN LEVEL 4 ---
        Map<String, ChoiceOutcome> nathanconvo1_l4 = new LinkedHashMap<>(); // Pattern Case 6
        nathanconvo1_l4.put("A", new ChoiceOutcome("Nathan: \"...So it's not about control, it's about care. Got it.\"", "TRUST"));
        nathanconvo1_l4.put("B", new ChoiceOutcome("Nathan: \"Alright... then don't make me regret it. Trust goes both ways.\"", "TURN_ON"));
        nathanconvo1_l4.put("C", new ChoiceOutcome("Nathan: \"Heh. You're impossible... but I'll give you that one.\"", "CHARISMA"));
        nathanconvo1_l4.put("D", new ChoiceOutcome("Nathan: \"You don't know what I carry. You just see the cracks, not what's holding me together.\"", "TURN_OFF2"));
        nathanconvo1_l4.put("E", new ChoiceOutcome("Nathan: \"...Then maybe you shouldn't be here if you don't believe in me.\"", "TURN_OFF"));
        nathanLevel4.put(1, nathanconvo1_l4);

        Map<String, ChoiceOutcome> nathanconvo2_l4 = new LinkedHashMap<>(); // Pattern Case 5
        nathanconvo2_l4.put("A", new ChoiceOutcome("Nathan: \"...Maybe you're right. Maybe it's too late.\"", "TURN_OFF2"));
        nathanconvo2_l4.put("B", new ChoiceOutcome("Nathan: \"...Didn't expect forgiveness. Just honesty.\"", "TURN_OFF"));
        nathanconvo2_l4.put("C", new ChoiceOutcome("Nathan: \"Maybe fate decided to wait for us.\"", "TRUST"));
        nathanconvo2_l4.put("D", new ChoiceOutcome("Nathan: \"Heh. Story of my life. Always late, but never insincere.\"", "CHARISMA"));
        nathanconvo2_l4.put("E", new ChoiceOutcome("Nathan: \"Guess the end of the world gave me a second chance.\"", "TURN_ON"));
        nathanLevel4.put(2, nathanconvo2_l4);

        Map<String, ChoiceOutcome> nathanconvo3_l4 = new LinkedHashMap<>(); // Pattern Case 6
        nathanconvo3_l4.put("A", new ChoiceOutcome("Nathan: \"You make it sound so simple... but maybe I needed to hear that.\"", "TRUST"));
        nathanconvo3_l4.put("B", new ChoiceOutcome("Nathan: \"You always knew how to steady me... even when I didn't deserve it.\"", "TURN_ON"));
        nathanconvo3_l4.put("C", new ChoiceOutcome("Nathan: \"Because you haven't stopped, and I'm too stubborn to fall behind.\"", "CHARISMA"));
        nathanconvo3_l4.put("D", new ChoiceOutcome("Nathan: \"You might be right... maybe I just fooled everyone into thinking I could.\"", "TURN_OFF2"));
        nathanconvo3_l4.put("E", new ChoiceOutcome("Nathan: \"...Didn't expect forgiveness. Just honesty.\"", "TURN_OFF"));
        nathanLevel4.put(3, nathanconvo3_l4);

        nathanLevels.put(4, nathanLevel4);

        // --- NATHAN LEVEL 5 ---
        Map<String, ChoiceOutcome> nathanconvo1_l5 = new LinkedHashMap<>(); // Pattern Case 5
        nathanconvo1_l5.put("A", new ChoiceOutcome("Nathan: \"You think I don't already know that?\"", "TURN_OFF2"));
        nathanconvo1_l5.put("B", new ChoiceOutcome("Nathan: \"Yeah... stubbornness isn't the same as strength.\"", "TURN_OFF"));
        nathanconvo1_l5.put("C", new ChoiceOutcome("Nathan: \"...You really think I've still got something left to give? (He manages a faint smile) Then I guess I owe you my faith.\"", "TRUST"));
        nathanconvo1_l5.put("D", new ChoiceOutcome("Nathan: \"Flattery won't fix this wound... but I'll take it.\"", "CHARISMA"));
        nathanconvo1_l5.put("E", new ChoiceOutcome("Nathan: \"...You always know how to mess with my heartbeat.\"", "TURN_ON"));
        nathanLevel5.put(1, nathanconvo1_l5);

        Map<String, ChoiceOutcome> nathanconvo2_l5 = new LinkedHashMap<>(); // Pattern Case 5
        nathanconvo2_l5.put("A", new ChoiceOutcome("Nathan: \"...If I ever accept that, I'm not me anymore.\"", "TURN_OFF2"));
        nathanconvo2_l5.put("B", new ChoiceOutcome("Nathan: \"You still think anyone's coming? That's sweet... and delusional.\"", "TURN_OFF"));
        nathanconvo2_l5.put("C", new ChoiceOutcome("Nathan: \"...That's the answer I hoped for. You remind me why I stayed human.\"", "TRUST"));
        nathanconvo2_l5.put("D", new ChoiceOutcome("Nathan: \"Cold, but right. Logic over panic. I can work with that.\"", "CHARISMA"));
        nathanconvo2_l5.put("E", new ChoiceOutcome("Nathan: \"You sound too sure... it's dangerous when I start believing you.\"", "TURN_ON"));
        nathanLevel5.put(2, nathanconvo2_l5);

        Map<String, ChoiceOutcome> nathanconvo3_l5 = new LinkedHashMap<>(); // Pattern Case 6
        nathanconvo3_l5.put("A", new ChoiceOutcome("Nathan: \"...You're too good for this world, you know that?\"", "TRUST"));
        nathanconvo3_l5.put("B", new ChoiceOutcome("Nathan: \"You sound like me on my worst day. Don't lose your soul over this.\"", "TURN_ON"));
        nathanconvo3_l5.put("C", new ChoiceOutcome("Nathan: \"Got it. No fear. Just movement.\"", "CHARISMA"));
        nathanconvo3_l5.put("D", new ChoiceOutcome("Nathan: \"If thinking's a liability, maybe this isn't worth surviving.\"", "TURN_OFF2"));
        nathanconvo3_l5.put("E", new ChoiceOutcome("Nathan: \"Don't play hero. I've patched up enough of those already.\"", "TURN_OFF"));
        nathanLevel5.put(3, nathanconvo3_l5);

        nathanLevels.put(5, nathanLevel5);

        // FIX: Add the completed maps to the main responseMap
        responseMap.get("Nathan").putAll(nathanLevels);



        // ====== AVY RESPONSES ======
        Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>> avyLevels = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> avyLevel1 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> avyLevel2 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> avyLevel3 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> avyLevel4 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> avyLevel5 = new HashMap<>();

        // --- AVY LEVEL 1 ---
        Map<String, ChoiceOutcome> avyconvo1_l1 = new LinkedHashMap<>();  // Pattern Case 1
        avyconvo1_l1.put("A", new ChoiceOutcome("Avy: \"Confident, huh? Guess I found someone who won't freeze up.\"", "CHARISMA"));
        avyconvo1_l1.put("B", new ChoiceOutcome("Avy: \"Practical. I like that. Maybe you'll balance me out when I get too loud.\"", "TRUST"));
        avyconvo1_l1.put("C", new ChoiceOutcome("Avy: \"...Hah. That's the thing, isn't it? Ordinary people turning out stronger than they thought. I respect that.\"", "TURN_ON"));
        avyconvo1_l1.put("D", new ChoiceOutcome("Avy: \"Oh? Then one day we'll race, zombies as cheerleaders.\"", "NEUTRAL"));
        avyconvo1_l1.put("E", new ChoiceOutcome("Avy: \"...Sheesh, touchy. Fine, we'll move.\"", "TURN_OFF"));
        avyLevel1.put(1, avyconvo1_l1);

        Map<String, ChoiceOutcome> avyconvo2_l1 = new LinkedHashMap<>(); // Pattern Case 2
        avyconvo2_l1.put("A", new ChoiceOutcome("Avy: \"Whoa... that's deep. Guess I better not let you down either.\"", "TRUST"));
        avyconvo2_l1.put("B", new ChoiceOutcome("Avy: \"H-Hey... don't make me blush mid-apocalypse. That means more than you know.\"", "TURN_ON"));
        avyconvo2_l1.put("C", new ChoiceOutcome("Avy: \"Ooo, tough talk! Don't let me see you crumble later.\"", "CHARISMA"));
        avyconvo2_l1.put("D", new ChoiceOutcome("Avy: \"Hmm. Not the smartest, but hey, it's working so far.\"", "NEUTRAL"));
        avyconvo2_l1.put("E", new ChoiceOutcome("Avy: \"...Well, at least you're honest. But yikes, not reassuring.\"", "TURN_OFF"));
        avyLevel1.put(2, avyconvo2_l1);

        Map<String, ChoiceOutcome> avyconvo3_l1 = new LinkedHashMap<>(); // Pattern Case 3
        avyconvo3_l1.put("A", new ChoiceOutcome("Avy: \"...That's... fair. I didn't realize how much it helps having backup. Thanks.\"", "TURN_ON"));
        avyconvo3_l1.put("B", new ChoiceOutcome("Avy: \"Okay, action hero. Let's hope you're not all talk.\"", "CHARISMA"));
        avyconvo3_l1.put("C", new ChoiceOutcome("Avy: \"Nice! Fear as fuel. That's the kind of mindset I like.\"", "TRUST"));
        avyconvo3_l1.put("D", new ChoiceOutcome("Avy: \"Fair. Denial's a coping strategy too, I guess.\"", "NEUTRAL"));
        avyconvo3_l1.put("E", new ChoiceOutcome("Avy: \"...Geez. Flattery during a zombie outbreak? Now's not the time.\"", "TURN_OFF"));
        avyLevel1.put(3, avyconvo3_l1);

        avyLevels.put(1, avyLevel1);

        // --- AVY LEVEL 2 ---
        // Dialogue 1: "...keep your head up..."
        Map<String, ChoiceOutcome> avyconvo3_l2  = new LinkedHashMap<>(); // Pattern Case 1
        avyconvo3_l2.put("A", new ChoiceOutcome("Avy: \"Ooo, confident! You'd have done great on my old team or in my office.\"", "CHARISMA"));
        avyconvo3_l2.put("B", new ChoiceOutcome("Avy: \"That's the way. You fight the feeling before it swallows you.\"", "TRUST"));
        avyconvo3_l2.put("C", new ChoiceOutcome("Avy: \"You're gonna make me soft at this rate. And I kinda hate that... but not really.\"", "TURN_ON"));
        avyconvo3_l2.put("D", new ChoiceOutcome("Avy: \"Ha! Napping in the middle of chaos? Now that's talent.\"", "NEUTRAL"));
        avyconvo3_l2.put("E", new ChoiceOutcome("Avy: \"..Don't talk like that. That's dangerous in more ways than one.\"", "TURN_OFF"));
        avyLevel2.put(1, avyconvo3_l2 );

        // Dialogue 2: "...miss most..."
        Map<String, ChoiceOutcome> avyconvo2_l2 = new LinkedHashMap<>(); // Pattern Case 4
        avyconvo2_l2.put("A", new ChoiceOutcome("Avy: \"..Maybe, but saying it like that? Feels like you're giving up on all of it. I can't think that way.\"", "TURN_OFF"));
        avyconvo2_l2.put("B", new ChoiceOutcome("Avy: \"...Yeah. Same here. People were the real home, not the walls.\"", "TRUST"));
        avyconvo2_l2.put("C", new ChoiceOutcome("Avy: \"Pfft! Don't tempt me. I'd trade my old paycheck for a decent burger right now.\"", "NEUTRAL"));
        avyconvo2_l2.put("D", new ChoiceOutcome("Avy: \"That... hits hard. I miss that too. More than anything.\"", "TURN_ON"));
        avyconvo2_l2.put("E", new ChoiceOutcome("Avy: \"Ah, so you're wired like me. Guess some of us will never outgrow it.\"", "CHARISMA"));
        avyLevel2.put(2, avyconvo2_l2);

        // Dialogue 3: "...feel useless..."
        Map<String, ChoiceOutcome> avyconvo1_l2 = new LinkedHashMap<>(); // Pattern Case 3
        avyconvo1_l2.put("A", new ChoiceOutcome("Avy: \"Wait, me? Ha... smooth. Careful, I might actually believe you.\"", "TURN_ON"));
        avyconvo1_l2.put("B", new ChoiceOutcome("Avy: \"Hey! That's my move too. Guess we'll be the comedy duo.\"", "CHARISMA"));
        avyconvo1_l2.put("C", new ChoiceOutcome("Avy: \"That's... actually solid. You're not just surviving for yourself. Respect.\"", "TRUST"));
        avyconvo1_l2.put("D", new ChoiceOutcome("Avy: \"...That's rough. But I get it. Sometimes not thinking is the only way to keep moving.\"", "NEUTRAL"));
        avyconvo1_l2.put("E", new ChoiceOutcome("Avy: \"...Ouch. Didn't think you'd call me out like that.\"", "TURN_OFF"));
        avyLevel2.put(3, avyconvo1_l2);

        avyLevels.put(2, avyLevel2);

        // --- AVY LEVEL 3 ---
        Map<String, ChoiceOutcome> avyconvo1_l3 = new LinkedHashMap<>(); // Pattern Case 5
        avyconvo1_l3.put("A", new ChoiceOutcome("Avy: \"...No. Don't say that. If you stop caring, then what's even the point of fighting?\"", "TURN_OFF2"));
        avyconvo1_l3.put("B", new ChoiceOutcome("Avy: \"Tch, don't say stuff like that when I'm sweaty.\"", "TURN_OFF"));
        avyconvo1_l3.put("C", new ChoiceOutcome("Avy: \"Brutal... but true. You're sharp when it counts.\"", "TRUST"));
        avyconvo1_l3.put("D", new ChoiceOutcome("Avy: \"Hah! Cocky, huh? Guess that confidence is contagious.\"", "CHARISMA"));
        avyconvo1_l3.put("E", new ChoiceOutcome("Avy: \"Hah! Guilty. Guess you're my unofficial babysitter now.\"", "TURN_ON"));
        avyLevel3.put(1, avyconvo1_l3);

        Map<String, ChoiceOutcome> avyconvo2_l3 = new LinkedHashMap<>(); // Pattern Case 5
        avyconvo2_l3.put("A", new ChoiceOutcome("Avy: \"...Wow. That's bleak. You really think nothing's ever gonna change?\"", "TURN_OFF2"));
        avyconvo2_l3.put("B", new ChoiceOutcome("Avy: \"...That's... cynical. I get why you'd say that, but it still stings.\"", "TURN_OFF"));
        avyconvo2_l3.put("C", new ChoiceOutcome("Avy: \"Damn... that's heavier than my old boss's deadlines. But you're right.\"", "TRUST"));
        avyconvo2_l3.put("D", new ChoiceOutcome("Avy: \"Ooo, big words. You sound like someone who could give a killer pep talk.\"", "CHARISMA"));
        avyconvo2_l3.put("E", new ChoiceOutcome("Avy: \"You're ridiculous... and I kinda like it.\"", "TURN_ON"));
        avyLevel3.put(2, avyconvo2_l3 );

        Map<String, ChoiceOutcome> avyconvo3_l3  = new LinkedHashMap<>(); // Pattern Case 6
        avyconvo3_l3.put("A", new ChoiceOutcome("Avy: \"Yeah... that makes sense. At least it means we're still changing, not stuck.\"", "TRUST"));
        avyconvo3_l3.put("B", new ChoiceOutcome("Avy: \"...You really think that about me? Careful, I might start leaning on you.\"", "TURN_ON"));
        avyconvo3_l3.put("C", new ChoiceOutcome("Avy: \"Pfft, don't flatter me. But... okay, maybe I needed to hear that.\"", "CHARISMA"));
        avyconvo3_l3.put("D", new ChoiceOutcome("Avy: \"...Don't say that. If you've already decided we're doomed, what's the point of fighting?\"", "TURN_OFF2"));
        avyconvo3_l3.put("E", new ChoiceOutcome("Avy: \"...That hits too close. Don't make me start thinking like that, too.\"", "TURN_OFF"));
        avyLevel3.put(3, avyconvo3_l3  );

        avyLevels.put(3, avyLevel3);

        // --- AVY LEVEL 4 ---
        Map<String, ChoiceOutcome> avyconvo1_l4 = new LinkedHashMap<>();  // Pattern Case 6
        avyconvo1_l4.put("A", new ChoiceOutcome("Avy: \"Hmph... you're too careful. But fine, maybe that's why we haven't died yet.\"", "TRUST"));
        avyconvo1_l4.put("B", new ChoiceOutcome("Avy: \"Finally! Someone gets it. See? We're on the same wavelength.\"", "TURN_ON"));
        avyconvo1_l4.put("C", new ChoiceOutcome("Avy: \"Bold talk. Dangerous, but bold.\"", "CHARISMA"));
        avyconvo1_l4.put("D", new ChoiceOutcome("Avy: \"...Excuse me? Wow. Guess I know what you really think.\"", "TURN_OFF2"));
        avyconvo1_l4.put("E", new ChoiceOutcome("Avy: \"What?! Don't joke like that! ...You're not joking, are you?\"", "TURN_OFF"));
        avyLevel4.put(1, avyconvo1_l4);

        Map<String, ChoiceOutcome> avyconvo2_l4 = new LinkedHashMap<>(); // Pattern Case 5
        avyconvo2_l4.put("A", new ChoiceOutcome("Avy: \"Wow. Thanks for the pep talk. Next time, keep it to yourself.\"", "TURN_OFF2"));
        avyconvo2_l4.put("B", new ChoiceOutcome("Avy: \"...Don't baby me. I can handle it.\"", "TURN_OFF"));
        avyconvo2_l4.put("C", new ChoiceOutcome("Avy: \"Huh... maybe you're right. Hard to switch off my brain, though.\"", "TRUST"));
        avyconvo2_l4.put("D", new ChoiceOutcome("Avy: \"...Confident much? Heh. I guess I don't hate hearing that.\"", "CHARISMA"));
        avyconvo2_l4.put("E", new ChoiceOutcome("Avy: \"...That's... actually smart. You get me more than most do.\"", "TURN_ON"));
        avyLevel4.put(2, avyconvo2_l4);

        Map<String, ChoiceOutcome> avyconvo3_l4  = new LinkedHashMap<>(); // Pattern Case 6
        avyconvo3_l4.put("A", new ChoiceOutcome("Avy: \"...You'd really risk going hungry yourself? Hm. Guess that's why people rely on you.\"", "TRUST"));
        avyconvo3_l4.put("B", new ChoiceOutcome("Avy: \"...Careful with that. You'll make me start believing I can carry all of this.\"", "TURN_ON"));
        avyconvo3_l4.put("C", new ChoiceOutcome("Avy: \"Hah... ruthless. But you're not wrong.\"", "CHARISMA"));
        avyconvo3_l4.put("D", new ChoiceOutcome("Avy: \"...Cold way to put it. People aren't numbers.\"", "TURN_OFF2"));
        avyconvo3_l4.put("E", new ChoiceOutcome("Avy: \"...So you're saying we just... sit on our hands? That doesn't sit right with me.\"", "TURN_OFF"));
        avyLevel4.put(3, avyconvo3_l4 );

        avyLevels.put(4, avyLevel4);

        // --- AVY LEVEL 5 ---
        Map<String, ChoiceOutcome> avyconvo1_l5 = new LinkedHashMap<>(); // Pattern Case 5
        avyconvo1_l5.put("A", new ChoiceOutcome("Avy: \"...So you think I'm a liability? Not what I needed to hear right now.\"", "TURN_OFF2"));
        avyconvo1_l5.put("B", new ChoiceOutcome("Avy: \"...Maybe you mean that as a compliment, but it feels like you don't trust me.\"", "TURN_OFF"));
        avyconvo1_l5.put("C", new ChoiceOutcome("Avy: \"...Damn. You mean that, don't you? I believe you.\"", "TRUST"));
        avyconvo1_l5.put("D", new ChoiceOutcome("Avy: \"Hah! Now that's the kind of confidence I need.\"", "CHARISMA"));
        avyconvo1_l5.put("E", new ChoiceOutcome("Avy: \"...Say that again when we're safe, and I'll give you my answer.\"", "TURN_ON"));
        avyLevel5.put(1, avyconvo1_l5);

        Map<String, ChoiceOutcome> avyconvo2_l5 = new LinkedHashMap<>(); // Pattern Case 5
        avyconvo2_l5.put("A", new ChoiceOutcome("Avy: \"...That's not bravery, that's suicide. Don't make me follow you into a death trap.\"", "TURN_OFF2"));
        avyconvo2_l5.put("B", new ChoiceOutcome("Avy: \"...I get the logic, but it sounds like you're already deciding who's expendable. Don't.\"", "TURN_OFF"));
        avyconvo2_l5.put("C", new ChoiceOutcome("Avy: \"...Logical. Doesn't mean I like it, but fine.-\"", "TRUST"));
        avyconvo2_l5.put("D", new ChoiceOutcome("Avy: \"...Heh. Cold way to put it, but... you're right. I needed that push.\"", "CHARISMA"));
        avyconvo2_l5.put("E", new ChoiceOutcome("Avy: \"...Why'd you have to say it like that? Now I'm not letting you out of my sight.\"", "TURN_ON"));
        avyLevel5.put(2, avyconvo2_l5);

        Map<String, ChoiceOutcome> avyconvo3_l5  = new LinkedHashMap<>(); // Pattern Case 6
        avyconvo3_l5.put("A", new ChoiceOutcome("Avy: \"...Alright. Just... don't take your eyes off me.\"", "TRUST"));
        avyconvo3_l5.put("B", new ChoiceOutcome("Avy: \"...Damn you. Saying stuff like that makes me reckless.\"", "TURN_ON"));
        avyconvo3_l5.put("C", new ChoiceOutcome("Avy: \"...Heh. Harsh, but... yeah. That's the push I needed.\"", "CHARISMA"));
        avyconvo3_l5.put("D", new ChoiceOutcome("Avy: \"...And if it snaps, I'm left watching you fall. Don't call that protecting me.\"", "TURN_OFF2"));
        avyconvo3_l5.put("E", new ChoiceOutcome("Avy: \"...I get it, but pushing me like that doesn't help. I need focus, not pressure.\"", "TURN_OFF"));
        avyLevel5.put(3, avyconvo3_l5 );

        avyLevels.put(5, avyLevel5);

        responseMap.get("Avy").putAll(avyLevels);

        // ====== MARINA RESPONSES ======
        Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>> marinaLevels = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> marinaLevel1 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> marinaLevel2 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> marinaLevel3 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> marinaLevel4 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> marinaLevel5 = new HashMap<>();

        // --- MARINA LEVEL 1 ---
        Map<String, ChoiceOutcome> marinaconvo1_l1 = new LinkedHashMap<>();  // Pattern Case 1
        marinaconvo1_l1.put("A", new ChoiceOutcome("Marina: \"...You make it sound simple. I panic too easily, so... maybe calm is what I need near me.\"", "CHARISMA"));
        marinaconvo1_l1.put("B", new ChoiceOutcome("Marina: \"...That's... braver than just surviving. Maybe...I want to believe that too.\"", "TRUST"));
        marinaconvo1_l1.put("C", new ChoiceOutcome("Marina: \"...You're...amazing. Most people only think about themselves now. It's... kind of nice hearing someone doesn't.(hiding her smile)\"", "TURN_ON"));
        marinaconvo1_l1.put("D", new ChoiceOutcome("Marina: \"...Heh. I get that. It's just food above anything else.\"", "NEUTRAL"));
        marinaconvo1_l1.put("E", new ChoiceOutcome("Marina: \"...That's... one way, I guess. (she glances away). We can move then.\"", "TURN_OFF"));
        marinaLevel1.put(1, marinaconvo1_l1);

        Map<String, ChoiceOutcome> marinaconvo2_l1 = new LinkedHashMap<>();  // Pattern Case 2
        marinaconvo2_l1.put("A", new ChoiceOutcome("Marina: \"...R-Really?(her shoulders relax slightly) ...I thought I'm imagining things. Thanks for trusting\"", "TRUST"));
        marinaconvo2_l1.put("B", new ChoiceOutcome("Marina: \"...M-me?... I don't usually hear that... but hearing you say that... it feels... nice. (she looks away, hiding a small smile)\"", "TURN_ON"));
        marinaconvo2_l1.put("C", new ChoiceOutcome("Marina: \"...You sound so confident. I feel safer with you here.\"", "CHARISMA"));
        marinaconvo2_l1.put("D", new ChoiceOutcome("Marina: \"...O-Okay...I'll follow along. (she glances at the footprints again)\"", "NEUTRAL"));
        marinaconvo2_l1.put("E", new ChoiceOutcome("Marina: \"...Well yeah...I-I guess you're right (glances down)...I just thought noticing things could help.\"", "TURN_OFF"));
        marinaLevel1.put(2, marinaconvo2_l1);

        Map<String, ChoiceOutcome> marinaconvo3_l1 = new LinkedHashMap<>(); // Pattern Case 3
        marinaconvo3_l1.put("A", new ChoiceOutcome("Marina: \"...I never thought someone would say that to me. Knowing you're here...it actually makes me feel... braver...I got your back too. (cheeks slightly pink)\"", "TURN_ON"));
        marinaconvo3_l1.put("B", new ChoiceOutcome("Marina: \"...I think you're right... Having you here... really helps a lot.\"", "CHARISMA"));
        marinaconvo3_l1.put("C", new ChoiceOutcome("Marina: \"... I don't usually get that kind of trust.. I'll do my best to not let you down.\"", "TRUST"));
        marinaconvo3_l1.put("D", new ChoiceOutcome("Marina: \"...I guess that's true. I-I suppose there's not much else we can do.\"", "NEUTRAL"));
        marinaconvo3_l1.put("E", new ChoiceOutcome("Marina: \"... I guess that makes sense. Focusing on ourselves...(slowly looks away with a heavy sigh)\"", "TURN_OFF"));
        marinaLevel1.put(3, marinaconvo3_l1);

        marinaLevels.put(1, marinaLevel1);

        // --- MARINA LEVEL 2 ---
        Map<String, ChoiceOutcome> marinaconvo1_l2 = new LinkedHashMap<>();  // Pattern Case 3
        marinaconvo1_l2.put("A", new ChoiceOutcome("Marina: \"I...I think that could work. It actually helps to have someone leading the way. You... you seem really reliable in a situation like this.(smiles)\"", "TURN_ON"));
        marinaconvo1_l2.put("B", new ChoiceOutcome("Marina: \"...You're right...It's better to plan things ahead than being...reckless.\"", "CHARISMA"));
        marinaconvo1_l2.put("C", new ChoiceOutcome("Marina: \"(blushing, she quickly looks away) I...I should probably examine the place more...make sure everything's safe. Y-Yeah, that...\"", "TRUST"));
        marinaconvo1_l2.put("D", new ChoiceOutcome("Marina: \"...O-okay, got it...I guess we can rest for now.\"", "NEUTRAL"));
        marinaconvo1_l2.put("E", new ChoiceOutcome("Marina: \"...O-okay...Let's just stay alert be careful to...not attract attention. (She immediately turns back, scanning the room while clutching her notebook tightly)\"", "TURN_OFF"));
        marinaLevel2.put(1, marinaconvo1_l2);


        Map<String, ChoiceOutcome> marinaconvo2_l2 = new LinkedHashMap<>();  // Pattern Case 4
        marinaconvo2_l2.put("A", new ChoiceOutcome("Marina: \"...I-It's just a precaution, we can adjust it if it doesn't... work. (she bites her lip nervously) But if you don't want it... I-I'll respect your decision.\"", "TURN_OFF"));
        marinaconvo2_l2.put("B", new ChoiceOutcome("Marina: \"(smiling with relief) I... I'll make sure it's as safe and fair as possible. You don't have to worry.\"", "TRUST"));
        marinaconvo2_l2.put("C", new ChoiceOutcome("Marina: \" Wow...that's really thoughtful of you. . If it's truly okay with you, then... I'll take the shift after you. (She gives a slight, shy smile.)\"", "NEUTRAL"));
        marinaconvo2_l2.put("D", new ChoiceOutcome("Marina: \"...I-I'm glad... uh, you liked it. Woah, I don't easily get tired though...(she bites her lip, cheeks turning pink)...B-but... if I ever do, I might... uh... call on you\"", "TURN_ON"));
        marinaconvo2_l2.put("E", new ChoiceOutcome("Marina: \"...Uhmm...I can take it so you can rest first. (she glances down nervously)\"", "CHARISMA"));
        marinaLevel2.put(2, marinaconvo2_l2);


        Map<String, ChoiceOutcome> marinaconvo3_l2 = new LinkedHashMap<>();  // Pattern Case 1
        marinaconvo3_l2.put("A", new ChoiceOutcome("Marina: \"...Traveling...that sounds...amazing.  I guess it's nice to hear someone else had dreams too... and that you still try to take things one step at a time.\"", "CHARISMA"));
        marinaconvo3_l2.put("B", new ChoiceOutcome("Marina: \"...Yeah... I can see that. (smiles) You make it sound... easier to focus on what matters now. I... I think I needed to hear that\"", "TRUST"));
        marinaconvo3_l2.put("C", new ChoiceOutcome("Marina: \"...R-Really? I... I think that someday, when all this ends, you could still... make that dream a reality. (She tucks a strand of hair behind her ear, avoiding direct eye contact, but her gaze lingers for a moment.)\"", "TURN_ON"));
        marinaconvo3_l2.put("D", new ChoiceOutcome("Marina: \"...Traveling...that sounds... ice(nods slowly)\"", "NEUTRAL"));
        marinaconvo3_l2.put("E", new ChoiceOutcome("Marina: \"Y-Yeah... survival... that's... important. I just... thought it was nice to remember, even a little.\"", "TURN_OFF"));
        marinaLevel2.put(3, marinaconvo3_l2);

        marinaLevels.put(2, marinaLevel2);

        // --- MARINA LEVEL 3 ---
        Map<String, ChoiceOutcome> marinaconvo1_l3 = new LinkedHashMap<>(); // Pattern Case 5
        marinaconvo1_l3.put("A", new ChoiceOutcome("Marina: \"...I-I can handle that... You're right. I just... froze. I'll do better next time. (gently wiping a tear forming in her right eye)\"", "TURN_OFF2"));
        marinaconvo1_l3.put("B", new ChoiceOutcome("Marina: \"(stammering, adjusting her glasses) O-Oh... Uhm... I'll try not to freeze up again... S-Sorry!\"", "TURN_OFF"));
        marinaconvo1_l3.put("C", new ChoiceOutcome("Marina: \"Th-thank you for trusting me.\"", "TRUST"));
        marinaconvo1_l3.put("D", new ChoiceOutcome("Marina: \"(adjusting glasses) Right. Together...we'll overcome obstacles.\"", "CHARISMA"));
        marinaconvo1_l3.put("E", new ChoiceOutcome("Marina: \"It's just... I recalled a map of this area I glanced at earlier. I remembered a potential escape route, though I wasn't entirely sure if it was safe. B-But it worked! (smiles softly)\"", "TURN_ON"));
        marinaLevel3.put(1, marinaconvo1_l3);

        Map<String, ChoiceOutcome> marinaconvo2_l3 = new LinkedHashMap<>(); // Pattern Case 5
        marinaconvo2_l3.put("A", new ChoiceOutcome("Marina: \"(lips trembling)...I... I was just trying to help. I-I'm trying my best.\"", "TURN_OFF2"));
        marinaconvo2_l3.put("B", new ChoiceOutcome("Marina: \"(stammering nervously)...I...I didn't mean to freeze at first. I'll... try harder...next time.\"", "TURN_OFF"));
        marinaconvo2_l3.put("C", new ChoiceOutcome("Marina: \"I'm just...glad I was of help. (smiles shyly)\"", "TRUST"));
        marinaconvo2_l3.put("D", new ChoiceOutcome("Marina: \"Y-you make it sound so simple...I should commend your confidence then.\"", "CHARISMA"));
        marinaconvo2_l3.put("E", new ChoiceOutcome("Marina: \"I-I remembered something about the skull being...fragile, s-so I panicked... I'm fine now, just...just scared something might happen to you. I couldn't handle that.(cheeks turning pink)\"", "TURN_ON"));
        marinaLevel3.put(2, marinaconvo2_l3 );

        Map<String, ChoiceOutcome> marinaconvo3_l3  = new LinkedHashMap<>(); // Pattern Case 6
        marinaconvo3_l3.put("A", new ChoiceOutcome("Marina: \"Th-thank you... I'll do my best to guide us safely. (flashing a small smile)\"", "TRUST"));
        marinaconvo3_l3.put("B", new ChoiceOutcome("Marina: \" I-I'm just... glad I could help...with my knowledge of the area.(smiles shyly with pink cheeks)...W-we should keep moving. (turned her back to hide her smile)\"", "TURN_ON"));
        marinaconvo3_l3.put("C", new ChoiceOutcome("Marina: \"(nodding with relief)...I won't let you down.\"", "CHARISMA"));
        marinaconvo3_l3.put("D", new ChoiceOutcome("Marina: \"...I...didn't mean to slow you down. I-I just wanted to help.(glances away with a heavy heart)\"", "TURN_OFF2"));
        marinaconvo3_l3.put("E", new ChoiceOutcome("Marina: \"I...I just thought it might be safer this way.\"", "TURN_OFF"));
        marinaLevel3.put(3, marinaconvo3_l3  );

        marinaLevels.put(3, marinaLevel3);

        // --- MARINA LEVEL 4 ---
        Map<String, ChoiceOutcome> marinaconvo1_l4 = new LinkedHashMap<>();  // Pattern Case 6
        marinaconvo1_l4.put("A", new ChoiceOutcome("Marina: \"Yeah...L-let's just be careful okay?\"", "TRUST"));
        marinaconvo1_l4.put("B", new ChoiceOutcome("Marina: \"(blushing hard) I-I...I know you'll...keep us safe... I just...hope I can do my part too...\"", "TURN_ON"));
        marinaconvo1_l4.put("C", new ChoiceOutcome("Marina: \"Y-yes...that sounds smart. Let us just observe...for now.\"", "CHARISMA"));
        marinaconvo1_l4.put("D", new ChoiceOutcome("Marina: \"....Y-you may call it like that. B-but...I'm just being careful. What's wrong with that?\"", "TURN_OFF2"));
        marinaconvo1_l4.put("E", new ChoiceOutcome("Marina: \"... It's not being weak...It's being highly attentive.\"", "TURN_OFF"));
        marinaLevel4.put(1, marinaconvo1_l4);

        Map<String, ChoiceOutcome> marinaconvo2_l4 = new LinkedHashMap<>(); // Pattern Case 5
        marinaconvo2_l4.put("A", new ChoiceOutcome("Marina: \"... I'm not overcomplicating things. I'm trying my best to be useful...to you.\"", "TURN_OFF2"));
        marinaconvo2_l4.put("B", new ChoiceOutcome("Marina: \"...I'm just trying to do my part...to be someone who's useful.\"", "TURN_OFF"));
        marinaconvo2_l4.put("C", new ChoiceOutcome("Marina: \"R-Really?... I'm glad you think so. Don't worry, I'm still not sleepy. (smiles)\"", "TRUST"));
        marinaconvo2_l4.put("D", new ChoiceOutcome("Marina: \"...Th-that's great. I also feel the same thing.\"", "CHARISMA"));
        marinaconvo2_l4.put("E", new ChoiceOutcome("Marina: \"I didn't think...anyone would notice...b-but...I'm glad...you're here too...I feel much safer. (glances up briefly with a smile)\"", "TURN_ON"));
        marinaLevel4.put(2, marinaconvo2_l4);

        Map<String, ChoiceOutcome> marinaconvo3_l4  = new LinkedHashMap<>(); // Pattern Case 6
        marinaconvo3_l4.put("A", new ChoiceOutcome("Marina: \"...Y-You really think so?... If that's for the best. I'll trust your words.\"", "TRUST"));
        marinaconvo3_l4.put("B", new ChoiceOutcome("Marina: \"...Y-Your words...they...they really mean a lot to me. Th-thank you for always being here...by my side. (blushing, looking down)\"", "TURN_ON"));
        marinaconvo3_l4.put("C", new ChoiceOutcome("Marina: \"R-right...Let's do this.\"", "CHARISMA"));
        marinaconvo3_l4.put("D", new ChoiceOutcome("Marina: \"...I am...it's just that I think being careful is more important\"", "TURN_OFF2"));
        marinaconvo3_l4.put("E", new ChoiceOutcome("Marina: \"...I-I'm sorry...I'll try to keep my thoughts... to myself...\"", "TURN_OFF"));
        marinaLevel4.put(3, marinaconvo3_l4 );

        marinaLevels.put(4, marinaLevel4);

        // --- MARINA LEVEL 5 ---
        Map<String, ChoiceOutcome> marinaconvo1_l5 = new LinkedHashMap<>(); // Pattern Case 5
        marinaconvo1_l5.put("A", new ChoiceOutcome("Marina: \"...D-don't worry, we'll be there... soon. (heavy sigh)\"", "TURN_OFF2"));
        marinaconvo1_l5.put("B", new ChoiceOutcome("Marina: \"...O-okay then.\"", "TURN_OFF"));
        marinaconvo1_l5.put("C", new ChoiceOutcome("Marina: \"...I just want us to make it out safely.\"", "TRUST"));
        marinaconvo1_l5.put("D", new ChoiceOutcome("Marina: \"Together. (smiles)\"", "CHARISMA"));
        marinaconvo1_l5.put("E", new ChoiceOutcome("Marina: \"I-I'm really glad I'm with you too..\"", "TURN_ON"));
        marinaLevel5.put(1, marinaconvo1_l5);

        Map<String, ChoiceOutcome> marinaconvo2_l5 = new LinkedHashMap<>(); // Pattern Case 5
        marinaconvo2_l5.put("A", new ChoiceOutcome("Marina: \"...I-I can...I just thought...maybe you'd explain it better...b-but I can try myself...\"", "TURN_OFF2"));
        marinaconvo2_l5.put("B", new ChoiceOutcome("Marina: \"I...I thought my plan could actually help...I didn't mean to bother anyone...\"", "TURN_OFF"));
        marinaconvo2_l5.put("C", new ChoiceOutcome("Marina: \"Th-thank you, I know I can always count on...you.\"", "TRUST"));
        marinaconvo2_l5.put("D", new ChoiceOutcome("Marina: \"Heh...Th-thank you for saying that.\"", "CHARISMA"));
        marinaconvo2_l5.put("E", new ChoiceOutcome("Marina: \"I... Is that so? That... that makes me a little flustered. Th-Thank you...\"", "TURN_ON"));
        marinaLevel5.put(2, marinaconvo2_l5);

        Map<String, ChoiceOutcome> marinaconvo3_l5 = new LinkedHashMap<>(); // Pattern Case 6
        marinaconvo3_l5.put("A", new ChoiceOutcome("Marina: \"...I'm thankful...for your trust.\"", "TRUST"));
        marinaconvo3_l5.put("B", new ChoiceOutcome("Marina: \"...Y-you also got me...always got me.\"", "TURN_ON"));
        marinaconvo3_l5.put("C", new ChoiceOutcome("Marina: \"L-let's get through this!.\"", "CHARISMA"));
        marinaconvo3_l5.put("D", new ChoiceOutcome("Marina: \"I-I get it, but don't...say it like that... W-why can't you see my efforts?\"", "TURN_OFF2"));
        marinaconvo3_l5.put("E", new ChoiceOutcome("Marina: \"... I-I'm not slacking!... I-I'm clearly doing my best!\"", "TURN_OFF"));
        marinaLevel5.put(3, marinaconvo3_l5);

        marinaLevels.put(5, marinaLevel5);

        responseMap.get("Marina").putAll(marinaLevels);


        // ====== KIM RESPONSES ======
        Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>> kimLevels = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> kimLevel1 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> kimLevel2 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> kimLevel3 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> kimLevel4 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> kimLevel5 = new HashMap<>();

        // --- KIM LEVEL 1 ---
        Map<String, ChoiceOutcome> kimconvo1_l1 = new LinkedHashMap<>(); // Pattern Case 1
        kimconvo1_l1.put("A", new ChoiceOutcome("Kim: \"Please... don't say things like that. You'll make me blush. But... if I can make you feel safe, even a little, then I'm glad.\"", "CHARISMA"));
        kimconvo1_l1.put("B", new ChoiceOutcome("Kim: \"I'm glad. Even one night of rest can keep hope alive. Maybe we can share that hope, just for a little while.\"", "TRUST"));
        kimconvo1_l1.put("C", new ChoiceOutcome("Kim: \"Oh! You certainly know how to surprise a person. I... I wasn't expecting a compliment, but it's a welcome one.\"", "TURN_ON"));
        kimconvo1_l1.put("D", new ChoiceOutcome("Kim: \"Of course. Just for a bit... that's enough.\"", "NEUTRAL"));
        kimconvo1_l1.put("E", new ChoiceOutcome("Kim: \"I understand. The world made us afraid of each other. I only wanted to help.\"", "TURN_OFF"));
        kimLevel1.put(1, kimconvo1_l1);

        Map<String, ChoiceOutcome> kimconvo2_l1 = new LinkedHashMap<>(); // Pattern Case 2
        kimconvo2_l1.put("A", new ChoiceOutcome("Kim: \"Brave? I don't feel brave. But if I can ease even one person's pain... maybe I can still be who I was.\"", "TRUST"));
        kimconvo2_l1.put("B", new ChoiceOutcome("Kim: \"An... angel? You shouldn't say things like that in this world. But... thank you. You made me smile.\"", "TURN_ON"));
        kimconvo2_l1.put("C", new ChoiceOutcome("Kim: \"...You really know how to say things, don't you? I... needed to hear that.\"", "CHARISMA"));
        kimconvo2_l1.put("D", new ChoiceOutcome("Kim: \"Me too. It's hard to remember what 'normal' was like. Thank you for saying that.\"", "NEUTRAL"));
        kimconvo2_l1.put("E", new ChoiceOutcome("Kim: \"...Maybe. But if survival is all we have, then what makes us human anymore?\"", "TURN_OFF"));
        kimLevel1.put(2, kimconvo2_l1);

        Map<String, ChoiceOutcome> kimconvo3_l1 = new LinkedHashMap<>(); // Pattern Case 3
        kimconvo3_l1.put("A", new ChoiceOutcome("Kim: \"I'm not sure I'm that strong. But if you really want me here, then I'll stay right beside you. I'll try to be brave for both of us.\"", "TURN_ON"));
        kimconvo3_l1.put("B", new ChoiceOutcome("Kim: \"You'd really do that? That... actually helps. Talking makes it feel less heavy out here.\"", "CHARISMA"));
        kimconvo3_l1.put("C", new ChoiceOutcome("Kim: \"Thank you, it just feels safer when you're here. Like maybe I don't have to be so afraid after all.\"", "TRUST"));
        kimconvo3_l1.put("D", new ChoiceOutcome("Kim: \"Me too... it feels a little easier when we share it together.\"", "NEUTRAL"));
        kimconvo3_l1.put("E", new ChoiceOutcome("Kim: \"Oh... I see. Of course. You must be exhausted. I'll... I'll try to be quiet then. \"", "TURN_OFF"));
        kimLevel1.put(3, kimconvo3_l1);

        kimLevels.put(1, kimLevel1);

        // --- KIM LEVEL 2 ---
        Map<String, ChoiceOutcome> kimconvo1_l2 = new LinkedHashMap<>(); // Pattern Case 3
        kimconvo1_l2.put("A", new ChoiceOutcome("Kim: \"Together. That makes me feel less small. If you mean it, then I'll hold on to that tomorrow too.\"", "TURN_ON"));
        kimconvo1_l2.put("B", new ChoiceOutcome("Kim: \"A proper dinner? That's a wonderful picture to hold onto. I'd like that very much.\"", "CHARISMA"));
        kimconvo1_l2.put("C", new ChoiceOutcome("Kim: \"Thank you... it helps to hear that. It feels safer knowing you still believe peace is possible.\"", "TRUST"));
        kimconvo1_l2.put("D", new ChoiceOutcome("Kim: \"Living and not just surviving. I used to dream of that too. Maybe someday, even if it's far.\"", "NEUTRAL"));
        kimconvo1_l2.put("E", new ChoiceOutcome("Kim: \"Okay. I understand. I just  wanted to believe it could be different.\"", "TURN_OFF"));
        kimLevel2.put(1, kimconvo1_l2);

        Map<String, ChoiceOutcome> kimconvo2_l2 = new LinkedHashMap<>(); // Pattern Case 4
        kimconvo2_l2.put("A", new ChoiceOutcome("Kim: \"Okay... but it hurts to hear that. I just wanted to help, even if it was small.\"", "TURN_OFF"));
        kimconvo2_l2.put("B", new ChoiceOutcome("Kim: \"Alright... I'll let it be. I trust you know what you're doing ,  even if I don't fully understand it.\"", "TRUST"));
        kimconvo2_l2.put("C", new ChoiceOutcome("Kim: \"It is. I'm glad someone else understands that. It's a strange comfort to know I'm not alone with the silence.\"", "NEUTRAL"));
        kimconvo2_l2.put("D", new ChoiceOutcome("Kim: \"If your hands are the ones patching me up, I almost don’t mind bleeding.\"", "TURN_ON"));
        kimconvo2_l2.put("E", new ChoiceOutcome("Kim: \"You shouldn't hide your pain. Thank you for trusting me with it, even just a little.\"", "CHARISMA"));
        kimLevel2.put(2, kimconvo2_l2);

        Map<String, ChoiceOutcome> kimconvo3_l2 = new LinkedHashMap<>(); // Pattern Case 1
        kimconvo3_l2.put("A", new ChoiceOutcome("Kim: \"That means more than you know. Maybe that's how we start healing... not alone, but together.\"", "CHARISMA"));
        kimconvo3_l2.put("B", new ChoiceOutcome("Kim: \"It does. It helps knowing someone else carries the same weight. Thank you for not feeling like you have to hide that from me.\"", "TRUST"));
        kimconvo3_l2.put("C", new ChoiceOutcome("Kim: \"You always know how to make the fear smaller. I... trust you more than I should.\"", "TURN_ON"));
        kimconvo3_l2.put("D", new ChoiceOutcome("Kim: \"You're right. Another day surviving is still something worth holding on to.\"", "NEUTRAL"));
        kimconvo3_l2.put("E", new ChoiceOutcome("Kim: \"Maybe that's how you survive. But I can't turn mine off. I wish I could.\"", "TURN_OFF"));
        kimLevel2.put(3, kimconvo3_l2);

        kimLevels.put(2, kimLevel2);

        // --- KIM LEVEL 3 ---
        Map<String, ChoiceOutcome> kimconvo1_l3 = new LinkedHashMap<>(); // Pattern Case 5
        kimconvo1_l3.put("A", new ChoiceOutcome("Kim: \"I... I can't believe you would say that right now. I thought we were in this together.\"", "TURN_OFF2"));
        kimconvo1_l3.put("B", new ChoiceOutcome("Kim: \"No... we need to stick together. I don't want to face this alone, it would be too much.\"", "TURN_OFF"));
        kimconvo1_l3.put("C", new ChoiceOutcome("Kim: \"That makes sense. I'll follow your lead... I just hope I can keep up, but I want to try.\"", "TRUST"));
        kimconvo1_l3.put("D", new ChoiceOutcome("Kim: \"Okay... together then. It feels less frightening knowing you're by my side.\"", "CHARISMA"));
        kimconvo1_l3.put("E", new ChoiceOutcome("Kim: \"You... you really mean that? It's a lot of pressure, but I won't let you down. I'll stay right here. Let's show them we're not easy targets.\"", "TURN_ON"));
        kimLevel3.put(1, kimconvo1_l3);

        Map<String, ChoiceOutcome> kimconvo2_l3 = new LinkedHashMap<>(); // Pattern Case 5
        kimconvo2_l3.put("A", new ChoiceOutcome("Kim: \"No. I can't face that alone. I don't want to be alone. Please stay with me.\"", "TURN_OFF2"));
        kimconvo2_l3.put("B", new ChoiceOutcome("Kim: \"Please try not to give up. I'm scared too, but I want to believe we can find a way.\"", "TURN_OFF"));
        kimconvo2_l3.put("C", new ChoiceOutcome("Kim: \"You really believe that? It makes me feel stronger. I'll try to stay calm with you\"", "TRUST"));
        kimconvo2_l3.put("D", new ChoiceOutcome("Kim: \"You make it sound so simple and confident. I'll rely on you, then. Let's make sure we find what we need.\"", "CHARISMA"));
        kimconvo2_l3.put("E", new ChoiceOutcome("Kim: \"Careful... yes. I can be careful. I just hope I don't slow you down, but I want to help.\"", "TURN_ON"));
        kimLevel3.put(2, kimconvo2_l3);

        Map<String, ChoiceOutcome> kimconvo3_l3 = new LinkedHashMap<>(); // Pattern Case 6
        kimconvo3_l3.put("A", new ChoiceOutcome("Kim: \"A smile? Even now? That's quite a goal, but I like it. Knowing you're focused on that makes everything a little brighter.\"", "CHARISMA"));
        kimconvo3_l3.put("B", new ChoiceOutcome("Kim: \"Hearing that makes me feel safe. I can be brave with you here.\"", "TRUST"));
        kimconvo3_l3.put("C", new ChoiceOutcome("Kim: \"Careful! I like that. I'll follow your lead, just stay close.\"", "TURN_ON"));
        kimconvo3_l3.put("D", new ChoiceOutcome("Kim: \"I understand... I just need someone steady. Please stay.\"", "TURN_OFF2"));
        kimconvo3_l3.put("E", new ChoiceOutcome("Kim: \"That's a very lonely way to live. I hope you don't truly mean that. We can't survive out here if we start seeing each other as obstacles.\"", "TURN_OFF"));
        kimLevel3.put(3, kimconvo3_l3);

        kimLevels.put(3, kimLevel3);

        // --- KIM LEVEL 4 ---
        Map<String, ChoiceOutcome> kimconvo1_l4 = new LinkedHashMap<>(); // Pattern Case 6
        kimconvo1_l4.put("A", new ChoiceOutcome("Kim: \"Careful but not afraid... I like that. It makes me feel like maybe we can survive this.\"", "TRUST"));
        kimconvo1_l4.put("B", new ChoiceOutcome("Kim: \"...You sound so sure. If you believe in this, I'll believe too. Just... stay close.\"", "TURN_ON"));
        kimconvo1_l4.put("C", new ChoiceOutcome("Kim: \"Me? Keeping watch? I'll certainly try my best. Thank you for that confidence. It helps me focus.\"", "CHARISMA"));
        kimconvo1_l4.put("D", new ChoiceOutcome("Kim: \"That's... a very cold way to look at it. I'm afraid I can't keep up if you push me like that.\"", "TURN_OFF2"));
        kimconvo1_l4.put("E", new ChoiceOutcome("Kim: \"...That's what I'm afraid of. I don't want to run forever.\"", "TURN_OFF"));
        kimLevel4.put(1, kimconvo1_l4);

        Map<String, ChoiceOutcome> kimconvo2_l4 = new LinkedHashMap<>(); // Pattern Case 5
        kimconvo2_l4.put("A", new ChoiceOutcome("Kim: \"No! I can't agree to that. That's monstrous. If that's what you think, I truly don't know who you are anymore.\"", "TURN_OFF2"));
        kimconvo2_l4.put("B", new ChoiceOutcome("Kim: \"That feels wrong... I don't want us to become like that.\"", "TURN_OFF"));
        kimconvo2_l4.put("C", new ChoiceOutcome("Kim: \"A bridge... I like that. Thank you for holding onto that hope. I'll follow your lead and try to be open.\"", "TRUST"));
        kimconvo2_l4.put("D", new ChoiceOutcome("Kim: \"You're right... maybe I've just forgotten how to believe in people. But if you think it's worth it, I'll try to trust again.\"", "CHARISMA"));
        kimconvo2_l4.put("E", new ChoiceOutcome("Kim: \"Smart. Careful but not reckless... I feel safer knowing you think ahead.\"", "TURN_ON"));
        kimLevel4.put(2, kimconvo2_l4);

        Map<String, ChoiceOutcome> kimconvo3_l4 = new LinkedHashMap<>(); // Pattern Case 6
        kimconvo3_l4.put("A", new ChoiceOutcome("Kim: \"You're right. If you can stay calm, I'll try to follow your example.\"", "TRUST"));
        kimconvo3_l4.put("B", new ChoiceOutcome("Kim: \"That's... bold. I don't know if I could, but if you stand firm, I'll support you.\"", "TURN_ON"));
        kimconvo3_l4.put("C", new ChoiceOutcome("Kim: \"Charm? That's certainly more my speed than fighting. If you think we can manage that, I'll do my best to keep the mood light.\"", "CHARISMA"));
        kimconvo3_l4.put("D", new ChoiceOutcome("Kim: \"Attack first? Without warning? That sounds ruthless, and it scares me. I don't want us to become aggressors just to survive.\"", "TURN_OFF2"));
        kimconvo3_l4.put("E", new ChoiceOutcome("Kim: \"Leave? Just like that? I... I don't think I could abandon people so easily.\"", "TURN_OFF"));
        kimLevel4.put(3, kimconvo3_l4);

        kimLevels.put(4, kimLevel4);

        // --- KIM LEVEL 5 ---
        Map<String, ChoiceOutcome> kimconvo1_l5 = new LinkedHashMap<>(); // Pattern Case 5
        kimconvo1_l5.put("A", new ChoiceOutcome("Kim: \"I... I see. That scares me, but I understand. I just hope you won't give up on us.\"", "TURN_OFF2"));
        kimconvo1_l5.put("B", new ChoiceOutcome("Kim: \"Annoying? I... I'm trying to be strong, but that just makes me feel terrible. Why are you saying this to me?\"", "TURN_OFF"));
        kimconvo1_l5.put("C", new ChoiceOutcome("Kim: \"Together... that word sounds different when you say it. It makes me believe in a future, even if it's small.\"", "TRUST"));
        kimconvo1_l5.put("D", new ChoiceOutcome("Kim: \"Hearing that... It makes my chest feel lighter. Maybe I can imagine a future like that, if you're there too.\"", "CHARISMA"));
        kimconvo1_l5.put("E", new ChoiceOutcome("Kim: \"I like that... being practical with you feels right. I trust you to keep us safe.\"", "TURN_ON"));
        kimLevel5.put(1, kimconvo1_l5);

        Map<String, ChoiceOutcome> kimconvo2_l5 = new LinkedHashMap<>(); // Pattern Case 5
        kimconvo2_l5.put("A", new ChoiceOutcome("Kim: \"So that's how little I mean to you? I thought we were more than just survivors...\"", "TURN_OFF2"));
        kimconvo2_l5.put("B", new ChoiceOutcome("Kim: \"I understand, I guess. I only hoped we could rely on each other to get through this. It's much harder when you feel completely alone.\"", "TURN_OFF"));
        kimconvo2_l5.put("C", new ChoiceOutcome("Kim: \"Then I'll follow, no matter how shaky it gets. Just don't let go.\"", "TRUST"));
        kimconvo2_l5.put("D", new ChoiceOutcome("Kim: \"Your confidence feels like a lifeline. If I watch you, maybe I can believe I'll survive this.\"", "CHARISMA"));
        kimconvo2_l5.put("E", new ChoiceOutcome("Kim: \"...That's reckless... but it makes me feel less alone. I'll take that risk if it's with you.\"", "TURN_ON"));
        kimLevel5.put(2, kimconvo2_l5);

        Map<String, ChoiceOutcome> kimconvo3_l5 = new LinkedHashMap<>(); // Pattern Case 6
        kimconvo3_l5.put("A", new ChoiceOutcome("Kim: \"You always say it like you mean it... and that's why I believe you.\"", "TRUST"));
        kimconvo3_l5.put("B", new ChoiceOutcome("Kim: \"That's... unfair. Saying things like that when I'm terrified... but it makes me want to hold on tighter.\"", "TURN_ON"));
        kimconvo3_l5.put("C", new ChoiceOutcome("Kim: \"Your voice... it cuts through the panic. Okay... one step at a time.\"", "CHARISMA"));
        kimconvo3_l5.put("D", new ChoiceOutcome("Kim: \"...Even if you mean it, hearing that hurts more than the water ever could.\"", "TURN_OFF2"));
        kimconvo3_l5.put("E", new ChoiceOutcome("Kim: \"I... I can't help it. I'm trying. Please don't be angry with me. I'm so sorry.\"", "TURN_OFF"));
        kimLevel5.put(3, kimconvo3_l5);

        kimLevels.put(5, kimLevel5);

        responseMap.get("Kim").putAll(kimLevels);

        // ====== YUBIE RESPONSES ======
        Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>> yubieLevels = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> yubieLevel1 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> yubieLevel2 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> yubieLevel3 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> yubieLevel4 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> yubieLevel5 = new HashMap<>();


        // --- YUBIE LEVEL 1 ---
        Map<String, ChoiceOutcome> yubieconvo1_l1 = new LinkedHashMap<>(); // Pattern Case 1
        yubieconvo1_l1.put("A", new ChoiceOutcome("Yubie: \"Phew... okay, good. If you were a zombie, I was totally ready to... uh... scream and run.\"", "CHARISMA"));
        yubieconvo1_l1.put("B", new ChoiceOutcome("Yubie: \"Same! I've been talking to myself for hours just to stay sane. Now I can annoy you instead.\"", "TRUST"));
        yubieconvo1_l1.put("C", new ChoiceOutcome("Yubie: \"Hey, fear is a survival strategy. Bravery just gets you eaten first.\"", "TURN_ON"));
        yubieconvo1_l1.put("D", new ChoiceOutcome("Yubie: \"(Laughs nervously) Ha! Good one. Terrifying, but good.\"", "NEUTRAL"));
        yubieconvo1_l1.put("E", new ChoiceOutcome("Yubie: \"Wow. Dark. I'm starting to wonder if teaming up with you is scarier than the zombies.\"", "TURN_OFF"));
        yubieLevel1.put(1, yubieconvo1_l1);

        Map<String, ChoiceOutcome> yubieconvo2_l1 = new LinkedHashMap<>(); // Pattern Case 2
        yubieconvo2_l1.put("A", new ChoiceOutcome("Yubie: \"Guilty! And still terrible at it.\"", "TRUST"));
        yubieconvo2_l1.put("B", new ChoiceOutcome("Yubie: \"Oh... wow. Uh, wasn't expecting that.\"", "TURN_ON"));
        yubieconvo2_l1.put("C", new ChoiceOutcome("Yubie: \"What?! No way, you were good! I was just... very caffeinated.\"", "CHARISMA"));
        yubieconvo2_l1.put("D", new ChoiceOutcome("Yubie: \"Hmm... either you're lying, or I've finally lost my mind.\"", "NEUTRAL"));
        yubieconvo2_l1.put("E", new ChoiceOutcome("Yubie: \"(awkward chuckle and hurt smile) Ouch. Guess I wasn't exactly memorable. Story of my life.\"", "TURN_OFF"));
        yubieLevel1.put(2, yubieconvo2_l1);

        Map<String, ChoiceOutcome> yubieconvo3_l1 = new LinkedHashMap<>(); // Pattern Case 3
        yubieconvo3_l1.put("A", new ChoiceOutcome("Yubie: \"Partner...? I like the sound of that.\"", "TURN_ON"));
        yubieconvo3_l1.put("B", new ChoiceOutcome("Yubie: \"Heh. If sarcasm burns calories, I'll live forever.\"", "CHARISMA"));
        yubieconvo3_l1.put("C", new ChoiceOutcome("Yubie: \"That's... the nicest thing anyone's said to me in years.\"", "TRUST"));
        yubieconvo3_l1.put("D", new ChoiceOutcome("Yubie: \"Over my dead body. Which... I guess is literally what you just said.\"", "NEUTRAL"));
        yubieconvo3_l1.put("E", new ChoiceOutcome("Yubie: \"Damn, worst apocalypse buddy ever.\"", "TURN_OFF"));
        yubieLevel1.put(3, yubieconvo3_l1);

        yubieLevels.put(1, yubieLevel1);

        // --- YUBIE LEVEL 2 ---
        Map<String, ChoiceOutcome> yubieconvo1_l2 = new LinkedHashMap<>(); // Pattern Case 3
        yubieconvo1_l2.put("A", new ChoiceOutcome("Yubie: \"Wow,  I suddenly feel like the sidekick in a rom-com.\"", "TURN_ON"));
        yubieconvo1_l2.put("B", new ChoiceOutcome("Yubie: \"Really? That's... wow. Usually people roll their eyes at me.\"", "CHARISMA"));
        yubieconvo1_l2.put("C", new ChoiceOutcome("Yubie: \"(playfully smirks) Ha-ha. Joke's on you, I'd probably run first.\"", "TRUST"));
        yubieconvo1_l2.put("D", new ChoiceOutcome("Yubie: \"(chuckles) Yeah... five-star apocalypse hotel. No Wi-Fi though, huge dealbreaker.\"", "NEUTRAL"));
        yubieconvo1_l2.put("E", new ChoiceOutcome("Yubie: \"Right. Because splitting up always ends so well in horror movies.\"", "TURN_OFF"));
        yubieLevel2.put(1, yubieconvo1_l2);

        Map<String, ChoiceOutcome> yubieconvo2_l2 = new LinkedHashMap<>(); // Pattern Case 4
        yubieconvo2_l2.put("A", new ChoiceOutcome("Yubie: \"Oh. Right. Sorry I asked. I just... I guess talking about the past makes me feel less like I woke up in some bad movie.\"", "TURN_OFF"));
        yubieconvo2_l2.put("B", new ChoiceOutcome("Yubie: \"Yeah. It's kinda wild how everything can vanish overnight.\"", "TRUST"));
        yubieconvo2_l2.put("C", new ChoiceOutcome("Yubie: \"Then hey, maybe the apocalypse is our weird chance to be... un-average?\"", "NEUTRAL"));
        yubieconvo2_l2.put("D", new ChoiceOutcome("Yubie: \"Or, uh... who knows. Sometimes people show up when you least expect them.\"", "TURN_ON"));
        yubieconvo2_l2.put("E", new ChoiceOutcome("Yubie: \"Hey, same! If doomscrolling was a survival skill, we'd be invincible.\"", "CHARISMA"));
        yubieLevel2.put(2, yubieconvo2_l2);

        Map<String, ChoiceOutcome> yubieconvo3_l2 = new LinkedHashMap<>(); // Pattern Case 1
        yubieconvo3_l2.put("A", new ChoiceOutcome("Yubie: \"Heh. You're ridiculous... and somehow it worked. See? I'm laughing. Okay, more like wheezing, but it's something. Thanks.\"", "CHARISMA"));
        yubieconvo3_l2.put("B", new ChoiceOutcome("Yubie: \"Okay, okay, here's one. Why don't zombies ever eat comedians? ...Because they taste funny.” (He chuckles awkwardly, then looks at you.)...Don't pity laugh... okay maybe pity laugh, I'll take it.\"", "TRUST"));
        yubieconvo3_l2.put("C", new ChoiceOutcome("Yubie: \"If my laugh is worth something to you, then... I'll fight like hell to keep it alive.\"", "TURN_ON"));
        yubieconvo3_l2.put("D", new ChoiceOutcome("Yubie: \"Someday feels like a word from a fairy tale. But... if you're here when it happens? Then maybe I'll believe it.\"", "NEUTRAL"));
        yubieconvo3_l2.put("E", new ChoiceOutcome("Yubie: \"Childish, huh? Yeah, maybe. I guess I just thought... even in the worst horror movies, people still laughed. Even if it was their last laugh. But fine... serious mode activated.\"", "TURN_OFF"));
        yubieLevel2.put(3, yubieconvo3_l2);

        yubieLevels.put(2, yubieLevel2);

        // --- YUBIE LEVEL 3 ---
        Map<String, ChoiceOutcome> yubieconvo1_l3 = new LinkedHashMap<>(); // Pattern Case 5
        yubieconvo1_l3.put("A", new ChoiceOutcome("Yubie: \"Right. Got it. Next time I'll just stay out of your way...\"", "TURN_OFF2"));
        yubieconvo1_l3.put("B", new ChoiceOutcome("Yubie: \"Yeah. I guess that it was stupid. One wrong move and I'm a zombie chow.\"", "TURN_OFF"));
        yubieconvo1_l3.put("C", new ChoiceOutcome("Yubie: \"Honestly? I surprised myself too. I thought I'd be a zombie chow in the first five minutes. But... maybe all that useless survival trivia I memorized wasn't so useless after all.\"", "TRUST"));
        yubieconvo1_l3.put("D", new ChoiceOutcome("Yubie: \"Guess I'm not as disposable as I thought, huh? I'll fight smarter next time. But know this, I'm not running when you're in danger. Not ever.\"", "CHARISMA"));
        yubieconvo1_l3.put("E", new ChoiceOutcome("Yubie: \"Careful. If you keep saying stuff like that, I might start believing I'm actually important to you. And then I'll never shut up about it.\"", "TURN_ON"));
        yubieLevel3.put(1, yubieconvo1_l3);

        Map<String, ChoiceOutcome> yubieconvo2_l3 = new LinkedHashMap<>(); // Pattern Case 5
        yubieconvo2_l3.put("A", new ChoiceOutcome("Yubie: \"That's... harsh. I was trying to be friendly! Okay, okay, I'll keep my weapons ready.\"", "TURN_OFF2"));
        yubieconvo2_l3.put("B", new ChoiceOutcome("Yubie: \"Oh... sure. But, uh... maybe we're missing some backup here...\"", "TURN_OFF"));
        yubieconvo2_l3.put("C", new ChoiceOutcome("Yubie: \"Right! Teamwork! Uh... don't mind the trembling hands, it's just adrenaline. And maybe excitement.\"", "TRUST"));
        yubieconvo2_l3.put("D", new ChoiceOutcome("Yubie: \"Well... my head is full of useless trivia and panic, but I guess it comes in handy sometimes.\"", "CHARISMA"));
        yubieconvo2_l3.put("E", new ChoiceOutcome("Yubie: \"...You would've made it. You're stronger than you think. But... hearing that from you, it means more than you probably realize\"", "TURN_ON"));
        yubieLevel3.put(2, yubieconvo2_l3);

        Map<String, ChoiceOutcome> yubieconvo3_l3 = new LinkedHashMap<>(); // Pattern Case 6
        yubieconvo3_l3.put("A", new ChoiceOutcome("Yubie: \"Really? Thank you. I promise I'll lead us safely, or at least smarter than blindly charging in.\"", "CHARISMA"));
        yubieconvo3_l3.put("B", new ChoiceOutcome("Yubie: \"Thanks. I've spent years memorizing this stuff for fun... but now it's real. If it keeps us alive... I'll keep planning every detail.\"", "TRUST"));
        yubieconvo3_l3.put("C", new ChoiceOutcome("Yubie: \"I won't let that trust go to waste. I promise.\"", "TURN_ON"));
        yubieconvo3_l3.put("D", new ChoiceOutcome("Yubie: \"... Right, sorry.\"", "TURN_OFF2"));
        yubieconvo3_l3.put("E", new ChoiceOutcome("Yubie: \"... Fine.\"", "TURN_OFF"));
        yubieLevel3.put(3, yubieconvo3_l3);

        yubieLevels.put(3, yubieLevel3);

        // --- YUBIE LEVEL 4 ---
        Map<String, ChoiceOutcome> yubieconvo1_l4 = new LinkedHashMap<>(); // Pattern Case 6
        yubieconvo1_l4.put("A", new ChoiceOutcome("Yubie: \"Thanks... it means a lot that you'd trust me on this. I'll make sure that trust actually counts for something.\"", "TRUST"));
        yubieconvo1_l4.put("B", new ChoiceOutcome("Yubie: \"Wow, that actually boosts my confidence more than any survival manual ever could.\"", "TURN_ON"));
        yubieconvo1_l4.put("C", new ChoiceOutcome("Yubie: \"You're right. Compromise is smarter than arguing.  As long as we stay alert and keep an eye on every danger. No one gets left behind\"", "CHARISMA"));
        yubieconvo1_l4.put("D", new ChoiceOutcome("Yubie: \"...Yeah. Maybe I do talk too much.\"", "TURN_OFF2"));
        yubieconvo1_l4.put("E", new ChoiceOutcome("Yubie: \"Reckless or not, I've thought this through. I don't want anyone getting hurt, especially you. So if we adjust the plan a little, we can actually survive.\"", "TURN_OFF"));
        yubieLevel4.put(1, yubieconvo1_l4);

        Map<String, ChoiceOutcome> yubieconvo2_l4 = new LinkedHashMap<>(); // Pattern Case 5
        yubieconvo2_l4.put("A", new ChoiceOutcome("Yubie: \"Are you sure? Can't we just... figure it out together? Much safer that way.\"", "TURN_OFF2"));
        yubieconvo2_l4.put("B", new ChoiceOutcome("Yubie: \"Fine... I'll try to stop overthinking. But careful planning literally keeps us alive. Maybe a little overthinking isn't the worst thing.\"", "TURN_OFF"));
        yubieconvo2_l4.put("C", new ChoiceOutcome("Yubie: \"Best? Ha! My mom would be proud...\"", "TRUST"));
        yubieconvo2_l4.put("D", new ChoiceOutcome("Yubie: \"All together? Fine, just don't blame me if my charts get attacked by zombies or jealous survivors!\"", "CHARISMA"));
        yubieconvo2_l4.put("E", new ChoiceOutcome("Yubie: \"That's... really nice to hear.\"", "TURN_ON"));
        yubieLevel4.put(2, yubieconvo2_l4);

        Map<String, ChoiceOutcome> yubieconvo3_l4 = new LinkedHashMap<>(); // Pattern Case 6
        yubieconvo3_l4.put("A", new ChoiceOutcome("Yubie: \"Yeah, I can hardly call that calm. My hands were shaking and my hoodie's covered in dust.\"", "TRUST"));
        yubieconvo3_l4.put("B", new ChoiceOutcome("Yubie: \"Wow... You really know how to mess with my head, huh? I'm supposed to be the one keeping you steady, not the other way around.\"", "TURN_ON"));
        yubieconvo3_l4.put("C", new ChoiceOutcome("Yubie: \"Thanks... really. Hearing that makes me feel like all the panicking, the charts, and the overthinking was worth it.\"", "CHARISMA"));
        yubieconvo3_l4.put("D", new ChoiceOutcome("Yubie: \"I'll just hide in a corner next time.\"", "TURN_OFF2"));
        yubieconvo3_l4.put("E", new ChoiceOutcome("Yubie: \"Fine... But no promises!\"", "TURN_OFF"));
        yubieLevel4.put(3, yubieconvo3_l4);

        yubieLevels.put(4, yubieLevel4);

        // --- YUBIE LEVEL 5 ---
        Map<String, ChoiceOutcome> yubieconvo1_l5 = new LinkedHashMap<>(); // Pattern Case 5
        yubieconvo1_l5.put("A", new ChoiceOutcome("Yubie: \"...Right. Got it.\"", "TURN_OFF2"));
        yubieconvo1_l5.put("B", new ChoiceOutcome("Yubie: \"Alright, but if I ever trip, don't push me towards the zombies!\"", "TURN_OFF"));
        yubieconvo1_l5.put("C", new ChoiceOutcome("Yubie: \"Right, safety comes first.\"", "TRUST"));
        yubieconvo1_l5.put("D", new ChoiceOutcome("Yubie: \"I get why you're worried but rethinking now would be a waste of time. We follow the plan, step by step, and we'll be fine!\"", "CHARISMA"));
        yubieconvo1_l5.put("E", new ChoiceOutcome("Yubie: \"And watch each other's backs... we can make it through this. I won't let anything happen to you.\"", "TURN_ON"));
        yubieLevel5.put(1, yubieconvo1_l5);

        Map<String, ChoiceOutcome> yubieconvo2_l5 = new LinkedHashMap<>(); // Pattern Case 5
        yubieconvo2_l5.put("A", new ChoiceOutcome("Yubie: \"I forgot you don't need anyone slowing you down.\"", "TURN_OFF2"));
        yubieconvo2_l5.put("B", new ChoiceOutcome("Yubie: \"Unless you wanna become a feast for the zombies, I suggest not.\"", "TURN_OFF"));
        yubieconvo2_l5.put("C", new ChoiceOutcome("Yubie: \"Safest... hmm... I'll adjust the plan slightly, but we're going for it!”\"", "TRUST"));
        yubieconvo2_l5.put("D", new ChoiceOutcome("Yubie: \"Brains here, yes... but clumsiness is also included!\"", "CHARISMA"));
        yubieconvo2_l5.put("E", new ChoiceOutcome("Yubie: \"Me too. No matter what happens, we're getting out together. I've got your back, always\"", "TURN_ON"));
        yubieLevel5.put(2, yubieconvo2_l5);

        Map<String, ChoiceOutcome> yubieconvo3_l5 = new LinkedHashMap<>(); // Pattern Case 6
        yubieconvo3_l5.put("A", new ChoiceOutcome("Yubie: \"Neither did I, yet, somehow, we did it. Weird, right?\"", "TRUST"));
        yubieconvo3_l5.put("B", new ChoiceOutcome("Yubie: \"Together... I guess that means I can't mess this up, right? I just really don't want to screw this up with you.\"", "TURN_ON"));
        yubieconvo3_l5.put("C", new ChoiceOutcome("Yubie: \"The end... maybe... but I'll admit, I'm kind of nervous. I guess it's normal to feel that way.\"", "CHARISMA"));
        yubieconvo3_l5.put("D", new ChoiceOutcome("Yubie: \"...Right. Lucky.\"", "TURN_OFF2"));
        yubieconvo3_l5.put("E", new ChoiceOutcome("Yubie: \"Right, let's focus on getting out of here.\"", "TURN_OFF"));
        yubieLevel5.put(3, yubieconvo3_l5);

        yubieLevels.put(5, yubieLevel5);

        responseMap.get("Yubie").putAll(yubieLevels);

        // ====== ADI RESPONSES ======
        Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>> adiLevels = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> adiLevel1 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> adiLevel2 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> adiLevel3 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> adiLevel4 = new HashMap<>();
        Map<Integer, Map<String, ChoiceOutcome>> adiLevel5 = new HashMap<>();

        // --- ADI LEVEL 1 ---
        Map<String, ChoiceOutcome> adiconvo1_l1 = new LinkedHashMap<>(); // Pattern Case 1
        adiconvo1_l1.put("A", new ChoiceOutcome("Adi: \"No one 'manages' alone out here. Let me help, please. I need to know you'll make it.\"", "CHARISMA"));
        adiconvo1_l1.put("B", new ChoiceOutcome("Adi: \"That's selfless and reckless. You matter too okay? I'll take care of this.\"", "TRUST"));
        adiconvo1_l1.put("C", new ChoiceOutcome("Adi: \"Wh, at? Don't joke right now... You'll make me lose focus. But... thanks.\"", "TURN_ON"));
        adiconvo1_l1.put("D", new ChoiceOutcome("Adi: \"...You don't owe me. Just... stay alive, alright?\"", "NEUTRAL"));
        adiconvo1_l1.put("E", new ChoiceOutcome("Adi: \"...Don't say that. I need you to fight to live, not give up.\"", "TURN_OFF"));
        adiLevel1.put(1, adiconvo1_l1);

        Map<String, ChoiceOutcome> adiconvo2_l1 = new LinkedHashMap<>(); // Pattern Case 2
        adiconvo2_l1.put("A", new ChoiceOutcome("Adi: \"You're so stubborn. Fine, half and half. Guess I needed someone to stop me.\"", "TRUST"));
        adiconvo2_l1.put("B", new ChoiceOutcome("Adi: \"That's not, ! ...Okay, maybe just a little. Don't make me regret it..\"", "TURN_ON"));
        adiconvo2_l1.put("C", new ChoiceOutcome("Adi: \"Maybe... but if it means you make it one more day, it's worth it.\"", "CHARISMA"));
        adiconvo2_l1.put("D", new ChoiceOutcome("Adi: \"At least it won't go to waste. Just... stay on your feet.\"", "NEUTRAL"));
        adiconvo2_l1.put("E", new ChoiceOutcome("Adi: \"...You think I don't know that? I'm just trying to help.\"", "TURN_OFF"));
        adiLevel1.put(2, adiconvo2_l1);

        Map<String, ChoiceOutcome> adiconvo3_l1 = new LinkedHashMap<>(); // Pattern Case 3
        adiconvo3_l1.put("A", new ChoiceOutcome("Adi: \"...You're impossible. But... I can't say I hate hearing that.\"", "TURN_ON"));
        adiconvo3_l1.put("B", new ChoiceOutcome("Adi: \"No one, I guess. Maybe I never learned how to let others.\"", "CHARISMA"));
        adiconvo3_l1.put("C", new ChoiceOutcome("Adi: \"...Hearing that... it helps. Thank you.\"", "TRUST"));
        adiconvo3_l1.put("D", new ChoiceOutcome("Adi: \"Yeah. That's all that's left for us.\"", "NEUTRAL"));
        adiconvo3_l1.put("E", new ChoiceOutcome("Adi: \"That's cruel. But... maybe you're right. Still, I won't stop.\"", "TURN_OFF"));
        adiLevel1.put(3, adiconvo3_l1);

        adiLevels.put(1, adiLevel1);

        // --- ADI LEVEL 2 ---
        Map<String, ChoiceOutcome> adiconvo1_l2 = new LinkedHashMap<>(); // Pattern Case 3
        adiconvo1_l2.put("A", new ChoiceOutcome("Adi: \"...You're unbelievable. I'm serious here. But... you have a way of keeping me grounded.\"", "TURN_ON"));
        adiconvo1_l2.put("B", new ChoiceOutcome("Adi: \"Because if I don't, someone might die. I can't live with that.\"", "CHARISMA"));
        adiconvo1_l2.put("C", new ChoiceOutcome("Adi: \"You're right. If I burn out, I can't help anyone. Thanks for reminding me..\"", "TRUST"));
        adiconvo1_l2.put("D", new ChoiceOutcome("Adi: \"Fair enough. Just... don't say I didn't warn you when I keep standing.\"", "NEUTRAL"));
        adiconvo1_l2.put("E", new ChoiceOutcome("Adi: \"...Maybe you're right. But I can't just stand by and do nothing.\"", "TURN_OFF"));
        adiLevel2.put(1, adiconvo1_l2);

        Map<String, ChoiceOutcome> adiconvo2_l2 = new LinkedHashMap<>(); // Pattern Case 4
        adiconvo2_l2.put("A", new ChoiceOutcome("Adi: \"Call it guessing if you want. But it's the best chance you've got.\"", "TURN_OFF"));
        adiconvo2_l2.put("B", new ChoiceOutcome("Adi: \"That means more than you know. I won't let you down.\"", "TRUST"));
        adiconvo2_l2.put("C", new ChoiceOutcome("Adi: \"That's good enough for me.\"", "NEUTRAL"));
        adiconvo2_l2.put("D", new ChoiceOutcome("Adi: \"Is that seriously your takeaway? You're impossible. But... thanks.\"", "TURN_ON"));
        adiconvo2_l2.put("E", new ChoiceOutcome("Adi: \"Never thought about it. Guess I don't see myself as worth the supplies.\"", "CHARISMA"));
        adiLevel2.put(2, adiconvo2_l2);

        Map<String, ChoiceOutcome> adiconvo3_l2 = new LinkedHashMap<>(); // Pattern Case 1
        adiconvo3_l2.put("A", new ChoiceOutcome("Adi: \"If I break... maybe someone like you will remind me how to stand again.\"", "CHARISMA"));
        adiconvo3_l2.put("B", new ChoiceOutcome("Adi: \"Maybe I'd like that. I've forgotten how it feels not to do everything alone.\"", "TRUST"));
        adiconvo3_l2.put("C", new ChoiceOutcome("Adi: \"...Are you trying to kill me here? ...Don't look at me like that.\"", "TURN_ON"));
        adiconvo3_l2.put("D", new ChoiceOutcome("Adi: \"Right, I should've thought of that, thanks.\"", "NEUTRAL"));
        adiconvo3_l2.put("E", new ChoiceOutcome("Adi: \"Maybe not. But if there's even a chance, I'll try.\"", "TURN_OFF"));
        adiLevel2.put(3, adiconvo3_l2);

        adiLevels.put(2, adiLevel2);

        // --- ADI LEVEL 3 ---
        Map<String, ChoiceOutcome> adiconvo1_l3 = new LinkedHashMap<>(); // Pattern Case 5
        adiconvo1_l3.put("A", new ChoiceOutcome("Adi: \"...You don't have to believe in it. I do. Otherwise, why am I still here?\"", "TURN_OFF2"));
        adiconvo1_l3.put("B", new ChoiceOutcome("Adi: \"...Maybe. But I'll never stop missing what this place stood for\"", "TURN_OFF"));
        adiconvo1_l3.put("C", new ChoiceOutcome("Adi: \"Maybe... Maybe that's enough. Thank you for saying that.\"", "TRUST"));
        adiconvo1_l3.put("D", new ChoiceOutcome("Adi: \"...You're right. I keep pretending it doesn't hurt. But it does.\"", "CHARISMA"));
        adiconvo1_l3.put("E", new ChoiceOutcome("Adi: \"Wh-  You can't just, ! ...Okay, maybe I was. (smiles faintly)\"", "TURN_ON"));
        adiLevel3.put(1, adiconvo1_l3);

        Map<String, ChoiceOutcome> adiconvo2_l3 = new LinkedHashMap<>(); // Pattern Case 5
        adiconvo2_l3.put("A", new ChoiceOutcome("Adi: \"...Right. Just another task, huh? No need to care who gets it done.\"", "TURN_OFF2"));
        adiconvo2_l3.put("B", new ChoiceOutcome("Adi: \"You think I don't know that risk? Don't treat me like I'm useless.\"", "TURN_OFF"));
        adiconvo2_l3.put("C", new ChoiceOutcome("Adi: \"You'd do that? Then I'll spot you. But don't you dare fall.\"", "TRUST"));
        adiconvo2_l3.put("D", new ChoiceOutcome("Adi: \"...Maybe I don't. Not as much as I value everyone else's.\"", "CHARISMA"));
        adiconvo2_l3.put("E", new ChoiceOutcome("Adi: \"...Why do you have to say things like that when I'm trying to focus?\"", "TURN_ON"));
        adiLevel3.put(2, adiconvo2_l3);

        Map<String, ChoiceOutcome> adiconvo3_l3 = new LinkedHashMap<>(); // Pattern Case 6
        adiconvo3_l3.put("A", new ChoiceOutcome("Adi: \"...Maybe I need someone like you to remind me of that.\"", "CHARISMA"));
        adiconvo3_l3.put("B", new ChoiceOutcome("Adi: \"...I hate it. But... you're right. I'll put our survival first.\"", "TRUST"));
        adiconvo3_l3.put("C", new ChoiceOutcome("Adi: \"...You really know how to mess with my head. But... thank you.\"", "TURN_ON"));
        adiconvo3_l3.put("D", new ChoiceOutcome("Adi: \"You think this is easy for me? These are lives, not numbers. But fine, if it keeps us breathing, I'll make the call\"", "TURN_OFF2"));
        adiconvo3_l3.put("E", new ChoiceOutcome("Adi: \"...That's not survival. That's surrender.\"", "TURN_OFF"));
        adiLevel3.put(3, adiconvo3_l3);

        adiLevels.put(3, adiLevel3);

        // --- ADI LEVEL 4 ---
        Map<String, ChoiceOutcome> adiconvo1_l4 = new LinkedHashMap<>(); // Pattern Case 6
        adiconvo1_l4.put("A", new ChoiceOutcome("Adi: \"That means more than you think. I'll keep trying.\"", "TRUST"));
        adiconvo1_l4.put("B", new ChoiceOutcome("Adi: \"...You're really saying that right now? ...Fine, I'll take the compliment.\"", "TURN_ON"));
        adiconvo1_l4.put("C", new ChoiceOutcome("Adi: \"...Maybe I've been avoiding that truth. Thanks for saying it.\"", "CHARISMA"));
        adiconvo1_l4.put("D", new ChoiceOutcome("Adi: \"So that's it? No faith left in anyone? If we keep running from people, what's the point of surviving?\"", "TURN_OFF2"));
        adiconvo1_l4.put("E", new ChoiceOutcome("Adi: \"Don't say that. We're supposed to be better than this.\"", "TURN_OFF"));
        adiLevel4.put(1, adiconvo1_l4);

        Map<String, ChoiceOutcome> adiconvo2_l4 = new LinkedHashMap<>(); // Pattern Case 5
        adiconvo2_l4.put("A", new ChoiceOutcome("Adi: \"That's a cruel kind of logic. I didn't think I'd hear it from you.\"", "TURN_OFF2"));
        adiconvo2_l4.put("B", new ChoiceOutcome("Adi: \"...I get it. I'll try harder.\"", "TURN_OFF"));
        adiconvo2_l4.put("C", new ChoiceOutcome("Adi: \"Maybe... maybe I'll try. If you're watching my back.\"", "TRUST"));
        adiconvo2_l4.put("D", new ChoiceOutcome("Adi: \"...That's what scares me. I don't know how to stop.\"", "CHARISMA"));
        adiconvo2_l4.put("E", new ChoiceOutcome("Adi: \"You really know how to throw me off balance, don't you?\"", "TURN_ON"));
        adiLevel4.put(2, adiconvo2_l4);

        Map<String, ChoiceOutcome> adiconvo3_l4 = new LinkedHashMap<>(); // Pattern Case 6
        adiconvo3_l4.put("A", new ChoiceOutcome("Adi: \"Thanks. I needed to hear that.\"", "TRUST"));
        adiconvo3_l4.put("B", new ChoiceOutcome("Adi: \"...You're going to be the end of my focus. And I'm okay with that\"", "TURN_ON"));
        adiconvo3_l4.put("C", new ChoiceOutcome("Adi: \"...You make it sound like that's a bad thing.\"", "CHARISMA"));
        adiconvo3_l4.put("D", new ChoiceOutcome("Adi: \"...Maybe not. But feelings don't care about time.\"", "TURN_OFF2"));
        adiconvo3_l4.put("E", new ChoiceOutcome("Adi: \"Yeah. For now.\"", "TURN_OFF"));
        adiLevel4.put(3, adiconvo3_l4);

        adiLevels.put(4, adiLevel4);

        // --- ADI LEVEL 5 ---
        Map<String, ChoiceOutcome> adiconvo1_l5 = new LinkedHashMap<>(); // Pattern Case 5
        adiconvo1_l5.put("A", new ChoiceOutcome("Adi: \"Then I'll just make sure you survive anyway.\"", "TURN_OFF2"));
        adiconvo1_l5.put("B", new ChoiceOutcome("Adi: \"You keep waiting for perfect timing. In this world, there's no such thing left.\"", "TURN_OFF"));
        adiconvo1_l5.put("C", new ChoiceOutcome("Adi: \"Then together it is. No matter what happens.\"", "TRUST"));
        adiconvo1_l5.put("D", new ChoiceOutcome("Adi: \"...I want to live. With you, if I can.\"", "CHARISMA"));
        adiconvo1_l5.put("E", new ChoiceOutcome("Adi: \"...Don't say things like that. My heart can't handle it.\"", "TURN_ON"));
        adiLevel5.put(1, adiconvo1_l5);

        Map<String, ChoiceOutcome> adiconvo2_l5 = new LinkedHashMap<>(); // Pattern Case 5
        adiconvo2_l5.put("A", new ChoiceOutcome("Adi: \"...Right. I'll see you on the other side... maybe.\"", "TURN_OFF2"));
        adiconvo2_l5.put("B", new ChoiceOutcome("Adi: \"You always stop short of saying what matters.\"", "TURN_OFF"));
        adiconvo2_l5.put("C", new ChoiceOutcome("Adi: \"Then maybe that's all I ever needed.\"", "TRUST"));
        adiconvo2_l5.put("D", new ChoiceOutcome("Adi: \"Yeah. The kind that survives... or dies together.\"", "CHARISMA"));
        adiconvo2_l5.put("E", new ChoiceOutcome("Adi: \"...Then I'll hold you to that when we make it out.\"", "TURN_ON"));
        adiLevel5.put(2, adiconvo2_l5);

        Map<String, ChoiceOutcome> adiconvo3_l5 = new LinkedHashMap<>(); // Pattern Case 6
        adiconvo3_l5.put("A", new ChoiceOutcome("Adi: \"Then let's go. One last run.\"", "TRUST"));
        adiconvo3_l5.put("B", new ChoiceOutcome("Adi: \"Wouldn't dream of it... not when you're yelling like that.\"", "TURN_ON"));
        adiconvo3_l5.put("C", new ChoiceOutcome("Adi: \"You always find a way to keep me alive... even when I don't deserve it.\"", "CHARISMA"));
        adiconvo3_l5.put("D", new ChoiceOutcome("Adi: \"...So that's it, huh? Fine. Just... make it count.\"", "TURN_OFF2"));
        adiconvo3_l5.put("E", new ChoiceOutcome("Adi: \"...You don't even trust me to stand on my own? Guess that says it all.\"", "TURN_OFF"));
        adiLevel5.put(3, adiconvo3_l5);

        adiLevels.put(5, adiLevel5);

        responseMap.get("Adi").putAll(adiLevels);
    }

    // ===== Character Response ==== //
    ChoiceOutcome getChoiceOutcome(String charName, int level, int convoNum, String choice) {
        if (responseMap.containsKey(charName)) {
            Map<Integer, Map<Integer, Map<String, ChoiceOutcome>>> levels = responseMap.get(charName);

            if (levels.containsKey(level)) {

                Map<Integer, Map<String, ChoiceOutcome>> convos = levels.get(level);

                if (convos.containsKey(convoNum)) {

                    Map<String, ChoiceOutcome> outcomes = convos.get(convoNum);

                    return outcomes.getOrDefault(choice, null);
                }
            }
        }
        return null;
    }


    // ====== Initialize Choices ======
    private void initializeChoices() {
        choiceMap.put("Avy", new HashMap<>());
        choiceMap.put("Marina", new HashMap<>());
        choiceMap.put("Kim", new HashMap<>());
        choiceMap.put("Nathan", new HashMap<>());
        choiceMap.put("Yubie", new HashMap<>());
        choiceMap.put("Adi", new HashMap<>());

        Map<Integer, Map<Integer, Map<String, String>>> avyLevels = new HashMap<>();
        Map<Integer, Map<Integer, Map<String, String>>> marinaLevels = new HashMap<>();
        Map<Integer, Map<Integer, Map<String, String>>> kimLevels = new HashMap<>();
        Map<Integer, Map<Integer, Map<String, String>>> nathanLevels = new HashMap<>();
        Map<Integer, Map<Integer, Map<String, String>>> yubieLevels = new HashMap<>();
        Map<Integer, Map<Integer, Map<String, String>>> adiLevels = new HashMap<>();

        // --- AVY CHOICES ---

        // Level 1
        Map<Integer, Map<String, String>> avylevel1 = new HashMap<>();
        Map<String, String> avyconvo1_l1 = new LinkedHashMap<>();
        avyconvo1_l1.put("A", "Doesn't matter now. I can handle myself.");
        avyconvo1_l1.put("B", "Just trying to live quietly before all this.");
        avyconvo1_l1.put("C", "Honestly? Nothing special. Just average… until today.");
        avyconvo1_l1.put("D", "I used to run too, though nowhere near varsity level like you.");
        avyconvo1_l1.put("E", "I don't owe you my life story. Let's move.");
        avylevel1.put(1, avyconvo1_l1);

        Map<String, String> avyconvo2_l1 = new LinkedHashMap<>();
        avyconvo2_l1.put("A", "I focus on the people counting on me.");
        avyconvo2_l1.put("B", "If you can push through, then so can I. You remind me to keep going.");
        avyconvo2_l1.put("C", "Pressure? I thrive on it. Makes me stronger.");
        avyconvo2_l1.put("D", "I just… keep moving. No plan, no thinking.");
        avyconvo2_l1.put("E", "I don't. I panic and hope for the best, of course, jokes aside.");
        avylevel1.put(2, avyconvo2_l1);

        Map<String, String> avyconvo3_l1 = new LinkedHashMap<>();
        avyconvo3_l1.put("A", "Honestly? Having someone here makes it easier. Alone, it'd be worse.");
        avyconvo3_l1.put("B", "Scared? Nah. I eat horror movies for breakfast.");
        avyconvo3_l1.put("C", "Of course I'm scared. But fear keeps me sharp.");
        avyconvo3_l1.put("D", "Eh, I try not to think about it. Helps me keep calm.");
        avyconvo3_l1.put("E", "Not when you're here. Somehow you make the fear… less heavy.");
        avylevel1.put(3, avyconvo3_l1);

        avyLevels.put(1, avylevel1);

        // Level 2
        Map<Integer, Map<String, String>> avylevel2 = new HashMap<>();

        // Dialogue 1: "...keep head up"
        Map<String, String> avyconvo3_l2 = new LinkedHashMap<>();
        avyconvo3_l2.put("A", "Nope. I make myself useful, no matter what.");
        avyconvo3_l2.put("B", "Yeah, but I push through it. Can't let it win.");
        avyconvo3_l2.put("C", "Sometimes. But having you here reminds me I'm not pointless.");
        avyconvo3_l2.put("D", "Sometimes, yeah. But hey, at least I can nap in peace.");
        avyconvo3_l2.put("E", "All the time. Honestly, I think I slow people down.");
        avylevel2.put(1, avyconvo3_l2);

        // Dialogue 2: "...miss most"
        Map<String, String> avyconvo2_l2 = new LinkedHashMap<>();
        avyconvo2_l2.put("A", "I don't really miss much. Life wasn't exactly great even before all this.");
        avyconvo2_l2.put("B", "I miss the people I cared about. Can't get that back.");
        avyconvo2_l2.put("C", "I miss dumb things, like real food. Burgers, fries, soda.");
        avyconvo2_l2.put("D", "I miss believing tomorrow would be normal. That hope kept me going.");
        avyconvo2_l2.put("E", "I miss the adrenaline, competition, pushing myself.");
        avylevel2.put(2, avyconvo2_l2);

        // Dialogue 3: "...feel useless"
        Map<String, String> avyconvo1_l2 = new LinkedHashMap<>();
        avyconvo1_l2.put("A", "By reminding myself I don't have to carry it all alone. You prove that.");
        avyconvo1_l2.put("B", "By making jokes, even when I'm hurting.");
        avyconvo1_l2.put("C", "By remembering there are people worth protecting.");
        avyconvo1_l2.put("D", "By not thinking too hard. If I stop to feel, I'll break.");
        avyconvo1_l2.put("E", "By pretending everything's fine. That's what you're doing too, isn't it?");
        avylevel2.put(3, avyconvo1_l2);

        avyLevels.put(2, avylevel2);

        // Level 3
        Map<Integer, Map<String, String>> avylevel3 = new HashMap<>();
        Map<String, String> avyconvo1_l3 = new LinkedHashMap<>();
        avyconvo1_l3.put("A", "Honestly, I just try not to think about it anymore. Living, dying,it all blurs together.");
        avyconvo1_l3.put("B", "Knowing that someone like you is fighting beside me.");
        avyconvo1_l3.put("C", "Because if I stop, we both die. Simple as that.”");
        avyconvo1_l3.put("D", "I keep going 'cause I'm strong. You'd be lost without me.");
        avyconvo1_l3.put("E", "Because someone has to keep you out of trouble.");
        avylevel3.put(1, avyconvo1_l3);

        Map<String, String> avyconvo2_l3 = new LinkedHashMap<>();
        avyconvo2_l3.put("A", "Maybe in a year it'll just look the same, ruins stacked on ruins.");
        avyconvo2_l3.put("B", "Rebuilding sounds good on paper, but people always mess it up in the end.");
        avyconvo2_l3.put("C", "It'll just be more ruins... unless we make something better.");
        avyconvo2_l3.put("D", "A place for survivors like us to rebuild. Step by step.");
        avyconvo2_l3.put("E", "If you're here, I don't care what the city looks like.");
        avylevel3.put(2, avyconvo2_l3);

        Map<String, String> avyconvo3_l3 = new LinkedHashMap<>();
        avyconvo3_l3.put("A", "I think every fight leaves a mark. Stronger in some ways, maybe weaker in others.");
        avyconvo3_l3.put("B", "Stronger or weaker, I know one thing,you keep pushing forward. That's what matters.");
        avyconvo3_l3.put("C", "You've handled worse. Honestly, look at you now.");
        avyconvo3_l3.put("D", "Feels like no matter how strong we get, it won't be enough in the long run.");
        avyconvo3_l3.put("E", "Sometimes it feels less like getting stronger and more like just... burning through what we've got left.");
        avylevel3.put(3, avyconvo3_l3);

        avyLevels.put(3, avylevel3);

        // Level 4
        Map<Integer, Map<String, String>> avylevel4 = new HashMap<>();
        Map<String, String> avyconvo1_l4 = new LinkedHashMap<>();
        avyconvo1_l4.put("A", "We should weigh options, not just rush. Safety first.");
        avyconvo1_l4.put("B", "We need to move. You're right to trust your instincts.");
        avyconvo1_l4.put("C", "Who cares what they think? We're the strongest anyway.");
        avyconvo1_l4.put("D", "Honestly? You're reckless. But kind of cool in a way.");
        avyconvo1_l4.put("E", "I dont want to stick my nose in. Maybe I should just leave?");
        avylevel4.put(1, avyconvo1_l4);

        Map<String, String> avyconvo2_l4 = new LinkedHashMap<>();
        avyconvo2_l4.put("A", "You worry too much. We got this okay?");
        avyconvo2_l4.put("B", "If you want, I'll take over watch. You look like you're about to collapse.");
        avyconvo2_l4.put("C", "Quiet usually means safe. You can breathe, just for tonight.");
        avyconvo2_l4.put("D", "If anything comes, we'll crush it. You don't need to worry when I'm here.");
        avyconvo2_l4.put("E", "Paranoia isn't weakness. It's what's kept us alive.");
        avylevel4.put(2, avyconvo2_l4);

        Map<String, String> avyconvo3_l4 = new LinkedHashMap<>();
        avyconvo3_l4.put("A", "Then we'll stretch it out. Everyone eats, no matter what");
        avyconvo3_l4.put("B", "Tough calls aren't easy, but you're the only one I trust to make them.");
        avyconvo3_l4.put("C", "Let them panic. You and I can handle things without their input.");
        avyconvo3_l4.put("D", "Sometimes tough calls mean not everyone gets an equal share. That's just survival math.");
        avyconvo3_l4.put("E", "If we're smart, we'll figure something out. No point in stressing.");
        avylevel4.put(3, avyconvo3_l4);

        avyLevels.put(4, avylevel4);

        // Level 5
        Map<Integer, Map<String, String>> avylevel5 = new HashMap<>();
        Map<String, String> avyconvo1_l5 = new LinkedHashMap<>();
        avyconvo1_l5.put("A", "Because you need someone to keep you grounded when panic takes over.");
        avyconvo1_l5.put("B", "Because if I weren't here, you'd probably push yourself too hard and burn out.");
        avyconvo1_l5.put("C", "Because I promised to protect you, and I don't break promises.");
        avyconvo1_l5.put("D", "Because together, we're unstoppable.");
        avyconvo1_l5.put("E", "Because you're more than just a teammate.");
        avylevel5.put(1, avyconvo1_l5);

        Map<String, String> avyconvo2_l5 = new LinkedHashMap<>();
        avyconvo2_l5.put("A", "We fight our way through. Better to face danger head-on than be hunted down.");
        avyconvo2_l5.put("B", "Whichever way we go, we can't drag dead weight. Everyone has to pull their share.");
        avyconvo2_l5.put("C", "We run. Fighting wastes energy we can't afford to lose.");
        avyconvo2_l5.put("D", "Doesn't matter which we pick, as long as we don't freeze up. Panic's the real killer.");
        avyconvo2_l5.put("E", "Either way, I'm with you. I'd rather fall beside you than run alone.");
        avylevel5.put(2, avyconvo2_l5);

        Map<String, String> avyconvo3_l5 = new LinkedHashMap<>();
        avyconvo3_l5.put("A", "You first, I'll keep you in sight and cover you from here.");
        avyconvo3_l5.put("B", "We grab that rope together. If one slips, the other pulls them up.");
        avyconvo3_l5.put("C", "Don't look down, don't hesitate. We move now, before this roof gives way.");
        avyconvo3_l5.put("D", "If I go first and test the rope, you'll know it's safe. That way you don't have to take the risk blind.");
        avyconvo3_l5.put("E", "The pilot's fighting the wind, if we hesitate too long, we make it harder for both of us.");
        avylevel5.put(3, avyconvo3_l5);

        avyLevels.put(5, avylevel5);

        choiceMap.get("Avy").putAll(avyLevels);

        // --- MARINA CHOICES ---

        // Level 1
        Map<Integer, Map<String, String>> marinalevel1 = new HashMap<>();
        Map<String, String> marinaconvo1_l1 = new LinkedHashMap<>();
        marinaconvo1_l1.put("A", "I keep going because when others panic, someone has to stay calm. I guess that's me.");
        marinaconvo1_l1.put("B", "If I can't trust anyone, then I've already lost. That thought alone keeps me going.");
        marinaconvo1_l1.put("C", "Maybe it's the thought that someone out there just needs a reason to keep fighting. Maybe I can be that reason.");
        marinaconvo1_l1.put("D", "Right now? Hunger. That's what's pushing me forward more than anything.");
        marinaconvo1_l1.put("E", "I don't think about it. Just... eat, run, hide, repeat. That's all there is. Maybe let's move out here.");
        marinalevel1.put(1, marinaconvo1_l1);

        Map<String, String> marinaconvo2_l1 = new LinkedHashMap<>();
        marinaconvo2_l1.put("A", "I trust your instincts. You notice things most people would miss");
        marinaconvo2_l1.put("B", "You're really attentive to details... that could save our lives. I'm really glad you're here.");
        marinaconvo2_l1.put("C", "Don't worry. If we stay careful and move together, we'll just be fine.");
        marinaconvo2_l1.put("D", "Oh yeah!... sticking to the walls sounds smart.");
        marinaconvo2_l1.put("E", "Maybe it's nothing. We should just keep moving and not overthink it.");
        marinalevel1.put(2, marinaconvo2_l1);

        Map<String, String> marinaconvo3_l1 = new LinkedHashMap<>();
        marinaconvo3_l1.put("A", "Even if we're alone, we'll handle it. We'll figure things out together. I got your back.");
        marinaconvo3_l1.put("B", "I'm not sure if anyone else is still out there... there could be zombies around, so we need to watch our moves.");
        marinaconvo3_l1.put("C", "I'm not sure but if there's anyone out there, I trust you'd notice them first. You're a great observer after all.");
        marinaconvo3_l1.put("D", "Maybe...or maybe not. We'll just see.");
        marinaconvo3_l1.put("E", "Does it really matter if anyone else is out there? We should just focus on surviving... on our own");
        marinalevel1.put(3, marinaconvo3_l1);

        marinaLevels.put(1, marinalevel1);

        // Level 2
        Map<Integer, Map<String, String>> marinalevel2 = new HashMap<>();
        Map<String, String> marinaconvo1_l2 = new LinkedHashMap<>();
        marinaconvo1_l2.put("A", "Yes...I think this spot is really safe for now. I'm glad you found it, and it feels a lot safer having you here.");
        marinaconvo1_l2.put("B", "It does look safe. But first...to make it even safer, let's organize this place, so we can be comfortable and ready for anything.");
        marinaconvo1_l2.put("C", "You're right. This place seems safe for us. We can stay here and plan our next steps.");
        marinaconvo1_l2.put("D", "Now this is a safe place to rest. Let's just figure things out later.");
        marinaconvo1_l2.put("E", "I'm not sure this place is really safe. It's messy and...could attract trouble or should I say, zombies.Your call.");
        marinalevel2.put(1, marinaconvo1_l2);

        Map<String, String> marinaconvo2_l2 = new LinkedHashMap<>();
        marinaconvo2_l2.put("A", "We can just move out if we sense something wrong though. That shift thing is quite...useless especially in this place full of uncertainty.");
        marinaconvo2_l2.put("B", "That's a smart plan. I trust your judgment, if you say these shifts will work, then I'm in");
        marinaconvo2_l2.put("C", "Alright... that works. Should I take the first shift or you'll take it?");
        marinaconvo2_l2.put("D", "Your mind is really impressive. I'm lucky to have you here. But if you get tired, you can really rest, and I'll watch...this place for you.");
        marinaconvo2_l2.put("E", "Good idea. I can take the first shift so you can rest more.");
        marinalevel2.put(2, marinaconvo2_l2);

        Map<String, String> marinaconvo3_l2 = new LinkedHashMap<>();
        marinaconvo3_l2.put("A", "I used to dream about travelling... but thinking about it now, staying alive is more important. Still, it's nice to remember the old plans sometimes.");
        marinaconvo3_l2.put("B", "I had plans too. Like travelling around the world... but I guess none of us expected all this. Now, I just try to take it day by day.");
        marinaconvo3_l2.put("C", "I still think about some of those dreams,  like travelling. It keeps me going... and knowing someone else notices makes surviving feel... worth it.");
        marinaconvo3_l2.put("D", "A dream? Maybe travelling, I guess.");
        marinaconvo3_l2.put("E", "Look, we're in the middle of an apocalypse with zombies everywhere. Thinking about the past doesn't matter, surviving does.");
        marinalevel2.put(3, marinaconvo3_l2);

        marinaLevels.put(2, marinalevel2);

        // Level 3
        Map<Integer, Map<String, String>> marinalevel3 = new HashMap<>();
        Map<String, String> marinaconvo1_l3 = new LinkedHashMap<>();
        marinaconvo1_l3.put("A", "This is how it'll be from now on. Close calls, near deaths and that random fact. You better hold it together. ");
        marinaconvo1_l3.put("B", "Quit that cortisol thing. Don't slow down next time. We almost turned into a zombie feast.");
        marinaconvo1_l3.put("C", "We made it because of you. You saw the exit when I didn't. I'll trust your calls every time.");
        marinaconvo1_l3.put("D", "Breathe. We're safe for now. It may come a long way, but we'll get through this.");
        marinaconvo1_l3.put("E", "You kept your head when it mattered most. Honestly, you saved me back there. I truly am grateful to have you.");
        marinalevel3.put(1, marinaconvo1_l3);

        Map<String, String> marinaconvo2_l3 = new LinkedHashMap<>();
        marinaconvo2_l3.put("A", "You should've smacked it earlier. I almost got killed because of how slow you are.");
        marinaconvo2_l3.put("B", "Next time don't freeze up like that and smack it directly. I can't protect both of us if you can't even think straight.");
        marinaconvo2_l3.put("C", "Thank you for smacking its head. We're still alive because of how brave you are.");
        marinaconvo2_l3.put("D", "Relax. I don't break that easily. You don't have to worry.");
        marinaconvo2_l3.put("E", "You're more worried about me than yourself... I should be the one checking on you.");
        marinalevel3.put(2, marinaconvo2_l3);

        Map<String, String> marinaconvo3_l3 = new LinkedHashMap<>();
        marinaconvo3_l3.put("A", "You've got a good eye. I trust your insight, lead the way.");
        marinaconvo3_l3.put("B", "The way you notice the details... It really amazes me. I don't think I could survive this alone without you.");
        marinaconvo3_l3.put("C", "Alright, I'll follow your lead. You clearly know this place better than I do.");
        marinaconvo3_l3.put("D", "Things have changed too much. Just keep your thoughts to yourself, it slows us down. Follow my lead.");
        marinaconvo3_l3.put("E", "I don't think it's a good plan. We're still unsure though, even now. Just move with me, I'll handle it.");
        marinalevel3.put(3, marinaconvo3_l3);

        marinaLevels.put(3, marinalevel3);

        // Level 4
        Map<Integer, Map<String, String>> marinalevel4 = new HashMap<>();
        Map<String, String> marinaconvo1_l4 = new LinkedHashMap<>();
        marinaconvo1_l4.put("A", "I know your gut is usually right. Let's keep an eye on them.");
        marinaconvo1_l4.put("B", "You're right, we need to be careful and mindful of our actions. If things go wrong, I'll be right here to protect you.");
        marinaconvo1_l4.put("C", "I agree. Let's set clear rules and boundaries. If anyone steps out of line, we deal with it.");
        marinaconvo1_l4.put("D", "Stop overthinking everything. Okay? you're just making things worse.");
        marinaconvo1_l4.put("E", "Your hesitation is slowing us down. Just go along with them or we'll look weak.");
        marinalevel4.put(1, marinaconvo1_l4);

        Map<String, String> marinaconvo2_l4 = new LinkedHashMap<>();
        marinaconvo2_l4.put("A", "If that's not overthinking, then what is? Stop overcomplicating things, you're just making yourself more uneasy.");
        marinaconvo2_l4.put("B", "To be honest, yes. You're just wasting your energy. Just go to sleep.");
        marinaconvo2_l4.put("C", "Not at all, we're safe. You can breathe and take a good rest, just for tonight.");
        marinaconvo2_l4.put("D", "No, you're not...It's better to watch together. I still have quite a bit of energy this late night...we'll handle it.");
        marinaconvo2_l4.put("E", "The way you notice everything, really helps. You're not overthinking, you're keeping us safe. I'll stay here with you.");
        marinalevel4.put(2, marinaconvo2_l4);

        Map<String, String> marinaconvo3_l4 = new LinkedHashMap<>();
        marinaconvo3_l4.put("A", "Your plan makes sense. I know it's risky, but... let's trust them, just like they're trusting us.");
        marinaconvo3_l4.put("B", "The way you figure out things out and plan ahead, it's incredible. No matter what happens, I'll always be here to back you up.");
        marinaconvo3_l4.put("C", "We'll handle this together. Your plan is smart, and I'll make sure the others follow it. We've got this.");
        marinaconvo3_l4.put("D", "Honestly... your constant doubting is frustrating. Why not just be grateful and trust them for once?");
        marinaconvo3_l4.put("E", "Don't overthink it so much.You're giving me headaches.");
        marinalevel4.put(3, marinaconvo3_l4);

        marinaLevels.put(4, marinalevel4);

        // Level 5
        Map<Integer, Map<String, String>> marinalevel5 = new HashMap<>();
        Map<String, String> marinaconvo1_l5 = new LinkedHashMap<>();
        marinaconvo1_l5.put("A", "I heard that, okay. Feels like it'll take a lifetime to get to the shore.");
        marinaconvo1_l5.put("B", "Stop talking and keep your eyes on the road.");
        marinaconvo1_l5.put("C", "You're right. It may be the longer route, but it's definitely much safer.");
        marinaconvo1_l5.put("D", "It really is. We'll stay alert and keep moving, together.");
        marinaconvo1_l5.put("E", "You always think ahead. That's why we're still here. I-I don't say it enough but... I'm really glad I'm with you.");
        marinalevel5.put(1, marinaconvo1_l5);

        Map<String, String> marinaconvo2_l5 = new LinkedHashMap<>();
        marinaconvo2_l5.put("A", "You can tell them yourself. You're being too dependent on me.");
        marinaconvo2_l5.put("B", "Whichever way we go, zombies are zombies. Do you really think they wouldn't notice humans?");
        marinaconvo2_l5.put("C", "No worries. I'll explain it exactly like you said, we'll follow your strategy.");
        marinaconvo2_l5.put("D", "Got it. I'll relay your plan clearly. You did an amazing job.");
        marinaconvo2_l5.put("E", "That kind of insight is what keeps us alive. That's... what I like about you...I'll tell them exactly as you planned.");
        marinalevel5.put(2, marinaconvo2_l5);

        Map<String, String> marinaconvo3_l5 = new LinkedHashMap<>();
        marinaconvo3_l5.put("A", "You're right. Let's leave the extras and move exactly as you planned. I trust you.");
        marinaconvo3_l5.put("B", "You're impressive, Marina. It makes me really glad you're here with me... Marina, you got me.");
        marinaconvo3_l5.put("C", "Alright, let's do it. Drop the extra bags, stick to your plan, and keep moving.");
        marinaconvo3_l5.put("D", "It's nonsense. They can catch up, except you. Even without a bag, you can't keep up.");
        marinaconvo3_l5.put("E", "Alright, Ms. Genius. Stop slacking or you'll end up as zombie food.");
        marinalevel5.put(3, marinaconvo3_l5);

        marinaLevels.put(5, marinalevel5);

        choiceMap.get("Marina").putAll(marinaLevels);

        // --- KIM CHOICES ---

        // Level 1
        Map<Integer, Map<String, String>> kimlevel1 = new HashMap<>();
        Map<String, String> kimconvo1_l1 = new LinkedHashMap<>();
        kimconvo1_l1.put("A", "Resting next to someone like you... feels safer already.");
        kimconvo1_l1.put("B", "That's kind of you. Thank you... I really needed this.");
        kimconvo1_l1.put("C", "A kind soul in the dark... I'm not sure if it's the rest I need, or just the light of your company.");
        kimconvo1_l1.put("D", "...If you don't mind, I'll stay just for a bit.");
        kimconvo1_l1.put("E", "No thanks. I don't sit near strangers, too risky.");
        kimlevel1.put(1, kimconvo1_l1);

        Map<String, String> kimconvo2_l1 = new LinkedHashMap<>();
        kimconvo2_l1.put("A", "It's brave of you. Even now, you're still helping people.");
        kimconvo2_l1.put("B", "Honestly, I think it suits you. You still look like an angel in scrubs.");
        kimconvo2_l1.put("C", "The world turned upside down... but you still shine in it.");
        kimconvo2_l1.put("D", "That's a painful kind of irony. I hope you find a way to use that training without losing your hope.");
        kimconvo2_l1.put("E", "Hospitals or ruins... doesn't matter. Survival's all that counts now.");
        kimlevel1.put(2, kimconvo2_l1);

        Map<String, String> kimconvo3_l1 = new LinkedHashMap<>();
        kimconvo3_l1.put("A", "If I have to stay awake, then I'd rather it be with you by my side.");
        kimconvo3_l1.put("B", "If it helps, I'll keep you talking. Words can be louder than silence.");
        kimconvo3_l1.put("C", "I'll stay awake with you. No one should face the night alone.");
        kimconvo3_l1.put("D", "Me too. It’s hard to remember what 'normal' was like. Thank you for saying that.");
        kimconvo3_l1.put("E", "I get it. But honestly, I'm too tired to care about the silence. Let's just try to get some sleep.");
        kimlevel1.put(3, kimconvo3_l1);

        kimLevels.put(1, kimlevel1);

        // Level 2
        Map<Integer, Map<String, String>> kimlevel2 = new HashMap<>();
        Map<String, String> kimconvo1_l2 = new LinkedHashMap<>();
        kimconvo1_l2.put("A", "A tomorrow where we're still standing. Where no matter what happens, we make it through together.");
        kimconvo1_l2.put("B", "A tomorrow where the ruins are fixed and the world is healing, and I can take you out for a proper dinner.");
        kimconvo1_l2.put("C", "A tomorrow where we wake up without running, where survival doesn't feel like a battle every second.");
        kimconvo1_l2.put("D", "A tomorrow where people stop hurting each other. Where we're not just surviving, but living.");
        kimconvo1_l2.put("E", "Tomorrow won't change anything. It'll just be another day to fight through.");
        kimlevel2.put(1, kimconvo1_l2);

        Map<String, String> kimconvo2_l2 = new LinkedHashMap<>();
        kimconvo2_l2.put("A", "It's nothing. Don't waste your time on me.");
        kimconvo2_l2.put("B", "Why does it even matter? Just leave it alone.");
        kimconvo2_l2.put("C", "I think everyone does, eventually. The quiet is harder than the noise sometimes.");
        kimconvo2_l2.put("D", "That's... a dangerous thing to say. But if it eases your fear, then I'll keep patching you up, no matter what.");
        kimconvo2_l2.put("E", "I didnt want to worry you. Thank you for noticing.");
        kimlevel2.put(2, kimconvo2_l2);

        Map<String, String> kimconvo3_l2 = new LinkedHashMap<>();
        kimconvo3_l2.put("A", "Yeah... quiet makes the ghosts louder. But sitting here with you, it feels a little less heavy.");
        kimconvo3_l2.put("B", "It's like the world tries to settle the score when you're finally safe. But talking about it helps.");
        kimconvo3_l2.put("C", "I don't want to think about the past. I'd rather focus on protecting what's in front of me, you.");
        kimconvo3_l2.put("D", "Sometimes silence is good. It means we made it through another day alive. That's enough.");
        kimconvo3_l2.put("E", "Memories don't matter. They're just dead weight slowing us down.");
        kimlevel2.put(3, kimconvo3_l2);

        kimLevels.put(2, kimlevel2);

        // Level 3
        Map<Integer, Map<String, String>> kimlevel3 = new HashMap<>();
        Map<String, String> kimconvo1_l3 = new LinkedHashMap<>();
        kimconvo1_l3.put("A", "This is overwhelming, so I'm only looking out for myself. Don't slow me down.");
        kimconvo1_l3.put("B", "Maybe we should split up?");
        kimconvo1_l3.put("C", "Let's sneak around. Speed over strength.");
        kimconvo1_l3.put("D", "We fight together, no holding back.");
        kimconvo1_l3.put("E", "Just stick close to me. Honestly, with you watching my back, I feel like I can take on anything.");
        kimlevel3.put(1, kimconvo1_l3);

        Map<String, String> kimconvo2_l3 = new LinkedHashMap<>();
        kimconvo2_l3.put("A", "Maybe we should split up and hope for the best.");
        kimconvo2_l3.put("B", "I don't know if we'll find anything. Maybe we're doomed.");
        kimconvo2_l3.put("C", "We'll make it. We just need to be smart and stick together.");
        kimconvo2_l3.put("D", "We'll find a way, we always do. And with you by my side, I know we won't miss anything important.");
        kimconvo2_l3.put("E", "Let's search carefully. Every little bit counts.");
        kimlevel3.put(2, kimconvo2_l3);

        Map<String, String> kimconvo3_l3 = new LinkedHashMap<>();
        kimconvo3_l3.put("A", "I focus on the next victory, and trying to make you smile while we're at it.");
        kimconvo3_l3.put("B", "I focus on keeping us alive together. That thought keeps me going.");
        kimconvo3_l3.put("C", "I plan every move carefully. Each step is a choice to survive.");
        kimconvo3_l3.put("D", "I usually panic and hope for the best. Don't expect me to be strong.");
        kimconvo3_l3.put("E", "I just focus on what I need. Everyone else has to look out for themselves.");
        kimlevel3.put(3, kimconvo3_l3);

        kimLevels.put(3, kimlevel3);

        // Level 4
        Map<Integer, Map<String, String>> kimlevel4 = new HashMap<>();
        Map<String, String> kimconvo1_l4 = new LinkedHashMap<>();
        kimconvo1_l4.put("A", "We stay, but we keep watch. If danger comes, we'll be ready.");
        kimconvo1_l4.put("B", "We take it. Shelter like this doesn't come often, we need rest to keep fighting.");
        kimconvo1_l4.put("C", "We stay and rest. With someone as sharp as you keeping watch, I know we're safe.");
        kimconvo1_l4.put("D", "Rest is for the dead. We keep moving, no matter how tired you are.");
        kimconvo1_l4.put("E", "Nowhere's safe anymore. All we can do is keep running until we drop.");
        kimlevel4.put(1, kimconvo1_l4);

        Map<String, String> kimconvo2_l4 = new LinkedHashMap<>();
        kimconvo2_l4.put("A", "They're a liability. We should rob them blind and leave before they become a problem.");
        kimconvo2_l4.put("B", "Let's just take what we need and not care what they think.");
        kimconvo2_l4.put("C", "A little faith is the only thing we have left. Let's accept their help and try to build a bridge.");
        kimconvo2_l4.put("D", "Trust has to start somewhere. We should give them a chance.");
        kimconvo2_l4.put("E", "We'll accept, but stay cautious. Never let our guard down.");
        kimlevel4.put(2, kimconvo2_l4);

        Map<String, String> kimconvo3_l4 = new LinkedHashMap<>();
        kimconvo3_l4.put("A", "We need to keep the peace, no matter what.");
        kimconvo3_l4.put("B", "Sometimes, you need to fight back to be respected.");
        kimconvo3_l4.put("C", "We'll find a way to make them respect us without fighting. With your help, we can charm our way out of anything.");
        kimconvo3_l4.put("D", "The only way to keep the peace is to eliminate the problem before it grows. We attack first.");
        kimconvo3_l4.put("E", "If they're trouble, we should leave.");
        kimlevel4.put(3, kimconvo3_l4);

        kimLevels.put(4, kimlevel4);

        //  Level 5
        Map<Integer, Map<String, String>> kimlevel5 = new HashMap<>();
        Map<String, String> kimconvo1_l5 = new LinkedHashMap<>();
        kimconvo1_l5.put("A", "I'd probably run off and be selfish. I can't promise I'd survive for anyone else.");
        kimconvo1_l5.put("B", "I told you to focus. Your panic is useless and annoying right now.");
        kimconvo1_l5.put("C", "Honestly? I don't know. I just hope we make it there together.");
        kimconvo1_l5.put("D", "I'd want to stay close to you. Make sure we're both okay before anything else.");
        kimconvo1_l5.put("E", "I'd want to find a safe place and plan carefully for the next steps. Survival comes first.");
        kimlevel5.put(1, kimconvo1_l5);

        Map<String, String> kimconvo2_l5 = new LinkedHashMap<>();
        kimconvo2_l5.put("A", "If you can't keep up, it'll slow us both down.");
        kimconvo2_l5.put("B", "I can only worry about my own steps right now. You need to focus on yourself.");
        kimconvo2_l5.put("C", "Step where I step. I'll guide you through every plank.");
        kimconvo2_l5.put("D", "Don't look down. Just keep your eyes on me, and we'll make it.");
        kimconvo2_l5.put("E", "If you fall, I'm going with you. I'm not leaving you behind.");
        kimlevel5.put(2, kimconvo2_l5);

        Map<String, String> kimconvo3_l5 = new LinkedHashMap<>();
        kimconvo3_l5.put("A", "I'm not letting go. I'll carry you out myself if I have to.");
        kimconvo3_l5.put("B", "Then we climb together. If you slip, I'll pull you back. I swear it.");
        kimconvo3_l5.put("C", "Listen to me, we've survived worse. Focus on my voice, one step at a time.");
        kimconvo3_l5.put("D", "If you can't keep up, I'll have to leave you. I'm sorry.");
        kimconvo3_l5.put("E", "You're slowing me down. Just don't panic, okay?");
        kimlevel5.put(3, kimconvo3_l5);

        kimLevels.put(5, kimlevel5);

        choiceMap.get("Kim").putAll(kimLevels);

        // --- NATHAN CHOICES ---

        // Level 1
        Map<Integer, Map<String, String>> nathanlevel1 = new HashMap<>();
        Map<String, String> nathanconvo1_l1_choice = new LinkedHashMap<>(); // Renamed to avoid conflict
        nathanconvo1_l1_choice.put("A", "Maybe I'm just too stubborn to die.");
        nathanconvo1_l1_choice.put("B", "People depended on me. I couldn't just give up.");
        nathanconvo1_l1_choice.put("C", "What, worried about me? Didn't know you cared.");
        nathanconvo1_l1_choice.put("D", "Surviving. Same as everyone else.");
        nathanconvo1_l1_choice.put("E", "Does it even matter? We're all probably dead soon anyway.");
        nathanlevel1.put(1, nathanconvo1_l1_choice);

        Map<String, String> nathanconvo2_l1_choice = new LinkedHashMap<>(); // Renamed
        nathanconvo2_l1_choice.put("A", "Every single day. But I can't let the past weigh me down.");
        nathanconvo2_l1_choice.put("B", "Miss them? Sure. But maybe I just miss you the most.");
        nathanconvo2_l1_choice.put("C", "Yeah. I miss them... and maybe the people I should've held closer.");
        nathanconvo2_l1_choice.put("D", "Sometimes. But it's dangerous to think too much about the past.");
        nathanconvo2_l1_choice.put("E", "What's the point of missing anything? The world's already gone.");
        nathanlevel1.put(2, nathanconvo2_l1_choice);

        Map<String, String> nathanconvo3_l1_choice = new LinkedHashMap<>(); // Renamed
        nathanconvo3_l1_choice.put("A", "Scared to sleep without me by your side?");
        nathanconvo3_l1_choice.put("B", "Want me to keep you company? Could... distract each other.");
        nathanconvo3_l1_choice.put("C", "I'll take first. You need the rest.");
        nathanconvo3_l1_choice.put("D", "We'll figure it out. Just... stay sharp.");
        nathanconvo3_l1_choice.put("E", "Do whatever you want. Not like I care.");
        nathanlevel1.put(3, nathanconvo3_l1_choice);

        nathanLevels.put(1, nathanlevel1);

        // Level 2
        Map<Integer, Map<String, String>> nathanlevel2 = new HashMap<>();

        // Dialogue 1: "...almost normal"
        Map<String, String> nathanconvo3_l2_choice = new LinkedHashMap<>(); // Renamed
        nathanconvo3_l2_choice.put("A", "Relax, I'll keep you safe.");
        nathanconvo3_l2_choice.put("B", "Then we'll face them together. You're not alone.");
        nathanconvo3_l2_choice.put("C", "Want me to keep you company? Could... distract each other.");
        nathanconvo3_l2_choice.put("D", "Don't worry, I'll be the night guard. You just try to get some rest."); //fixed
        nathanconvo3_l2_choice.put("E", "If you can't handle this, maybe you're not cut out to survive.");
        nathanlevel2.put(1, nathanconvo3_l2_choice);

        // Dialogue 2: "...cooking"
        Map<String, String> nathanconvo2_l2_choice = new LinkedHashMap<>(); // Renamed
        nathanconvo2_l2_choice.put("A", "Who cares about food? We just need to survive.");
        nathanconvo2_l2_choice.put("B", "You cook? That's...surprisingly useful.");
        nathanconvo2_l2_choice.put("C", "Scars or not, it means you've been trying. That's what matters.");
        nathanconvo2_l2_choice.put("D", "I like a guy who knows his way around a kitchen.");
        nathanconvo2_l2_choice.put("E", "Guess I'll let you handle meals, then. I trust you.");
        nathanlevel2.put(2, nathanconvo2_l2_choice);

        // Dialogue 3: "...Can't sleep"
        Map<String, String> nathanconvo1_l2_choice = new LinkedHashMap<>(); // Renamed
        nathanconvo1_l2_choice.put("A", "Careful, you're sounding sentimental.");
        nathanconvo1_l2_choice.put("B", "At least you've got me here. That counts for something, right?");
        nathanconvo1_l2_choice.put("C", "Normal's gone. But we can make something new.");
        nathanconvo1_l2_choice.put("D", "Fire's nice. Makes it easier to forget what's out there.");
        nathanconvo1_l2_choice.put("E", "Normal's dead. Stop clinging to it.");
        nathanlevel2.put(3, nathanconvo1_l2_choice);

        nathanLevels.put(2, nathanlevel2);

        // Level 3
        Map<Integer, Map<String, String>> nathanlevel3 = new HashMap<>();
        Map<String, String> nathanconvo1_l3_choice = new LinkedHashMap<>();
        nathanconvo1_l3_choice.put("A", "Next time, don't play the hero. You're not invincible.");
        nathanconvo1_l3_choice.put("B", "If that's the best you can do, we're dead.");
        nathanconvo1_l3_choice.put("C", "I'm fine. We should watch each other's backs as always.");
        nathanconvo1_l3_choice.put("D", "You handled that like a pro. I'm impressed.");
        nathanconvo1_l3_choice.put("E", "Didn't know you cared so much. Cute.?");
        nathanlevel3.put(1, nathanconvo1_l3_choice);

        Map<String, String> nathanconvo2_l3_choice = new LinkedHashMap<>();
        nathanconvo2_l3_choice.put("A", "Weakness like that will get us both killed.");
        nathanconvo2_l3_choice.put("B", "Stop making excuses. Killing is part of survival.");
        nathanconvo2_l3_choice.put("C", "That means you're still human. Don't lose that.");
        nathanconvo2_l3_choice.put("D", "You've got grit, Nathan. That's what keeps us alive.");
        nathanconvo2_l3_choice.put("E", "I don't mind the blood. Makes you look dangerous.");
        nathanlevel3.put(2, nathanconvo2_l3_choice); // FIXED: Key 2

        Map<String, String> nathanconvo3_l3_choice = new LinkedHashMap<>();
        nathanconvo3_l3_choice.put("A", "You didn't lose me, Nathan. You never will, not if we keep fighting together.");
        nathanconvo3_l3_choice.put("B", "Didn't know you cared that much.");
        nathanconvo3_l3_choice.put("C", "You saved my life back there. Guess I owe you one.");
        nathanconvo3_l3_choice.put("D", "If you can't handle me in danger, maybe you should stay behind.");
        nathanconvo3_l3_choice.put("E", "What are you talking about? If you had been faster, we would've made it earlier.");
        nathanlevel3.put(3, nathanconvo3_l3_choice); // FIXED: Key 3

        nathanLevels.put(3, nathanlevel3);


        // Level 4
        Map<Integer, Map<String, String>> nathanlevel4 = new HashMap<>();
        Map<String, String> nathanconvo1_l4_choice = new LinkedHashMap<>();

        nathanconvo1_l4_choice.put("A", "Because I couldn't stand watching you walk into danger alone.");
        nathanconvo1_l4_choice.put("B", "Because I trust my gut. And I need you to trust me, too.");
        nathanconvo1_l4_choice.put("C", "Because I like proving you wrong. Keeps things interesting.");
        nathanconvo1_l4_choice.put("D", "Because someone has to think for both of us. You're slipping.");
        nathanconvo1_l4_choice.put("E", "Because your orders don't mean anything anymore.");
        nathanlevel4.put(1, nathanconvo1_l4_choice); // FIXED: Key 1

        Map<String, String> nathanconvo2_l4_choice = new LinkedHashMap<>();
        nathanconvo2_l4_choice.put("A", "That's the past. It doesn't matter anymore.");
        nathanconvo2_l4_choice.put("B", "You think a confession makes up for everything?");
        nathanconvo2_l4_choice.put("C", "You should've said something back then. But... I'm here now.");
        nathanconvo2_l4_choice.put("D", "Guess your timing's terrible, but at least you're finally honest.");
        nathanconvo2_l4_choice.put("E", "Then ask me now. My answer won't change.");
        nathanlevel4.put(2, nathanconvo2_l4_choice); // FIXED: Key 2

        Map<String, String> nathanconvo3_l4_choice = new LinkedHashMap<>();
        nathanconvo3_l4_choice.put("A", "You're not God, Nathan. You can't save everyone, but you can still save who's left.");
        nathanconvo3_l4_choice.put("B", "Then stop trying to carry it alone. Let me share the weight.");
        nathanconvo3_l4_choice.put("C", "If you really believe that, then why are you still fighting?");
        nathanconvo3_l4_choice.put("D", "Maybe you should've never led us. You're not built for this.");
        nathanconvo3_l4_choice.put("E", "That's defeatist talk. If you're giving up, tell me now.");
        nathanlevel4.put(3, nathanconvo3_l4_choice); // FIXED: Key 3

        nathanLevels.put(4, nathanlevel4);


        // Level 5
        Map<Integer, Map<String, String>> nathanlevel5 = new HashMap<>();
        Map<String, String> nathanconvo1_l5_choice = new LinkedHashMap<>();

        nathanconvo1_l5_choice.put("A", "Because if you fall apart now, everything we've done will mean nothing.");
        nathanconvo1_l5_choice.put("B", "Because you're too stubborn to die, remember?");
        nathanconvo1_l5_choice.put("C", "Because you've kept us alive this long. You don't have to give up now.");
        nathanconvo1_l5_choice.put("D", "Because if anyone can pull off a miracle, it's you.");
        nathanconvo1_l5_choice.put("E", "Because I'm not leaving without you.");
        nathanlevel5.put(1, nathanconvo1_l5_choice); // FIXED: Key 1

        Map<String, String> nathanconvo2_l5_choice = new LinkedHashMap<>();
        nathanconvo2_l5_choice.put("A", "We can't save everyone. It's time you accepted that.");
        nathanconvo2_l5_choice.put("B", "We wait for rescue. Someone will find us eventually.");
        nathanconvo2_l5_choice.put("C", "We dig through. No one gets left behind.");
        nathanconvo2_l5_choice.put("D", "We find another path. If we stay, we die.");
        nathanconvo2_l5_choice.put("E", "You're not dying here, Nathan. I won't let that happen.");
        nathanlevel5.put(2, nathanconvo2_l5_choice); // FIXED: Key 2

        Map<String, String> nathanconvo3_l5_choice = new LinkedHashMap<>();
        nathanconvo3_l5_choice.put("A", "You first. You've earned your shot at living.");
        nathanconvo3_l5_choice.put("B", "We go together. If one falls, the other pulls up.");
        nathanconvo3_l5_choice.put("C", "Follow my lead and don't look down. Eyes on survival, not fear.");
        nathanconvo3_l5_choice.put("D", "If we hesitate, we're both dead. Don't think, just move.");
        nathanconvo3_l5_choice.put("E", "If I go first, I'll test the rope. Safer that way.");
        nathanlevel5.put(3, nathanconvo3_l5_choice); // FIXED: Key 3

        nathanLevels.put(5, nathanlevel5);

        choiceMap.get("Nathan").putAll(nathanLevels);



//  // ====== YUBIE ======

        // // Level 1

        // Level 1
        Map<Integer, Map<String, String>> yubielevel1 = new HashMap<>();
        Map<String, String> yubieconvo1_l1 = new LinkedHashMap<>();

        yubieconvo1_l1.put("A", "Relax, I'm human.");
        yubieconvo1_l1.put("B", "Thank god, I thought I was alone.");
        yubieconvo1_l1.put("C", "Wow. You look more afraid than me.");
        yubieconvo1_l1.put("D", "Depends, are you offering your brains?");
        yubieconvo1_l1.put("E", "If I was a zombie, you'd already be dead.");
        yubielevel1.put(1, yubieconvo1_l1);

        Map<String, String> yubieconvo2_l1 = new LinkedHashMap<>();
        yubieconvo2_l1.put("A", "Yeah, I remember you. You always hogged the DDR machine.");
        yubieconvo2_l1.put("B", "You look...more than familiar. Kind of hard to forget you, actually");
        yubieconvo2_l1.put("C", "Yeah, and you always beat me at Time Crisis. I hated that.");
        yubieconvo2_l1.put("D", "You must be mistaken. I never hung out in arcades.");
        yubieconvo2_l1.put("E", "Nope. Don't know you, don't care.");
        yubielevel1.put(2, yubieconvo2_l1);

        Map<String, String> yubieconvo3_l1 = new LinkedHashMap<>();
        yubieconvo3_l1.put("A", "Don't worry. We'll find some to eat. Can't let my partner starve, right?");
        yubieconvo3_l1.put("B", "Don't be dramatic. You'll survive.");
        yubieconvo3_l1.put("C", "We'll find food. I won't let you starve.");
        yubieconvo3_l1.put("D", "If you die, I call dibs on your crowbar.");
        yubieconvo3_l1.put("E", "Guess we're starving together then.");
        yubielevel1.put(3, yubieconvo3_l1);

        yubieLevels.put(1, yubielevel1);


        // Level 2
        Map<Integer, Map<String, String>> yubielevel2 = new HashMap<>();
        Map<String, String> yubieconvo1_l2 = new LinkedHashMap<>();

        yubieconvo1_l2.put("A", "Don't worry. I'll protect you.");
        yubieconvo1_l2.put("B", "You've really thought this through, huh? I'll trust your instincts.");
        yubieconvo1_l2.put("C", "If zombies pop out, I'm tripping you and running. Sorry, survival rules.");
        yubieconvo1_l2.put("D", "Hey, at least it has walls and a roof. That's luxury now.");
        yubieconvo1_l2.put("E", "If you're too scared, just wait outside. I'll handle it.");
        yubielevel2.put(1, yubieconvo1_l2); // FIXED: Key 1

        Map<String, String> yubieconvo2_l2 = new LinkedHashMap<>();
        yubieconvo2_l2.put("A", "Does it really matter? The past is dead. Don't bring it up.");
        yubieconvo2_l2.put("B", "I had dreams, hobbies... but none of that matters now. Survival's what counts.");
        yubieconvo2_l2.put("C", "Just average. Work, eat, sleep, repeat. Nothing exciting.");
        yubieconvo2_l2.put("D", "I was looking for someone worth living for. I still haven't found them yet.");
        yubieconvo2_l2.put("E", "Honestly? I wasted way too much time watching shows and scrolling online.");
        yubielevel2.put(2, yubieconvo2_l2); // FIXED: Key 2

        Map<String, String> yubieconvo3_l2 = new LinkedHashMap<>();
        yubieconvo3_l2.put("A", "Then let's laugh now. Even if it's forced, it still counts.");
        yubieconvo3_l2.put("B", "Tell me one of your dumb apocalypse jokes. I'll laugh, I promise.");
        yubieconvo3_l2.put("C", "I could try making you laugh. I kinda want to see you smile more.");
        yubieconvo3_l2.put("D", "Yeah... I miss it too. But maybe someday we'll laugh again.");
        yubieconvo3_l2.put("E", "That's childish. This isn't the time for laughter.");
        yubielevel2.put(3, yubieconvo3_l2); // FIXED: Key 3

        yubieLevels.put(2, yubielevel2);


        // Level 3
        Map<Integer, Map<String, String>> yubielevel3 = new HashMap<>();
        Map<String, String> yubieconvo1_l3 = new LinkedHashMap<>();

        yubieconvo1_l3.put("A", "You call that surviving? You almost got us both killed. You're dead weight if you can't control yourself.");
        yubieconvo1_l3.put("B", "You're reckless. You almost rushed in without thinking.");
        yubieconvo1_l3.put("C", "You surprised me. I thought you'd break down, but you stood your ground.");
        yubieconvo1_l3.put("D", "You almost got us both killed. Think next time.");
        yubieconvo1_l3.put("E", "I thought I was going to lose you back there. That scared me more than the zombies.");
        yubielevel3.put(1, yubieconvo1_l3); // FIXED: Key 1

        Map<String, String> yubieconvo2_l3 = new LinkedHashMap<>();
        yubieconvo2_l3.put("A", "If they attack us later, it's on you.");
        yubieconvo2_l3.put("B", "Stay back. We don't know if we can trust them.");
        yubieconvo2_l3.put("C", "Let's introduce ourselves. They'll feel safer with us working together.");
        yubieconvo2_l3.put("D", "Let's work together. Two heads are better than one.");
        yubieconvo2_l3.put("E", "You really held it together back there. I don't know what I'd do without you.");
        yubielevel3.put(2, yubieconvo2_l3); // FIXED: Key 2

        Map<String, String> yubieconvo3_l3 = new LinkedHashMap<>();
        yubieconvo3_l3.put("A", "At least you've thought about everything. That counts for something.");
        yubieconvo3_l3.put("B", "I'm just glad you're here to guide us.");
        yubieconvo3_l3.put("C", "Let's test your theory together. I trust you.");
        yubieconvo3_l3.put("D", "Theory or not, we've survived worse. Stop being a coward.");
        yubieconvo3_l3.put("E", "We don't need theory. Just keep moving.");
        yubielevel3.put(3, yubieconvo3_l3); // FIXED: Key 3

        yubieLevels.put(3, yubielevel3);


        // Level 4
        Map<Integer, Map<String, String>> yubielevel4 = new HashMap<>();
        Map<String, String> yubieconvo1_l4 = new LinkedHashMap<>();

        yubieconvo1_l4.put("A", "Your plan makes sense. Let's follow it.");
        yubieconvo1_l4.put("B", "I trust you. You've proven yourself today.");
        yubieconvo1_l4.put("C", "Let's hear everyone out and maybe we can compromise on this.");
        yubieconvo1_l4.put("D", "You always talk like you know everything. Just... let someone else think for once.");
        yubieconvo1_l4.put("E", "We should do it my way. Your plan's too reckless.");
        yubielevel4.put(1, yubieconvo1_l4); // FIXED: Key 1

        Map<String, String> yubieconvo2_l4 = new LinkedHashMap<>();
        yubieconvo2_l4.put("A", "It's just food. People can fight for it if they want.");
        yubieconvo2_l4.put("B", "Stop overthinking. It's just supplies.");
        yubieconvo2_l4.put("C", "We'll follow your distribution plan. You're the best at this.");
        yubieconvo2_l4.put("D", "Maybe we can all pitch in and organize together?");
        yubieconvo2_l4.put("E", "You always think ahead. I feel safer knowing you're with me.");
        yubielevel4.put(2, yubieconvo2_l4); // FIXED: Key 2

        Map<String, String> yubieconvo3_l4 = new LinkedHashMap<>();
        yubieconvo3_l4.put("A", "It's impressive how calm you stayed under pressure.");
        yubieconvo3_l4.put("B", "You kept everyone safe. And... you made me feel safe too.");
        yubieconvo3_l4.put("C", "You did great, Yubie. I'm proud of you.");
        yubieconvo3_l4.put("D", "You still panic too much. We can't rely on you fully.");
        yubieconvo3_l4.put("E", "Stop worrying so much. You overthink everything.");
        yubielevel4.put(3, yubieconvo3_l4); // FIXED: Key 3

        yubieLevels.put(4, yubielevel4);


        // Level 5
        Map<Integer, Map<String, String>> yubielevel5 = new HashMap<>();
        Map<String, String> yubieconvo1_l5 = new LinkedHashMap<>();

        yubieconvo1_l5.put("A", "You're talking too much again. Just shut up and fight.");
        yubieconvo1_l5.put("B", "Just rush through! No thinking!");
        yubieconvo1_l5.put("C", "We have to be careful... don't take unnecessary risks.");
        yubieconvo1_l5.put("D", "Maybe we should rethink this...");
        yubieconvo1_l5.put("E", "Stick to the plan. We can do this together.");
        yubielevel5.put(1, yubieconvo1_l5); // FIXED: Key 1

        Map<String, String> yubieconvo2_l5 = new LinkedHashMap<>();
        yubieconvo2_l5.put("A", "Stop hesitating already. Just move before I leave you behind.");
        yubieconvo2_l5.put("B", "Forget planning, just sprint now!");
        yubieconvo2_l5.put("C", "Do what feels safest, Yubie.");
        yubieconvo2_l5.put("D", "I'll follow your lead, Yubie. You've got all the brains here.");
        yubieconvo2_l5.put("E", "After this, we'll be free. I'm glad it's with you.");
        yubielevel5.put(2, yubieconvo2_l5); // FIXED: Key 2

        Map<String, String> yubieconvo3_l5 = new LinkedHashMap<>();
        yubieconvo3_l5.put("A", "I didn't think we could survive this long. It feels surreal.");
        yubieconvo3_l5.put("B", "I'm glad too, Yubie. We made it together.");
        yubieconvo3_l5.put("C", "This is it... the end of the nightmare, I hope.");
        yubieconvo3_l5.put("D", "Don't get sentimental. You just got lucky this time.");
        yubieconvo3_l5.put("E", "Save the sappy stuff for later. Let's move.");
        yubielevel5.put(3, yubieconvo3_l5); // FIXED: Key 3

        yubieLevels.put(5, yubielevel5);

        choiceMap.get("Yubie").putAll(yubieLevels);





        // // ADI NI DIRI MAYGAHD ///

        // Level 1
        Map<Integer, Map<String, String>> adilevel1 = new HashMap<>();
        Map<String, String> adiconvo1_l1 = new LinkedHashMap<>();

        adiconvo1_l1.put("A", "You shouldn't waste supplies on me. I'll manage.");
        adiconvo1_l1.put("B", "I'm fine. Save your supplies for someone who really needs it.");
        adiconvo1_l1.put("C", "You're patching me up like we're still in a hospital. Kind of attractive, actually.");
        adiconvo1_l1.put("D", "Thanks... I owe you one.");
        adiconvo1_l1.put("E", "Does it really matter? Infection or zombies, same ending.");
        adilevel1.put(1, adiconvo1_l1);

        Map<String, String> adiconvo2_l1 = new LinkedHashMap<>();
        adiconvo2_l1.put("A", "No way. We're splitting it. I'm not letting you starve.");
        adiconvo2_l1.put("B", "Giving me food? Careful, Doc. I might start thinking you're sweet on me.");
        adiconvo2_l1.put("C", "You keep sacrificing yourself like this, you'll burn out.");
        adiconvo2_l1.put("D", "Fine. Thanks. I'll eat it then.");
        adiconvo2_l1.put("E", "If you're too weak to fight, you'll drag us down.");
        adilevel1.put(2, adiconvo2_l1);

        Map<String, String> adiconvo3_l1 = new LinkedHashMap<>();
        adiconvo3_l1.put("A", "You stayed behind for strangers? That's... kind of hot.");
        adiconvo3_l1.put("B", "But who's taking care of you, Adi?");
        adiconvo3_l1.put("C", "You made the right choice. People need you.");
        adiconvo3_l1.put("D", "No changing the past. Focus on what's next.");
        adiconvo3_l1.put("E", "Doesn't matter. All doctors are useless now.");
        adilevel1.put(3, adiconvo3_l1);

        adiLevels.put(1, adilevel1);


        // Level 2
        Map<Integer, Map<String, String>> adilevel2 = new HashMap<>();
        Map<String, String> adiconvo1_l2 = new LinkedHashMap<>();

        adiconvo1_l2.put("A", "Self-sacrificing hero, huh? Careful, I might start falling for you.");
        adiconvo1_l2.put("B", "Why do you always push yourself past the limit?");
        adiconvo1_l2.put("C", "You've already done enough, Adi. Get some rest.");
        adiconvo1_l2.put("D", "Fine. But don't expect me to carry you if you pass out.");
        adiconvo1_l2.put("E", "If you collapse from exhaustion, you'll just become dead weight.");
        adilevel2.put(1, adiconvo1_l2); // FIXED: Key 1

        Map<String, String> adiconvo2_l2 = new LinkedHashMap<>();
        adiconvo2_l2.put("A", "So basically you're guessing. Not very reassuring, Doc.");
        adiconvo2_l2.put("B", "I trust your hands more than any pharmacy.");
        adiconvo2_l2.put("C", "As long as it works, I won't complain.");
        adiconvo2_l2.put("D", "Improvised medicine? You just keep getting hotter.");
        adiconvo2_l2.put("E", "When was the last time you treated yourself, though?");
        adilevel2.put(2, adiconvo2_l2); // FIXED: Key 2

        Map<String, String> adiconvo3_l2 = new LinkedHashMap<>();
        adiconvo3_l2.put("A", "You're not a machine, Adi. You need to stop before you break.");
        adiconvo3_l2.put("B", "You don't have to carry this alone. Let me help.");
        adiconvo3_l2.put("C", "You're so dedicated it's... honestly interesting.");
        adiconvo3_l2.put("D", "Take a break, or keep collapsing. Your choice.");
        adiconvo3_l2.put("E", "Notes won't stop the dead from tearing us apart.");
        adilevel2.put(3, adiconvo3_l2); // FIXED: Key 3

        adiLevels.put(2, adilevel2);


        // Level 3
        Map<Integer, Map<String, String>> adilevel3 = new HashMap<>();
        Map<String, String> adiconvo1_l3 = new LinkedHashMap<>();

        adiconvo1_l3.put("A", "Hospitals don't matter anymore. The dead don't care about medicine.");
        adiconvo1_l3.put("B", "Places fall. People survive. That's all that counts now.");
        adiconvo1_l3.put("C", "You're still saving lives, Adi. Right now.");
        adiconvo1_l3.put("D", "You can't keep carrying ghosts. Let yourself grieve, Adi.");
        adiconvo1_l3.put("E", "You in a doctor's coat back then? Bet you were dangerously attractive.");
        adilevel3.put(1, adiconvo1_l3); // FIXED: Key 1

        Map<String, String> adiconvo2_l3 = new LinkedHashMap<>();
        adiconvo2_l3.put("A", "Then hurry up already. We don't have time to waste.");
        adiconvo2_l3.put("B", "Fine. If you fall and break your neck, that's on you.");
        adiconvo2_l3.put("C", "No, you're not risking yourself for maybe, pills. I'll go instead.");
        adiconvo2_l3.put("D", "Keep risking yourself, and it'll cost us both.");
        adilevel3.put(2, adiconvo2_l3);

        Map<String, String> adiconvo3_l3 = new LinkedHashMap<>();
        adiconvo3_l3.put("A", "We can't save everyone. Choose us.");
        adiconvo3_l3.put("B", "Your compassion? It's one of the sexiest things about you.");
        adiconvo3_l3.put("C", "You always choose others over yourself. Maybe it's time to choose differently.");
        adiconvo3_l3.put("D", "Stop hesitating. Either give it or don't.");
        adiconvo3_l3.put("E", "Give it to them. If we die, that's life.");
        adilevel3.put(3, adiconvo3_l3);

        adiLevels.put(3, adilevel3);


        // Level 4
        Map<Integer, Map<String, String>> adilevel4 = new HashMap<>();
        Map<String, String> adiconvo1_l4 = new LinkedHashMap<>();

        adiconvo1_l4.put("A", "You're the only one holding this group together.");
        adiconvo1_l4.put("B", "You being all protective like that? Hard not to notice.");
        adiconvo1_l4.put("C", "You can't save everyone, Adi. Pick your side.");
        adiconvo1_l4.put("D", "This place won't hold long. We should focus on moving.");
        adiconvo1_l4.put("E", "Let them fight. The weak won't last anyway.");
        adilevel4.put(1, adiconvo1_l4);

        Map<String, String> adiconvo2_l4 = new LinkedHashMap<>();
        adiconvo2_l4.put("A", "People die every day. Sleep won't change that.");
        adiconvo2_l4.put("B", "You're no use to us if you collapse.");
        adiconvo2_l4.put("C", "You're human, Adi. Rest before you break.");
        adiconvo2_l4.put("D", "You keep fixing others but ignore your own pain.");
        adiconvo2_l4.put("E", "Even tired, you still look unfairly good.");
        adilevel4.put(2, adiconvo2_l4);

        Map<String, String> adiconvo3_l4 = new LinkedHashMap<>();
        adiconvo3_l4.put("A", "Ignore them. You've done nothing wrong.");
        adiconvo3_l4.put("B", "If you're distracted, at least it's mutual.");
        adiconvo3_l4.put("C", "Maybe you are distracted. By me.");
        adiconvo3_l4.put("D", "We don't have time for feelings, Adi.");
        adiconvo3_l4.put("E", "Let's stay professional, for now.");
        adilevel4.put(3, adiconvo3_l4);

        adiLevels.put(4, adilevel4);


        // Level 5
        Map<Integer, Map<String, String>> adilevel5 = new HashMap<>();
        Map<String, String> adiconvo1_l5 = new LinkedHashMap<>();

        adiconvo1_l5.put("A", "Survival comes first. No attachments.");
        adiconvo1_l5.put("B", "We'll figure it out when the sun rises.");
        adiconvo1_l5.put("C", "We move. Together.");
        adiconvo1_l5.put("D", "You always let others decide. What do you want, Adi?");
        adiconvo1_l5.put("E", "If we die, at least I'll be with you.");
        adilevel5.put(1, adiconvo1_l5);

        Map<String, String> adiconvo2_l5 = new LinkedHashMap<>();
        adiconvo2_l5.put("A", "You did your job. Don't get sentimental.");
        adiconvo2_l5.put("B", "Stay silent, just squeeze his hand. ");
        adiconvo2_l5.put("C", "You saved more than lives, Adi. You saved me.");
        adiconvo2_l5.put("D", "Guess we make a pretty good team, huh?");
        adiconvo2_l5.put("E", "If the world wasn't ending, I'd ask you out properly.");
        adilevel5.put(2, adiconvo2_l5);

        Map<String, String> adiconvo3_l5 = new LinkedHashMap<>();
        adiconvo3_l5.put("A", "Not leaving you behind. We're finishing this together.");
        adiconvo3_l5.put("B", "Damn it, Adi, don't you dare die on me!");
        adiconvo3_l5.put("C", "You're bleeding, let me help you walk!");
        adiconvo3_l5.put("D", "You'll only slow us down. Stay if you have to.");
        adiconvo3_l5.put("E", "Grab his hand wordlessly and pull him along.");
        adilevel5.put(3, adiconvo3_l5);

        adiLevels.put(5, adilevel5);

        choiceMap.get("Adi").putAll(adiLevels);

    }
}
