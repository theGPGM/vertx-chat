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

public class PengCmd {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_not_exists = "房间不存在";

    private static final String not_in_room = "不在房间内";

    private static final String low_priority = "其他人还在进行操作，请等待";

    private static final String can_not_peng = "不能碰";

    public Messages peng(String...args){

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
            }else if(!alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家不在房间中
                list.add(new Message(userId, not_in_room));
            }else if(!canPeng(playerId, Integer.parseInt(args[1]))){
                list.add(new Message(userId, can_not_peng));
            } else if(lowPengPriority(playerId, Integer.parseInt(args[1]))){
                // 在碰之前，有人已经胡了
                list.add(new Message(userId, low_priority));
            } else{

                Integer roomId = Integer.parseInt(args[1]);
                RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                // 打的牌
                Integer playerCard = room.getPlayCard();
                room.setPlayCard(-1);
                // 轮到他打牌了
                room.setWhoPlay(playerId);
                room.setWhoPeng(-1);
                room.setWhoChi(-1);
                MahJongUtils.clearPlayerState(player);

                int[] h = player.getHandCard();
                h[playerCard] -= 2;
                player.addPeng(playerCard);

                list.add(new Message(userId, "您碰了这张牌，请打出一张牌"));
                list.add(new Message(userId, MahJongUtils.mahJongInfo(player, room)));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private boolean roomExists(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId) != null;
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

    private boolean canPeng(Integer playerId, Integer roomId) {
        return mahJongCache.getPlayerByPlayerId(roomId, playerId).isPeng();
    }

    private boolean lowPengPriority(Integer playerId, Integer roomId){
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        if(room.getWhoHu() != -1) return true;
        if(room.getWhoMingGang() != -1) return true;
        return false;
    }
}
