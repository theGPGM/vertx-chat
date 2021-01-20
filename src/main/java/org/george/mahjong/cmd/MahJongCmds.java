package org.george.mahjong.cmd;

import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;
import org.george.dungeon_game.util.JedisPool;
import org.george.dungeon_game.util.ThreadLocalJedisUtils;
import org.george.hall.model.PlayerModel;
import org.george.mahjong.cache.MahJongCache;
import org.george.mahjong.cache.bean.MahJongPlayerCacheBean;
import org.george.mahjong.cache.bean.MahJongRoomCacheBean;
import org.george.mahjong.uitl.MahJongUtils;
import org.george.mahjong.uitl.NumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.net.MalformedURLException;
import java.util.*;

public class MahJongCmds {

    private MahJongCache mahJongCache = MahJongCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private static final String game_start = new StringBuilder()
            .append("=============================================================================\r\n")
            .append("游戏开始了")
            .append("您可以使用以下的命令:\r\n")
            .append("[get:[麻将房间号]]:在房间中获取牌\r\n")
            .append("[play:[麻将房间号]:[1-9]、[11-19]、[21-29]、[31-37]]:\r\n")
            .append("在房间中打牌，[1-9]为万、[10-19]为饼、[20-29]为条、[30-37]为东西南北中发白\r\n")
            .append("[chi:[0、1、2]:[麻将房间号]]:吃上家打出的牌，[0、1、2]为左吃或中吃或右吃\r\n")
            .append("[peng:[麻将房间号]]:碰打出的牌\r\n")
            .append("[gang:[麻将房间号]]:暗杠或者杠打出的牌都使用该命令\r\n")
            .append("[hu:[麻将房间号]]:如果已经胡了，就使用该命令\r\n")
            .append("[pass:[麻将房间号]:不对其他玩家打出的牌做吃、碰、杠、胡等操作\r\n")
            .append("=============================================================================")
            .toString();

    private static final String input_format_error = "输入格式错误";

    private static final String room_format_error = "房间名只能由数字组成，当前房间名不符合规范";

    private static final String room_exists = "房间已存在";

    private static final String room_not_exists = "房间不存在";

    private static final String already_in_room = "已在房间中";

    private static final String not_in_room = "不在房间内";

    private static final String room_is_full = "房间已满人";

    private static final String room_is_not_full = "房间已未满人";

    private static final String already_in_ready_state = "已经处于准备状态";

    private static final String already_start = "已经开始游戏";

    private static final String game_not_start = "游戏尚未开始";

    private static final String not_you_turn = "当前不是你的回合";

    private static final String room_create_success = "房间创建成功，请等待其他玩家加入";

    private static final String can_not_pass = "不满足跳过的条件";

    private static final String low_priority = "其他人还在进行操作，请等待";

    private static final String card_is_not_full = "您的牌还没有满，请摸一张牌再打";

    private static final String card_not_exists = "要打出的牌不存在";

    private static final String can_not_chi = "不能吃";

    private static final String can_not_peng = "不能碰";

    private static final String can_not_gang = "不能杠";

    private static final String can_not_hu = "无法胡";

    private static final String quan_over = "一圈游戏结束了，开始新的一圈";

    private static final String pan_over = "一盘游戏结束了，开始新的一盘";

    private static final String game_over = "游戏结束了";

    private Logger log = LoggerFactory.getLogger(MahJongCmds.class);

    /**
     * 玩家创建房间
     * @param args
     * @return
     */
    public Messages createMahJongRoom(String...args){
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
            }else if(alreadyInRoom(Integer.parseInt(args[1]), playerId)){
                // 玩家已在房间中
                list.add(new Message(userId, already_in_room));
            }else{
                Integer roomId = Integer.parseInt(args[0]);
                MahJongRoomCacheBean roomCacheBean = new MahJongRoomCacheBean(roomId);
                MahJongPlayerCacheBean cacheBean = new MahJongPlayerCacheBean(playerId);
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
    public Messages joinMahJongRoom(String...args){

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
                Integer roomId = Integer.parseInt(args[0]);
                MahJongPlayerCacheBean cacheBean = new MahJongPlayerCacheBean(playerId);
                mahJongCache.addPlayer(roomId, cacheBean);

                list.add(new Message(userId, room_create_success));

                log.info("玩家: {} 加入麻将房: {}", playerId, roomId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    /**
     * 玩家进行游戏准备
     * @param args
     * @return
     */
    public Messages ready(String...args){

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
            } else if(alreadyStart(Integer.parseInt(args[1]))){
                // 已经开始游戏
                list.add(new Message(userId, already_start));
            } else if(isReady(playerId, Integer.parseInt(args[1]))){
                // 已经处于准备状态
                list.add(new Message(userId, already_in_ready_state));
            } else{

                Integer roomId = Integer.parseInt(args[0]);
                MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                MahJongPlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                player.setReady(true);

                int count = 0;
                for(MahJongPlayerCacheBean p : room.getPlayers()){
                    if(p.isReady()){
                        count++;
                    }
                }

                // 人都准备好了，开始游戏
                if(count == 4){

                    // 设置玩家东南西北的方位
                    setRandPosition(room);

                    // 发牌
                    deal(roomId);

                    for(MahJongPlayerCacheBean p : room.getPlayers()){
                        list.add(new Message("" + p.getPlayerId(), game_start));
                        // 与牌局相关的信息
                        String msg = mahJongInfo(p, room);
                        list.add(new Message("" + p.getPlayerId(), msg));
                    }

                    // 输出庄家能够使用的操作
                    MahJongPlayerCacheBean zhuang = mahJongCache.getPlayerByIndex(roomId, 0);

                    boolean isHu = false;
                    boolean isGang = false;
                    if(MahJongUtils.canHu(player.getHandCards(), null)){
                        isHu = calculateHu(null, true, false, false, playerId, roomId);
                        if(isHu){
                            room.addWhoHu(playerId);
                        }
                    }
                    if(MahJongUtils.canGang(player.getHandCards(), null)){
                        isGang = true;
                        room.setWhoGang(playerId);
                    }

                    String option = getOptions(isHu, false, false, isGang, false);
                    list.add(new Message("" + zhuang.getPlayerId(), option));

                    log.info("麻将房:{}的游戏开始了", roomId);
                }
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
            }else if(Integer.parseInt(args[2]) <= 0 || Integer.parseInt(args[2]) > 37 || Integer.parseInt(args[2]) == 10 || Integer.parseInt(args[2]) == 20 || Integer.parseInt(args[2]) == 30){
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
            }else if(!isExistsCard(playerId, Integer.parseInt(args[1]), Integer.parseInt(args[2]))){
                // 牌不存在
                list.add(new Message(userId, card_not_exists));
            }else{

                Integer roomId = Integer.parseInt(args[1]);
                Integer card = Integer.parseInt(args[2]);
                MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                MahJongPlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
                // 打出一张牌
                int[] handCards = player.getHandCards();
                handCards[card]--;
                // 牌池中加入牌
                room.getCardPool().add(card);

                // 获取打牌的玩家的索引
                Integer index = mahJongCache.getPlayerIndex(roomId, playerId);
                // 获取他的下家的索引
                int next = (index + 1) % 4;

                // 检查对玩家打出的牌，其他三家是否有吃、碰、杠、胡等操作
                for(int i = 0; i < 4; i++){
                    if(i == index){
                        StringBuilder sb = new StringBuilder();
                        sb.append("=================================\r\n");
                        sb.append("您打出了" + getCardInfo(card) + "\r\n");
                        sb.append("=================================");
                    }else{
                        MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                        StringBuilder sb = new StringBuilder();
                        sb.append("=================================\r\n");
                        sb.append("玩家[" + playerModel.getPlayerByPlayerId(playerId).getPlayerName() + "]打出了" + getCardInfo(card) + "\r\n");
                        sb.append("=================================");
                        list.add(new Message("" + mahJongCache.getPlayerByIndex(roomId, i).getPlayerId(), sb.toString()));

                        boolean isHu = false;
                        boolean isChi = false;
                        boolean isGang = false;
                        boolean isPeng = false;
                        if(MahJongUtils.canHu(p.getHandCards(), card)){
                            isHu = calculateHu(card, false, false, false, playerId, roomId);
                            if(isHu){
                                room.addWhoHu(p.getPlayerId());
                            }
                        }
                        if(MahJongUtils.canGang(p.getHandCards(), card)){
                            isGang = true;
                            room.setWhoGang(p.getPlayerId());
                        }else if(MahJongUtils.canPeng(p.getHandCards(), card)){
                            isPeng = true;
                            room.setWhoPeng(p.getPlayerId());
                        }

                        // 下家
                        if(i == next){
                            if(MahJongUtils.canLeftChi(handCards, card) || MahJongUtils.canMidChi(handCards, card) || MahJongUtils.canRightChi(handCards, card)){
                                isChi = true;
                                room.setWhoChi(p.getPlayerId());
                            }
                        }
                        String option = getOptions(isHu, isChi, isPeng, isGang, true);
                        list.add(new Message("" + p.getPlayerId(), option));
                    }
                }

                // 此处需要判断是否会荒牌
                boolean flag = false;
                if(room.getWhoChi() == -1 && room.getWhoGang() == -1 && room.getWhoPeng() == - 1 && room.getWhoHu().size() == 0){
                    for(int i = 0; i < 3; i++){
                        // 输出牌局信息
                        MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, index);
                        String msg = mahJongInfo(p, room);
                        list.add(new Message("" + p.getPlayerId(), msg));
                        // 牌空了，这局游戏结束
                        Integer drawCard = pickCard(roomId, index);
                        if(drawCard != -1){
                            int[] h = p.getHandCards();
                            h[drawCard]++;

                            boolean isHu = false;
                            boolean isGang = false;
                            if(MahJongUtils.canHu(p.getHandCards(), drawCard)){
                                isHu = calculateHu(card, false, false, false, playerId, roomId);
                                if(isHu){
                                    room.addWhoHu(p.getPlayerId());
                                }
                            }
                            if(MahJongUtils.canGang(p.getHandCards(), null)){
                                isGang = true;
                            }
                            String option = getOptions(isHu, false, false, isGang, false);
                        }else{
                            // 荒牌，设置标签
                            flag = true;
                            break;
                        }
                        index = (index + 1) % 4;
                    }
                }

                // 荒牌：一盘游戏结束，清除前面的所有消息，重新开始新一轮游戏或者结束游戏
                if(flag) {
                    // 清除所有的信息
                    list.clear();
                    if (room.getQuan() == 3 && room.getPan() == 3) {
                        // 游戏彻底结束
                        mahJongCache.deleteCacheBean(roomId);
                        for (int i = 0; i < 3; i++) {
                            MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
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
                            MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                            String msg = mahJongInfo(p, room);
                            list.add(new Message("" + p.getPlayerId(), msg));
                        }
                        // 输出庄家能够使用的操作
                        MahJongPlayerCacheBean zhuang = mahJongCache.getPlayerByIndex(roomId, 0);
                        boolean isHu = false;
                        boolean isGang = false;
                        if (MahJongUtils.canHu(player.getHandCards(), null)) {
                            isHu = calculateHu(null, true, false, false, playerId, roomId);
                            if (isHu) {
                                room.addWhoHu(playerId);
                            }
                        }
                        if(MahJongUtils.canGang(player.getHandCards(), null)) {
                            isGang = true;
                            room.setWhoGang(playerId);
                        }
                        String option = getOptions(isHu, false, false, isGang, false);
                        list.add(new Message("" + zhuang.getPlayerId(), option));
                    }
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

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
            }else if(lowPriority(playerId, Integer.parseInt(args[1]))){
                // 跳过的优先级较低
                list.add(new Message(userId, low_priority));
            } else{

                Integer roomId = Integer.parseInt(args[1]);
                MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);

                // 将所有与该玩家有关的标志都清除
                pass(playerId, roomId);

                // 获取打牌的玩家的索引
                Integer index = mahJongCache.getPlayerIndex(roomId, playerId);
                // 获取他的下家的索引
                int next = (index + 1) % 4;

                // 此处需要判断是否会荒牌
                boolean flag = false;
                if(room.getWhoChi() == -1 && room.getWhoGang() == -1 && room.getWhoPeng() == - 1 && room.getWhoHu().size() == 0){
                    for(int i = 0; i < 3; i++){
                        // 输出牌局信息
                        MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, index);
                        String msg = mahJongInfo(p, room);
                        list.add(new Message("" + p.getPlayerId(), msg));
                        // 牌空了，这局游戏结束
                        Integer drawCard = pickCard(roomId, index);
                        if(drawCard != -1){
                            int[] h = p.getHandCards();
                            h[drawCard]++;

                            boolean isHu = false;
                            boolean isGang = false;
                            if(MahJongUtils.canHu(p.getHandCards(), drawCard)){
                                isHu = calculateHu(null, true, false, false, playerId, roomId);
                                if(isHu){
                                    room.addWhoHu(p.getPlayerId());
                                }
                            }
                            if(MahJongUtils.canGang(p.getHandCards(), null)){
                                isGang = true;
                            }
                            String option = getOptions(isHu, false, false, isGang, false);
                            list.add(new Message("" + p.getPlayerId(), option));
                        }else{
                            // 荒牌，设置标签
                            flag = true;
                            break;
                        }
                        index = (index + 1) % 4;
                    }
                }

                // 荒牌：一盘游戏结束，清除前面的所有消息，重新开始新一轮游戏或者结束游戏
                if(flag) {
                    // 清除所有的信息
                    list.clear();
                    if (room.getQuan() == 3 && room.getPan() == 3) {
                        // 游戏彻底结束
                        mahJongCache.deleteCacheBean(roomId);
                        for (int i = 0; i < 3; i++) {
                            MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                            list.add(new Message("" + p.getPlayerId(), game_over));
                        }
                        log.info("麻将房:{}中的游戏结束了", roomId);
                    } else {
                        if (room.getPan() == 3) {
                            room.setQuan(room.getQuan() + 1);
                            room.setPan(0);
                            // 一圈游戏结束，换座位
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
                            MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, i);
                            String msg = mahJongInfo(p, room);
                            list.add(new Message("" + p.getPlayerId(), msg));
                        }
                        // 输出庄家能够使用的操作
                        MahJongPlayerCacheBean zhuang = mahJongCache.getPlayerByIndex(roomId, 0);
                        boolean isHu = false;
                        boolean isGang = false;
                        if (MahJongUtils.canHu(zhuang.getHandCards(), null)) {
                            isHu = calculateHu(null, true, false, false, playerId, roomId);
                            if (isHu) {
                                room.addWhoHu(playerId);
                            }
                        }
                        if(MahJongUtils.canGang(zhuang.getHandCards(), null)) {
                            isGang = true;
                            room.setWhoGang(playerId);
                        }
                        String option = getOptions(isHu, false, false, isGang, false);
                        list.add(new Message("" + zhuang.getPlayerId(), option));
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
            }else if(lowHuPriority(playerId, Integer.parseInt(args[1]))){
                // 胡的优先级比较低
                list.add(new Message(userId, low_priority));
            } else{
                Integer roomId = Integer.parseInt(args[0]);
                MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
                MahJongPlayerCacheBean player = new MahJongPlayerCacheBean(playerId);


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
            }else if(NumUtils.checkDigit(args[1])){
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
            if (args.length != 2) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(NumUtils.checkDigit(args[1])){
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
                Integer roomId = Integer.parseInt(args[0]);
                MahJongRoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);
                MahJongPlayerCacheBean cacheBean = new MahJongPlayerCacheBean(playerId);
                roomCacheBean.setRoomId(roomId);
                roomCacheBean.addPlayer(cacheBean);
                mahJongCache.addCacheBean(roomCacheBean);

                list.add(new Message(userId, room_create_success));

                log.info("玩家: {} 加入麻将房: {}", playerId, roomId);
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
            if (args.length != 2) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(NumUtils.checkDigit(args[1])){
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
                Integer roomId = Integer.parseInt(args[0]);
                MahJongRoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);
                MahJongPlayerCacheBean cacheBean = new MahJongPlayerCacheBean(playerId);
                roomCacheBean.setRoomId(roomId);
                roomCacheBean.addPlayer(cacheBean);
                mahJongCache.addCacheBean(roomCacheBean);

                list.add(new Message(userId, room_create_success));

                log.info("玩家: {} 加入麻将房: {}", playerId, roomId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
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

        MahJongRoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);
        MahJongPlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        int count = 0;
        if(card != null){
            for(int k : roomCacheBean.getCardWall()){
                if(k == card){
                    count++;
                }
            }
        }

        int[] h = player.getHandCards();

        Map<String, Object> param = new HashMap<>();
        param.put("isZiMo", isZiMo);
        param.put("isLastCard", roomCacheBean.getCardWall().size() == 0);
        param.put("isGangMoPai", isGangMoPai);
        param.put("isQiangGangHu", isQiangGangHu);
        param.put("isHuJueZhang", card != null && count == 0);
        param.put("quanFeng", roomCacheBean.getQuan());
        param.put("menFeng", mahJongCache.getPlayerIndex(roomCacheBean.getRoomId(), player.getPlayerId()));
        param.put("chi", player.getChi());
        param.put("peng", player.getPeng());
        param.put("mingGang", player.getMingGang());
        param.put("anGang", player.getAnGang());
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

    private boolean canHu(Integer playerId, Integer roomId) {
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        List<Integer> whoHu = room.getWhoHu();
        for(Integer p : whoHu){
            if(p.equals(playerId)){
                return true;
            }
        }
        return false;
    }

    private boolean lowHuPriority(Integer playerId, Integer roomId){
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        List<Integer> whoHu = room.getWhoHu();
        return whoHu.get(0).equals(playerId);
    }

    private void pass(Integer playerId, Integer roomId){
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        if(room.getWhoHu().size() > 0){
            room.getWhoHu().remove(0);
        }
        if(room.getWhoGang().equals(playerId)){
            room.setWhoGang(-1);
        }
        if(room.getWhoPeng().equals(playerId)){
            room.setWhoPeng(-1);
        }
        if(room.getWhoChi().equals(playerId)){
            room.setWhoChi(-1);
        }
    }

    private boolean lowPriority(Integer playerId, Integer roomId){
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);

        if(room.getWhoHu().size() > 0){
            if(playerId.equals(room.getWhoHu().get(0))){
                return true;
            }
            return false;
        }

        if(room.getWhoGang() != -1){
            if(playerId.equals(room.getWhoGang())){
                return true;
            }else{
                return false;
            }
        }

        if(room.getWhoPeng() != -1){
            if(playerId.equals(room.getWhoPeng())){
                return true;
            }else{
                return false;
            }
        }

        if(room.getWhoChi() != -1){
            if(playerId.equals(room.getWhoChi())){
                return true;
            }else{
                return false;
            }
        }

        return false;
    }

    private boolean canPass(Integer playerId, Integer roomId) {
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        if(playerId.equals(room.getWhoPeng())){
            return true;
        }
        if(playerId.equals(room.getWhoGang())){
            return true;
        }
        if(playerId.equals(room.getWhoChi())){
            return true;
        }
        for(Integer p : room.getWhoHu()){
            if(playerId.equals(p)){
                return true;
            }
        }
        return false;
    }

    /**
     * 换座位
     */
    private void changePosition(Integer roomId){
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        List<MahJongPlayerCacheBean> list = room.getList();
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

    private void swap(List<MahJongPlayerCacheBean> list, int i, int j){
        MahJongPlayerCacheBean temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    private Integer pickCard(Integer roomId, Integer index) {
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        List<Integer> cardWall = room.getCardWall();

        MahJongPlayerCacheBean p = mahJongCache.getPlayerByIndex(roomId, index);
        while(cardWall.get(0) == 34 && cardWall.size() != 0){
            cardWall.remove(0);
            p.addFlowerCard(1);
        }

        if(cardWall.size() == 0){
            return -1;
        }
        return cardWall.remove(0);
    }

    private void setRandPosition(MahJongRoomCacheBean room) {
        Random random = new Random();
        List<MahJongPlayerCacheBean> list = room.getList();
        for(int i = 3; i >= 0; i--){
            swap(list, i, random.nextInt(i + 1));
        }
    }

    private void changeZhuang(MahJongRoomCacheBean room){
        List<MahJongPlayerCacheBean> list = room.getList();
        MahJongPlayerCacheBean first = list.remove(0);
        list.add(first);
    }

    private boolean notMyPlayTurn(Integer playerId, int roomId) {
        return !mahJongCache.getRoomByRoomId(roomId).getWhoPlay().equals(playerId);
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
    private String getOptions(boolean isHu, boolean isChi, boolean isPeng, boolean isGang, boolean isPass){
        StringBuilder sb = new StringBuilder();
        if(!isHu && !isChi && !isPeng && !isGang && !isPass){
            sb.append("您无可使用的操作");
        }else{
            sb.append("=================================\r\n");
            sb.append("您可以使用的操作如下：");
            if(isHu){
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

    /**
     * 发牌，默认数组的第一个玩家为庄家，发十四张牌
     * @param roomId
     */
    private void deal(Integer roomId){

        MahJongRoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);

        List<MahJongPlayerCacheBean> players = roomCacheBean.getPlayers();
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
        players.get(0).addHandCards(h1);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(1).addFlowerCard(1);
                cards.remove(0);
            }
            h2[cards.remove(0)]++;
        }
        players.get(1).addHandCards(h2);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(2).addFlowerCard(1);
                cards.remove(0);
            }
            h3[cards.remove(0)]++;
        }
        players.get(2).addHandCards(h3);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(3).addFlowerCard(1);
                cards.remove(0);
            }
            h4[cards.remove(0)]++;
        }
        players.get(3).addHandCards(h4);
    }

    private boolean isExistsCard(Integer playerId, Integer roomId, Integer card) {
        MahJongPlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        if(card < 10){
            card -= 1;
        }else if(card < 20){
            card -= 2;
        }else if(card < 30){
            card -= 3;
        }else{
            card -= 4;
        }
        int[] h = player.getHandCards();
        return h[card] != 0;
    }

    private boolean isHandCardFull(Integer playerId, Integer roomId){
        MahJongPlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        int[] h = player.getHandCards();
        int count = 0;
        for(int k : h){
            count += k;
        }
        count += 3 * player.getChi().size();
        count += 3 * player.getPeng().size();
        count += 4 * player.getMingGang().size();
        count += 4 * player.getAnGang().size();
        return count == 14;
    }

    private String mahJongInfo(MahJongPlayerCacheBean p, MahJongRoomCacheBean r) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\r\n");
        sb.append("牌池: \r\n");
        sb.append(getCardPoolInfo(r.getCardPool()));
        sb.append("您的手牌: \r\n");
        sb.append(getHandCardsInfo(p.getHandCards()) + "\r\n");
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
    private String getHandCardsInfo(int[] h){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < h.length; i++){
            if(h[i] != 0){
                for(int j = 0; j < h[i]; i++){
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

    private boolean alreadyStart(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId).isStart();
    }

    private boolean isReady(Integer playerId, int roomId) {
        MahJongPlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);
        return player.isReady();
    }

    private boolean roomFull(int roomId) {
        return mahJongCache.getRoomByRoomId(roomId).getPlayers().size() == 4;
    }

    private boolean alreadyInRoom(int roomId, Integer playerId) {
        MahJongRoomCacheBean room = mahJongCache.getRoomByRoomId(roomId);
        List<MahJongPlayerCacheBean> players = room.getPlayers();
        for(MahJongPlayerCacheBean playerCacheBean : players){
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
