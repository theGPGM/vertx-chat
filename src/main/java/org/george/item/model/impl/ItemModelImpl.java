package org.george.item.model.impl;

import org.george.bag.model.BagModel;
import org.george.bag.model.pojo.PlayerItemResult;
import org.george.item.cache.ItemCache;
import org.george.item.cache.bean.ItemCacheBean;
import org.george.item.dao.ItemDao;
import org.george.item.dao.bean.ItemBean;
import org.george.item.model.ItemModel;
import org.george.item.model.pojo.ItemResult;

public class ItemModelImpl implements ItemModel {

    private ItemModelImpl(){}

    private static ItemModelImpl instance = new ItemModelImpl();

    public static ItemModelImpl getInstance(){
        return instance;
    }

    private BagModel bagModel = BagModel.getInstance();

    private ItemCache itemCache = ItemCache.getInstance();

    private ItemDao itemDao = ItemDao.getInstance();

    @Override
    public ItemResult getItemByItemId(Integer id) {
        ItemResult result = null;
        ItemCacheBean cacheBean = itemCache.getItemByItemId(id);
        if(cacheBean != null){
            result = cacheBean2ItemResult(cacheBean);
        }else{
            ItemBean itemBean = itemDao.getItemByItemId(id);
            if(itemBean == null){
                return null;
            }else{
                cacheBean = bean2CacheBean(itemBean);
                itemCache.addItem(cacheBean);
                result = cacheBean2ItemResult(cacheBean);
            }
        }
        return result;
    }

    private ItemResult cacheBean2ItemResult(ItemCacheBean bean){
        ItemResult result = new ItemResult();
        result.setItemId(bean.getItemId());
        result.setItemName(bean.getItemName());
        result.setDescription(bean.getDescription());
        return result;
    }

    private ItemCacheBean bean2CacheBean(ItemBean bean){
        ItemCacheBean cacheBean = new ItemCacheBean();
        cacheBean.setItemId(bean.getItemId());
        cacheBean.setItemName(bean.getItemName());
        cacheBean.setDescription(bean.getDescription());
        return cacheBean;
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
