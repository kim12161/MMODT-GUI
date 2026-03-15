package Characters;
import Player.Gender;
public class Kim extends Character {
    public Kim() {
        super(
                "Kim",
                "A nursing student who volunteered at hospitals during the early outbreak. Despite trauma, she refuses to give up helping the injured and sick.",
                "Medical Aid & Improvisation",
                "Gentle & Compassionate",
                "Overly Self-Sacrificing & Can't say no to people in need",
                "She heals others but carries invisible scars, your bond helps her remember she deserves care, too.",
                Gender.FEMALE  // Added gender
        );
    }
}