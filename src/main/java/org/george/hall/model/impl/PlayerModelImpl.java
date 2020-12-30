package org.george.hall.model.impl;

import org.george.hall.cache.PlayerCache;
import org.george.hall.cache.bean.PlayerCacheBean;
import org.george.hall.dao.PlayerDao;
import org.george.hall.dao.bean.PlayerBean;
import org.george.hall.model.PlayerModel;
import org.george.hall.model.pojo.PlayerResult;

import java.util.List;

public class PlayerModelImpl implements PlayerModel {

    private PlayerModelImpl(){}

    private static PlayerModelImpl instance = new PlayerModelImpl();

    public static PlayerModelImpl getInstance(){
        return instance;
    }

    private PlayerDao playerDao = PlayerDao.getInstance();

    private PlayerCache playerCache = PlayerCache.getInstance();

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
    public List<Integer> getAllPlayerId() {
        List<Integer> list = playerCache.getAllPlayerId();
        if(list == null){
            return playerDao.getAllPlayerId();
        }else{
            return list;
        }
    }

    @Override
    public Integer getPlayerHP(Integer playerId) {
        Integer hp = playerCache.getPlayerHp(playerId);
        PlayerBean bean = new PlayerBean();
        bean.setPlayerId(playerId);
        bean.setHp(hp);
        playerDao.updatePlayerSelective(bean);
        return hp;
    }

    private PlayerCacheBean bean2PlayerCacheBean(PlayerBean bean){
        PlayerCacheBean pcb = new PlayerCacheBean();
        pcb.setPlayerId(bean.getPlayerId());
        pcb.setPlayerName(bean.getPlayerName());
        pcb.setPassword(bean.getPassword());
        pcb.setHp(bean.getHp());
        pcb.setGold(bean.getGold());
        return pcb;
    }

    private PlayerResult cacheBean2PlayerResult(PlayerCacheBean bean){
        PlayerResult pr = new PlayerResult();
        pr.setPlayerId(bean.getPlayerId());
        pr.setPlayerName(bean.getPlayerName());
        pr.setPassword(bean.getPassword());
        pr.setHp(bean.getHp());
        pr.setGold(bean.getGold());
        return pr;
    }
}
