package org.george.mahjong.cache.bean;

import java.util.ArrayList;
import java.util.List;


public class MahJongPlayerCacheBean {

    /**
     * 玩家 ID
     */
    private Integer playerId;

    /**
     * 游戏点数
     */
    private Integer point;

    /** 以下为副露中的内容 **/
    private List<List<Integer>> chi = new ArrayList<>();

    private List<Integer> peng = new ArrayList<>();

    private List<Integer> mingGang = new ArrayList<>();

    private List<Integer> anGang = new ArrayList<>();
    /** 以上为副露中的内容 **/

    /**
     * 手牌数据结构，有一个 int[] 存储手牌的索引
     * 0 - 8 为 1 - 9 万
     * 9 - 17 为 1 - 9 饼
     * 18 - 26 为 1- 9 条
     * 27 - 33 东西南北中发白
     */
    private int[] handCard;

    /**
     * 花牌数量
     */
    private Integer flowerCard = 0;

    /**
     * 是否处于准备就绪状态
     */
    private boolean ready = false;

    /**
     * 是否离线
     */
    private boolean isOffline = false;


    public Integer getPlayerId(){
        return playerId;
    }

    public void addChi(List<Integer> c){
        chi.add(c);
    }

    public List<List<Integer>> getChi(){
        return chi;
    }

    public void addPeng(int card){
        peng.add(card);
    }

    public List<Integer> getPeng(){
        return peng;
    }

    public void addMingGang(int card){
        mingGang.add(card);
    }

    public List<Integer> getMingGang(){
        return mingGang;
    }

    public void addAnGang(int card){
        anGang.add(card);
    }

    public List<Integer> getAnGang(){
        return anGang;
    }

    public void addFlowerCard(Integer count){
        flowerCard += count;
    }

    public int getFlowerCard(){
        return flowerCard;
    }

    public boolean isOffline(){
        return isOffline;
    }

    public void setOffline(boolean val){
        this.isOffline = val;
    }

    public boolean isReady(){
        return ready;
    }

    public void setReady(boolean val){
        this.ready = val;
    }

    public void addHandCards(int[] handCards){
        this.handCard = new int[34];
        for(int k : handCards){
            handCard[k] ++;
        }
    }

    public int[] getHandCards(){
        return handCard;
    }

    public MahJongPlayerCacheBean(Integer playerId) {
        this.playerId = playerId;
    }
}
