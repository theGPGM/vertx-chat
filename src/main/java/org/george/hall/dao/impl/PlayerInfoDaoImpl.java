package org.george.hall.dao.impl;

import com.jfinal.plugin.activerecord.Db;

import com.jfinal.plugin.activerecord.Record;
import org.george.hall.dao.PlayerInfoDao;
import org.george.hall.dao.bean.PlayerInfoBean;

public class PlayerInfoDaoImpl implements PlayerInfoDao {

    private PlayerInfoDaoImpl(){}

    private static PlayerInfoDaoImpl dao = new PlayerInfoDaoImpl();

    public static PlayerInfoDaoImpl getInstance(){
        return dao;
    }

    @Override
    public void updatePlayerSelective(PlayerInfoBean bean) {
        Record record = Db.findFirst("select * from player where player_id = ?", bean.getPlayerId());
        if(bean.getPlayerName() != null){
            record.set("player_name", bean.getPlayerName());
        }
        if(bean.getGold() != null){
            record.set("gold", bean.getGold());
        }
        if(bean.getHp() != null){
            record.set("hp", bean.getHp());
        }
        Db.update("player", "player_id",  record);
    }

    @Override
    public void addPlayer(PlayerInfoBean bean) {
        Record record = new Record().set("player_name", bean.getPlayerName()).set("player_id", bean.getPlayerId());
        Db.save("player", record);
    }

    @Override
    public void deletePlayer(Integer playerId) {
        Db.delete("delete from player where player_id = ?", playerId);
    }

    @Override
    public PlayerInfoBean loadPlayerByPlayerId(Integer playerId) {
        Record record = Db.findFirst("select * from player where player_id = ?", playerId);
        PlayerInfoBean bean = null;
        if(record != null && record.getInt("player_id") != null){
            bean = new PlayerInfoBean();
            bean.setPlayerId(record.getInt("player_id"));
            bean.setHp(record.getInt("hp"));
            bean.setGold(record.getInt("gold"));
            bean.setPlayerName(record.getStr("player_name"));
        }
        return bean;
    }
}
