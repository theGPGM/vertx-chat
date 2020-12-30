package org.george.item.cache;

import org.george.item.cache.bean.ItemCacheBean;
import org.george.item.cache.impl.ItemCacheImpl;

public interface ItemCache {

    ItemCacheBean getItemByItemId(Integer id);

    void addItem(ItemCacheBean itemBean);

    void deleteItem(Integer itemId);

    void updateItemSelective(ItemCacheBean itemBean);

    static ItemCache getInstance(){
        return ItemCacheImpl.getInstance();
    }
}
