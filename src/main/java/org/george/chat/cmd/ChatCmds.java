package org.george.chat.cmd;


import org.george.chat.cache.RoomCache;
import org.george.pojo.Message;
import org.george.pojo.Messages;
import org.george.hall.model.PlayerModel;
import org.george.util.NumUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatCmds {

    private RoomCache roomCache = RoomCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private static final String in_chat_room = "已在房间中";

    private static final String not_in_chat_room = "不在房间中";

    private static final String chat_room_exists = "房间已存在";

    private static final String chat_room_not_exists = "房间不存在";

    private static final String quit_chat_room_success = "退出房间成功";

    private static final String create_chat_room_format_error = "房间名只能由数字组成，当前房间名不符合命名规范";

    private static final String create_chat_room_success = new StringBuilder()
            .append("=============================================\r\n")
            .append("++++创建房间成功++++\r\n")
            .append("欢迎进入聊天室，你可以使用以下的命令：\r\n")
            .append("=============================================\r\n")
            .append("[chat:房间 ID:消息[只支持英文]]:发送群聊消息\r\n")
            .append("[start_rgame:房间 ID]:在聊天室中发起猜拳游戏\r\n")
            .append("[play_rgame:房间 ID:[0、1、2[石头、剪刀、布]]]:参与猜拳游戏\r\n")
            .append("[get_result:房间 ID]:获取猜拳结果\r\n")
            .append("[exit_room:房间 ID]:退出聊天室\r\n")
            .append("=============================================").toString();

    private static final String join_chat_room_success =  new StringBuilder()
            .append("=============================================\r\n")
            .append("++++加入房间成功++++\r\n")
            .append("欢迎进入聊天室，你可以使用以下的命令：\r\n")
            .append("=============================================\r\n")
            .append("[chat:房间 ID:消息[只支持英文]]:发送群聊消息\r\n")
            .append("[start_rgame:房间 ID]:在聊天室中发起猜拳游戏\r\n")
            .append("[play_rgame:房间 ID:[0、1、2[石头、剪刀、布]]]:参与猜拳游戏\r\n")
            .append("[get_result:房间 ID]:获取猜拳结果\r\n")
            .append("[exit_room:房间 ID]:退出聊天室\r\n")
            .append("=============================================").toString();

    private static final String input_format_error = "输入格式错误";

    public Messages createChatRoom(String...args){

        List<Message> list = new ArrayList<>();
        Message msg = null;

        String userId = args[0];
        if(args.length != 2){
            msg = new Message(userId, input_format_error);
        }

        String roomId = args[1];
        if(roomCache.existsRoom(roomId)){
            msg = new Message(userId, chat_room_exists);
            list.add(msg);
        }
        else if(!NumUtils.checkDigit(roomId)){
            msg = new Message(userId, create_chat_room_format_error);
            list.add(msg);
        }
        else{
            list.add(new Message(userId, create_chat_room_success));
            roomCache.join(roomId, userId);
        }
        return new Messages(list);
    }

    public Messages joinChatRoom(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 2){
            Message msg = new Message(userId, input_format_error);
            list.add(msg);
        }

        String roomId = args[1];
        if(!roomCache.existsRoom(roomId)){
            Message msg = new Message(userId, chat_room_not_exists);
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
                Message msg = new Message(userId, in_chat_room);
                list.add(msg);
            }else{
                roomCache.join(roomId, userId);

                for (String uId : roomCache.getAllUserId(roomId)){
                    if(!uId.equals(userId)){
                        StringBuilder sb = new StringBuilder();
                        sb.append("======================================================================\r\n");
                        sb.append("[" + playerModel.getPlayerNameByPlayerId(userId) +"]加入房间["+ roomId +"]中\r\n");
                        sb.append("======================================================================");
                        list.add(new Message(uId, sb.toString()));
                    }
                }
                list.add(new Message(userId, join_chat_room_success));
            }
        }
        return new Messages(list);
    }

    public Messages exitRoom(String...args){
        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 2){
            Message msg =  new Message(userId, input_format_error);
            list.add(msg);
        }
        else if(!roomCache.existsRoom(args[1])){
            Message msg =  new Message(userId, chat_room_not_exists);
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
                Message msg =  new Message(userId, not_in_chat_room);
                list.add(msg);
            }else{

                for (String uId : roomCache.getAllUserId(args[1])){
                    if(!uId.equals(userId)){
                        StringBuilder sb = new StringBuilder();
                        sb.append("======================================================================\r\n");
                        sb.append("["+ playerModel.getPlayerNameByPlayerId(userId) +"]退出房间["+ args[1] +"]\r\n" );
                        sb.append("======================================================================");
                        list.add(new Message(uId, sb.toString()));
                    }
                }

                Message msg =  new Message(userId, quit_chat_room_success);
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
            list.add(new Message(userId,input_format_error));
        }
        else if(!roomCache.existsRoom(args[1])) {
            list.add(new Message(userId,chat_room_not_exists ));
        }else{
            boolean flag = false;
            for(String uId : roomCache.getAllUserId(args[1])){
                if(uId.equals(userId)){
                    flag = true;
                }
            }

            if(!flag){
                list.add(new Message(userId,not_in_chat_room ));
            }else{
                for(String uId : roomCache.getAllUserId(args[1])){
                    StringBuilder sb = new StringBuilder();
                    sb.append("======================================================================\r\n");
                    if(!uId.equals(userId)){
                        sb.append("["+ playerModel.getPlayerNameByPlayerId(userId) +"]在房间["+ args[1] +"]说：" + args[2] + "\r\n" );
                    }else{
                        sb.append("您在房间["+ args[1] +"]说：" + args[2] + "\r\n" );
                    }
                    sb.append("======================================================================");
                    list.add(new Message(uId, sb.toString()));
                }
            }
        }
        return new Messages(list);
    }
}
