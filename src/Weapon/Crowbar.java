package Weapon;

public class Crowbar extends Weapon {
    public Crowbar() {
        super("Crowbar", 70, 4, "A heavy metal tool capable of smashing zombie skulls.");
    }

    @Override
    public int getDamage() {
        return 35;
    }
}
