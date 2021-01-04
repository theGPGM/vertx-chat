package org.george.chat.cmd;


import org.george.chat.cache.RoomCache;
import org.george.pojo.Message;
import org.george.pojo.Messages;
import org.george.hall.model.PlayerModel;

import java.util.ArrayList;
import java.util.List;

public class ChatCMDs {

    private RoomCache roomCache = RoomCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    public Messages createChatRoom(String...args){

        List<Message> list = new ArrayList<>();
        Message msg = null;

        String userId = args[0];
        if(args.length != 2){
            msg = new Message(userId, "输入格式错误");
        }

        String roomId = args[1];
        if(roomCache.existsRoom(roomId)){
            msg = new Message(userId, "当前房间已存在");
        }
        else if(!checkDigit(roomId)){
            msg = new Message(userId, "房间名只能由数字组成，当前房间名不符合命名规范");
        }
        else{
            roomCache.join(roomId, userId);
            msg = new Message(userId, "房间创建成功");
        }

        list.add(msg);
        return new Messages(list);
    }

    public Messages joinChatRoom(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 2){
            Message msg = new Message(userId, "输入格式错误");
            list.add(msg);
        }

        String roomId = args[1];
        if(!roomCache.existsRoom(roomId)){
            Message msg = new Message(userId, "当前房间不存在");
            list.add(msg);
        }
        else{
            boolean flag = false;
            for(String uId : roomCache.getAllUserId(roomId)){
                if(uId.equals(userId)){
                    flag = true;
                }
            }
            if(flag){
                Message msg = new Message(userId, "用户已存在房间中");
                list.add(msg);
            }else{
                roomCache.join(roomId, userId);

                for (String uId : roomCache.getAllUserId(roomId)){
                    if(!uId.equals(userId)){
                        Message msg = new Message(uId, "用户[" + playerModel.getPlayerNameByPlayerId(userId) +"]加入房间["+ roomId +"]中");
                        list.add(msg);
                    }
                }
                Message msg =  new Message(userId, "加入房间成功");
                list.add(msg);
            }
        }
        return new Messages(list);
    }

    public Messages exitRoom(String...args){
        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 2){
            Message msg =  new Message(userId, "输入格式错误");
            list.add(msg);
        }
        else if(!roomCache.existsRoom(args[1])){
            Message msg =  new Message(userId, "当前房间不存在");
            list.add(msg);
        }
        else{
            boolean flag = true;
            for(String uId : roomCache.getAllUserId(args[1])){
                if(uId.equals(userId)){
                    flag = false;
                }
            }

            if(flag){
                Message msg =  new Message(userId, "用户不在房间中");
                list.add(msg);
            }else{

                for (String uId : roomCache.getAllUserId(args[1])){
                    if(!uId.equals(userId)){
                        Message msg = new Message(uId, "用户[" + playerModel.getPlayerNameByPlayerId(userId) +"]退出房间["+ args[1] +"]");
                        list.add(msg);
                    }
                }

                Message msg =  new Message(userId, "用户退出房间成功");
                list.add(msg);
                roomCache.clearUserRoomCache(userId, args[1]);
            }
        }
        return new Messages(list);
    }

    public Messages chat(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 3){
            list.add(new Message(userId,"输入格式错误" ));
        }
        else if(!roomCache.existsRoom(args[1])) {
            list.add(new Message(userId,"当前房间不存在" ));
        }else{
            boolean flag = false;
            for(String uId : roomCache.getAllUserId(args[1])){
                if(uId.equals(userId)){
                    flag = true;
                }
            }

            if(!flag){
                list.add(new Message(userId,"您不在房间中" ));
            }else{
                for(String uId : roomCache.getAllUserId(args[1])){
                    if(!uId.equals(userId)){
                        list.add(new Message(uId, "用户["+ playerModel.getPlayerNameByPlayerId(userId) +"]在房间["+ args[1] +"]说：" + args[2]));
                    }
                }
            }
        }
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
