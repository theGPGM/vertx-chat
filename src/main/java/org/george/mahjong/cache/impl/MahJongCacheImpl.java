package org.george.mahjong.cache.impl;

import org.george.mahjong.cache.MahJongCache;
import org.george.mahjong.cache.bean.MahJongRoomCacheBean;

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
    public MahJongRoomCacheBean getCacheBeanByRoomId(int roomId) {
        return map.get(roomId);
    }

    @Override
    public void addCacheBean(MahJongRoomCacheBean cacheBean) {
        map.put(cacheBean.getRoomId(), cacheBean);
    }

    @Override
    public void UpdateCacheBean(MahJongRoomCacheBean cacheBean) {
        map.put(cacheBean.getRoomId(), cacheBean);
    }

    @Override
    public void deleteCacheBean(int roomId) {
        map.remove(roomId);
    }
}
