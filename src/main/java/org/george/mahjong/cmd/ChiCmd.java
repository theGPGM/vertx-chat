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

public class ChiCmd {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_not_exists = "房间不存在";

    private static final String not_in_room = "不在房间内";

    private static final String low_priority = "其他人还在进行操作，请等待";

    private static final String can_not_chi = "不能吃";


    public Messages chi(String...args){

        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if (args.length != 3) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(!NumUtils.checkDigit(args[1]) || !NumUtils.checkDigit(args[2])){
                // 房间名不符合规范
                list.add(new Message(userId, room_format_error));
            }else if(Integer.parseInt(args[2]) < 0 || Integer.parseInt(args[2]) > 2){
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            } else if(!roomExists(Integer.parseInt(args[1]))){
                // 房间不存在
                list.add(new Message(userId, room_not_exists ));
            }else if(!alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家不在房间中
                list.add(new Message(userId, not_in_room));
            }else if(!canChi(mahJongCache.getPlayerByPlayerId(Integer.parseInt(args[1]), playerId))){
                list.add(new Message(userId, can_not_chi));
            }else if(lowChiPriority(mahJongCache.getRoomByRoomId(Integer.parseInt(args[1])))){
                // 在吃之前，有人胡、杠、碰了
                list.add(new Message(userId, low_priority));
            }else{
                Integer roomId = Integer.parseInt(args[1]);
                Integer chiPos = Integer.parseInt(args[2]);
                RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                // 打的牌
                Integer playerCard = room.getPlayCard();
                room.setPlayCard(-1);
                // 轮到他打牌了
                room.setWhoPlay(playerId);

                int[] h = player.getHandCard();
                List<Integer> chi = new ArrayList<>();

                // 左吃
                if(chiPos == 0){
                    chi.add(playerCard);
                    chi.add(playerCard + 1);
                    chi.add(playerCard + 2);

                    h[playerCard + 1]--;
                    h[playerCard + 2]--;
                }else if(chiPos == 1){
                    chi.add(playerCard - 1);
                    chi.add(playerCard);
                    chi.add(playerCard + 1);

                    h[playerCard - 1]--;
                    h[playerCard + 1]--;
                }else{
                    chi.add(playerCard - 2);
                    chi.add(playerCard - 1);
                    chi.add(playerCard);

                    h[playerCard - 2]--;
                    h[playerCard - 1]--;
                }

                player.addChi(chi);

                for(int i = 0; i < 4; i++){
                    PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                    MahJongUtils.clearPlayerState(p);
                }

                list.add(new Message(userId, "您吃了这张牌，请打出一张牌"));
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

    private boolean canChi(PlayerCacheBean player){
        return player.isChi();
    }

    private boolean lowChiPriority(RoomCacheBean room){
        if(room.getWhoHu() != -1) return true;
        if(room.getWhoPeng() != -1) return true;
        if(room.getWhoMingGang() != -1) return true;
        return false;
    }
}
