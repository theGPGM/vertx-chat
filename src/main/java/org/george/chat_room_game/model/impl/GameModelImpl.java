package org.george.chat_room_game.model.impl;

import org.george.chat_room_game.model.GameModel;
import org.george.common.pojo.Message;
import org.george.chat_room_game.cache.GameCache;
import org.george.chat_room_game.cache.impl.GameCacheImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameModelImpl implements GameModel {

    private GameCache gameCache = GameCacheImpl.getInstance();

    private GameModelImpl(){};

    private static GameModelImpl instance = new GameModelImpl();

    public static GameModelImpl getInstance(){
        return instance;
    }

    /**
     * 处理游戏结果
     * @param roomId
     */
    @Override
    public List<Message> settle(String roomId){

        List<Message> list  = new ArrayList<>();

        Map<String, String> allUserAction = gameCache.getAllUserAction(roomId);

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

        gameCache.removeRoom(roomId);
        return list;
    }

    @Override
    public Integer getPlayerNum(String roomId) {
        return gameCache.getPlayUserList(roomId).size();
    }
}
