package org.george.mahjong.cache;

import org.george.mahjong.cache.bean.MahJongRoomCacheBean;
import org.george.mahjong.cache.impl.MahJongCacheImpl;

public interface MahJongCache {

    MahJongRoomCacheBean getCacheBeanByRoomId(int roomId);

    void addCacheBean(MahJongRoomCacheBean cacheBean);

    void UpdateCacheBean(MahJongRoomCacheBean cacheBean);

    void deleteCacheBean(int roomId);

    static MahJongCache getInstance(){
        return MahJongCacheImpl.getInstance();
    }
}
