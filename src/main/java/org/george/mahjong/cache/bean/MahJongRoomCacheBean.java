package org.george.mahjong.cache.bean;

import java.util.ArrayList;
import java.util.List;

public class MahJongRoomCacheBean {

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
     * 谁是庄
     */
    private Integer zhuang;

    private Integer whoPeng = -1;

    private Integer whoGang = -1;

    private Integer whoChi = -1;

    private Integer whoPlay = -1;

    private List<Integer> whoHu = new ArrayList<>();

    private Integer playCard = -1;

    private Integer whoQiangGang = -1;

    /**
     * 四位玩家
     */
    private List<MahJongPlayerCacheBean> list = new ArrayList<>();

    /**
     * 牌墙
     */
    private List<Integer> cardWall = new ArrayList<>();

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

    public List<MahJongPlayerCacheBean> getPlayers(){
        return list;
    }

    public List<MahJongPlayerCacheBean> getList() {
        return list;
    }

    public MahJongRoomCacheBean(Integer roomId) {
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

    public Integer getZhuang() {
        return zhuang;
    }

    public void setZhuang(Integer zhuang) {
        this.zhuang = zhuang;
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

    public Integer getWhoPeng() {
        return whoPeng;
    }

    public void setWhoPeng(Integer whoPeng) {
        this.whoPeng = whoPeng;
    }

    public Integer getWhoGang() {
        return whoGang;
    }

    public void setWhoGang(Integer whoGang) {
        this.whoGang = whoGang;
    }

    public Integer getWhoChi() {
        return whoChi;
    }

    public void setWhoChi(Integer whoChi) {
        this.whoChi = whoChi;
    }

    public List<Integer> getWhoHu() {
        return whoHu;
    }

    public void addWhoHu(Integer playerId) {
        this.whoHu.add(playerId);
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

    public Integer getWhoQiangGang() {
        return whoQiangGang;
    }

    public void setWhoQiangGang(Integer whoQiangGang) {
        this.whoQiangGang = whoQiangGang;
    }
}
