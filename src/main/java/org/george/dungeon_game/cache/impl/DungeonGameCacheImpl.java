package org.george.dungeon_game.cache.impl;

import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.cache.PlayerBuyHpRecordCache;
import org.george.dungeon_game.cache.PlayerLevelCache;
import org.george.pojo.LevelBean;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DungeonGameCacheImpl implements DungeonGameCache {

    private DungeonGameCacheImpl(){}

    private static DungeonGameCacheImpl instance = new DungeonGameCacheImpl();

    private Set<String> playerSet = new HashSet<>();

    private List<LevelBean> list;

    private PlayerLevelCache playerLevelCache = PlayerLevelCacheImpl.getInstance();

    private PlayerBuyHpRecordCache playerBuyHpRecordCache = PlayerBuyHpRecordCacheImpl.getInstance();

    public static DungeonGameCacheImpl getInstance(){
        return instance;
    }

    @Override
    public void addPlayer(String userId) {
        playerSet.add(userId);
    }

    @Override
    public void deletePlayer(String userId) {
        playerSet.remove(userId);
    }

    @Override
    public boolean playerAtGame(String userId) {
        return playerSet.contains(userId);
    }

    @Override
    public Integer getBuyHpCount(Integer playerId) {
        Integer count = playerBuyHpRecordCache.getBuyHpCount(playerId);
        if(count == null){
            playerBuyHpRecordCache.addBuyHpCount(playerId, getSecondsNextEarlyMorning());
            return 0;
        }else{
            return count;
        }
    }

    @Override
    public void incrBuyHpCount(Integer playerId) {
        Integer count = playerBuyHpRecordCache.getBuyHpCount(playerId);
        if(count == null){
            playerBuyHpRecordCache.addBuyHpCount(playerId, getSecondsNextEarlyMorning());
        }
        playerBuyHpRecordCache.incrBuyHpCount(playerId);
    }

    private Long getSecondsNextEarlyMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }
}
