package org.george.item.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.item.cache.ItemCache;
import org.george.item.cache.bean.ItemCacheBean;
import org.george.item.dao.ItemDao;
import org.george.item.dao.bean.ItemBean;
import org.george.item.uitl.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class ItemCacheImpl implements ItemCache {

    private ItemCacheImpl(){}

    private static ItemCacheImpl instance = new ItemCacheImpl();

    public static ItemCacheImpl getInstance(){
        return instance;
    }

    private ItemDao itemDao = ItemDao.getInstance();

    @Override
    public ItemCacheBean getItemByItemId(Integer id) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        if( !jedis.exists("item#" + id)){
            ItemBean itemBean = itemDao.getItemByItemId(id);
            if(itemBean == null){
                return null;
            }else{

                // 添加缓存
                ItemCacheBean cacheBean = new ItemCacheBean();
                cacheBean.setItemId(itemBean.getItemId());
                cacheBean.setDescription(itemBean.getDescription());
                cacheBean.setItemName(itemBean.getItemName());
                try {
                    String json = objectMapper.writeValueAsString(cacheBean);
                    jedis.set("item#" + cacheBean.getItemId(), json);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return cacheBean;
            }
        }else {
            String json = jedis.get("item#" + id);
            try {
                ItemCacheBean cacheBean = objectMapper.readValue(json, ItemCacheBean.class);
                return cacheBean;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void addItem(ItemCacheBean itemBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        // 存储数据库
        ItemBean bean = new ItemBean();
        bean.setItemId(itemBean.getItemId());
        bean.setItemName(itemBean.getItemName());
        bean.setDescription(itemBean.getDescription());
        itemDao.addItem(bean);

        // 添加缓存
        try {
            String json = objectMapper.writeValueAsString(itemBean);
            jedis.set("item#" + itemBean.getItemId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteItem(Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.del("item#" + itemId);
        itemDao.deleteItem(itemId);
    }

    @Override
    public void updateItemSelective(ItemCacheBean itemBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper om = new ObjectMapper();
        String json = jedis.get("item#" + itemBean.getItemId());

        // 更新数据库
        ItemBean bean = new ItemBean();
        bean.setItemId(itemBean.getItemId());
        bean.setItemName(itemBean.getItemName());
        bean.setDescription(itemBean.getDescription());
        itemDao.updateItemSelective(bean);

        // 更新缓存
        try {
            ItemCacheBean old = om.readValue(json, ItemCacheBean.class);
            if(itemBean.getDescription() == null){
                itemBean.setDescription(old.getDescription());
            }
            if(itemBean.getItemName() == null){
                itemBean.setItemName(old.getItemName());
            }
            String newJson = om.writeValueAsString(itemBean);
            jedis.set("item#" + itemBean.getItemId(), newJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
