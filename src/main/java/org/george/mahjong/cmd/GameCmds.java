package org.george.mahjong.cmd;

import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;
import org.george.dungeon_game.util.JedisPool;
import org.george.dungeon_game.util.ThreadLocalJedisUtils;
import org.george.hall.model.PlayerModel;
import org.george.mahjong.cache.MahJongCache;
import org.george.mahjong.cache.bean.PlayerCacheBean;
import org.george.mahjong.cache.bean.RoomCacheBean;
import org.george.mahjong.pojo.HuState;
import org.george.mahjong.uitl.MahJongUtils;
import org.george.mahjong.uitl.NumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

public class GameCmds {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

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

    private static final String already_in_room = "已在房间中";

    private static final String not_in_room = "不在房间内";

    private static final String room_is_full = "房间已满人";

    private static final String room_is_not_full = "房间已未满人";

    private static final String already_in_ready_state = "已经处于准备状态";

    private static final String already_start = "已经开始游戏";

    private static final String game_not_start = "游戏尚未开始";

    private static final String not_you_turn = "当前不是你的回合";

    private static final String can_not_pass = "不满足跳过的条件";

    private static final String low_priority = "其他人还在进行操作，请等待";

    private static final String card_not_exists = "要打出的牌不存在";

    private static final String can_not_chi = "不能吃";

    private static final String can_not_peng = "不能碰";

    private static final String can_not_gang = "不能杠";

    private static final String can_not_hu = "无法胡";

    private static final String quan_over = "一圈游戏结束了，开始新的一圈";

    private static final String pan_over = "一盘游戏结束了，开始新的一盘";

    private static final String game_over = "游戏结束了";

    private Logger log = LoggerFactory.getLogger(GameCmds.class);

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
                    setRandPosition(room);
                    // 发牌
                    deal(roomId);
                    // 输出与玩家牌面相关的信息
                    for(PlayerCacheBean p : room.getPlayers()){
                        list.add(new Message("" + p.getPlayerId(), game_start));
                        String msg = mahJongInfo(p, room);
                        list.add(new Message("" + p.getPlayerId(), msg));
                    }
                    // 检查并输出庄家能够使用的操作
                    PlayerCacheBean zhuang = mahJongCache.getPlayerByIndex(roomId, 0);
                    room.setWhoPlay(zhuang.getPlayerId());
                    boolean isHu = false;
                    boolean isGang = false;
                    boolean isPlay = false;
                    if(MahJongUtils.canHu(zhuang.getHandCard(), null)){
                        isHu = calculateHu(null, true, false, false, playerId, roomId);
                        if(isHu){
                            zhuang.setHuState(HuState.TianHu);
                            room.setWhoHu(zhuang.getPlayerId());
                        }
                    }else if(MahJongUtils.canGang(zhuang.getHandCard(), null)){
                        isPlay = true;
                        zhuang.setAnGang(true);
                    }else{
                        isPlay = true;
                    }
                    String option = getOptions(isPlay, isHu, false, false, isGang, false);
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

    public Messages play(String...args){
        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if(args.length != 3) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(!NumUtils.checkDigit(args[1]) || !NumUtils.checkDigit(args[2])){
                // 房间名不符合规范
                list.add(new Message(userId, room_format_error));
            }else if(Integer.parseInt(args[2]) < 0 || Integer.parseInt(args[2]) > 33){
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(!roomExists(Integer.parseInt(args[1]))){
                // 房间不存在
                list.add(new Message(userId, room_not_exists ));
            }else if(!alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家不在房间中
                list.add(new Message(userId, not_in_room));
            }else if(!alreadyStart(Integer.parseInt(args[1]))){
                // 还没开始游戏
                list.add(new Message(userId, game_not_start));
            }else if(notMyPlayTurn(playerId, Integer.parseInt(args[1]))){
                // 不是我的回合
                list.add(new Message(userId, not_you_turn));
            }else if(!cardExists(playerId, Integer.parseInt(args[1]), Integer.parseInt(args[2]))){
                list.add(new Message(userId, card_not_exists));
            } else{
                Integer roomId = Integer.parseInt(args[1]);
                Integer card = Integer.parseInt(args[2]);
                for(Message msg : doPlayCard(playerId, roomId, card)){
                    list.add(msg);
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private List<Message> doPlayCard(Integer playerId, Integer roomId, Integer card){

        List<Message> list = new ArrayList<>();
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        room.setPlayCard(card);

        // 打出一张牌
        int[] handCards = player.getHandCard();
        handCards[card]--;
        // 牌池中加入牌
        room.getCardPool().add(card);
        // 消除玩家的碰、吃、胡状态
        clearPlayerState(playerId, roomId);
        // 获取打牌的玩家的索引
        Integer index = mahJongCache.getPlayerIndex(roomId, playerId);
        // 检查对玩家打出的牌，其他三家是否有吃、碰、杠、胡等操作
        for(int i = 0; i < 3; i++){
            index = (index + 1) % 4;
            PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, index);
            if(MahJongUtils.canHu(p.getHandCard(), card)){
                boolean isHu = calculateHu(card, false, false, false, playerId, roomId);
                if(isHu){
                    p.setHuState(HuState.FanPaoHu);
                    room.setWhoHu(p.getPlayerId());
                    break;
                }
            }
            if(MahJongUtils.canGang(p.getHandCard(), card)){
                p.setMingGang(true);
            }else if(MahJongUtils.canPeng(p.getHandCard(), card)){
                p.setPeng(true);
            }
            // 下家
            if(i == 0){
                if(MahJongUtils.canLeftChi(handCards, card) || MahJongUtils.canMidChi(handCards, card) || MahJongUtils.canRightChi(handCards, card)){
                    p.setChi(true);
                }
            }
        }
        // 打出的这张牌，三家都没用，下家摸牌
        if(noOneNeedThisCard(player.getPlayerId(), roomId)){
            // 上面的消息需要清除
            list.clear();
            Integer nextIndex = (mahJongCache.getPlayerIndex(roomId, playerId) + 1) % 4;
            PlayerCacheBean nextPlayer = mahJongCache.getPlayerByIndex(roomId, nextIndex);
            // 摸牌
            List<Message> messages = pickCard(nextPlayer.getPlayerId(), roomId, false);
            for(Message msg : messages){
                list.add(msg);
            }
        }else {
            StringBuilder sb = new StringBuilder();
            sb.append("=================================\r\n");
            sb.append("您打出了" + getCardInfo(card) + "\r\n");
            sb.append("=================================");
            list.add(new Message("" + playerId, sb.toString()));

            index = mahJongCache.getPlayerIndex(roomId, playerId);
            for (int i = 0; i < 3; i++) {
                index = (index + 1) % 4;
                PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, index);
                sb = new StringBuilder();
                sb.append("=================================\r\n");
                sb.append("玩家[" + playerModel.getPlayerByPlayerId(playerId).getPlayerName() + "]打出了" + getCardInfo(card) + "\r\n");
                sb.append("=================================");
                list.add(new Message("" + mahJongCache.getPlayerByIndex(roomId, i).getPlayerId(), sb.toString()));
                // 输出能够执行的操作
                boolean fanPao = p.getHuState().equals(HuState.FanPaoHu);
                String option = getOptions(false, fanPao, p.isChi(), p.isPeng(), p.isMingGang(), true);
                list.add(new Message("" + p.getPlayerId(), option));
            }
        }
        return list;
    }

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
            }else if(!NumUtils.checkDigit(args[1]) || !NumUtils.checkDigit(args[2])){
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

                list.add(new Message(userId, "您跳过了" + getCardInfo(card)));
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
                Map<String, Integer> map = calculateFan(card, isZiMo, isGangMoPai, isQiangGangHu, playerId, roomId);

                Integer fan = countFan(map);

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
            }else if(lowPassPriority(playerId, Integer.parseInt(args[1]))){
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

                clearPlayerState(playerId, roomId);

                int[] h = player.getHandCard();
                h[playerCard] -= 2;
                player.addPeng(playerCard);

                String msg = mahJongInfo(player, room);
                list.add(new Message(userId, "您碰了这张牌，请打出一张牌"));
                list.add(new Message(userId, msg));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

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
            }else if(!NumUtils.checkDigit(args[1]) || !!NumUtils.checkDigit(args[2])){
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
            }else if(lowPassPriority(playerId, Integer.parseInt(args[1]))){
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

                String msg = mahJongInfo(player, room);
                list.add(new Message(userId, "您吃了这张牌，请打出一张牌"));
                list.add(new Message(userId, msg));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

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

                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                int[] h = player.getHandCard();
                h[card] = 0;
                player.addGang(card);
                list.add(new Message(userId, "您杠了一张牌，请打出一张牌"));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

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
                room.setWhoPlay(playerId);

                PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                int[] h = player.getHandCard();
                if(h[card] == 4){
                    h[card] = 0;
                    List<Message> messages = pickCard(playerId, roomId, true);
                    for(Message message : messages){
                        list.add(message);
                    }
                }else{
                    for(int i = 0; i < 4; i++){
                        PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                        if(!p.getPlayerId().equals(playerId)){
                            if(calculateHu(card, false, false, true, p.getPlayerId(), roomId)){
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

    private boolean canMGang(Integer playerId, Integer roomId){
        return mahJongCache.getPlayerByPlayerId(roomId, playerId).isMingGang();
    }

    private boolean lowGangPriority(Integer playerId, Integer roomId){
        return mahJongCache.getRoomByRoomId(roomId).getWhoHu() != -1;
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

    /*********************************** 打牌的相关辅助函数 ***************************************/
    private void clearPlayerState(Integer playerId, Integer roomId) {
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        player.setAnGang(false);
        player.setMingGang(false);
        player.setGangMoHu(false);
        player.setPeng(false);
        player.setChi(false);
        player.setTianHu(false);
        player.setQiangGangHu(false);
        player.setZiMo(false);
        player.setFanPao(false);
        player.setNeedPlay(false);
    }

    private boolean noOneNeedThisCard(Integer playerId, Integer roomId){
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        if(room.getWhoHu() != -1 || room.getWhoChi() != -1 || room.getWhoMingGang() != -1 || room.getWhoPeng() != -1){
            return false;
        }
        return true;
    }

    /********************************** 摸牌的函数 ***********************************************/

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
            String msg = mahJongInfo(player, room);
            list.add(new Message("" + player.getPlayerId(), msg));

            player.setDrawCard(drawCard);
            room.setWhoPlay(playerId);

            boolean isHu = false;
            boolean isGang = false;
            boolean isPlay = false;
            if(MahJongUtils.canHu(player.getHandCard(), drawCard)){
                isHu = calculateHu(null, true, isGangMoPai, false, playerId, roomId);
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
            String option = getOptions(isPlay, isHu, false, false, isGang, false);
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
                changePosition(roomId);
            } else {
                // 一盘游戏结束, 换庄
                changeZhuang(mahJongCache.getRoomByRoomId(roomId));
                room.setPan(room.getPan() + 1);
            }
            // 发牌
            deal(roomId);
            // 向四位玩家发送牌局相关消息
            for (int i = 0; i < 4; i++) {
                PlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                String msg = mahJongInfo(p, room);
                list.add(new Message("" + p.getPlayerId(), msg));
            }
            // 输出庄家能够使用的操作
            PlayerCacheBean zhuang = mahJongCache.getPlayerByIndex(roomId, 0);
            boolean isHu = false;
            boolean isGang = false;
            boolean isPlay = false;
            if (MahJongUtils.canHu(zhuang.getHandCard(), null)) {
                isHu = calculateHu(null, true, false, false, zhuang.getPlayerId(), roomId);
                if (isHu) {
                    zhuang.setTianHu(true);
                }
            }else if(MahJongUtils.canGang(zhuang.getHandCard(), null)) {
                isGang = true;
                isPlay = true;
                zhuang.setAnGang(true);
                room.setWhoPlay(zhuang.getPlayerId());
            }
            String option = getOptions(isPlay, isHu, false, false, isGang, false);
            list.add(new Message("" + zhuang.getPlayerId(), option));
        }
        return list;
    }

    /*********************************** 跳过的函数 **********************************************/
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
        player.setChi(false);
        player.setMingGang(false);
        player.setPeng(false);
    }

    /********************************** 胡牌的相关函数 ********************************************/

    private boolean canHu(Integer playerId, Integer roomId) {
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        return room.getWhoHu().equals(playerId);
    }

    /**
     * 判断是否能达到 8 番
     * @param card
     * @param isZiMo
     * @param isGangMoPai
     * @param isQiangGangHu
     * @param playerId
     * @param roomId
     * @return
     */
    private boolean calculateHu(Integer card, boolean isZiMo, boolean isGangMoPai, boolean isQiangGangHu, Integer playerId, Integer roomId){

        RoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        int count = 0;
        if(card != null){
            for(int k : roomCacheBean.getCardWall()){
                if(k == card){
                    count++;
                }
            }
        }

        int[] h = player.getHandCard();

        Map<String, Object> param = new HashMap<>();
        param.put("isZiMo", isZiMo);
        param.put("isLastCard", roomCacheBean.getCardWall().size() == 0);
        param.put("isGangMoPai", isGangMoPai);
        param.put("isQiangGangHu", isQiangGangHu);
        param.put("isHuJueZhang", card != null && count == 0);
        param.put("quanFeng", roomCacheBean.getQuan());
        param.put("menFeng", mahJongCache.getPlayerIndex(roomCacheBean.getRoomId(), player.getPlayerId()));
        param.put("chi", player.getChis());
        param.put("peng", player.getPengs());
        param.put("mingGang", player.getMingGangs());
        param.put("anGang", player.getAnGangs());
        param.put("huCard", card);
        param.put("flowerCount", player.getFlowerCard());
        Map<String, Integer> map = MahJongUtils.calculate(h, param);

        int point = 0;
        for(Integer p : map.values()){
            point += p;
        }

        if(point >= 8){
            return true;
        }
        return false;
    }

    private Map<String, Integer> calculateFan(Integer card, boolean isZiMo, boolean isGangMoPai, boolean isQiangGangHu, Integer playerId, Integer roomId){
        RoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        int count = 0;
        if(card != null){
            for(int k : roomCacheBean.getCardWall()){
                if(k == card){
                    count++;
                }
            }
        }

        int[] h = player.getHandCard();

        Map<String, Object> param = new HashMap<>();
        param.put("isZiMo", isZiMo);
        param.put("isLastCard", roomCacheBean.getCardWall().size() == 0);
        param.put("isGangMoPai", isGangMoPai);
        param.put("isQiangGangHu", isQiangGangHu);
        param.put("isHuJueZhang", card != null && count == 0);
        param.put("quanFeng", roomCacheBean.getQuan());
        param.put("menFeng", mahJongCache.getPlayerIndex(roomCacheBean.getRoomId(), player.getPlayerId()));
        param.put("chi", player.getChis());
        param.put("peng", player.getPengs());
        param.put("mingGang", player.getMingGangs());
        param.put("anGang", player.getAnGangs());
        param.put("huCard", card);
        param.put("flowerCount", player.getFlowerCard());
        Map<String, Integer> map = MahJongUtils.calculate(h, param);
        return map;
    }

    private Integer countFan(Map<String, Integer> map){
        int count = 0;
        for(Map.Entry<String,Integer> entry : map.entrySet()){
            count += entry.getValue();
        }
        return count;
    }

    /********************************** 检验输入是否有效的相关函数 **********************************/

    private boolean canChi(Integer playerId, Integer roomId){

        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        player.isChi();
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

    private boolean notMyPlayTurn(Integer playerId, int roomId) {
        return !mahJongCache.getRoomByRoomId(roomId).getWhoPlay().equals(playerId);
    }

    private boolean cardExists(Integer playerId, Integer roomId, Integer card){
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        int[] h = player.getHandCard();
        return h[card] != 0;
    }

    private boolean alreadyStart(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId).isStart();
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

    private boolean roomExists(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId) != null;
    }
    /********************************** 消息输出的相关函数 *****************************************/
    private String fangInfo(Map<String, Integer> map){

        StringBuilder sb = new StringBuilder();
        sb.append("=======================================\r\n");
        sb.append("您的番数:\r\n");
        for(Map.Entry<String,Integer> entry : map.entrySet()){
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
        }
        sb.append("=======================================");
        return sb.toString();
    }

    private String mahJongInfo(PlayerCacheBean p, RoomCacheBean r) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\r\n");
        sb.append("牌池: \r\n");
        sb.append(getCardPoolInfo(r.getCardPool()));
        sb.append("您的手牌: \r\n");
        sb.append(getHandCardInfo(p.getHandCard()) + "\r\n");
        sb.append("您的花牌数量: " + p.getFlowerCard() + "\r\n");
        sb.append("=================================================================");
        return sb.toString();
    }

    /**
     * 获得牌池的信息
     * @param cardPool
     * @return
     */
    private String getCardPoolInfo(List<Integer> cardPool) {
        StringBuilder sb = new StringBuilder();
        // 每二十张牌显示一行
        for(int i = 0; i < cardPool.size(); i += 20){
            for(int j = i; j < i + 20; j++){
                if(cardPool.get(j) < 9){
                    sb.append("|" + oneToNight(cardPool.get(j)) + "万|");
                }else if(cardPool.get(j) < 18){
                    sb.append("|" + oneToNight(cardPool.get(j) % 9) + "饼|");
                }else if(cardPool.get(j) < 27){
                    sb.append("|" + oneToNight(cardPool.get(j) % 9) + "条|");
                }else{
                    sb.append("|" + numToTile(cardPool.get(j) - 27) + "|");
                }
                if(j == cardPool.size() - 1){
                    break;
                }
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    /**
     * 获取玩家手牌的信息
     * @param h
     * @return
     */
    private String getHandCardInfo(int[] h){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < h.length; i++){
            if(h[i] != 0){
                for(int j = 0; j < h[i]; j++){
                    sb.append(getCardInfo(i));
                }
            }
        }
        return sb.toString();
    }

    private String getCardInfo(int card){
        if(card < 9){
            return "|" + oneToNight(card) + "万|";
        }else if(card < 18){
            return "|" + oneToNight(card % 9) + "饼|";
        }else if(card < 27){
            return "|" + oneToNight(card % 9) + "条|";
        }else{
            return "|" + numToTile(card - 27) + "|";
        }
    }

    /**
     * 将数字转化为大写数字
     * @param num
     * @return
     */
    private String oneToNight(int num){
        switch(num) {
            case 0:
                return "一";
            case 1:
                return "二";
            case 2:
                return "三";
            case 3 :
                return "四";
            case 4:
                return "五";
            case 5:
                return "六";
            case 6 :
                return "七";
            case 7:
                return "八";
            case 8:
                return "九";
            default:
                return null;
        }
    }

    /**
     * 将数字转化为东西南北中发白
     * @param num
     * @return
     */
    private String numToTile(int num){
        switch(num) {
            case 0:
                return "东";
            case 1:
                return "南";
            case 2:
                return "西";
            case 3 :
                return "北";
            case 4:
                return "中";
            case 5:
                return "发";
            case 6 :
                return "白";
            default:
                return null;
        }
    }

    /**
     * 返回玩家所有能够使用的操作
     * @param isHu
     * @param isChi
     * @param isPeng
     * @param isGang
     * @param isPass
     * @return
     */
    private String getOptions(boolean isPlay, boolean isHu, boolean isChi, boolean isPeng, boolean isGang, boolean isPass){
        StringBuilder sb = new StringBuilder();
        if(!isPlay && !isHu && !isChi && !isPeng && !isGang && !isPass){
            sb.append("您无可使用的操作");
        }else{
            sb.append("=================================\r\n");
            sb.append("您可以使用的操作如下：");
            if(isPlay){
                sb.append("[play:[num]]:打一张牌\r\n");
            }if(isHu){
                sb.append("[hu]:胡\r\n");
            }
            if(isGang){
                sb.append("[gang]杠\r\n");
            }
            if(isPeng){
                sb.append("[peng]碰\r\n");
            }
            if(isChi){
                sb.append("[chi:0、1、2]: 左、中、右吃\r\n");
            }
            if(isPass){
                sb.append("[pass]过\r\n");
            }
            sb.append("=================================");
        }
        return sb.toString();
    }

    /************************* 与方位有关的相关函数 **************************************/
    /**
     * 换座位
     */
    private void changePosition(Integer roomId){
        RoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        List<PlayerCacheBean> list = room.getList();
        int quan = room.getQuan();

        // 先换庄恢复之前的状态
        changeZhuang(room);

        // 东风圈
        if(quan == 0 || quan == 2){
            swap(list, 0, 1);
            swap(list, 2, 3);
        }else{
            swap(list, 0, 3);
            swap(list, 1, 2);
            swap(list, 2, 3);
        }
    }

    private void swap(List<PlayerCacheBean> list, int i, int j){
        PlayerCacheBean temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    private void setRandPosition(RoomCacheBean room) {
        Random random = new Random();
        List<PlayerCacheBean> list = room.getList();
        for(int i = 3; i >= 0; i--){
            swap(list, i, random.nextInt(i + 1));
        }
    }

    private void changeZhuang(RoomCacheBean room){
        List<PlayerCacheBean> list = room.getList();
        PlayerCacheBean first = list.remove(0);
        list.add(first);
    }

    /************************ 发牌 ************************************/
    /**
     * 发牌，默认数组的第一个玩家为庄家，发十四张牌
     * @param roomId
     */
    private void deal(Integer roomId){

        RoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);

        List<PlayerCacheBean> players = roomCacheBean.getPlayers();
        // 发牌
        int[] h1 = new int[34];
        int[] h2 = new int[34];
        int[] h3 = new int[34];
        int[] h4 = new int[34];

        List<Integer> cards = roomCacheBean.getCardWall();
        for(int i = 0; i < 14; i++) {
            while(cards.get(0) == 34){
                players.get(0).addFlowerCard(1);
                cards.remove(0);
            }
            h1[cards.remove(0)]++;
        }
        players.get(0).setHandCard(h1);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(1).addFlowerCard(1);
                cards.remove(0);
            }
            h2[cards.remove(0)]++;
        }
        players.get(1).setHandCard(h2);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(2).addFlowerCard(1);
                cards.remove(0);
            }
            h3[cards.remove(0)]++;
        }
        players.get(2).setHandCard(h3);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(3).addFlowerCard(1);
                cards.remove(0);
            }
            h4[cards.remove(0)]++;
        }
        players.get(3).setHandCard(h4);
    }
}
