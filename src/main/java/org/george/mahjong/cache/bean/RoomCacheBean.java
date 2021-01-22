package org.george.mahjong.cache.bean;

import java.util.ArrayList;
import java.util.List;

public class RoomCacheBean {

    /**
     * 房间号
     */
    private Integer roomId;

    /**
     * 是否开始游戏
     */
    private boolean start;

    /**
     * 第几圈
     */
    private Integer quan;

    /**
     * 第几盘
     */
    private Integer pan;

    /**
     * 四位玩家
     */
    private List<PlayerCacheBean> list = new ArrayList<>();

    /**
     * 牌墙
     */
    private List<Integer> cardWall = new ArrayList<>();

    private Integer whoHu = -1;

    private Integer whoPeng = -1;

    private Integer whoChi = -1;

    private Integer whoMingGang = -1;

    private Integer whoPlay = -1;

    private Integer playCard = -1;

    /**
     * 牌池
     */
    private List<Integer> cardPool = new ArrayList<>();

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public List<PlayerCacheBean> getPlayers(){
        return list;
    }

    public List<PlayerCacheBean> getList() {
        return list;
    }

    public RoomCacheBean(Integer roomId) {
        this.roomId = roomId;
    }

    public void addCardWall(List<Integer> cards){
        this.cardWall = cards;
    }

    public List<Integer> getCardWall(){
        return cardWall;
    }

    public Integer getQuan() {
        return quan;
    }

    public void setQuan(Integer quan) {
        this.quan = quan;
    }

    public Integer getPan() {
        return pan;
    }

    public void setPan(Integer pan) {
        this.pan = pan;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public List<Integer> getCardPool() {
        return cardPool;
    }

    public void setCardPool(List<Integer> cardPool) {
        this.cardPool = cardPool;
    }

    public Integer getWhoHu() {
        return whoHu;
    }

    public void setWhoHu(Integer whoHu) {
        this.whoHu = whoHu;
    }

    public Integer getWhoPeng() {
        return whoPeng;
    }

    public void setWhoPeng(Integer whoPeng) {
        this.whoPeng = whoPeng;
    }

    public Integer getWhoChi() {
        return whoChi;
    }

    public void setWhoChi(Integer whoChi) {
        this.whoChi = whoChi;
    }

    public Integer getWhoMingGang() {
        return whoMingGang;
    }

    public void setWhoMingGang(Integer whoMingGang) {
        this.whoMingGang = whoMingGang;
    }

    public Integer getWhoPlay() {
        return whoPlay;
    }

    public void setWhoPlay(Integer whoPlay) {
        this.whoPlay = whoPlay;
    }

    public Integer getPlayCard() {
        return playCard;
    }

    public void setPlayCard(Integer playCard) {
        this.playCard = playCard;
    }

    public void setList(List<PlayerCacheBean> list) {
        this.list = list;
    }

    public void setCardWall(List<Integer> cardWall) {
        this.cardWall = cardWall;
    }
}
