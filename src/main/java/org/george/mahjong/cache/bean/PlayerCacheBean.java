package org.george.mahjong.cache.bean;

import java.util.ArrayList;
import java.util.List;


public class PlayerCacheBean {

    /**
     * 玩家 ID
     */
    private Integer playerId;

    /**
     * 游戏点数
     */
    private Integer point;

    /** 以下为副露中的内容 **/
    private List<List<Integer>> chis = new ArrayList<>();

    private List<Integer> pengs = new ArrayList<>();

    private List<Integer> mingGangs = new ArrayList<>();

    private List<Integer> anGangs = new ArrayList<>();

    private boolean tianHu = false;

    private boolean ziMo = false;

    private boolean fanPao = false;

    private boolean qiangGangHu = false;

    private boolean chi = false;

    private boolean mingGang = false;

    private boolean anGang = false;

    private boolean gangMoHu = false;

    private boolean peng = false;

    private boolean needPlay = false;

    /**
     * 摸的牌
     */
    private Integer drawCard = -1;

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

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public List<List<Integer>> getChis() {
        return chis;
    }

    public void setChis(List<List<Integer>> chis) {
        this.chis = chis;
    }

    public List<Integer> getPengs() {
        return pengs;
    }

    public void setPengs(List<Integer> pengs) {
        this.pengs = pengs;
    }

    public List<Integer> getMingGangs() {
        return mingGangs;
    }

    public void setMingGangs(List<Integer> mingGangs) {
        this.mingGangs = mingGangs;
    }

    public List<Integer> getAnGangs() {
        return anGangs;
    }

    public void setAnGangs(List<Integer> anGangs) {
        this.anGangs = anGangs;
    }

    public boolean isTianHu() {
        return tianHu;
    }

    public void setTianHu(boolean tianHu) {
        this.tianHu = tianHu;
    }

    public boolean isZiMo() {
        return ziMo;
    }

    public void setZiMo(boolean ziMo) {
        this.ziMo = ziMo;
    }

    public boolean isFanPao() {
        return fanPao;
    }

    public void setFanPao(boolean fanPao) {
        this.fanPao = fanPao;
    }

    public boolean isQiangGangHu() {
        return qiangGangHu;
    }

    public void setQiangGangHu(boolean qiangGangHu) {
        this.qiangGangHu = qiangGangHu;
    }

    public boolean isChi() {
        return chi;
    }

    public void setChi(boolean chi) {
        this.chi = chi;
    }

    public boolean isMingGang() {
        return mingGang;
    }

    public void setMingGang(boolean mingGang) {
        this.mingGang = mingGang;
    }

    public boolean isAnGang() {
        return anGang;
    }

    public void setAnGang(boolean anGang) {
        this.anGang = anGang;
    }

    public boolean isGangMoHu() {
        return gangMoHu;
    }

    public void setGangMoHu(boolean gangMoHu) {
        this.gangMoHu = gangMoHu;
    }

    public boolean isPeng() {
        return peng;
    }

    public void setPeng(boolean peng) {
        this.peng = peng;
    }

    public boolean isNeedPlay() {
        return needPlay;
    }

    public void setNeedPlay(boolean needPlay) {
        this.needPlay = needPlay;
    }

    public Integer getDrawCard() {
        return drawCard;
    }

    public void setDrawCard(Integer drawCard) {
        this.drawCard = drawCard;
    }

    public int[] getHandCard() {
        return handCard;
    }

    public void setHandCard(int[] handCard) {
        this.handCard = handCard;
    }

    public Integer getFlowerCard() {
        return flowerCard;
    }

    public void setFlowerCard(Integer flowerCard) {
        this.flowerCard = flowerCard;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public void addChi(List<Integer> chi){
        chis.add(chi);
    }

    public void addPeng(Integer peng){
        pengs.add(peng);
    }

    public void addGang(Integer gang){
        anGangs.add(gang);
    }

    public PlayerCacheBean(Integer playerId){
        this.playerId = playerId;
    }

    public void addFlowerCard(Integer count){
        this.flowerCard += count;
    }
}
