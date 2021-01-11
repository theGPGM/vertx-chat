package org.george.chat.cmd;


import org.george.chat.cache.RoomCache;
import org.george.chat.util.JedisPool;
import org.george.chat.util.NumUtils;
import org.george.chat.util.ThreadLocalJedisUtils;
import org.george.hall.model.PlayerModel;
import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;
import org.george.chat.cache.GameCache;
import org.george.hall.model.pojo.PlayerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatGameCmds {

    private GameCache gameCache = GameCache.getInstance();

    private RoomCache roomCache = RoomCache.getInstance();

    private static PlayerModel playerModel = PlayerModel.getInstance();

    private static final String input_format_error = "输入格式错误";

    private static final String choose_rock = "您选择了石头，正在等待其他玩家输入，稍等一会可以使用 [get_result:房间 ID] 获取结果";

    private static final String choose_scissor = "您选择了剪刀，正在等待其他玩家输入，稍等一会可以使用 [get_result:房间 ID] 获取结果";

    private static final String choose_paper = "您选择了布，正在等待其他玩家输入，稍等一会可以使用 [get_result:房间 ID] 获取结果";

    private static final String forbidden_second_input = "禁止二次输入";

    private static final String rules = new StringBuilder()
            .append("您可以使用 play_rgame:[房间 ID]:[0、1、2] 来发出石头、剪刀、布加入房中正在进行的比赛")
            .toString();

    private static final String game_in_the_room = "当前房间正在游戏中，请稍候";

    private static final String room_not_in_game = "房间中未发起游戏";

    private static final String room_not_exists = "当前房间不存在";

    private static final String not_in_the_room = "不在房间内";

    private static final String not_in_the_game = "未参与游戏";

    private static final String room_member_not_enough = "当前房间中玩家不够";

    private static final String not_enough_player = "当前房间中参与游戏的玩家不够";

    private static final String waiting_player_input = "需要一段时间的等待后才能获得游戏结果，请稍候";

    private static final Integer expired_second = 600;

    private static final Integer get_result_waiting_second = 30;

    private Logger logger = LoggerFactory.getLogger(ChatGameCmds.class);

    public Messages createGame(String...args){


        String userId = args[0];
        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            if(args.length != 2) {
                list.add(new Message(userId, input_format_error));
            } else if(!inRoom(userId, args[1])){
                list.add(new Message(userId, not_in_the_room));
            } else if(!roomCache.existsRoom(args[1])){
                list.add(new Message(userId, room_not_exists));
            } else if(gameCache.gameExists(args[1])){
                list.add(new Message(userId, game_in_the_room));
            } else if(roomCache.getAllUserId(args[1]).size() < 2){
                list.add(new Message(userId, room_member_not_enough));
            } else{
                String roomId = args[1];
                // 设置等待期，等待玩家输入结果
                // 在等待期不能获取游戏结果
                gameCache.addWaitingTime(roomId, get_result_waiting_second);

                // 设置游戏过期时间，超过时间无法进行游戏
                // 在这段时间内，不能在该房间内发起游戏
                gameCache.createGame(roomId, userId, expired_second);

                List<String> users = roomCache.getAllUserId(roomId);
                PlayerResult player = playerModel.getPlayerByPlayerId(Integer.parseInt(userId));
                for(String uId : users){
                    StringBuilder sb = new StringBuilder();
                    sb.append("===================================================\r\n");
                    if(!uId.equals(userId)){
                        sb.append("用户[" + player.getPlayerName() + "]在房间[" + roomId +"]中发起了一场猜拳游戏\r\n");
                    }else{
                        sb.append("您在房间[" + roomId +"]中发起了一场猜拳游戏\r\n");
                    }
                    sb.append(rules);
                    sb.append("\r\n");
                    sb.append("===================================================");
                    list.add(new Message(uId, sb.toString()));
                }

                logger.info("用户:{} 在房间:[{}]发起一场猜拳游戏", userId, roomId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages play(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            if(args.length != 3){
                list.add(new Message(userId, input_format_error));
            }else if(!roomCache.existsRoom(args[1])){
                // 房间不存在
                list.add(new Message(userId, room_not_exists));
            } else if(!inRoom(userId, args[1])){
                // 不在房间中
                list.add(new Message(userId, not_in_the_room));
            } else if(!NumUtils.checkDigit(args[2])){
                // 输入格式错误
                list.add(new Message(userId, input_format_error));
            }else if(Integer.parseInt(args[2]) > 2 || Integer.parseInt(args[2]) < 0){
                // 输入不是石头、剪刀、布
                list.add(new Message(userId, input_format_error));
            }else if(!gameCache.gameExists(args[1])){
                // 当前房间中没有发起游戏
                list.add(new Message(userId, room_not_in_game));
            } else if(gameCache.getUserAction(args[1], userId) != null){
                // 禁止两次输入
                list.add(new Message(userId, forbidden_second_input));
            }  else{

                String roomId = args[1];
                Integer action = Integer.parseInt(args[2]);

                switch (action){
                    case 0 :{
                        list.add(new Message(userId, choose_rock));
                        break;
                    }
                    case 1 :{
                        list.add(new Message(userId, choose_scissor));
                        break;
                    }
                    case 2 :{
                        list.add(new Message(userId, choose_paper));
                        break;
                    }
                }
                gameCache.addGameUser(roomId, userId);
                gameCache.addUserAction(roomId, userId, "" + action, expired_second);

                logger.info("用户:{}参加房间:[{}]中的猜拳游戏", userId, roomId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages getResult(String...args){
        List<Message> list = new ArrayList<>();
        String userId = args[0];
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            if(args.length != 2){
                list.add(new Message(userId, input_format_error));
            } else if(!roomCache.existsRoom(args[1])){
                // 房间不存在
                list.add(new Message(userId, room_not_exists));
            } else if(!gameCache.gameExists(args[1])){
                // 未发起游戏
                list.add(new Message(userId, room_not_in_game));
            } else if(!inRoom(userId, args[1])){
                // 不在房间中
                list.add(new Message(userId, not_in_the_room));
            } else if(!isTakePartInGame(userId, args[1])){
                // 未参加游戏
                list.add(new Message(userId, not_in_the_game));
            } else if(gameCache.existsWaitingTime(args[1])){
                // 等待时间没到
                list.add(new Message(userId, waiting_player_input));
            } else if(gameCache.getAllUserAction(args[1]).size() < 2){
                // 参与人数过少
                list.add(new Message(userId, room_member_not_enough));
                gameCache.clearCache(args[1]);
            } else{
                String roomId = args[1];
                for(Message msg : settle(roomId)){
                    list.add(msg);
                }
                gameCache.clearCache(args[1]);
                logger.info("获取在房间:[{}]中的游戏结果", roomId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private List<Message> settle(String roomId){
        List<Message> list  = new ArrayList<>();

        Map<String, String> allUserAction = gameCache.getAllUserAction(roomId);

        if(allUserAction.size() < 2){
           for(String player : allUserAction.keySet()){
               list.add(new Message(player, not_enough_player));
           }
           return list;
        }

        Map<Integer, List<String>> map = new HashMap<>();
        map.put(0, new ArrayList<>());
        map.put(1, new ArrayList<>());
        map.put(2, new ArrayList<>());

        for(Map.Entry<String, String> entry : allUserAction.entrySet()){
            if(entry.getValue().equals("0")){
                map.get(0).add(entry.getKey());
            }else if(entry.getValue().equals("1")){
                map.get(1).add(entry.getKey());
            }else{
                map.get(2).add(entry.getKey());
            }
        }

        List<String> winners = new ArrayList<>();
        List<String> losers = new ArrayList<>();
        List<String> draws = new ArrayList<>();

        // 有胜负产生
        if(map.get(0).size() * map.get(1).size() * map.get(2).size() == 0){

            // 找出胜者和败者
            // 石头和剪刀
            if(map.get(0).size() * map.get(1).size() != 0){
                // 赢了
                for(String user : map.get(0)){
                    winners.add(user);
                }
                // 输了
                for(String user : map.get(1)){
                    losers.add(user);
                }
            }
            // 石头和布
            else if(map.get(0).size() * map.get(2).size() != 0){
                // 赢了
                for(String user : map.get(2)){
                    winners.add(user);
                }
                // 输了
                for(String user : map.get(0)){
                    losers.add(user);
                }
            }
            // 剪刀和布
            else if(map.get(1).size() * map.get(2).size() != 0){
                // 赢了
                for(String user : map.get(1)){
                    winners.add(user);
                }
                // 输了
                for(String user : map.get(2)){
                    losers.add(user);
                }
            }else{
                // 平局
                for(List<String> users : map.values()){
                    for(String user : users){
                        draws.add(user);
                    }
                }
            }
        }else{
            // 平局
            for(List<String> users : map.values()){
                for(String user : users){
                    draws.add(user);
                }
            }
        }

        // 发送游戏结果
        if(draws.size() != 0){
            for(String user : draws){
                list.add(new Message( user, "您在房间["+ roomId +"]中的游戏平局了"));
            }
        }else{
            for (String user : winners){
                list.add(new Message(user, "您赢了在房间["+ roomId +"]中的游戏"));
            }
            for (String user : losers){
                list.add(new Message(user, "您输了在房间["+ roomId +"]中的游戏"));
            }
        }
        return list;
    }

    private boolean inRoom(String userId, String roomId){
        List<String> users = roomCache.getAllUserId(roomId);
        for(String uId : users){
            if(uId.equals(userId)){
                return true;
            }
        }
        return false;
    }

    private boolean isTakePartInGame(String userId, String roomId){
        Map<String, String> allUserAction = gameCache.getAllUserAction(roomId);
        for(String p : allUserAction.keySet()){
            if(p.equals(userId)){
                return true;
            }
        }
        return false;
    }
}
