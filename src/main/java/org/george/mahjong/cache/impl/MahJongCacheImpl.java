package org.george.mahjong.cache.impl;

import org.george.mahjong.cache.MahJongCache;
import org.george.mahjong.cache.bean.MahJongPlayerCacheBean;
import org.george.mahjong.cache.bean.MahJongRoomCacheBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MahJongCacheImpl implements MahJongCache {

    private Map<Integer, MahJongRoomCacheBean> map = new ConcurrentHashMap<>();

    private MahJongCacheImpl(){}

    private static MahJongCacheImpl instance = new MahJongCacheImpl();

    public static MahJongCacheImpl getInstance(){
        return instance;
    }

    @Override
    public void addPlayer(Integer roomId, MahJongPlayerCacheBean player) {
        List<MahJongPlayerCacheBean> list = map.get(roomId).getList();
        list.add(player);
    }

    @Override
    public Integer getPlayerIndex(Integer roomId, Integer playerId) {
        List<MahJongPlayerCacheBean> list = map.get(roomId).getList();
        for(int i = 0; i < 4; i++){
            if(list.get(i).equals(playerId)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public MahJongPlayerCacheBean getPlayerByIndex(Integer roomId, Integer index) {
        List<MahJongPlayerCacheBean> list = map.get(roomId).getList();
        return list.get(index);
    }

    @Override
    public MahJongPlayerCacheBean getPlayerByPlayerId(Integer roomId, Integer playerId) {
        List<MahJongPlayerCacheBean> list = map.get(roomId).getList();
        for(int i = 0; i < 4; i++){
            if(list.get(i).equals(playerId)){
                return list.get(i);
            }
        }
        return null;
    }

    @Override
    public MahJongRoomCacheBean getRoomByRoomId(int roomId) {
        return map.get(roomId);
    }

    @Override
    public void addCacheBean(MahJongRoomCacheBean cacheBean) {
        map.put(cacheBean.getRoomId(), cacheBean);
    }

    @Override
    public void deleteCacheBean(int roomId) {
        map.remove(roomId);
    }
}
