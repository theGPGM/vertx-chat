package org.george.auction.pojo;

public class AuctionItem {

    private Integer itemId;

    private Integer cost;

    private Integer num;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public AuctionItem() {
    }

    @Override
    public String toString() {
        return "AuctionItem{" +
                "itemId=" + itemId +
                ", cost=" + cost +
                ", num=" + num +
                '}';
    }
}
