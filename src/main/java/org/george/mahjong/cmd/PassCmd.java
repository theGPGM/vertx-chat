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

public class PassCmd {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_not_exists = "房间不存在";

    private static final String not_in_room = "不在房间内";

    private static final String game_not_start = "游戏尚未开始";

    private static final String can_not_pass = "不满足跳过的条件";

    private static final String low_priority = "其他人还在进行操作，请等待";

    private static final String game_over = "游戏结束了";

    private Logger log = LoggerFactory.getLogger(PassCmd.class);

    /**
     * 只有吃、碰、明杠才能过
     * @param args
     * @return
     */
    public Messages pass(String...args){
        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if(args.length != 2) {
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
            }else if(!alreadyStart(Integer.parseInt(args[1]))){
                // 还没开始游戏
                list.add(new Message(userId, game_not_start));
            }else if(!canPass(playerId, Integer.parseInt(args[1]))){
                // 不满足跳过的条件
                list.add(new Message(userId, can_not_pass));
            }else if(lowPassPriority(playerId, Integer.parseInt(args[1]))){
                // 跳过的优先级较低
                list.add(new Message(userId, low_priority));
            }else{

                Integer roomId = Integer.parseInt(args[1]);
                RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);

                Integer card = room.getPlayCard();

                // 将所有与玩家有关的标志都清除
                pass(playerId, roomId);

                list.add(new Message(userId, "您跳过了" + MahJongUtils.getCardInfo(card)));
                if(room.getWhoChi() == -1 && room.getWhoMingGang() == -1 && room.getWhoPeng() == - 1 && room.getWhoHu() == -1){
                    // 获取打牌的玩家的索引
                    Integer index = mahJongCache.getPlayerIndex(roomId, room.getWhoPlay());
                    // 获取他的下家的索引
                    PlayerCacheBean nextPlayer = mahJongCache.getPlayerByIndex(roomId, (index + 1)  % 4);
                    List<Message> messages = pickCard(nextPlayer.getPlayerId(), roomId, false);
                    for(Message msg : messages){
                        list.add(msg);
                    }
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private boolean alreadyStart(Integer roomId ) {
        return mahJongCache.getRoomByRoomId(roomId).isStart();
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

    private void pass(Integer playerId, Integer roomId){
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        if(room.getWhoChi().equals(playerId)){
            room.setWhoChi(-1);
        }
        if(room.getWhoPeng().equals(playerId)){
            room.setWhoPeng(-1);
        }
        if(room.getWhoMingGang().equals(playerId)){
            room.setWhoMingGang(-1);
        }
        MahJongUtils.clearPlayerState(player);
    }

    private boolean lowPassPriority(Integer playerId, Integer roomId){
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);

        if(!room.getWhoMingGang().equals(-1)){
            if(!room.getWhoMingGang().equals(playerId)){
                return false;
            }else{
                return true;
            }
        }
        if(!room.getWhoPeng().equals(-1)){
            if(!room.getWhoPeng().equals(playerId)){
                return false;
            }else{
                return true;
            }
        }
        if(!room.getWhoChi().equals(-1)){
            return true;
        }
        return false;
    }

    private boolean canPass(Integer playerId, Integer roomId) {
        PlayerCacheBean p = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        if(!room.getWhoHu().equals(-1)){
            return false;
        }
        if(p.isMingGang()){
            return true;
        }
        if(p.isPeng()){
            return true;
        }
        if(p.isChi()){
            return true;
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
            if(canZiMo(player)){
                isHu = MahJongUtils.calculateHu(null, true, isGangMoPai, false, playerId, roomId);
                if(isHu){
                    player.setZiMo(true);
                    room.setWhoHu(playerId);
                }
            } else if(canAnGang(player)){
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

    private boolean canAnGang(PlayerCacheBean player){
        int[] h = player.getHandCard();
        for(int k : h){
            if(k == 4) return true;
        }
        return false;
    }

    private boolean canZiMo(PlayerCacheBean player) {
        return MahJongUtils.canHu(player.getHandCard(), null);
    }
}
