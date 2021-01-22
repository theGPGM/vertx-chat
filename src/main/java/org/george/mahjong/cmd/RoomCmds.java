package org.george.mahjong.cmd;

import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;
import org.george.dungeon_game.util.JedisPool;
import org.george.dungeon_game.util.ThreadLocalJedisUtils;
import org.george.mahjong.cache.MahJongCache;
import org.george.mahjong.cache.bean.PlayerCacheBean;
import org.george.mahjong.cache.bean.RoomCacheBean;
import org.george.mahjong.uitl.MahJongUtils;
import org.george.mahjong.uitl.NumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class RoomCmds {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private Logger log = LoggerFactory.getLogger(RoomCmds.class);

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_exists = "房间已存在";

    private static final String room_not_exists = "房间不存在";

    private static final String already_in_room = "已在房间中";

    private static final String room_is_full = "房间已满人";

    private static final String room_create_success = "房间创建成功，请等待其他玩家加入";

    private static final String room_join_success = "房间加入成功";

    /**
     * 玩家创建房间
     * @param args
     * @return
     */
    public Messages create(String...args){
        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if (args.length != 2) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(!NumUtils.checkDigit(args[1])){
                // 房间名不符合规范
                list.add(new Message(userId, room_format_error));
            }else if(roomExists(Integer.parseInt(args[1]))){
                // 房间存在
                list.add(new Message(userId, room_exists ));
            }else{
                Integer roomId = Integer.parseInt(args[1]);
                RoomCacheBean roomCacheBean = new RoomCacheBean(roomId);
                PlayerCacheBean cacheBean = new PlayerCacheBean(playerId);
                roomCacheBean.setRoomId(roomId);
                roomCacheBean.addCardWall(MahJongUtils.shuffle());

                mahJongCache.addCacheBean(roomCacheBean);
                mahJongCache.addPlayer(roomId, cacheBean);

                list.add(new Message(userId, room_create_success));

                log.info("玩家: {} 创建麻将房: {}", playerId, roomId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    /**
     * 玩家进入房间
     * @param args
     * @return
     */
    public Messages join(String...args){

        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if (args.length != 2) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(!NumUtils.checkDigit(args[1])){
                // 房间名不符合规范
                list.add(new Message(userId, room_format_error));
            }else if(!roomExists(Integer.parseInt(args[1]))){
                // 房间不存在
                list.add(new Message(userId, room_not_exists ));
            }else if(alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家已在房间中
                list.add(new Message(userId, already_in_room));
            }else if(roomFull(Integer.parseInt(args[1]))){
                // 房间已满人
                list.add(new Message(userId, room_is_full));
            } else{
                // 正常加入房间
                Integer roomId = Integer.parseInt(args[1]);
                PlayerCacheBean cacheBean = new PlayerCacheBean(playerId);
                mahJongCache.addPlayer(roomId, cacheBean);

                list.add(new Message(userId, room_join_success));

                log.info("玩家: {} 加入麻将房: {}", playerId, roomId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private boolean roomFull(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId).getPlayers().size() == 4;
    }

    private boolean alreadyInRoom(int roomId, Integer playerId) {
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        List<PlayerCacheBean> players = room.getPlayers();
        for(PlayerCacheBean playerCacheBean : players){
            if(playerCacheBean.getPlayerId().equals(playerId)){
                return true;
            }
        }
        return false;
    }

    private boolean roomExists(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId) != null;
    }
}
