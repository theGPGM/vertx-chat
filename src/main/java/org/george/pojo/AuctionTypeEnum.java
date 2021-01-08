package org.george.pojo;

public enum AuctionTypeEnum {

    Item(1);

    private int type;

    public int getType(){
        return type;
    }

    AuctionTypeEnum(int type) {
        this.type = type;
    }
}
