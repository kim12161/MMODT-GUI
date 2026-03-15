package Weapon;

public class KitchenKnife extends Weapon {
    public KitchenKnife() {
        super("Kitchen Knife", 60, 3, "A sharp knife perfect for close combat.");
    }

    @Override
    public int getDamage() {
        return 25;
    }
}
