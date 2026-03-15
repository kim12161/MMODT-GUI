package Weapon;

public class BaseballBat extends Weapon{
        public BaseballBat() {
            super("Baseball Bat", 70, 3, "A sturdy bat ideal for smashing skulls.");
        }
        @Override
        public int getDamage() {
            return 25;
        }
}
