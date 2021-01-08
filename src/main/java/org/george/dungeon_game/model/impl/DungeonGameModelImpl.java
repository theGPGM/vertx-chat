package org.george.dungeon_game.model.impl;

import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.cache.impl.DungeonGameCacheImpl;
import org.george.dungeon_game.model.DungeonGameModel;
import org.george.hall.model.PlayerModel;

public class DungeonGameModelImpl implements DungeonGameModel {

    private DungeonGameCache dungeonGameCache = DungeonGameCacheImpl.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private DungeonGameModelImpl(){}

    private static DungeonGameModelImpl instance = new DungeonGameModelImpl();

    public static DungeonGameModelImpl getInstance(){
        return instance;
    }

    @Override
    public void clientCloseNotify(String hId) {
        String userId = playerModel.getUId(hId);
        if(userId != null){
            if(dungeonGameCache.playerAtGame(userId)){
                dungeonGameCache.deletePlayer(userId);
            }
        }
    }
}
