package org.george.mahjong.cache.bean;

import java.util.ArrayList;
import java.util.List;

public class MahJongRoomCacheBean {

    // 房间号
    private Integer roomId;

    // 四个玩家
    private List<MahJongPlayerCacheBean> list = new ArrayList<>();

    // 轮到谁发出命令
    private int whoAct = 0;

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
}
