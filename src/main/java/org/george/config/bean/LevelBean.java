package org.george.config.bean;

public class LevelBean {

    private Integer levelId;

    private String levelName;

    private MonsterBean monsterBean;

    private Integer winningRate;

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

    public MonsterBean getMonsterBean() {
        return monsterBean;
    }

    public void setMonsterBean(MonsterBean monsterBean) {
        this.monsterBean = monsterBean;
    }

    public Integer getWinningRate() {
        return winningRate;
    }

    public void setWinningRate(Integer winningRate) {
        this.winningRate = winningRate;
    }


    public LevelBean() {
    }

    @Override
    public String toString() {
        return "Level{" +
                "level=" + levelId +
                ", levelName='" + levelName + '\'' +
                ", monster=" + monsterBean +
                ", winningRate=" + winningRate +
                '}';
    }
}
