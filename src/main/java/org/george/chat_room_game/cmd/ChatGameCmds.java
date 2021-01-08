package org.george.chat_room_game.cmd;


import org.george.chat.model.ChatRoomModel;
import org.george.hall.model.PlayerModel;
import org.george.pojo.Message;
import org.george.pojo.Messages;
import org.george.chat_room_game.cache.GameCache;
import org.george.chat_room_game.cache.impl.GameCacheImpl;
import org.george.util.NumUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatGameCmds {

    private ChatRoomModel chatRoomModel = ChatRoomModel.getInstance();

    private GameCache gameCache = GameCacheImpl.getInstance();

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

    public Messages createGame(String...args){

        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 2) {
            Message msg = new Message(userId, input_format_error);
            list.add(msg);
            return new Messages(list);
        }

        String roomId = args[1];
        if(!chatRoomModel.existChatRoom(args[1])){
            Message msg = new Message(userId, room_not_exists);
            list.add(msg);
        }
        else if(gameCache.gameExists(roomId)){
            Message msg = new Message(userId, game_in_the_room);
            list.add(msg);
        } else if(chatRoomModel.getRoomUsers(roomId).size() < 2){
            list.add(new Message(userId, room_member_not_enough));
        }
        else{
            // 在这段时间内，不能在该房间内发起游戏
            gameCache.createGame(roomId, userId, expired_second);
            // 设置等待期，等待玩家输入结果
            gameCache.addWaitingTime(roomId, get_result_waiting_second);

            List<String> users = chatRoomModel.getRoomUsers(roomId);
            for(String uId : users){
                StringBuilder sb = new StringBuilder();
                sb.append("===================================================\r\n");
                if(!uId.equals(userId)){
                    sb.append("用户[" + playerModel.getPlayerNameByPlayerId(userId) + "]在房间[" + roomId +"]中发起了一场猜拳游戏\r\n");
                }else{
                    sb.append("您在房间[" + roomId +"]中发起了一场猜拳游戏\r\n");
                }
                sb.append(rules);
                sb.append("\r\n");
                sb.append("===================================================");
                list.add(new Message(uId, sb.toString()));
            }
        }
        return new Messages(list);
    }

    public Messages play(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 3){
            list.add(new Message(userId, input_format_error));
            return new Messages(list);
        }

        String roomId = args[1];
        String action = args[2];
        if(!NumUtils.checkDigit(action)){
            // 输入格式错误
            list.add(new Message(userId, input_format_error));
        }else if(Integer.parseInt(action) > 2 || Integer.parseInt(action) < 0){
            // 输入不是石头、剪刀、布
            list.add(new Message(userId, input_format_error));
        }else if(!gameCache.gameExists(roomId)){
            // 当前房间中没有发起游戏
            list.add(new Message(userId, room_not_in_game));
        } else if(gameCache.getUserAction(roomId, userId) != null){
            // 禁止两次输入
            list.add(new Message(userId, forbidden_second_input));
        } else{

            List<String> players = chatRoomModel.getRoomUsers(roomId);
            boolean flag = false;
            for(String p : players){
                if(p.equals(userId)){
                    flag = true;
                }
            }

            // 确认玩家在该房间中，如果不在，不能参加游戏
            if(!flag){
                list.add(new Message(userId, not_in_the_room));
            }else{
                switch (Integer.parseInt(action)){
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
                gameCache.addUserAction(roomId, userId, action, expired_second);
            }
        }
        return new Messages(list);
    }

    public Messages getResult(String...args){
        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 2){
            list.add(new Message(userId, input_format_error));
            return new Messages(list);
        }else{

            String roomId = args[1];
            if(!gameCache.gameExists(roomId)){
                list.add(new Message(userId, room_not_in_game));
            }else if(gameCache.existsWaitingTime(roomId)){
                list.add(new Message(userId, waiting_player_input));
            } else{
                boolean flag = false;
                Map<String, String> allUserAction = gameCache.getAllUserAction(roomId);
                for(String p : allUserAction.keySet()){
                    if(p.equals(userId)){
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    list.add(new Message(userId, not_in_the_game));
                }else{
                    if(allUserAction.size() < 2){
                        list.add(new Message(userId, room_member_not_enough));
                    }else{
                        for(Message msg : settle(roomId)){
                            list.add(msg);
                        }
                    }
                    gameCache.clearCache(roomId);
                }
            }
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
}
