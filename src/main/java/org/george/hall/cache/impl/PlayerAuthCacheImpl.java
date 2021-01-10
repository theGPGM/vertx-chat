package org.george.hall.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.hall.cache.PlayerAuthCache;
import org.george.hall.cache.bean.PlayerAuthCacheBean;
import org.george.hall.dao.PlayerAuthDao;
import org.george.hall.dao.bean.PlayerAuthBean;
import org.george.hall.uitl.JedisPool;
import org.george.hall.uitl.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class PlayerAuthCacheImpl implements PlayerAuthCache {

    private PlayerAuthCacheImpl(){}

    private static PlayerAuthCacheImpl instance = new PlayerAuthCacheImpl();

    public static PlayerAuthCacheImpl getInstance(){
        return instance;
    }

    private PlayerAuthDao playerAuthDao = PlayerAuthDao.getInstance();

    @Override
    public PlayerAuthCacheBean loadPlayerAuthCacheBeanByName(String playerName) {

        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        PlayerAuthCacheBean cacheBean = null;
        try {
            if(!jedis.exists("playerNameIDMap") || jedis.hget("playerNameIDMap", playerName) == null){
                PlayerAuthBean bean = playerAuthDao.loadPlayerAuthBeanByPlayerName(playerName);
                if(bean != null){
                    cacheBean = new PlayerAuthCacheBean();
                    cacheBean.setPlayerName(bean.getPlayerName());
                    cacheBean.setPassword(bean.getPassword());
                    cacheBean.setPlayerId(bean.getPlayerId());

                    String newJson = objectMapper.writeValueAsString(cacheBean);
                    jedis.hset("playerNameIDMap", cacheBean.getPlayerName(), "" + bean.getPlayerId());
                    jedis.set("playerAuth#" + bean.getPlayerId(), newJson);
                }
            }else{
                String id = jedis.hget("playerNameIDMap", playerName);
                String json = jedis.get("playerAuth#" + id);
                cacheBean = objectMapper.readValue(json, PlayerAuthCacheBean.class);
            }
            return cacheBean;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addPlayer(PlayerAuthCacheBean cacheBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        playerAuthDao.addPlayer(cacheBean.getPlayerName(), cacheBean.getPassword());
        PlayerAuthBean bean = playerAuthDao.loadPlayerAuthBeanByPlayerName(cacheBean.getPlayerName());
        try {
            cacheBean.setPlayerId(bean.getPlayerId());
            String json = objectMapper.writeValueAsString(cacheBean);

            jedis.hset("playerNameIDMap", cacheBean.getPlayerName(), "" + bean.getPlayerId());
            jedis.set("playerAuth#" + bean.getPlayerId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePlayerSelective(PlayerAuthCacheBean cacheBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        PlayerAuthBean bean = new PlayerAuthBean();
        bean.setPlayerId(cacheBean.getPlayerId());
        bean.setPlayerName(cacheBean.getPlayerName());
        bean.setPassword(cacheBean.getPassword());
        playerAuthDao.updatePlayerSelective(bean);

        try {
            String json = jedis.get("playerAuth#" + cacheBean.getPlayerId());
            PlayerAuthBean oldCacheBean = objectMapper.readValue(json, PlayerAuthBean.class);

            if(cacheBean.getPlayerName() != null || cacheBean.getPlayerName().length() != 0){
                jedis.hdel("playerNameIDMap", oldCacheBean.getPlayerName());
                jedis.hset("playerNameIDMap", cacheBean.getPlayerName(), "" + cacheBean.getPlayerId());
            }
            if(cacheBean.getPassword() == null || cacheBean.getPassword().length() == 0){
                cacheBean.setPassword(oldCacheBean.getPassword());
            }

            String newJson = objectMapper.writeValueAsString(cacheBean);
            jedis.set("playerAuth#" + cacheBean.getPlayerId(), newJson);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePlayer(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = jedis.get("playerAuth#" + playerId);
        try {
            if(json != null && json.length() != 0){
                PlayerAuthCacheBean cacheBean = objectMapper.readValue(json, PlayerAuthCacheBean.class);
                jedis.hdel("playerNameIDMap", cacheBean.getPlayerName());
                jedis.del("playerAuth#" + playerId);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playerAuthDao.deletePlayer(playerId);
    }
}
