package org.george.bag.model.pojo;

public class PlayerItemResult {

    private Integer playerId;

    private Integer itemId;

    private Integer num;

    public Integer getItemId() {
        return itemId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "PlayerItem{" +
                "playerId=" + playerId +
                ", itemId=" + itemId +
                ", num=" + num +
                '}';
    }
}
