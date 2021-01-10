package org.george.item.dao.impl;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.george.item.dao.ItemDao;
import org.george.item.dao.bean.ItemBean;

import java.util.ArrayList;
import java.util.List;

public class ItemDaoImpl implements ItemDao {

    private ItemDaoImpl(){}

    private static ItemDaoImpl instance = new ItemDaoImpl();

    public static ItemDaoImpl getInstance(){
        return instance;
    }

    @Override
    public List<ItemBean> getAllItems() {
        List<Object[]> list = Db.query("select item_id, item_name, description from item");
        List<ItemBean> beans = new ArrayList<>();
        for(Object[] o : list){
            ItemBean bean = new ItemBean();
            bean.setItemId((Integer) o[0]);
            bean.setItemName((String) o[1]);
            bean.setDescription((String) o[2]);
            beans.add(bean);
        }
        return beans;
    }

    @Override
    public ItemBean getItemByItemId(Integer id) {
        ItemBean bean = null;
        Record record = Db.findFirst("select * from item where item_id = ?", id);
        if(record != null){
            bean = new ItemBean();
            bean.setItemId(record.getInt("item_id"));
            bean.setItemName(record.getStr("item_name"));
            bean.setDescription(record.getStr("description"));
        }
        return bean;
    }

    @Override
    public void addItem(ItemBean bean) {
        Record record = new Record();
        record.set("item_name", bean.getItemName()).set("description", bean.getDescription());
        Db.save("item", record);
    }

    @Override
    public void deleteItem(Integer itemId) {
        Db.delete("delete from item where item_id = ?", itemId);
    }

    @Override
    public void updateItemSelective(ItemBean bean) {
        Record record = Db.findFirst("select * from item where item_id = ?", bean.getItemId());
        if(bean.getItemName() != null){
            record.set("item_name", bean.getItemName());
        }
        if(bean.getDescription() != null){
            record.set("description", bean.getDescription());
        }
        Db.update("item", "item_id", record);
    }
}
