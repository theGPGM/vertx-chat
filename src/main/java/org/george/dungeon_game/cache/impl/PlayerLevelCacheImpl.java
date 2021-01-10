package org.george.dungeon_game.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.dungeon_game.cache.PlayerLevelCache;
import org.george.dungeon_game.cache.bean.PlayerLevelCacheBean;
import org.george.dungeon_game.dao.PlayerLevelDao;
import org.george.dungeon_game.dao.bean.PlayerLevelBean;
import org.george.dungeon_game.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class PlayerLevelCacheImpl implements PlayerLevelCache {

    private PlayerLevelCacheImpl(){}

    private static PlayerLevelCacheImpl instance = new PlayerLevelCacheImpl();

    public static PlayerLevelCacheImpl getInstance(){
        return instance;
    }

    private PlayerLevelDao playerLevelDao = PlayerLevelDao.getInstance();

    @Override
    public PlayerLevelCacheBean getPlayerLevelByPlayerId(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        if(jedis.exists("player_level#" + playerId)){
            String json = jedis.get("player_level#" + playerId);
            try {
                return objectMapper.readValue(json, PlayerLevelCacheBean.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            PlayerLevelBean bean = playerLevelDao.loadPlayerLevelByPlayerId(playerId);

            if(bean == null){
                return null;
            }

            PlayerLevelCacheBean level = new PlayerLevelCacheBean();
            level.setPlayerId(bean.getPlayerId());
            level.setLevel(bean.getLevel());
            level.setLoseCount(bean.getLoseCount());

            // 添加缓存
            try {
                String json = objectMapper.writeValueAsString(level);
                jedis.set("player_level#" + level.getPlayerId(), json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return level;
        }
        return null;
    }

    @Override
    public void updatePlayerLevelSelective(PlayerLevelCacheBean level) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = jedis.get("player_level#" + level.getPlayerId());

        // 更新数据库
        PlayerLevelBean bean = new PlayerLevelBean();
        bean.setLevel(level.getLevel());
        bean.setLoseCount(level.getLoseCount());
        bean.setPlayerId(level.getPlayerId());
        playerLevelDao.updateRecordSelective(bean);

        // 更新缓存
        try {
            PlayerLevelCacheBean oldCacheBean = objectMapper.readValue(json, PlayerLevelCacheBean.class );
            if(level.getLevel() == null){
                level.setLevel(oldCacheBean.getLevel());
            }
            if(level.getLoseCount() == null){
                level.setLevel(oldCacheBean.getLoseCount());
            }
            String newJson = objectMapper.writeValueAsString(level);
            jedis.set("player_level#" + level.getPlayerId(), newJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addPlayerLevel(PlayerLevelCacheBean level) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        // 添加数据库
        playerLevelDao.addPlayerLevel(level.getPlayerId());

        // 添加缓存
        try {
            String json = objectMapper.writeValueAsString(level);
            jedis.set("player_level#" + level.getPlayerId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
