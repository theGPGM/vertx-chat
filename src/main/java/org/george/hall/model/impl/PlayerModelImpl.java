package org.george.hall.model.impl;

import org.george.hall.cache.PlayerInfoCache;

import org.george.hall.cache.bean.PlayerInfoCacheBean;
import org.george.hall.model.PlayerModel;
import org.george.hall.model.pojo.PlayerResult;
import org.george.hall.uitl.JedisPool;
import org.george.hall.uitl.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class PlayerModelImpl implements PlayerModel {

    private PlayerModelImpl(){}

    private static PlayerModelImpl instance = new PlayerModelImpl();

    public static PlayerModelImpl getInstance(){
        return instance;
    }

    private PlayerInfoCache playerInfoCache = PlayerInfoCache.getInstance();

    Map<String, String> hIdMap = new HashMap<>();

    Map<String, String> userIdMap = new HashMap<>();

    @Override
    public PlayerResult getPlayerByPlayerId(Integer playerId) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            PlayerInfoCacheBean cacheBean = playerInfoCache.loadPlayerByPlayerId(playerId);
            if(cacheBean == null){
                return null;
            }else{
                return cacheBean2PlayerResult(cacheBean);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    @Override
    public void updatePlayerHP(Integer playerId, Integer hp) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            PlayerInfoCacheBean cacheBean = new PlayerInfoCacheBean();
            cacheBean.setPlayerId(playerId);
            cacheBean.setHp(hp);
            playerInfoCache.updateSelective(cacheBean);
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    @Override
    public void updatePlayerGold(Integer playerId, Integer gold) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            PlayerInfoCacheBean cacheBean = new PlayerInfoCacheBean();
            cacheBean.setPlayerId(playerId);
            cacheBean.setGold(gold);
            playerInfoCache.updateSelective(cacheBean);
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    private PlayerResult cacheBean2PlayerResult(PlayerInfoCacheBean bean){
        PlayerResult pr = new PlayerResult();
        pr.setPlayerId(bean.getPlayerId());
        pr.setPlayerName(bean.getPlayerName());
        pr.setHp(bean.getHp());
        pr.setGold(bean.getGold());
        return pr;
    }

    @Override
    public boolean deductionNotify(Integer playerId, Integer num) {

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            PlayerInfoCacheBean cacheBean = playerInfoCache.loadPlayerByPlayerId(playerId);
            if(cacheBean.getGold() < num){
                return false;
            }else{
                cacheBean.setGold(cacheBean.getGold() - num);
                playerInfoCache.updateSelective(cacheBean);
                return true;
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
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
}
