package org.george.dungeon_game.cache.impl;

import org.george.dungeon_game.cache.DungeonGameCache;

import org.george.dungeon_game.cache.PlayerBuyHpRecordCache;
import org.george.dungeon_game.util.CalendarUtils;

import java.util.HashSet;
import java.util.Set;

public class DungeonGameCacheImpl implements DungeonGameCache {

    private DungeonGameCacheImpl(){}

    private static DungeonGameCacheImpl instance = new DungeonGameCacheImpl();

    public static DungeonGameCacheImpl getInstance(){
        return instance;
    }

    private Set<String> playerSet = new HashSet<>();

    private PlayerBuyHpRecordCache playerBuyHpRecordCache = PlayerBuyHpRecordCache.getInstance();

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
            playerBuyHpRecordCache.addBuyHpCount(playerId, CalendarUtils.getNextEarlyMorningTime());
            return 0;
        }else{
            return count;
        }
    }

    @Override
    public void incrBuyHpCount(Integer playerId) {
        Integer count = playerBuyHpRecordCache.getBuyHpCount(playerId);
        if(count == null){
            playerBuyHpRecordCache.addBuyHpCount(playerId, CalendarUtils.getNextEarlyMorningTime());
        }
        playerBuyHpRecordCache.incrBuyHpCount(playerId);
    }
}
