package Weapon;

public class WaterBottle extends Weapon {
    public WaterBottle() {
        super("Water Bottle", 20, 1, "Not very useful, but better than nothing.");
    }

    @Override
    public int getDamage() {
        return 5;
    }
}
