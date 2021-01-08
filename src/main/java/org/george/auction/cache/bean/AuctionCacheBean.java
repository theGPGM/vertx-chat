package org.george.auction.cache.bean;

public class AuctionCacheBean {

    private Integer auctionId;

    private Integer auctionType;

    private Integer deductionType;

    private Integer cost;

    private Integer num;

    public Integer getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Integer auctionId) {
        this.auctionId = auctionId;
    }

    public Integer getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(Integer auctionType) {
        this.auctionType = auctionType;
    }

    public Integer getDeductionType() {
        return deductionType;
    }

    public void setDeductionType(Integer deductionType) {
        this.deductionType = deductionType;
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

    public AuctionCacheBean() {
    }

    @Override
    public String toString() {
        return "AuctionCacheBean{" +
                "auctionId=" + auctionId +
                ", auctionType=" + auctionType +
                ", deductionType=" + deductionType +
                ", cost=" + cost +
                ", num=" + num +
                '}';
    }
}
