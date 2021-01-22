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
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class MGangCmd {

    private MahJongCache mahJongCache = MahJongCache.getInstance();


    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_not_exists = "房间不存在";

    private static final String already_in_room = "已在房间中";

    private static final String room_is_full = "房间已满人";

    private static final String can_not_gang = "不能杠";

    public Messages mGang(String...args){
        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if (args.length != 2) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(!NumUtils.checkDigit(args[1]) || !NumUtils.checkDigit(args[2])){
                // 房间名不符合规范
                list.add(new Message(userId, room_format_error));
            }else if(!roomExists(Integer.parseInt(args[1]))){
                // 房间不存在
                list.add(new Message(userId, room_not_exists ));
            }else if(!alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家不在房间中
                list.add(new Message(userId, already_in_room));
            }else if(roomFull(Integer.parseInt(args[1]))){
                // 房间已满人
                list.add(new Message(userId, room_is_full));
            }else if(!canMGang(playerId, Integer.parseInt(args[1]))){
                list.add(new Message(userId, can_not_gang));
            }else if(lowGangPriority(playerId, Integer.parseInt(args[1]))){
                list.add(new Message(userId, can_not_gang));
            }else{
                // 正常加入房间
                Integer roomId = Integer.parseInt(args[1]);
                Integer card = Integer.parseInt(args[2]);
                RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                room.setWhoPlay(playerId);
                room.setPlayCard(-1);
                room.setWhoPeng(-1);
                room.setWhoChi(-1);

                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                int[] h = player.getHandCard();
                h[card] = 0;
                player.addGang(card);

                for(int i = 0; i < 4; i++){
                    PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                    MahJongUtils.clearPlayerState(p);
                }

                list.add(new Message(userId, "您杠了一张牌，请打出一张牌"));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private boolean roomExists(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId) != null;
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

    private boolean canMGang(Integer playerId, Integer roomId){
        return mahJongCache.getPlayerByPlayerId(roomId, playerId).isMingGang();
    }

    private boolean lowGangPriority(Integer playerId, Integer roomId){
        return mahJongCache.getRoomByRoomId(roomId).getWhoHu() != -1;
    }
}
