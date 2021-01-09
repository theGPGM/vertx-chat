package org.george.auction.cmd;

import org.george.auction.DeductionHandler;
import org.george.auction.DeliveryHandler;
import org.george.auction.cache.AuctionCache;
import org.george.auction.cache.bean.AuctionCacheBean;
import org.george.auction.dao.AuctionDao;
import org.george.auction.dao.bean.AuctionBean;
import org.george.config.AuctionConfig;
import org.george.config.bean.AuctionInfoBean;
import org.george.hall.model.pojo.PlayerResult;
import org.george.item.model.ItemModel;
import org.george.auction.pojo.DeductionTypeEnum;
import org.george.core.pojo.Message;
import org.george.core.pojo.Messages;
import org.george.item.model.pojo.ItemResult;
import org.george.util.NumUtils;
import org.george.util.RedisLockUtils;
import org.george.hall.model.PlayerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionCmds {

    private AuctionCache auctionCache = AuctionCache.getInstance();

    private AuctionDao auctionDao = AuctionDao.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private ItemModel itemModel = ItemModel.getInstance();

    private AuctionConfig auctionConfig = AuctionConfig.getInstance();

    private AtomicBoolean flag = new AtomicBoolean(false);

    /**
     * 购买拍卖会物品
     * @param args
     * @return
     */
    public Messages buyAuction(String...args){

        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 3){
            list.add(new Message(userId, "输入格式错误"));
        }else if(!NumUtils.checkDigit(args[1]) || !NumUtils.checkDigit(args[2])){
            list.add(new Message(userId, "输入格式错误"));
        }else{
            // 判断是否需要先刷新商店
            if(auctionCache.timestampExpired()){
                if(flag.compareAndSet(false, true)){

                    // 刷新商店
                    refreshShop();

                    // 添加时间戳
                    auctionCache.addTimeStamp();
                    flag.set(false);
                }else{
                    list.add(new Message(userId, "拍卖行正在刷新中，请稍后再试"));
                    return new Messages(list);
                }
            }

            String requestId = UUID.randomUUID().toString();
            int count = 0;
            boolean locked = false;
            while(true){
                // 加锁，10 秒过期
                locked = RedisLockUtils.tryLock("buy_auction", requestId, 10);
                if(locked){
                    break;
                }
                // 尝试 10 次
                if(count > 10){
                    break;
                }
                count++;
            }

            if(locked){
                try{
                    Integer auctionId = Integer.parseInt(args[1]);
                    Integer buyNum = Integer.parseInt(args[2]);
                    AuctionCacheBean auctionCacheBean = auctionCache.getAuction(auctionId);
                    PlayerResult player = playerModel.getPlayerByPlayerId(Integer.parseInt(userId));

                    if(buyNum <= 0){
                        list.add(new Message(userId, "输入格式错误"));
                    }else if(auctionCacheBean == null){
                        list.add(new Message(userId, "购买商品不存在"));
                    }else if(auctionCacheBean.getNum() < buyNum){
                        list.add(new Message(userId, "库存不足"));
                    } else {

                        if(!DeductionHandler.deductionHandle(auctionCacheBean.getDeductionType(), player.getPlayerId(), buyNum * auctionCacheBean.getCost())){
                            list.add(new Message(userId, "扣减操作失败"));

                        }else{
                            DeliveryHandler.handle(auctionCacheBean.getAuctionType(), player.getPlayerId(), auctionCacheBean.getAuctionId(), buyNum);

                            // 更新拍卖会数据
                            AuctionCacheBean cacheBean = new AuctionCacheBean();
                            cacheBean.setAuctionId(auctionCacheBean.getAuctionId());
                            cacheBean.setNum(auctionCacheBean.getNum() - buyNum);
                            auctionCache.updateSelective(cacheBean);

                            AuctionBean bean = new AuctionBean();
                            bean.setAuctionId(auctionId);
                            bean.setNum(auctionCacheBean.getNum() - buyNum);
                            auctionDao.updateSelective(bean);

                            list.add(new Message(userId, "购买成功"));
                        }
                    }
                }finally {
                    // 解锁
                    RedisLockUtils.releaseLock("buy_auction", requestId);
                }
            }else{
                list.add(new Message(userId, "系统繁忙，请稍候重试"));
            }
        }
        return new Messages(list);
    }

    public Messages showAuctions(String...args){
        List<Message> list = new ArrayList<>();
        String userId = args[0];
        if(args.length != 1){
            list.add(new Message(userId, "输入格式错误"));
        }else{
            // 判断是否需要先刷新商店
            if(auctionCache.timestampExpired()){
                // 由第一个玩家来刷新商店，其他玩家如果这时候进入就让他们等待一会
                if(flag.compareAndSet(false, true)){
                    // 刷新
                    refreshShop();
                    // 添加时间戳
                    auctionCache.addTimeStamp();
                    flag.set(false);
                }else{
                    list.add(new Message(userId, "拍卖行正在刷新中，请稍后再试"));
                    return new Messages(list);
                }
            }
            String msg = info2Msg(auctionCache.getAuctions());
            list.add(new Message(userId, msg));
        }
        return new Messages(list);
    }

    private void refreshShop(){

        for(AuctionInfoBean infoBean : auctionConfig.getAllAuctionInfo()){
            // 刷新
            AuctionBean bean = new AuctionBean();
            bean.setAuctionType(infoBean.getAuctionType());
            bean.setAuctionId(infoBean.getAuctionId());
            bean.setDeductionType(infoBean.getDeductionType());
            bean.setNum(infoBean.getNum());
            bean.setCost(infoBean.getCost());

            AuctionCacheBean cacheBean = new AuctionCacheBean();
            cacheBean.setAuctionType(infoBean.getAuctionType());
            cacheBean.setAuctionId(infoBean.getAuctionId());
            cacheBean.setDeductionType(infoBean.getDeductionType());
            cacheBean.setNum(infoBean.getNum());
            cacheBean.setCost(infoBean.getCost());

            auctionCache.addAuctionItemCacheBean(cacheBean);
            auctionDao.updateSelective(bean);
        }
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
}
