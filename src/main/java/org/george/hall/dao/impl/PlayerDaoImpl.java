package org.george.hall.dao.impl;

import com.jfinal.plugin.activerecord.Db;

import com.jfinal.plugin.activerecord.Record;
import org.george.hall.dao.PlayerDao;
import org.george.hall.dao.bean.PlayerBean;
import org.george.util.JFinalUtils;

public class PlayerDaoImpl implements PlayerDao {

    private PlayerDaoImpl(){}

    private static PlayerDaoImpl dao = new PlayerDaoImpl();

    public static PlayerDaoImpl getInstance(){
        return dao;
    }

    @Override
    public PlayerBean loadPlayerByPlayerName(String playerName) {
        Record record = Db.findFirst("select * from player where player_name = ?", playerName);
        PlayerBean bean = null;
        if(record != null && record.getInt("player_id") != null){
            bean = new PlayerBean();
            bean.setPlayerId(record.getInt("player_id"));
            bean.setHp(record.getInt("hp"));
            bean.setGold(record.getInt("gold"));
            bean.setPlayerName(record.getStr("player_name"));
        }
        return bean;
    }

    @Override
    public void updatePlayerSelective(PlayerBean bean) {
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
    public void addPlayer(PlayerBean bean) {
        Record record = new Record().set("player_name", bean.getPlayerName()).set("player_id", bean.getPlayerId());
        Db.save("player", record);
    }

    @Override
    public void deletePlayer(Integer playerId) {
        Db.delete("delete from player where player_id = ?", playerId);
    }

    @Override
    public PlayerBean loadPlayerByPlayerId(Integer playerId) {
        Record record = Db.findFirst("select * from player where player_id = ?", playerId);
        PlayerBean bean = null;
        if(record != null && record.getInt("player_id") != null){
            bean = new PlayerBean();
            bean.setPlayerId(record.getInt("player_id"));
            bean.setHp(record.getInt("hp"));
            bean.setGold(record.getInt("gold"));
            bean.setPlayerName(record.getStr("player_name"));
        }
        return bean;
    }

    public static void main(String[] args) {
        JFinalUtils.initJFinalConfig();
        dao.deletePlayer(17);
    }
}
