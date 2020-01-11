package rpgGame;

import java.util.ArrayList;

public class Player extends Character {
    ArrayList<String> skills = new ArrayList();

    public Player(String initialSkill) {
        super();
        this.skills.add(initialSkill);
    }

    public ArrayList<String> getSkills() {
        return skills;
    }
}
