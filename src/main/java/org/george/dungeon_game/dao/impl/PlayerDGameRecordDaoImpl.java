package org.george.dungeon_game.dao.impl;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.george.dungeon_game.dao.PlayerDGameRecordDao;
import org.george.dungeon_game.dao.bean.PlayerLevelBean;

public class PlayerDGameRecordDaoImpl implements PlayerDGameRecordDao {

    private PlayerDGameRecordDaoImpl(){}

    private static PlayerDGameRecordDaoImpl instance = new PlayerDGameRecordDaoImpl();

    public static PlayerDGameRecordDaoImpl getInstance(){
        return instance;
    }

    @Override
    public PlayerLevelBean getPlayerLevelByPlayerId(Integer playerId) {
        Record record = Db.findFirst("select player_id, level, lose_count from player_level where player_id = ?", playerId);
        if(record == null){
            return null;
        }else{
            PlayerLevelBean bean = new PlayerLevelBean();
            bean.setPlayerId(record.getInt("player_id"));
            bean.setLevel(record.getInt("level"));
            bean.setLoseCount(record.getInt("lose_count"));
            return bean;
        }
    }

    @Override
    public void updateRecordSelective(PlayerLevelBean bean) {
        Record record = Db.findFirst("select player_id, level, lose_count from player_level where player_id = ?", bean.getPlayerId());
        if(record != null){
            if(bean.getLevel() != null){
                record.set("level", bean.getLevel());
            }
            if(bean.getLoseCount() != null){
                record.set("lose_count", bean.getLoseCount());
            }
            Db.update("player_level", "player_id", record);
        }
    }

    @Override
    public void addPlayerLevel(PlayerLevelBean level) {
        Record record = new Record();
        record.set("level", level.getLevel()).set("lose_count", level.getLoseCount());
        Db.save("level", "player_id", record);
    }
}
