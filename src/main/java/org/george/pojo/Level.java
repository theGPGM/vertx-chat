package org.george.pojo;

public class Level {

    private Integer level;

    private String levelName;

    private Monster monster;

    private Integer winningRate;

    private Integer DroppingWinItemRate;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    public Integer getWinningRate() {
        return winningRate;
    }

    public void setWinningRate(Integer winningRate) {
        this.winningRate = winningRate;
    }

    public Integer getDroppingWinItemRate() {
        return DroppingWinItemRate;
    }

    public void setDroppingWinItemRate(Integer droppingWinItemRate) {
        DroppingWinItemRate = droppingWinItemRate;
    }

    public Level() {
    }

    @Override
    public String toString() {
        return "Level{" +
                "level=" + level +
                ", levelName='" + levelName + '\'' +
                ", monster=" + monster +
                ", winningRate=" + winningRate +
                ", DroppingWinItemRate=" + DroppingWinItemRate +
                '}';
    }
}
