package org.george.chat_room_game.cmd;


import org.george.chat.model.ChatRoomModel;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;
import org.george.chat_room_game.cache.GameCache;
import org.george.chat_room_game.cache.impl.GameCacheImpl;

import java.util.ArrayList;
import java.util.List;

public class ChatGameCMDs {

    private ChatRoomModel chatRoomModel = ChatRoomModel.getInstance();

    private GameCache gameCache = GameCacheImpl.getInstance();

    public Messages createGame(String...args){

        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 2) {
            Message msg = new Message(userId, "输入格式错误");
            list.add(msg);
            return new Messages(list);
        }

        String roomId = args[1];
        if(!chatRoomModel.existChatRoom(args[1])){
            Message msg = new Message(userId, "当前房间不存在");
            list.add(msg);
        }
        else if(gameCache.gameExists(roomId)){
            Message msg = new Message(userId, "当前房间正在游戏中，请稍候");
            list.add(msg);
        }
        else{
            gameCache.addGameUser(roomId, userId);

            List<String> users = chatRoomModel.getRoomUsers(roomId);
            for(String uId : users){
                StringBuilder sb = new StringBuilder();
                if(!uId.equals(userId)){
                    sb.append("用户在[" + roomId +"]发起了一场猜拳游戏\r\n");
                }
                sb.append("您可以使用 play:[0、1、2] 来发出石头、剪刀、布");
                list.add(new Message(uId, sb.toString()));
            }
        }
        return new Messages(list);
    }

    public Messages play(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 3){
            list.add(new Message(userId, "输入格式错误"));
            return new Messages(list);
        }

        String roomId = args[1];
        String action = args[2];
        if(!checkDigit(action)){
            list.add(new Message(userId, "输入指令错误"));
        }else if(Integer.parseInt(action) > 2 || Integer.parseInt(action) < 0){
            list.add(new Message(userId, "输入指令错误"));
        }else if(gameCache.getUserAction(roomId, userId) != null){
            list.add(new Message(userId, "您已经输入了指令，请不要重复输入"));
        }else{
            switch (Integer.parseInt(action)){
                case 0 :{
                    list.add(new Message(userId, "您选择了石头，正在等待其他玩家输入"));
                    break;
                }
                case 1 :{
                    list.add(new Message(userId, "您选择了剪刀，正在等待其他玩家输入"));
                    break;
                }
                case 2 :{
                    list.add(new Message(userId, "您选择了布，正在等待其他玩家输入"));
                    break;
                }
            }
        }
        gameCache.addGameUser(roomId, userId);
        gameCache.addUserAction(roomId, userId, action);
        return new Messages(list);
    }

    private boolean checkDigit(String roomId){
        char[] arr = roomId.toCharArray();
        for(char c : arr){
            if(c > '9' || c < '0') return false;
        }
        return true;
    }
}
