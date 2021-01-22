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

public class GangCmd {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_not_exists = "房间不存在";

    private static final String already_in_room = "已在房间中";

    private static final String can_not_gang = "不能杠";

    private static final String game_over = "游戏结束了";

    private Logger log = LoggerFactory.getLogger(GangCmd.class);


    public Messages gang(String...args){
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
            }else if(Integer.parseInt(args[2]) < 0 || Integer.parseInt(args[2]) > 33){
                // 数字错误
                list.add(new Message(userId, input_format_error));
            } else if(!roomExists(Integer.parseInt(args[1]))){
                // 房间不存在
                list.add(new Message(userId, room_not_exists ));
            }else if(!alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家不在房间中
                list.add(new Message(userId, already_in_room));
            }else if(!canGang(playerId, Integer.parseInt(args[1]), Integer.parseInt(args[2]))){
                // 无法杠
                list.add(new Message(userId, can_not_gang));
            }else{
                // 正常加入房间
                Integer roomId = Integer.parseInt(args[1]);
                Integer card = Integer.parseInt(args[2]);
                RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                int[] h = player.getHandCard();
                if(h[card] == 4){
                    h[card] = 0;
                    List<Message> messages = pickCard(playerId, roomId, true);
                    for(Message message : messages){
                        list.add(message);
                    }
                    player.getAnGangs().add(card);
                }else{
                    List<Integer> pengs = player.getPengs();
                    for(int i = 0; i < pengs.size(); i++){
                        if(pengs.get(i).equals(card)){
                            pengs.remove(i);
                        }
                    }
                    player.getMingGangs().add(card);
                    for(int i = 0; i < 4; i++){
                        PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                        if(!p.getPlayerId().equals(playerId)){
                            if(MahJongUtils.calculateHu(card, false, false, true, p.getPlayerId(), roomId)){
                                p.setQiangGangHu(true);
                                room.setWhoHu(p.getPlayerId());
                            }
                        }
                    }
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

    private boolean isReady(Integer playerId, int roomId) {
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        return player.isReady();
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

    private boolean canGang(Integer playerId, Integer roomId, Integer card) {
        // 先查是不是摸牌状态
        if(mahJongCache.getRoomByRoomId(roomId).getWhoPlay() != -1) return false;

        // 在查查是不是暗杠
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        int[] h = player.getHandCard();
        if(h[card] == 4){
            return true;
        }

        // 是不是摸杠
        List<Integer> pengs = player.getPengs();
        for(int c : pengs){
            if(c == card){
                return true;
            }
        }
        return false;
    }

    private List<Message> pickCard(Integer playerId, Integer roomId, boolean isGangMoPai){
        List<Message> list = new ArrayList<>();
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        List<Integer> cardWall = room.getCardWall();

        while(cardWall.get(0) == 34){
            player.addFlowerCard(1);
            cardWall.remove(0);
        }

        // 荒牌
        if(cardWall.size() == 0){
            return newGame(roomId);
        }else{

            Integer drawCard = cardWall.remove(0);
            int[] h = player.getHandCard();
            h[drawCard]++;
            // 输出牌局信息
            String msg = MahJongUtils.mahJongInfo(player, room);
            list.add(new Message("" + player.getPlayerId(), msg));

            player.setDrawCard(drawCard);
            room.setWhoPlay(playerId);

            boolean isHu = false;
            boolean isGang = false;
            boolean isPlay = false;
            if(MahJongUtils.canHu(player.getHandCard(), drawCard)){
                isHu = MahJongUtils.calculateHu(null, true, isGangMoPai, false, playerId, roomId);
                if(isHu){
                    player.setZiMo(true);
                    room.setWhoHu(playerId);
                }
            } else if(canGang(playerId, roomId, null)){
                player.setAnGang(true);
                isPlay = true;
                isGang = true;
            }else{
                isPlay = true;
            }
            String option = MahJongUtils.getOptions(isPlay, isHu, false, false, isGang);
            list.add(new Message("" + player.getPlayerId(), option));
        }
        return list;
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
