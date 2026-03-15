package Characters;
import Player.Gender;

public class Character {
    private String name;
    private String role;
    private String survivalSkills;
    private String personality;
    private String flaws;
    private String romanceHook;
    private Gender gender;

    public Character(String name, String role, String survivalSkills,
                     String personality, String flaws, String romanceHook, Gender gender) {
        this.name = name;
        this.role = role;
        this.survivalSkills = survivalSkills;
        this.personality = personality;
        this.flaws = flaws;
        this.romanceHook = romanceHook;
        this.gender = gender;
    }

    // Getters
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getSurvivalSkills() { return survivalSkills; }
    public String getPersonality() { return personality; }
    public String getFlaws() { return flaws; }
    public String getRomanceHook() { return romanceHook; }
    public Gender getGender() { return gender; }
}