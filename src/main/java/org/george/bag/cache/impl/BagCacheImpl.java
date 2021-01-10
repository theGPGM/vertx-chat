package org.george.bag.cache.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.bag.cache.BagCache;
import org.george.bag.cache.bean.PlayerItemCacheBean;
import org.george.bag.dao.BagDao;
import org.george.bag.dao.bean.PlayerItemBean;
import org.george.bag.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BagCacheImpl implements BagCache {

    private BagCacheImpl(){}

    private static BagCacheImpl instance = new BagCacheImpl();

    public static BagCacheImpl getInstance(){
        return instance;
    }

    private BagDao bagDao = BagDao.getInstance();

    @Override
    public List<PlayerItemCacheBean> getAllPlayerItem(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        if(!jedis.exists("player_items#"+ playerId + "_items")){

            List<PlayerItemBean> beans = bagDao.getPlayerItems(playerId);
            List<PlayerItemCacheBean> cacheBeans = new ArrayList<>();
            for(PlayerItemBean bean : beans){
                PlayerItemCacheBean cacheBean = new PlayerItemCacheBean();
                cacheBean.setItemId(bean.getItemId());
                cacheBean.setPlayerId(playerId);
                cacheBean.setNum(bean.getNum());
                cacheBeans.add(cacheBean);
            }

            try {
                String json = objectMapper.writeValueAsString(cacheBeans);
                jedis.set("player_items#" + playerId, json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return cacheBeans;
        }else{
            String json = jedis.get("player_items#" + playerId);
            try {
                List<PlayerItemCacheBean> list = objectMapper.readValue(json, new TypeReference<List<PlayerItemCacheBean>>() {});
                return list;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void addPlayerItem(PlayerItemCacheBean item) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        // 添加数据库
        PlayerItemBean bean = new PlayerItemBean();
        bean.setPlayerId(item.getPlayerId());
        bean.setItemId(item.getItemId());
        bean.setNum(item.getNum());
        bagDao.addPlayerItem(bean);

        // 添加缓存
        String json = jedis.get("player_items#"+ item.getPlayerId());
        try {
            List<PlayerItemCacheBean> list = objectMapper.readValue(json, new TypeReference<List<PlayerItemCacheBean>>() {});
            list.add(item);
            String newJson = objectMapper.writeValueAsString(list);
            jedis.set("player_items#"+ item.getPlayerId(), newJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSelective(PlayerItemCacheBean item) {
        // 添加道具数据
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        // 更新数据库
        PlayerItemBean bean = new PlayerItemBean();
        bean.setPlayerId(item.getPlayerId());
        bean.setItemId(item.getItemId());
        bean.setNum(item.getNum());
        bagDao.updateSelective(bean);

        // 更新缓存
        String json = jedis.get("player_items#"+ item.getPlayerId());
        try {
            List<PlayerItemCacheBean> list = objectMapper.readValue(json, new TypeReference<List<PlayerItemCacheBean>>() {});
            for(PlayerItemCacheBean cacheBean : list){
                if(cacheBean.getItemId().equals(item.getItemId())){
                    if(item.getNum() != null){
                        cacheBean.setNum(item.getNum());
                    }
                }
            }

            String newJson = objectMapper.writeValueAsString(list);
            jedis.set("player_items#"+ item.getPlayerId(), newJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerItemCacheBean getPlayerItem(Integer playerId, Integer itemId) {
        List<PlayerItemCacheBean> cacheBeans = getAllPlayerItem(playerId);
        for(PlayerItemCacheBean cacheBean : cacheBeans){
            if(cacheBean.getItemId().equals(itemId)){
                return cacheBean;
            }
        }
        return null;
    }

    @Override
    public void deletePlayerItem(Integer playerId, Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        // 更新数据库
        bagDao.deletePlayerItem(playerId, itemId);

        // 更新缓存
        String json = jedis.get("player_items#"+ playerId);
        try {
            List<PlayerItemCacheBean> list = objectMapper.readValue(json, new TypeReference<List<PlayerItemCacheBean>>() {});
            int count = 0;
            for(PlayerItemCacheBean cacheBean : list){
                if(cacheBean.getItemId().equals(itemId)){
                    break;
                }
                count ++;
            }

            if(list.size() > count){
                list.remove(count);
                String newJson = objectMapper.writeValueAsString(list);
                jedis.set("player_items#"+ playerId, newJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
