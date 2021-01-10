package org.george.auction.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.auction.cache.AuctionCache;
import org.george.auction.cache.bean.AuctionCacheBean;
import org.george.auction.dao.AuctionDao;
import org.george.auction.dao.bean.AuctionBean;
import org.george.auction.util.CalendarUtils;
import org.george.auction.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuctionCacheImpl implements AuctionCache {

    private AuctionCacheImpl(){}

    private static AuctionCacheImpl instance = new AuctionCacheImpl();

    public static AuctionCacheImpl getInstance(){
        return instance;
    }

    private AuctionDao auctionDao = AuctionDao.getInstance();

    @Override
    public List<AuctionCacheBean> getAuctions() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        if(!jedis.exists("auctions")){
            List<AuctionCacheBean> list = new ArrayList<>();
            List<AuctionBean> beans = auctionDao.getAuctions();
            for(AuctionBean bean : beans){
                AuctionCacheBean cacheBean = new AuctionCacheBean();
                cacheBean.setAuctionId(bean.getAuctionId());
                cacheBean.setAuctionType(bean.getAuctionType());
                cacheBean.setCost(bean.getCost());
                cacheBean.setDeductionType(bean.getDeductionType());
                cacheBean.setNum(bean.getNum());

                list.add(cacheBean);
            }

            // 添加缓存
            try {
                String json = objectMapper.writeValueAsString(list);
                jedis.set("auctions", json);
                return list;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }else{
            String json = jedis.get("auctions");
            try {
                return objectMapper.readValue(json, new TypeReference<List<AuctionCacheBean>>(){});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void batchUpdateSelective(List<AuctionCacheBean> list) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ArrayList<AuctionBean> beans = new ArrayList<>();
        for(AuctionCacheBean cacheBean : list){
            AuctionBean bean = new AuctionBean();
            bean.setAuctionId(cacheBean.getAuctionId());
            bean.setAuctionType(cacheBean.getAuctionType());
            bean.setCost(cacheBean.getCost());
            bean.setDeductionType(cacheBean.getDeductionType());
            bean.setNum(cacheBean.getNum());

            beans.add(bean);
        }

        // 批量更新数据库
        auctionDao.batchUpdateSelective(beans);

        // 批量更新缓存
        List<AuctionCacheBean> cacheBeans = getAuctions();
        for(AuctionCacheBean old : cacheBeans){
            for(AuctionCacheBean cacheBean : list){
                if(cacheBean.getAuctionId().equals(old.getAuctionId())){
                    if(cacheBean.getAuctionType() != null){
                        old.setAuctionType(cacheBean.getAuctionType());
                    }
                    if(cacheBean.getCost() != null){
                        old.setCost(cacheBean.getCost());
                    }
                    if(cacheBean.getDeductionType() != null){
                        old.setDeductionType(cacheBean.getDeductionType());
                    }
                    if(cacheBean.getNum() != null){
                        old.setNum(cacheBean.getNum());
                    }
                }
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(cacheBeans);
            jedis.set("auctions", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSelective(AuctionCacheBean cacheBean) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();

        // 更新数据库
        AuctionBean bean = new AuctionBean();
        bean.setAuctionId(cacheBean.getAuctionId());
        bean.setAuctionType(cacheBean.getAuctionType());
        bean.setCost(cacheBean.getCost());
        bean.setDeductionType(cacheBean.getDeductionType());
        bean.setNum(cacheBean.getNum());
        auctionDao.updateSelective(bean);

        // 更新缓存
        List<AuctionCacheBean> auctions = getAuctions();
        for(AuctionCacheBean old : auctions){
            if(old.getAuctionId().equals(cacheBean.getAuctionId())){
                if(cacheBean.getAuctionType() != null){
                    old.setAuctionType(cacheBean.getAuctionType());
                }
                if(cacheBean.getCost() != null){
                    old.setCost(cacheBean.getCost());
                }
                if(cacheBean.getDeductionType() != null){
                    old.setDeductionType(cacheBean.getDeductionType());
                }
                if(cacheBean.getNum() != null){
                    old.setNum(cacheBean.getNum());
                }
            }
        }
        try {
            String json = objectMapper.writeValueAsString(auctions);
            jedis.set("auctions", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer auctionId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        ObjectMapper objectMapper = new ObjectMapper();
        // 更新数据库
        auctionDao.deleteAuctionItem(auctionId);

        // 更新缓存
        if(jedis.exists("auctions")){
            String json = jedis.get("auctions");
            try {
                List<AuctionCacheBean> list = objectMapper.readValue(json, new TypeReference<List<AuctionCacheBean>>(){});
                int count = 0;
                for(AuctionCacheBean bean : list){
                    if(bean.getAuctionId().equals(auctionId)){
                        break;
                    }
                    count++;
                }
                if(list.size() > count){
                    list.remove(count);
                    String newJson = objectMapper.writeValueAsString(list);
                    jedis.set("auctions", newJson);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public AuctionCacheBean getAuction(Integer auctionId) {
        List<AuctionCacheBean> auctions = getAuctions();
        for(AuctionCacheBean cacheBean : auctions){
            if(cacheBean.getAuctionId().equals(auctionId)){
                return cacheBean;
            }
        }
        return null;
    }

    @Override
    public boolean timestampExpired() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        return !jedis.exists("auctions_time_stamp");
    }

    @Override
    public void addTimeStamp() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.set("auctions_time_stamp", "***");
        jedis.expireAt("auctions_time_stamp", CalendarUtils.getNextEarlyMorningTime());
    }
}
