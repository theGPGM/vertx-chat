package org.george.bag.model.impl;

import org.george.bag.cache.BagCache;
import org.george.bag.cache.bean.PlayerItemCacheBean;
import org.george.bag.dao.BagDao;
import org.george.bag.dao.bean.PlayerItemBean;
import org.george.bag.model.BagModel;
import org.george.bag.model.pojo.PlayerItemResult;

import java.util.ArrayList;
import java.util.List;

public class BagModelImpl implements BagModel {

    private BagModelImpl(){}

    private static BagModelImpl instance = new BagModelImpl();

    public static BagModelImpl getInstance(){
        return instance;
    }

    private BagCache bagCache = BagCache.getInstance();

    private BagDao bagDao = BagDao.getInstance();

    @Override
    public List<PlayerItemResult> getAllPlayerItems(Integer playerId) {
        List<PlayerItemResult> results = new ArrayList<>();
        List<PlayerItemCacheBean> list = bagCache.getAllPlayerItem(playerId);
        if(list != null){
            for(PlayerItemCacheBean cacheBean : list){
                results.add(playerItemCacheBean2Result(cacheBean));
            }
        }else{
            List<PlayerItemBean> playerItems = bagDao.getPlayerItems(playerId);
            for(PlayerItemBean bean : playerItems){
                PlayerItemCacheBean cacheBean = playerItemBean2CacheBean(bean);
                bagCache.addPlayerItem(cacheBean);
                results.add(playerItemCacheBean2Result(cacheBean));
            }
        }
        return results;
    }

    @Override
    public void addPlayerItem(PlayerItemResult item) {
        PlayerItemBean bean = playerItemResult2Bean(item);
        PlayerItemCacheBean cacheBean = playerItemResult2CacheBean(item);
        bagCache.addPlayerItem(cacheBean);
        bagDao.addPlayerItem(bean);
    }

    @Override
    public void updatePlayerItem(PlayerItemResult item) {
        PlayerItemBean bean = playerItemResult2Bean(item);
        PlayerItemCacheBean cacheBean = playerItemResult2CacheBean(item);
        bagCache.updatePlayerItem(cacheBean);
        bagDao.updatePlayerItem(bean);
    }

    @Override
    public PlayerItemResult getPlayerItem(Integer playerId, Integer itemId) {
        PlayerItemCacheBean cacheBean = bagCache.getPlayerItem(playerId, itemId);
        if(cacheBean == null){
            PlayerItemBean bean = bagDao.getPlayerItem(playerId, itemId);
            if(bean == null){
                return null;
            }
            cacheBean = playerItemBean2CacheBean(bean);
            bagCache.addPlayerItem(cacheBean);
        }
        return playerItemCacheBean2Result(cacheBean);
    }

    private PlayerItemCacheBean playerItemResult2CacheBean(PlayerItemResult result){
        PlayerItemCacheBean cacheBean = new PlayerItemCacheBean();
        cacheBean.setPlayerId(result.getPlayerId());
        cacheBean.setItemId(result.getItemId());
        cacheBean.setNum(result.getNum());
        return cacheBean;
    }

    private PlayerItemBean playerItemResult2Bean(PlayerItemResult result){
        PlayerItemBean bean = new PlayerItemBean();
        bean.setPlayerId(result.getPlayerId());
        bean.setItemId(result.getItemId());
        bean.setNum(result.getNum());
        return bean;
    }

    private PlayerItemResult playerItemCacheBean2Result(PlayerItemCacheBean cacheBean){
        PlayerItemResult result = new PlayerItemResult();
        result.setPlayerId(cacheBean.getPlayerId());
        result.setItemId(cacheBean.getItemId());
        result.setNum(cacheBean.getNum());
        return result;
    }

    private PlayerItemCacheBean playerItemBean2CacheBean(PlayerItemBean bean){
        PlayerItemCacheBean cacheBean = new PlayerItemCacheBean();
        cacheBean.setPlayerId(bean.getPlayerId());
        cacheBean.setItemId(bean.getItemId());
        cacheBean.setNum(bean.getNum());
        return cacheBean;
    }
}
