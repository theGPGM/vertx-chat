package org.george.item.cache.impl;

import org.george.item.cache.ItemCache;
import org.george.item.cache.bean.ItemCacheBean;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

public class ItemCacheImpl implements ItemCache {

    private ItemCacheImpl(){}

    private static ItemCacheImpl instance = new ItemCacheImpl();

    public static ItemCacheImpl getInstance(){
        return instance;
    }

    @Override
    public ItemCacheBean getItemByItemId(Integer id) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if( !jedis.exists("item#" + id)){
            return null;
        }else {
            String itemName = jedis.hget("item#" + id, "item_name");
            String description = jedis.hget("item#" + id, "description");
            ItemCacheBean itemBean = new ItemCacheBean();
            itemBean.setItemId(id);
            itemBean.setItemName(itemName);
            itemBean.setDescription(description);
            return itemBean;
        }
    }

    @Override
    public void addItem(ItemCacheBean itemBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("item#" + itemBean.getItemId(), "item_name", itemBean.getItemName());
        jedis.hset("item#" + itemBean.getItemId(), "description", itemBean.getDescription());
    }

    @Override
    public void deleteItem(Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.del("item#" + itemId);
    }

    @Override
    public void updateItemSelective(ItemCacheBean itemBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(itemBean.getItemName() != null){
            jedis.hset("item#" + itemBean.getItemId(), "item_name", itemBean.getItemName());
        }
        if(itemBean.getDescription() != null){
            jedis.hset("item#" + itemBean.getItemId(), "description", itemBean.getDescription());
        }
    }
}
