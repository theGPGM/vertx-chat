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

    /**
     * 轮到谁操作
     */
    private Integer whoAct;

    private Integer passCount = 0;

    private Integer playCard = -1;

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

    public MahJongPlayerCacheBean getPlayer(Integer playerId){
        for(MahJongPlayerCacheBean player: list){
            if(player.getPlayerId().equals(playerId)){
                return player;
            }
        }
        return null;
    }

    public void addPlayer(MahJongPlayerCacheBean cacheBean){
        list.add(cacheBean);
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

    public int getWhoAct() {
        return whoAct;
    }

    public void setWhoAct(int whoAct) {
        this.whoAct = whoAct;
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

    public Integer getPassCount() {
        return passCount;
    }

    public void setPassCount(Integer passCount) {
        this.passCount = passCount;
    }

    public Integer getPlayCard() {
        return playCard;
    }

    public void setPlayCard(Integer playCard) {
        this.playCard = playCard;
    }
}
