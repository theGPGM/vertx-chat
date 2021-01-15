package org.george.mahjong.cache.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 0 - 8 万
 * 9 - 17 饼
 * 18 - 26 条
 * 27 - 33 东西南北中发白
 */
public class MahJongPlayerCacheBean {

    private Integer playerId;

    // 吃的牌
    private List<List<Integer>> chi = new ArrayList<>();

    // 碰的牌
    private List<Integer> peng = new ArrayList<>();

    // 明杠
    private List<Integer> mingGang = new ArrayList<>();

    // 暗杠
    private List<Integer> anGang = new ArrayList<>();

    // 手牌
    private int[] handCard = new int[34];

    // 花牌数
    private int flowerCard = 0;

    // 是否离线
    private boolean isOffline = false;

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

    public void addFlowerCard(){
        flowerCard++;
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

    public void signHandCards(List<Integer> handCards){
        for(int k : handCards){
            handCard[k] ++;
        }
    }

    public MahJongPlayerCacheBean(Integer playerId) {
        this.playerId = playerId;
    }

    public void clearCache(){
        this.chi = new ArrayList<>();
        this.peng = new ArrayList<>();
        this.mingGang = new ArrayList<>();
        this.anGang = new ArrayList<>();
        this.handCard = new int[34];
        this.flowerCard = 0;
    }
}
