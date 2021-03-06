package org.george.dungeon_game.config.bean;

public class MonsterBean {

    private Integer monsterId;

    private String monsterName;


    public Integer getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(Integer monsterId) {
        this.monsterId = monsterId;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public MonsterBean(Integer monsterId, String monsterName) {
        this.monsterId = monsterId;
        this.monsterName = monsterName;
    }

    public MonsterBean() {
    }

    @Override
    public String toString() {
        return "Monster{" +
                "monsterId=" + monsterId +
                ", monsterName='" + monsterName + '\'' +
                '}';
    }
}
