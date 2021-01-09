package org.george.hall.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.hall.cache.PlayerInfoCache;

import org.george.hall.cache.bean.PlayerInfoCacheBean;
import org.george.hall.dao.PlayerInfoDao;
import org.george.hall.dao.bean.PlayerInfoBean;
import org.george.util.JFinalUtils;
import org.george.util.JedisPool;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class PlayerInfoInfoCacheImpl implements PlayerInfoCache {

    private PlayerInfoInfoCacheImpl(){}

    private static PlayerInfoInfoCacheImpl instance = new PlayerInfoInfoCacheImpl();

    public static PlayerInfoInfoCacheImpl getInstance(){
        return instance;
    }

    private PlayerInfoDao playerInfoDao = PlayerInfoDao.getInstance();

    @Override
    public void addPlayer(PlayerInfoCacheBean cacheBean) {

        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(cacheBean);
            jedis.set("playerInfo#" + cacheBean.getPlayerId(), json);

            PlayerInfoBean infoBean = new PlayerInfoBean();
            infoBean.setGold(cacheBean.getGold());
            infoBean.setHp(cacheBean.getHp());
            infoBean.setPlayerId(cacheBean.getPlayerId());
            infoBean.setPlayerName(cacheBean.getPlayerName());
            playerInfoDao.addPlayer(infoBean);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerInfoCacheBean loadPlayerByPlayerId(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        PlayerInfoCacheBean cacheBean = null;
        if(!jedis.exists("playerInfo#" + playerId)){

            cacheBean = new PlayerInfoCacheBean();
            PlayerInfoBean bean = playerInfoDao.loadPlayerByPlayerId(playerId);
            cacheBean.setPlayerName(bean.getPlayerName());
            cacheBean.setPlayerId(bean.getPlayerId());
            cacheBean.setGold(bean.getGold());
            cacheBean.setHp(bean.getHp());

            try {
                jedis.set("playerInfo#" + playerId, objectMapper.writeValueAsString(cacheBean));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }


        String json = jedis.get("playerInfo#" + playerId);
        try {
            cacheBean = objectMapper.readValue(json, PlayerInfoCacheBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 更新 hp
        if(jedis.exists("player_timestamp#" + playerId)){

            long cur = System.currentTimeMillis();
            int inc = (int) (cur - getTimeStamp(playerId)) / (1000 * 60 * 10);
            if(cacheBean.getHp() + inc > 100){
                cacheBean.setHp(100);
            }else{
                cacheBean.setHp(cacheBean.getHp() + inc);
            }
        }

        try {
            String newJson = objectMapper.writeValueAsString(cacheBean);
            jedis.set("playerInfo#" + playerId, newJson);
            jedis.set("player_timestamp#" + playerId, String.valueOf(System.currentTimeMillis()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return cacheBean;
    }

    @Override
    public void updateSelective(PlayerInfoCacheBean cacheBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        PlayerInfoBean bean = new PlayerInfoBean();
        bean.setPlayerId(cacheBean.getPlayerId());
        bean.setPlayerName(bean.getPlayerName());
        bean.setHp(cacheBean.getHp());
        bean.setGold(cacheBean.getGold());
        playerInfoDao.updatePlayerSelective(bean);

        String json = jedis.get("playerInfo#" + cacheBean.getPlayerId());
        try {
            PlayerInfoCacheBean old = objectMapper.readValue(json, PlayerInfoCacheBean.class);
            if(cacheBean.getPlayerName() == null){
                cacheBean.setPlayerName(old.getPlayerName());
            }
            if(cacheBean.getHp() == null){
                cacheBean.setHp(old.getHp());
            }
            if(cacheBean.getGold() == null){
                cacheBean.setHp(old.getGold());
            }
            String newJson = objectMapper.writeValueAsString(cacheBean);
            jedis.set("playerInfo#" + cacheBean.getPlayerId(), newJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addTimeStampIfNotExisting(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.exists("player_timestamp#" + playerId)){
            jedis.set("player_timestamp#" + playerId, String.valueOf(System.currentTimeMillis()));
        }
    }

    @Override
    public Long getTimeStamp(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        return Long.parseLong(jedis.get("player_timestamp#" + playerId));
    }

    public static void main(String[] args) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        JFinalUtils.initJFinalConfig();

        System.out.println(instance.loadPlayerByPlayerId(19));
    }
}
