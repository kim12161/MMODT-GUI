package Characters;

import Player.Gender;

public class Adi extends Character {
    public Adi() {
        super(
                "Adi",
                "A medical intern who never finished his residency before the outbreak. He stayed behind when others fled, trying to help as many survivors as possible.",
                "Medical Expertise & Resourcefulness",
                "Compassionate & Calm under pressure",
                "Overextends himself & Struggles with boundaries",
                "He's the one who can patch up wounds, but who will patch up his tired heart?",
                Gender.MALE  // Added gender
        );
    }
}