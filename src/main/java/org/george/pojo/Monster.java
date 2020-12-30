package org.george.pojo;

public class Monster {

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

    public Monster(Integer monsterId, String monsterName) {
        this.monsterId = monsterId;
        this.monsterName = monsterName;
    }

    public Monster() {
    }

    @Override
    public String toString() {
        return "Monster{" +
                "monsterId=" + monsterId +
                ", monsterName='" + monsterName + '\'' +
                '}';
    }
}
