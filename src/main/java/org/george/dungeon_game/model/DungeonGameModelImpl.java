package org.george.dungeon_game.model;

import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.cache.impl.DungeonGameCacheImpl;
import org.george.hall.model.ClientModel;

public class DungeonGameModelImpl implements DungeonGameModel{

    private DungeonGameCache dungeonGameCache = DungeonGameCacheImpl.getInstance();

    private ClientModel clientModel = ClientModel.getInstance();

    private DungeonGameModelImpl(){}

    private static DungeonGameModelImpl instance = new DungeonGameModelImpl();

    public static DungeonGameModelImpl getInstance(){
        return instance;
    }

    @Override
    public void update(String hId) {
        String userId = clientModel.getUserIdByHId(hId);
        if(userId != null){
            if(dungeonGameCache.playerAtGame(userId)){
                dungeonGameCache.deletePlayer(userId);
            }
        }
    }
}
