package org.george.hall.cache.bean;

public class PlayerCacheBean {

    private Integer playerId;

    private String playerName;

    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
                ", password='" + password + '\'' +
                ", hp=" + hp +
                ", gold=" + gold +
                '}';
    }
}
