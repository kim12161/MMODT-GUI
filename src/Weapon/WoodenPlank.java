package Weapon;
public class WoodenPlank extends Weapon {
    public WoodenPlank() {
        super("Wooden Plank", 40, 2, "A broken plank, decent for quick defense.");
    }

    @Override
    public int getDamage() {
        return 15;
    }
}
