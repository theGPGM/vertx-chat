package org.george.hall.model.impl;

import org.george.hall.cache.PlayerCache;

import org.george.hall.cache.bean.PlayerCacheBean;
import org.george.hall.dao.PlayerDao;
import org.george.hall.dao.bean.PlayerBean;
import org.george.hall.model.PlayerModel;
import org.george.hall.model.pojo.PlayerResult;

import java.util.HashMap;
import java.util.Map;

public class PlayerModelImpl implements PlayerModel {

    private PlayerModelImpl(){}

    private static PlayerModelImpl instance = new PlayerModelImpl();

    public static PlayerModelImpl getInstance(){
        return instance;
    }

    private PlayerDao playerDao = PlayerDao.getInstance();

    private PlayerCache playerCache = PlayerCache.getInstance();

    Map<String, String> hIdMap = new HashMap<>();

    Map<String, String> userIdMap = new HashMap<>();

    @Override
    public String getPlayerNameByPlayerId(String userId){
        return getPlayerByPlayerId(Integer.parseInt(userId)).getPlayerName();
    }

    @Override
    public PlayerResult getPlayerByPlayerId(Integer playerId) {
        PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerId(playerId);
        if(cacheBean != null){
            return cacheBean2PlayerResult(cacheBean);
        }else{
            PlayerBean bean = playerDao.loadPlayerByPlayerId(playerId);
            if(bean == null){
                return null;
            }else{
                cacheBean = bean2PlayerCacheBean(bean);
                playerCache.addPlayer(cacheBean);
                return cacheBean2PlayerResult(cacheBean);
            }
        }
    }

    @Override
    public void updatePlayerHP(Integer playerId, Integer hp) {
        PlayerCacheBean cacheBean = new PlayerCacheBean();
        cacheBean.setPlayerId(playerId);
        cacheBean.setHp(hp);
        playerCache.updateSelective(cacheBean);

        PlayerBean bean = new PlayerBean();
        bean.setPlayerId(playerId);
        bean.setHp(hp);
        playerDao.updatePlayerSelective(bean);
    }

    @Override
    public void updatePlayerGold(Integer playerId, Integer gold) {
        PlayerCacheBean cacheBean = new PlayerCacheBean();
        cacheBean.setPlayerId(playerId);
        cacheBean.setGold(gold);
        playerCache.updateSelective(cacheBean);

        PlayerBean bean = new PlayerBean();
        bean.setPlayerId(playerId);
        bean.setGold(gold);
        playerDao.updatePlayerSelective(bean);
    }

    @Override
    public Integer getPlayerCurrentHP(Integer playerId) {

        PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerId(playerId);
        PlayerBean bean = playerDao.loadPlayerByPlayerId(playerId);
        if (cacheBean == null) {
            return bean.getHp();
        }else{

            long timeStamp = playerCache.getTimeStamp(playerId);
            int increment = (int)(System.currentTimeMillis() - timeStamp) / (1000 * 60  * 5);
            int curHP = cacheBean.getHp() + increment;
            if(curHP > 100) curHP = 100;

            cacheBean.setHp(curHP);
            bean.setHp(curHP);

            playerCache.addTimeStampIfNotExisting(playerId);
            playerCache.updateSelective(cacheBean);
            playerDao.updatePlayerSelective(bean);
            return curHP;
        }
    }

    private PlayerCacheBean bean2PlayerCacheBean(PlayerBean bean){
        PlayerCacheBean pcb = new PlayerCacheBean();
        pcb.setPlayerId(bean.getPlayerId());
        pcb.setPlayerName(bean.getPlayerName());
        pcb.setHp(bean.getHp());
        pcb.setGold(bean.getGold());
        return pcb;
    }

    private PlayerResult cacheBean2PlayerResult(PlayerCacheBean bean){
        PlayerResult pr = new PlayerResult();
        pr.setPlayerId(bean.getPlayerId());
        pr.setPlayerName(bean.getPlayerName());
        pr.setHp(bean.getHp());
        pr.setGold(bean.getGold());
        return pr;
    }

    @Override
    public boolean deductionNotify(Integer playerId, Integer num) {

        PlayerCacheBean cacheBean = playerCache.loadPlayerByPlayerId(playerId);

        if(cacheBean != null){
            if(cacheBean.getGold() < num){
                return false;
            }else{
                PlayerBean bean = new PlayerBean();
                bean.setPlayerId(playerId);
                bean.setGold(cacheBean.getGold() - num);
                playerDao.updatePlayerSelective(bean);

                cacheBean.setGold(cacheBean.getGold() - num);
                playerCache.updateSelective(cacheBean);
            }
        }else{
            PlayerBean bean = playerDao.loadPlayerByPlayerId(playerId);
            if(bean == null || bean.getGold() < num){
                return false;
            }else{
                bean.setGold(bean.getGold() - num);
                playerDao.updatePlayerSelective(bean);
                addCache(playerId);
            }
        }
        return true;
    }

    @Override
    public String getUId(String hId) {
        return userIdMap.get(hId);
    }

    @Override
    public String getHId(String userId) {
        return hIdMap.get(userId);
    }

    @Override
    public void addUIdAndHId(String userId, String hId) {
        userIdMap.put(hId, userId);
        hIdMap.put(userId, hId);
    }

    @Override
    public void logout(String userId) {
        String hId = hIdMap.get(userId);
        if(hId != null){
            userIdMap.remove(hId);
        }
        hIdMap.remove(userId);
    }

    @Override
    public void clientCloseNotify(String hId) {
        String userId = getUId(hId);
        if(userId != null){
            logout(userId);
        }
    }

    private void addCache(Integer playerId){
        PlayerBean bean = playerDao.loadPlayerByPlayerId(playerId);
        PlayerCacheBean cacheBean = new PlayerCacheBean();
        cacheBean.setPlayerId(bean.getPlayerId());
        cacheBean.setPlayerName(bean.getPlayerName());
        cacheBean.setGold(bean.getGold());
        cacheBean.setHp(bean.getHp());
        playerCache.addPlayer(cacheBean);
    }
}
