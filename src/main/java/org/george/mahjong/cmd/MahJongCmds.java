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

import java.util.ArrayList;
import java.util.List;

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

    private static final String card_is_full = "您的牌已经满了，请打出一张";

    private static final String card_is_not_full = "您的牌还没有满，请摸一张牌再打";

    private static final String card_not_exists = "要打出的牌不存在";

    private static final String can_not_pass = "不能跳过";

    private static final String can_not_chi = "不能吃";

    private static final String can_not_peng = "不能碰";

    private static final String can_not_gang = "不能杠";

    private static final String can_not_hu = "无法胡";

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
                // 正常创建房间
                Integer roomId = Integer.parseInt(args[0]);
                MahJongRoomCacheBean roomCacheBean = new MahJongRoomCacheBean(roomId);
                MahJongPlayerCacheBean cacheBean = new MahJongPlayerCacheBean(playerId);
                roomCacheBean.setRoomId(roomId);
                roomCacheBean.addPlayer(cacheBean);
                // 添加牌墙
                roomCacheBean.addCardWall(MahJongUtils.shuffle());
                mahJongCache.addCacheBean(roomCacheBean);

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
                MahJongRoomCacheBean roomCacheBean = mahJongCache.getCacheBeanByRoomId(roomId);
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
                MahJongRoomCacheBean roomCacheBean = mahJongCache.getCacheBeanByRoomId(roomId);
                MahJongPlayerCacheBean cacheBean = roomCacheBean.getPlayer(playerId);
                cacheBean.setReady(true);

                int count = 0;
                for(MahJongPlayerCacheBean p : roomCacheBean.getPlayers()){
                    if(p.isReady()){
                        count++;
                    }
                }

                // 人都准备好了，开始游戏
                if(count == 4){

                    // 设置开始游戏标志
                    roomCacheBean.setStart(true);
                    // 谁来做操作
                    roomCacheBean.setWhoAct(roomCacheBean.getPlayers().get(0).getPlayerId());
                    // 设置庄家
                    roomCacheBean.setZhuang(roomCacheBean.getPlayers().get(0).getPlayerId());

                    List<MahJongPlayerCacheBean> players = roomCacheBean.getPlayers();
                    // 发牌
                    int[] h1 = new int[34];
                    int[] h2 = new int[34];
                    int[] h3 = new int[34];
                    int[] h4 = new int[34];

                    // 花牌
                    int f1 = 0;
                    int f2 = 0;
                    int f3 = 0;
                    int f4 = 0;

                    List<Integer> cards = roomCacheBean.getCardWall();
                    for(int i = 0; i < 14; i++) {
                        if(cards.get(0) == 34){
                            while(cards.get(0) == 34){
                                f1++;
                                cards.remove(0);
                            }
                        }else{
                            h1[cards.remove(0)]++;
                        }
                    }

                    for(int i = 0; i < 13; i++){
                        if(cards.get(0) == 34){
                            while(cards.get(0) == 34){
                                f2++;
                                cards.remove(0);
                            }
                        }else{
                            h2[cards.remove(0)]++;
                        }
                    }
                    for(int i = 0; i < 13; i++){
                        if(cards.get(0) == 34){
                            while(cards.get(0) == 34){
                                f3++;
                                cards.remove(0);
                            }
                        }else{
                            h3[cards.remove(0)]++;
                        }
                    }
                    for(int i = 0; i < 13; i++){
                        if(cards.get(0) == 34){
                            while(cards.get(0) == 34){
                                f4++;
                                cards.remove(0);
                            }
                        }else{
                            h4[cards.remove(0)]++;
                        }
                    }

                    // 东
                    players.get(0).addHandCards(h1);
                    players.get(0).addFlowerCard(f1);
                    // 南
                    players.get(1).addHandCards(h1);
                    players.get(1).addFlowerCard(f2);
                    // 西
                    players.get(2).addHandCards(h1);
                    players.get(2).addFlowerCard(f3);
                    // 北
                    players.get(3).addHandCards(h1);
                    players.get(3).addFlowerCard(f3);

                    for(MahJongPlayerCacheBean p : players){
                        String msg = info(p, roomCacheBean);
                        list.add(new Message("" + p.getPlayerId(), game_start));
                        list.add(new Message("" + p.getPlayerId(), msg));
                    }

                    log.info("麻将房:{}的游戏开始了", roomId);
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages getCard(String...args){
        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try {
            String userId = args[0];
            Integer playerId = Integer.parseInt(args[0]);
            if(args.length != 2) {
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(NumUtils.checkDigit(args[1])){
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
            }else if(notMyTurn(playerId, Integer.parseInt(args[1]))){
                // 不是我的回合
                list.add(new Message(userId, not_you_turn));
            }else if(isHandCardFull(playerId, Integer.parseInt(args[1]))){
                // 牌已经满了
                list.add(new Message(userId, card_is_full));
            }else{

                Integer roomId = Integer.parseInt(args[1]);

                MahJongRoomCacheBean room = mahJongCache.getCacheBeanByRoomId(roomId);
                MahJongPlayerCacheBean player = room.getPlayer(playerId);

                List<Integer> cardWall = room.getCardWall();

                // 牌没了，游戏结束
                if(cardWall.size() == 0){
                    if(room.getQuan() == 3 && room.getPan() == 3){
                        // 整局游戏结束
                    }else if(room.getPan() == 3){
                        // 某圈游戏结束
                        room.setPan(0);
                        room.setQuan(room.getQuan() + 1);
                    }else{
                        // 某盘游戏结束
                        room.setPan(room.getPan() + 1);
                    }
                }else{
                    // 摸到花补花
                    if(cardWall.get(0) == 34){
                        while(cardWall.get(0) == 34){
                            cardWall.remove(0);
                            player.addFlowerCard(1);
                        }
                    }

                    // 摸牌
                    int[] h = player.getHandCards();
                    h[cardWall.remove(0)]++;
                    List<MahJongPlayerCacheBean> players = room.getPlayers();
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
            }else if(notMyTurn(playerId, Integer.parseInt(args[1]))){
                // 不是我的回合
                list.add(new Message(userId, not_you_turn));
            }else if(!isHandCardFull(playerId, Integer.parseInt(args[1]))){
                // 牌未满
                list.add(new Message(userId, card_is_not_full));
            }else if(!isExistsCard(playerId, Integer.parseInt(args[1]), Integer.parseInt(args[2]))){
                // 牌不存在
                list.add(new Message(userId, card_not_exists));
            } else{

                Integer roomId = Integer.parseInt(args[1]);
                Integer card = Integer.parseInt(args[2]);
                MahJongRoomCacheBean room = mahJongCache.getCacheBeanByRoomId(roomId);
                MahJongPlayerCacheBean player = room.getPlayer(playerId);
                int[] handCards = player.getHandCards();
                handCards[card]--;

                for(MahJongPlayerCacheBean p : room.getPlayers()){
                    if(p.getPlayerId().equals(playerId)){
                        String msg = info(player, room);
                        list.add(new Message(userId, msg));
                    }else{
                        StringBuilder sb = new StringBuilder();
                        sb.append("=================================\r\n");
                        sb.append("玩家[" + playerModel.getPlayerByPlayerId(playerId).getPlayerName() + "]打出了" + getCardInfo(card) + "\r\n");
                        sb.append("=================================");
                        list.add(new Message("" + p.getPlayerId(), sb.toString()));
                    }
                }

                int index;
                for(index = 0; index < 4; index++){
                    if(room.getPlayers().get(index).getPlayerId().equals(playerId)){
                        break;
                    }
                }
                String msg = info(player, room);
                list.add(new Message(userId, msg));
                room.setWhoAct((index + 1) % 4);

                room.setPassCount(0);
                room.setPlayCard(card);
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
            }else if(notMyTurn(playerId, Integer.parseInt(args[1]))){
                // 不是我的回合
                list.add(new Message(userId, not_you_turn));
            }else if(!canPass(playerId, Integer.parseInt(args[1]))){
                // 牌未满
                list.add(new Message(userId, card_is_full));
            }else{

                Integer roomId = Integer.parseInt(args[1]);
                Integer card = Integer.parseInt(args[2]);
                MahJongRoomCacheBean room = mahJongCache.getCacheBeanByRoomId(roomId);
                MahJongPlayerCacheBean player = room.getPlayer(playerId);
                int[] handCards = player.getHandCards();
                handCards[card]--;

                for(MahJongPlayerCacheBean p : room.getPlayers()){
                    if(p.getPlayerId().equals(playerId)){
                        String msg = info(player, room);
                        list.add(new Message(userId, msg));
                    }else{
                        StringBuilder sb = new StringBuilder();
                        sb.append("=================================\r\n");
                        sb.append("玩家[" + playerModel.getPlayerByPlayerId(playerId).getPlayerName() + "]打出了" + getCardInfo(card) + "\r\n");
                        sb.append("=================================");
                        list.add(new Message("" + p.getPlayerId(), sb.toString()));
                    }
                }

                int index;
                for(index = 0; index < 4; index++){
                    if(room.getPlayers().get(index).getPlayerId().equals(playerId)){
                        break;
                    }
                }
                String msg = info(player, room);
                list.add(new Message(userId, msg));
                room.setWhoAct((index + 1) % 4);
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
                MahJongRoomCacheBean roomCacheBean = mahJongCache.getCacheBeanByRoomId(roomId);
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
                MahJongRoomCacheBean roomCacheBean = mahJongCache.getCacheBeanByRoomId(roomId);
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
                MahJongRoomCacheBean roomCacheBean = mahJongCache.getCacheBeanByRoomId(roomId);
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

    private boolean isExistsCard(Integer playerId, Integer roomId, Integer card) {
        MahJongPlayerCacheBean player = mahJongCache.getCacheBeanByRoomId(roomId).getPlayer(playerId);

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

    /**
     * 判断当前是否是我的回合
     * @param playerId
     * @param roomId
     * @return
     */
    private boolean notMyTurn(Integer playerId, Integer roomId) {
        return !playerId.equals(mahJongCache.getCacheBeanByRoomId(roomId).getWhoAct());
    }

    private boolean isHandCardFull(Integer playerId, Integer roomId){
        MahJongPlayerCacheBean player = mahJongCache.getCacheBeanByRoomId(roomId).getPlayer(playerId);
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

    private String info(MahJongPlayerCacheBean p, MahJongRoomCacheBean r) {
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
        return mahJongCache.getCacheBeanByRoomId(roomId).isStart();
    }

    private boolean isReady(Integer playerId, int roomId) {
        MahJongRoomCacheBean cacheBean = mahJongCache.getCacheBeanByRoomId(roomId);
        MahJongPlayerCacheBean player = cacheBean.getPlayer(playerId);
        return player.isReady();
    }

    private boolean roomFull(int roomId) {
        return mahJongCache.getCacheBeanByRoomId(roomId).getPlayers().size() == 4;
    }

    private boolean alreadyInRoom(int roomId, Integer playerId) {
        MahJongRoomCacheBean room = mahJongCache.getCacheBeanByRoomId(roomId);
        List<MahJongPlayerCacheBean> players = room.getPlayers();
        for(MahJongPlayerCacheBean playerCacheBean : players){
            if(playerCacheBean.getPlayerId().equals(playerId)){
                return true;
            }
        }
        return false;
    }

    private boolean roomExists(int roomId) {
        return mahJongCache.getCacheBeanByRoomId(roomId) != null;
    }
}
