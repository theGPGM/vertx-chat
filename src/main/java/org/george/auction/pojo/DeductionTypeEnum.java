package org.george.auction.pojo;

public enum DeductionTypeEnum {

    GOLD(1, "金币");

    private int type;

    private String name;

    public int getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    DeductionTypeEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }
}
