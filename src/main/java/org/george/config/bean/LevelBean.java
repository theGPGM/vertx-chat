package org.george.config.bean;

public class LevelBean {

    private Integer levelId;

    private String levelName;

    private Monster monster;

    private Integer winningRate;

    private Integer DroppingWinItemRate;

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer level) {
        this.levelId = level;
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

    public LevelBean() {
    }

    @Override
    public String toString() {
        return "Level{" +
                "level=" + levelId +
                ", levelName='" + levelName + '\'' +
                ", monster=" + monster +
                ", winningRate=" + winningRate +
                ", DroppingWinItemRate=" + DroppingWinItemRate +
                '}';
    }
}
