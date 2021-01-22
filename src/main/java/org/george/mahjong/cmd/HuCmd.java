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
import java.util.Map;

public class HuCmd {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_not_exists = "房间不存在";

    private static final String not_in_room = "不在房间内";

    private static final String can_not_hu = "无法胡";

    private static final String game_over = "游戏结束了";

    private Logger log = LoggerFactory.getLogger(HuCmd.class);

    public Messages hu(String...args){

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
            }else if(!canHu(playerId, Integer.parseInt(args[1]))){
                // 无法胡
                list.add(new Message(userId, can_not_hu));
            }else{
                Integer roomId = Integer.parseInt(args[1]);
                RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

                Integer card = room.getPlayCard() == -1 ? null :  room.getPlayCard();
                boolean isZiMo = room.getPlayCard() == -1 ? true : false;
                boolean isGangMoPai = player.isGangMoHu();
                boolean isQiangGangHu = player.isQiangGangHu();
                Integer menFeng = mahJongCache.getPlayerIndex(room.getRoomId(), player.getPlayerId());
                Map<String, Integer> map = MahJongUtils.calculateFan(card, menFeng,  isZiMo, isGangMoPai, isQiangGangHu, player, room);

                Integer fan = MahJongUtils.countFan(map);

                List<PlayerCacheBean> ps = mahJongCache.getAllPlayer(roomId);
                for(PlayerCacheBean p : ps){
                    if(!p.getPlayerId().equals(playerId)){
                        p.setPoint(p.getPoint() - 8);
                    }
                }
                player.setPoint(player.getPoint() + 24);
                if(player.isFanPao() || player.isQiangGangHu()){
                    PlayerCacheBean loser = mahJongCache.getPlayerByPlayerId(roomId, room.getWhoPlay());
                    loser.setPoint(loser.getPoint() - fan);
                    player.setPoint(player.getPoint() + fan);
                }else if(player.isZiMo() || player.isGangMoHu()){
                    for(PlayerCacheBean p : ps){
                        if(!p.getPlayerId().equals(playerId)){
                            p.setPoint(p.getPoint() - fan);
                        }else{
                            p.setPoint(p.getPoint() + fan * 3);
                        }
                    }
                }
                List<Message> msgs = newGame(roomId);
                for(Message msg : msgs){
                    list.add(msg);
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private boolean canHu(Integer playerId, Integer roomId) {
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        return room.getWhoHu().equals(playerId);
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

    /**
     * 开始一局新的游戏
     * @param roomId
     * @return
     */
    private List<Message> newGame(Integer roomId){
        List<Message> list = new ArrayList<>();
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        if (room.getQuan() == 3 && room.getPan() == 3) {
            // 游戏彻底结束
            mahJongCache.deleteCacheBean(roomId);
            for (int i = 0; i < 3; i++) {
                PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                list.add(new Message("" + p.getPlayerId(), game_over));
            }
            log.info("麻将房:{}中的游戏结束了", roomId);
        } else {
            if (room.getPan() == 3) {
                // 一圈游戏结束，换座位
                room.setQuan(room.getQuan() + 1);
                room.setPan(0);
                MahJongUtils.changePosition(room);
            } else {
                // 一盘游戏结束, 换庄
                MahJongUtils.changeZhuang(mahJongCache.getRoomByRoomId(roomId));
                room.setPan(room.getPan() + 1);
            }
            // 发牌
            MahJongUtils.deal(room.getPlayers(), room.getCardWall());
            // 向四位玩家发送牌局相关消息
            for (int i = 0; i < 4; i++) {
                PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                String msg = MahJongUtils.mahJongInfo(p, room);
                list.add(new Message("" + p.getPlayerId(), msg));
            }
            // 输出庄家能够使用的操作
            PlayerCacheBean zhuang = mahJongCache.getPlayerByIndex(roomId, 0);
            boolean isHu = false;
            boolean isGang = false;
            boolean isPlay = false;
            if (MahJongUtils.canHu(zhuang.getHandCard(), null)) {
                isHu = MahJongUtils.calculateHu(null, true, false, false, zhuang.getPlayerId(), roomId);
                if (isHu) {
                    zhuang.setTianHu(true);
                }
            }else if(MahJongUtils.canGang(zhuang.getHandCard(), null)) {
                isGang = true;
                isPlay = true;
                zhuang.setAnGang(true);
                room.setWhoPlay(zhuang.getPlayerId());
            }
            String option = MahJongUtils.getOptions(isPlay, isHu, false, false, isGang);
            list.add(new Message("" + zhuang.getPlayerId(), option));
        }
        return list;
    }
}
