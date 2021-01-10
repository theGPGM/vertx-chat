package org.george.chat.model.impl;

import org.george.chat.cache.RoomCache;
import org.george.chat.model.ChatRoomModel;
import org.george.chat.util.JedisPool;
import org.george.chat.util.ThreadLocalJedisUtils;
import org.george.hall.model.PlayerModel;
import redis.clients.jedis.Jedis;

import java.util.List;

public class ChatRoomModelImpl implements ChatRoomModel {

    private ChatRoomModelImpl(){}

    private static ChatRoomModelImpl instance = new ChatRoomModelImpl();

    public static ChatRoomModelImpl getInstance(){
        return instance;
    }

    private PlayerModel playerModel = PlayerModel.getInstance();

    private RoomCache roomCache = RoomCache.getInstance();

    @Override
    public void clientCloseNotify(String hId) {

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{

            String uId = playerModel.getUId(hId);
            if(uId != null){
                List<String> roomIds = roomCache.getUserRoomIds(uId);
                for(String rId : roomIds){
                    roomCache.clearUserRoomCache(uId, rId);
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }
}
