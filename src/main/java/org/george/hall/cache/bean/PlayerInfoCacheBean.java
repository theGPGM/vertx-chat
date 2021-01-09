package org.george.hall.cache.bean;

public class PlayerInfoCacheBean {

    private Integer playerId;

    private String playerName;

    private Integer hp;

    private Integer gold;

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }


    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
        this.gold = gold;
    }

    @Override
    public String toString() {
        return "PlayerCacheBean{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", hp=" + hp +
                ", gold=" + gold +
                '}';
    }
}
