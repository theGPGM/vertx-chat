package org.george.dungeon_game.model;

import org.george.pojo.LevelBean;
import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.cache.impl.DungeonGameCacheImpl;

import java.util.List;

public class DungeonGameModelImpl implements DungeonGameModel{

    private DungeonGameCache dungeonGameCache = DungeonGameCacheImpl.getInstance();

    private DungeonGameModelImpl(){}

    private static DungeonGameModelImpl instance = new DungeonGameModelImpl();

    public static DungeonGameModelImpl getInstance(){
        return instance;
    }

    @Override
    public void quitGame(String userId) {
        if(dungeonGameCache.playerAtGame(userId)){
            dungeonGameCache.deletePlayer(userId);
        }
    }
}
