package org.george.dungeon_game.model.impl;

import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.cache.impl.DungeonGameCacheImpl;
import org.george.dungeon_game.model.DungeonGameModel;
import org.george.dungeon_game.util.JedisPool;
import org.george.dungeon_game.util.ThreadLocalJedisUtils;
import org.george.hall.model.PlayerModel;
import redis.clients.jedis.Jedis;

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
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{

            String userId = playerModel.getUId(hId);
            if(userId != null){
                if(dungeonGameCache.playerAtGame(userId)){
                    dungeonGameCache.deletePlayer(userId);
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }
}
