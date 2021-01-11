package org.george.auction.cmd;

import org.george.auction.DeductionHandler;

import org.george.auction.DeliveryHandler;
import org.george.auction.cache.AuctionCache;
import org.george.auction.cache.bean.AuctionCacheBean;
import org.george.auction.config.AuctionConfig;
import org.george.auction.config.bean.AuctionInfoBean;
import org.george.auction.pojo.AuctionTypeEnum;
import org.george.auction.util.JedisPool;
import org.george.auction.util.NumUtils;
import org.george.auction.util.RedisLockUtils;
import org.george.auction.util.ThreadLocalJedisUtils;
import org.george.core.util.JFinalUtils;
import org.george.hall.model.pojo.PlayerResult;
import org.george.item.model.ItemModel;
import org.george.auction.pojo.DeductionTypeEnum;
import org.george.cmd.pojo.Message;
import org.george.cmd.pojo.Messages;
import org.george.item.model.pojo.ItemResult;
import org.george.hall.model.PlayerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionCmds {

    private AuctionCache auctionCache = AuctionCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private ItemModel itemModel = ItemModel.getInstance();

    private AuctionConfig auctionConfig = AuctionConfig.getInstance();

    private AtomicBoolean flag = new AtomicBoolean(false);

    private AtomicInteger lock = new AtomicInteger(0);

    private Logger logger = LoggerFactory.getLogger(AuctionCmds.class);

    /**
     * 购买拍卖会物品
     * @param args
     * @return
     */
    public Messages buyAuction(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            if(args.length != 3){
                list.add(new Message(userId, "输入格式错误"));
            } else if(!NumUtils.checkDigit(args[1]) || !NumUtils.checkDigit(args[2])){
                list.add(new Message(userId, "请输入正确的物品 ID 和 购买数量"));
            } else if(Integer.parseInt(args[2]) <= 0){
                list.add(new Message(userId, "购买数量不能小于 0"));
            } else if(!isAuctionsRefresh()){
                list.add(new Message(userId, "拍卖行正在刷新中，请稍后再试"));
            }  else if(!existsAuction(Integer.parseInt(args[1]))){
                list.add(new Message(userId, "购买商品不存在"));
            } else{

                String requestId = UUID.randomUUID().toString();
                while(true){
                    boolean flag = RedisLockUtils.tryLock("buy_auctions", requestId, 10    );
                    if(flag){
                        break;
                    }
                }

                try {
                    Integer auctionId = Integer.parseInt(args[1]);
                    Integer buyNum = Integer.parseInt(args[2]);
                    AuctionCacheBean auctionCacheBean = auctionCache.getAuction(auctionId);
                    PlayerResult player = playerModel.getPlayerByPlayerId(Integer.parseInt(userId));

                    if (auctionCache.getAuction(Integer.parseInt(args[1])).getNum() < Integer.parseInt(args[2])) {
                        list.add(new Message(userId, "库存不足"));
                    } else if (!DeductionHandler.deductionHandle(auctionCacheBean.getDeductionType(), player.getPlayerId(), buyNum * auctionCacheBean.getCost())) {
                        list.add(new Message(userId, "扣减操作失败"));
                    } else {

                        DeliveryHandler.handle(auctionCacheBean.getAuctionType(), player.getPlayerId(), auctionCacheBean.getAuctionId(), buyNum);

                        // 更新拍卖会数据
                        AuctionCacheBean cacheBean = new AuctionCacheBean();
                        cacheBean.setAuctionId(auctionCacheBean.getAuctionId());
                        cacheBean.setNum(auctionCacheBean.getNum() - buyNum);
                        auctionCache.updateSelective(cacheBean);

                        list.add(new Message(userId, "购买成功"));

                        logger.info("用户:{} 购买:{}成功，数量:{}", userId, auctionId, buyNum);
                    }
                }finally {
                    RedisLockUtils.releaseLock("buy_auctions", requestId);
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages showAuctions(String...args){
        List<Message> list = new ArrayList<>();
        String userId = args[0];
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            if(args.length != 1){
                list.add(new Message(userId, "输入格式错误"));
            }else if(!isAuctionsRefresh()){
                list.add(new Message(userId, "拍卖行正在刷新中，请稍后再试"));
            } else{
                String msg = info2Msg(auctionCache.getAuctions());
                list.add(new Message(userId, msg));
            }
        }finally {
           JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private String info2Msg(List<AuctionCacheBean> auctions){
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================");
        sb.append("\r\n");
        sb.append("++++欢迎进入拍卖会，使用[buy_a:itemId:num]命令：可以购买想要的物品++++");
        sb.append("\r\n");
        sb.append("====================================================================");
        sb.append("\r\n");
        sb.append("    拍卖会物品如下：");
        sb.append("\r\n");
        for(AuctionCacheBean auction : auctions){
            ItemResult itemBean = itemModel.getItemByItemId(auction.getAuctionId());

            String deductionType = null;
            for(DeductionTypeEnum deductionTypeEnum : DeductionTypeEnum.values()){
                if(auction.getDeductionType() == deductionTypeEnum.getType()){
                    deductionType = deductionTypeEnum.getName();
                }
            }

            sb.append("===>道具 ID：" + auction.getAuctionId());
            sb.append("\r\n");
            sb.append("    道具名：" + itemBean.getItemName());
            sb.append("\r\n");
            sb.append("    数量: " + auction.getNum());
            sb.append("\r\n");
            sb.append("    售价: " + auction.getCost() + deductionType);
            sb.append("\r\n");
            sb.append("    介绍：" + itemBean.getDescription());
            sb.append("\r\n");
        }
        sb.append("====================================================================");
        return sb.toString();
    }

    private boolean isAuctionsRefresh(){

        // 需要刷新商店
        if (auctionCache.timestampExpired()) {
            // 有人正在刷新商店
            if (flag.get()) {
                return false;
            } else if (flag.compareAndSet(false, true)){
                // 抢到锁
                // 刷新商店
                refreshShop();
                // 添加时间戳
                auctionCache.addTimeStamp();
                // 重置锁
                flag.set(false);

                return true;
            } else{
                // 抢不到锁
                return false;
            }
        }
        // 不需要刷新商店
        return true;
    }

    private void refreshShop(){


        List<AuctionInfoBean> auctionInfo = auctionConfig.getAllAuctionInfo();
        List<AuctionCacheBean> list = new ArrayList<>();
        for(AuctionInfoBean infoBean : auctionInfo){
            // 刷新
            AuctionCacheBean cacheBean = new AuctionCacheBean();
            cacheBean.setAuctionType(infoBean.getAuctionType());
            cacheBean.setAuctionId(infoBean.getAuctionId());
            cacheBean.setDeductionType(infoBean.getDeductionType());
            cacheBean.setNum(infoBean.getNum());
            cacheBean.setCost(infoBean.getCost());

            list.add(cacheBean);
        }
        auctionCache.batchUpdateSelective(list);
    }

    private boolean existsAuction(Integer auctionId){
        return auctionCache.getAuction(auctionId) != null;
    }

    public static void main(String[] args) {

        // 观察者模式，添加货币扣减事件观察者，用于金币等货币单位的扣减
        DeductionHandler.addObserver(DeductionTypeEnum.GOLD.getType(), PlayerModel.getInstance());

        // 观察者模式，添加货物派发事件观察者，用于道具等物品的派发
        DeliveryHandler.addObserver(AuctionTypeEnum.Item.getType(), ItemModel.getInstance());

        AuctionCmds auctionCmds = new AuctionCmds();
        JFinalUtils.initJFinalConfig();
        for(int i = 0; i < 10000; i++){

            new Thread(() -> {
                auctionCmds.buyAuction("19", "1", "1");
                System.out.println("结束");
            }).start();
        }
    }
}
