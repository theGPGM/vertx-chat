package org.george.hall.dao.impl;

import com.jfinal.plugin.activerecord.Db;

import com.jfinal.plugin.activerecord.Record;
import org.george.hall.dao.PlayerAuthDao;
import org.george.hall.dao.bean.PlayerAuthBean;

public class PlayerAuthDaoImpl implements PlayerAuthDao {

    private PlayerAuthDaoImpl(){}

    private static PlayerAuthDaoImpl dao = new PlayerAuthDaoImpl();

    public static PlayerAuthDaoImpl getInstance(){
        return dao;
    }

    @Override
    public PlayerAuthBean loadPlayerAuthBeanByPlayerName(String playerName) {
        Record record = Db.findFirst("select * from auth where player_name = ?", playerName);
        PlayerAuthBean bean = null;
        if(record != null && record.getInt("player_id") != null){
            bean = new PlayerAuthBean();
            bean.setPlayerId(record.getInt("player_id"));
            bean.setPlayerName(record.getStr("player_name"));
            bean.setPassword(record.getStr("password"));
        }
        return bean;
    }

    @Override
    public boolean addPlayer(String playerName, String password) {
        Record record = new Record().set("player_name", playerName).set("password", password);
        try{
            return Db.save("auth", record);
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean updatePlayerSelective(PlayerAuthBean bean) {
        Record record = Db.findFirst("select * from auth where player_id = ?", bean.getPlayerId());
        if(bean.getPlayerName() != null){
            record.set("player_name", bean.getPlayerName());
        }
        if(bean.getPassword() != null){
            record.set("password", bean.getPassword());
        }
        return Db.update("auth", "player_id",  record);
    }

    @Override
    public int deletePlayer(Integer playerId) {
        return Db.delete("delete from auth where player_id = ?", playerId);
    }
}
