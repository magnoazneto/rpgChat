package rpgGame;

public class Character {
    protected int lifePoints;
    protected int maxLife = 50;

    Character(){
        this.lifePoints = maxLife;
    }

    public void setLifePoints(int lifePoints) {
        if (getLifePoints() < maxLife) {
            this.lifePoints = lifePoints;
        }
        if (getLifePoints() > maxLife){ setLifePoints(maxLife);}
        if (getLifePoints() < 0){ setLifePoints(0);}
    }

    public int getLifePoints() {
        return lifePoints;
    }

    public void incrementLife(int points){
        if (points > 0){
            setLifePoints(getLifePoints() + points);
        }
    }

    public void decrementLife(int points){
        if (points > 0){
            setLifePoints(getLifePoints() - points);
        }
    }
}
