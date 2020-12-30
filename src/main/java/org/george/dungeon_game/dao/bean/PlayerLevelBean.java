package org.george.dungeon_game.dao.bean;

public class PlayerLevelBean {

    private Integer playerId;

    private Integer level;

    private Integer loseCount;

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getLoseCount() {
        return loseCount;
    }

    public void setLoseCount(Integer loseCount) {
        this.loseCount = loseCount;
    }

    public PlayerLevelBean() {
    }

    @Override
    public String toString() {
        return "PlayerLevel{" +
                "playerId=" + playerId +
                ", level=" + level +
                ", loseCount=" + loseCount +
                '}';
    }
}
