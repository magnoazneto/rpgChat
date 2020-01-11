package rpgGame;

public class TestaGame {
    public static void main(String[] args) {
        Player p1 = new Player("Thunderbolt");
        Player p2 = new Player("Flamethrower");

        System.out.println(p1.getSkills());
        System.out.println(p2.getSkills());

    }
}
