package org.george.mahjong.pojo;

public enum HuState {
    NotHu(1), FanPaoHu(2), QiangGangHu(3), ZiMoHu(4), TianHu(5), GangMoHu(6);

    private int state;

    HuState(int state){
        this.state = state;
    }
}
