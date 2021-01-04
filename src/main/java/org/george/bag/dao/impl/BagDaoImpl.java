package org.george.bag.dao.impl;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.george.bag.dao.BagDao;
import org.george.bag.dao.bean.PlayerItemBean;

import java.util.ArrayList;
import java.util.List;

public class BagDaoImpl implements BagDao {

    private static BagDaoImpl instance = new BagDaoImpl();

    private BagDaoImpl(){}

    public static BagDaoImpl getInstance(){
        return instance;
    }

    @Override
    public void addPlayerItem(PlayerItemBean item) {
        Record record = new Record();
        record.set("player_id", item.getPlayerId()).set("item_id", item.getItemId()).set("num", item.getNum());
        Db.save("player_item", "player_id", record);
    }

    @Override
    public List<PlayerItemBean> getPlayerItems(Integer playerId) {
        List<PlayerItemBean> res = new ArrayList<>();
        List<Object[]> list = Db.query("select player_id, item_id, num from player_item where player_id = ?", playerId);
        for(Object[] o : list){
            PlayerItemBean bean = new PlayerItemBean();
            bean.setPlayerId((Integer) o[0]);
            bean.setItemId((Integer) o[1]);
            bean.setNum((Integer) o[2]);

            res.add(bean);
        }
        return res;
    }

    @Override
    public void updatePlayerItem(PlayerItemBean item) {
        Record record = Db.findFirst("select * from player_item where player_id = ? and item_id = ?", item.getPlayerId(), item.getItemId());
        record.set("num", item.getNum());
        Db.update("player_item", "player_id", record);
    }

    @Override
    public void deletePlayerItem(Integer playerId, Integer itemId) {
        Db.delete("delete from player_item where player_id = ? and item_id = ?", playerId, itemId);
    }

    @Override
    public PlayerItemBean getPlayerItem(Integer playerId, Integer itemId) {
        PlayerItemBean bean = null;
        Record record = Db.findFirst("delete from player_item where player_id = ? and item_id = ?", playerId, itemId);
        if(record != null){
            bean = new PlayerItemBean();
            bean.setItemId(record.getInt("item_id"));
            bean.setPlayerId(record.getInt("player_id"));
            bean.setNum(record.getInt("num"));
        }
        return bean;
    }
}
