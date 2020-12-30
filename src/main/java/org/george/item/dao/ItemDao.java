package org.george.item.dao;

import org.george.item.dao.bean.ItemBean;
import org.george.item.dao.impl.ItemDaoImpl;

import java.util.List;

public interface ItemDao {

    List<ItemBean> getAllItems();

    ItemBean getItemByItemId(Integer id);

    void addItem(ItemBean itemBean);

    void deleteItem(Integer itemId);

    void updateItemSelective(ItemBean itemBean);

    static ItemDao getInstance(){
        return ItemDaoImpl.getInstance();
    }
}
