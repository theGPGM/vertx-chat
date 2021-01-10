package org.george.item.model.impl;

import org.george.bag.model.BagModel;
import org.george.bag.model.pojo.PlayerItemResult;
import org.george.item.cache.ItemCache;
import org.george.item.cache.bean.ItemCacheBean;
import org.george.item.dao.ItemDao;
import org.george.item.dao.bean.ItemBean;
import org.george.item.model.ItemModel;
import org.george.item.model.pojo.ItemResult;
import org.george.item.uitl.JedisPool;
import org.george.item.uitl.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

public class ItemModelImpl implements ItemModel {

    private ItemModelImpl(){}

    private static ItemModelImpl instance = new ItemModelImpl();

    public static ItemModelImpl getInstance(){
        return instance;
    }

    private BagModel bagModel = BagModel.getInstance();

    private ItemCache itemCache = ItemCache.getInstance();

    @Override
    public ItemResult getItemByItemId(Integer id) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            ItemCacheBean cacheBean = itemCache.getItemByItemId(id);
            if(cacheBean == null){
                return null;
            }else{
                return cacheBean2ItemResult(cacheBean);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    private ItemResult cacheBean2ItemResult(ItemCacheBean bean){
        ItemResult result = new ItemResult();
        result.setItemId(bean.getItemId());
        result.setItemName(bean.getItemName());
        result.setDescription(bean.getDescription());
        return result;
    }

    @Override
    public boolean deliveryNotify(Integer playerId, Integer id, Integer num) {
        PlayerItemResult item = bagModel.getPlayerItem(playerId, id);
        if(item != null){
            item.setNum(item.getNum() + num);
            bagModel.updatePlayerItem(item);
        }else{
            item = new PlayerItemResult();
            item.setPlayerId(playerId);
            item.setItemId(id);
            item.setNum(num);
            bagModel.addPlayerItem(item);
        }
        return true;
    }
}
