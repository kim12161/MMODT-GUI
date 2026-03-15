package RelationshipSystem;

public class Relationship {
    private int trust;
    private int turnOn;
    private int turnOff;

    public Relationship() {
        this.trust = 0;
        this.turnOn = 0;
        this.turnOff = 0;
    }

    public void increaseTrust(int points) { this.trust += points; }
    public void increaseTurnOn(int points) { this.turnOn += points; }
    public void increaseTurnOff(int points) { this.turnOff += points; }

    public double calculateFinalScore(int charisma) {
        double baseScore = (trust * 2 + turnOn * 3 + charisma * 2 - turnOff * 3);
        return Math.max(0, Math.min(100, baseScore));
    }

    // Getters
    public int getTrust() { return trust; }
    public int getTurnOn() { return turnOn; }
    public int getTurnOff() { return turnOff; }
}