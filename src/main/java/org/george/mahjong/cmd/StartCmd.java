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

public class StartCmd {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private static final String game_start = new StringBuilder()
            .append("=============================================================================\r\n")
            .append("游戏开始了")
            .append("您可以使用以下的命令:\r\n")
            .append("[play:[麻将房间号]:[0-8]、[9-17]、[18-26]、[27-33]]:\r\n")
            .append("在房间中打牌，[0-8]为万、[9-17]为饼、[18-26]为条、[27-33]为东西南北中发白\r\n")
            .append("[chi:[0、1、2]:[麻将房间号]]:吃上家打出的牌，[0、1、2]为左吃或中吃或右吃\r\n")
            .append("[peng:[麻将房间号]]:碰打出的牌\r\n")
            .append("[gang:[麻将房间号]:[0-8]、[9-17]、[18-26]、[27-33]]: 暗杠或者摸杠使用该命令\r\n")
            .append("[mgang:[麻将房间号]]:明杠使用该命令\r\n")
            .append("[hu:[麻将房间号]]:如果已经胡了，就使用该命令\r\n")
            .append("[pass:[麻将房间号]:不对其他玩家打出的牌做吃、碰、杠、胡等操作\r\n")
            .append("=============================================================================")
            .toString();

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_not_exists = "房间不存在";

    private static final String not_in_room = "不在房间内";

    private static final String room_is_not_full = "房间已未满人";

    private static final String already_in_ready_state = "已经处于准备状态";

    private Logger log = LoggerFactory.getLogger(StartCmd.class);

    /**
     * 玩家进行游戏准备
     * @param args
     * @return
     */
    public Messages start(String...args){

        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if (args.length != 2) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            } else if(!NumUtils.checkDigit(args[1])){
                // 房间名不符合规范
                list.add(new Message(userId, room_format_error));
            } else if(!roomExists(Integer.parseInt(args[1]))){
                // 房间不存在
                list.add(new Message(userId, room_not_exists ));
            } else if(!alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家不在房间中
                list.add(new Message(userId, not_in_room));
            } else if(!roomFull(Integer.parseInt(args[1]))){
                // 未满人
                list.add(new Message(userId, room_is_not_full));
            } else if(isReady(playerId, Integer.parseInt(args[1]))){
                // 已经处于准备状态
                list.add(new Message(userId, already_in_ready_state));
            } else{
                // 房间 ID
                Integer roomId = Integer.parseInt(args[1]);
                // 麻将房
                RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                // 玩家就绪
                player.setReady(true);
                list.add(new Message(userId, "进入就绪状态"));

                // 人都准备好了，开始游戏
                if(canGameStart(roomId)){
                    // 设置开始标志
                    room.setStart(true);
                    // 随机设置玩家方位
                    MahJongUtils.setRandPosition(room);
                    // 发牌
                    MahJongUtils.deal(room.getPlayers(), room.getCardWall());
                    // 输出与玩家牌面相关的信息
                    for(PlayerCacheBean p : room.getPlayers()){
                        list.add(new Message("" + p.getPlayerId(), game_start));
                        String msg = MahJongUtils.mahJongInfo(p, room);
                        list.add(new Message("" + p.getPlayerId(), msg));
                    }
                    // 检查并输出庄家能够使用的操作
                    PlayerCacheBean zhuang = mahJongCache.getPlayerByIndex(roomId, 0);
                    zhuang.setNeedPlay(true);
                    room.setWhoPlay(zhuang.getPlayerId());
                    boolean isHu = false;
                    boolean isGang = false;
                    boolean isPlay = false;
                    if(MahJongUtils.canHu(zhuang.getHandCard(), null)){
                        isHu = MahJongUtils.calculateHu(null, true, false, false, playerId, roomId);
                        if(isHu){
                            zhuang.setTianHu(true);
                            room.setWhoHu(zhuang.getPlayerId());
                        }
                    }else if(MahJongUtils.canGang(zhuang.getHandCard(), null)){
                        isPlay = true;
                        zhuang.setAnGang(true);
                    }else{
                        isPlay = true;
                    }
                    String option = MahJongUtils.getOptions(isPlay, isHu, false, false, isGang);
                    list.add(new Message("" + zhuang.getPlayerId(), option));
                    list.add(new Message("" + zhuang.getPlayerId(), "您是庄家，请先出牌"));
                    log.info("麻将房:{}的游戏开始了", roomId);
                }
                log.info("玩家:{}处于就绪状态", playerId);
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

    private boolean canGameStart(Integer roomId){
        List<PlayerCacheBean> ps = mahJongCache.getAllPlayer(roomId);
        int count = 0;
        for(PlayerCacheBean p : ps){
            if(p.isReady()){
                count++;
            }
        }
        return count == 4;
    }
}
