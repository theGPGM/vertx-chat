package org.george.mahjong.cache.impl;

import org.george.mahjong.cache.MahJongCache;
import org.george.mahjong.cache.bean.PlayerCacheBean;
import org.george.mahjong.cache.bean.RoomCacheBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MahJongCacheImpl implements MahJongCache {

    private Map<Integer, RoomCacheBean> map = new ConcurrentHashMap<>();

    private MahJongCacheImpl(){}

    private static MahJongCacheImpl instance = new MahJongCacheImpl();

    public static MahJongCacheImpl getInstance(){
        return instance;
    }

    @Override
    public void addPlayer(Integer roomId, PlayerCacheBean player) {
        List<PlayerCacheBean> list = map.get(roomId).getList();
        list.add(player);
    }

    @Override
    public Integer getPlayerIndex(Integer roomId, Integer playerId) {
        List<PlayerCacheBean> list = map.get(roomId).getList();
        for(int i = 0; i < 4; i++){
            if(list.get(i).getPlayerId().equals(playerId)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public PlayerCacheBean getPlayerByIndex(Integer roomId, Integer index) {
        List<PlayerCacheBean> list = map.get(roomId).getList();
        return list.get(index);
    }

    @Override
    public PlayerCacheBean getPlayerByPlayerId(Integer roomId, Integer playerId) {
        List<PlayerCacheBean> list = map.get(roomId).getList();
        for(int i = 0; i < 4; i++){
            if(list.get(i).getPlayerId().equals(playerId)){
                return list.get(i);
            }
        }
        return null;
    }

    @Override
    public List<PlayerCacheBean> getAllPlayer(Integer roomId) {
        return map.get(roomId).getList();
    }

    @Override
    public PlayerCacheBean getZhuang(Integer roomId) {
        return map.get(roomId).getList().get(0);
    }

    @Override
    public void changZhuang(Integer roomId) {
        List<PlayerCacheBean> list = map.get(roomId).getList();
        PlayerCacheBean e = list.remove(0);
        list.add(e);
    }

    @Override
    public RoomCacheBean getRoomByRoomId(int roomId) {
        return map.get(roomId);
    }

    @Override
    public void addCacheBean(RoomCacheBean cacheBean) {
        map.put(cacheBean.getRoomId(), cacheBean);
    }

    @Override
    public void deleteCacheBean(int roomId) {
        map.remove(roomId);
    }
}
