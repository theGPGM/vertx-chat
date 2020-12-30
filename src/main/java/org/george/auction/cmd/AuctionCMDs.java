package org.george.auction.cmd;

import org.george.auction.cache.AuctionCache;

import org.george.auction.cache.AuctionCacheImpl;
import org.george.auction.pojo.AuctionItem;
import org.george.bag.model.BagModel;
import org.george.bag.model.BagModelImpl;
import org.george.bag.pojo.PlayerItem;
import org.george.item.model.ItemModel;
import org.george.item.model.impl.ItemModelImpl;
import org.george.item.dao.bean.ItemBean;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;
import org.george.util.RedisLockUtils;
import org.george.hall.model.PlayerModel;
import org.george.hall.model.PlayerModelImpl;
import org.george.hall.pojo.Player;

import java.util.ArrayList;
import java.util.List;

public class AuctionCMDs {

    private AuctionCache auctionCache = AuctionCacheImpl.getInstance();

    private PlayerModel playerModel = PlayerModelImpl.getInstance();

    private BagModel bagModel = BagModelImpl.getInstance();

    private ItemModel itemModel = ItemModelImpl.getInstance();

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
        }else{

            if(!checkDigit(args[1]) || !checkDigit(args[2])){
                list.add(new Message(userId, "输入格式错误"));
            }else{

                Integer itemId = Integer.parseInt(args[1]);
                Integer buyNum = Integer.parseInt(args[2]);
                AuctionItem auction = auctionCache.getAuction(itemId);
                Player player = playerModel.getPlayerByPlayerId(Integer.parseInt(userId));

                try{
                    // 购买加分布式锁，10 秒过期
                    RedisLockUtils.tryLock("buy_auction", 10);

                    if(buyNum <= 0){
                        list.add(new Message(userId, "输入格式错误"));
                    }else if(auction == null){
                        list.add(new Message(userId, "购买商品不存在"));
                    }else if(auction.getNum() < buyNum){
                        list.add(new Message(userId, "库存不足"));
                    }else if(player.getGold() < buyNum * auction.getCost()){
                        list.add(new Message(userId, "元宝不足"));
                    }else{
                        // 增加背包物品
                        List<PlayerItem> items = bagModel.getAllPlayerItems(player.getPlayerId());

                        boolean owned = false;
                        for(PlayerItem item : items){
                            if(item.getItemId().equals(auction.getItemId())){
                                item.setNum(item.getNum() + buyNum);
                                bagModel.updatePlayerItem(player.getPlayerId(), item);
                                owned = true;
                            }
                        }
                        if(!owned){
                            PlayerItem item = new PlayerItem();
                            item.setNum(buyNum);
                            item.setItemId(auction.getItemId());
                            bagModel.addPlayerItem(player.getPlayerId(), item);
                        }
                        // 更新拍卖会数据
                        auctionCache.updateAuctionItemNum(auction.getItemId(), auction.getNum() - buyNum);
                        // 扣钱
                        playerModel.updatePlayerGold(player.getPlayerId(), player.getGold() - (buyNum * auction.getCost()));
                        list.add(new Message(userId, "购买成功"));
                    }
                }finally {
                    // 解锁
                    RedisLockUtils.releaseLock("buy_auction");
                }
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
            List<AuctionItem> auctions = auctionCache.getAuctions();
            StringBuilder sb = new StringBuilder();
            sb.append("================================================");
            sb.append("\r\n");
            sb.append("     欢迎进入拍卖会，使用[buy_a:itemId:num]命令：可以购买想要的道具");
            sb.append("\r\n");
            sb.append("     拍卖会物品如下：");
            sb.append("\r\n");
            for(AuctionItem auctionItem : auctions){
                ItemBean itemBean = itemModel.getItemByItemId(auctionItem.getItemId());
                sb.append("====>道具 ID：" + auctionItem.getItemId());
                sb.append("\r\n");
                sb.append("     道具名：" + itemBean.getItemName());
                sb.append("\r\n");
                sb.append("     数量: " + auctionItem.getNum());
                sb.append("\r\n");
                sb.append("     售价: " + auctionItem.getCost());
                sb.append("\r\n");
                sb.append("     介绍：" + itemBean.getDescription());
                sb.append("\r\n");
            }
            sb.append("================================================");
            list.add(new Message(userId, sb.toString()));
        }
        return new Messages(list);
    }

    private boolean checkDigit(String roomId){

        if(roomId.startsWith("-") || roomId.length() >= 5 || roomId.startsWith("0")){
            return false;
        }

        char[] arr = roomId.toCharArray();
        for(char c : arr){
            if(c > '9' || c < '0') return false;
        }
        return true;
    }
}
