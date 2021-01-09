package org.george.dungeon_game.config.bean;

public class DropItemInfoBean {

    /**
     * 关卡
     */
    private Integer level;

    /**
     * 道具 ID
     */
    private Integer itemId;

    /**
     * 掉落率
     */
    private Integer rate;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "DropItemInfoBean{" +
                "level=" + level +
                ", itemId=" + itemId +
                ", rate=" + rate +
                '}';
    }
}
