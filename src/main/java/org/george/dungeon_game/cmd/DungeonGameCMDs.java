package org.george.dungeon_game.cmd;

import org.george.bag.model.BagModel;
import org.george.bag.model.pojo.PlayerItemResult;
import org.george.config.DropItemConfig;
import org.george.config.ItemConfig;
import org.george.config.LevelInfoConfig;
import org.george.config.bean.DropItemInfoBean;
import org.george.config.bean.ItemInfoBean;
import org.george.dungeon_game.cache.PlayerLevelCache;
import org.george.dungeon_game.cache.bean.PlayerLevelCacheBean;
import org.george.hall.model.pojo.PlayerResult;
import org.george.item.model.pojo.ItemResult;
import org.george.config.bean.LevelBean;
import org.george.core.pojo.Message;
import org.george.core.pojo.Messages;
import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.dao.bean.PlayerLevelBean;
import org.george.hall.model.PlayerModel;
import org.george.item.model.ItemModel;
import org.george.util.JedisPool;
import org.george.util.NumUtils;
import org.george.util.ThreadLocalJedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonGameCMDs {

    private DungeonGameCache dungeonGameCache = DungeonGameCache.getInstance();

    private PlayerLevelCache playerLevelCache = PlayerLevelCache.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private BagModel bagModel = BagModel.getInstance();

    private ItemModel itemModel = ItemModel.getInstance();

    private LevelInfoConfig levelInfoConfig = LevelInfoConfig.getInstance();

    private DropItemConfig dropItemConfig = DropItemConfig.getInstance();

    private ItemConfig itemConfig = ItemConfig.getInstance();

    private Random rand = new Random();

    private Logger logger = LoggerFactory.getLogger(DungeonGameCMDs.class);

    private static final String not_at_game = "未处于游戏状态";

    private static final String at_game = "处于游戏状态";

    private static final String introduction = new StringBuilder()
            .append("========================================================================================\r\n")
            .append("++++进入地下城++++\r\n")
            .append("========================================================================================\r\n")
            .append("可以使用的命令：\r\n")
            .append("[play_dgame:[0、1、2]]:来发出石头、剪头、布与关卡中的怪物猜拳，获得胜利即可通往下一关\r\n")
            .append("[bag]:查看背包\r\n")
            .append("[use_item:道具 ID]:使用背包中的道具\r\n")
            .append("[exit_dgame]:退出地下城闯关游戏\r\n")
            .append("[hp_cost]:获取购买体力需要的花费\r\n")
            .append("[buy_hp]:购买体力\r\n")
            .append("========================================================================================").toString();


    private static final String quit_game = "退出游戏";

    private static final String hp_0 = "您的体力为0， 处于疲劳状态";

    private static final String hp_is_full = "您的体力已经充满";

    private static final String gold_not_enough = "金币不足";

    private static final String buy_hp_cost = "本次购买一点体力的花费：";

    private static final String reach_daily_buy_limit = "今天的购买达到上限，请明天再购买";

    private static final String input_format_error = "输入格式错误";

    private static final String already_clear = "您已经通关了";

    private static final String lost = "您输给了怪物，请重新挑战";

    private static final String win = "您赢得了这个关卡的胜利";

    private static final String get_a_gold = "您获得了一个金币，可以用来购买体力";

    private static final String get_a_item = "您获得了道具: ";

    private static final String bag_is_empty = "您的背包为空";

    private static final String bag_not_exists_item = "背包中没有该道具";

    private static final String item_must_use_at_game = "道具需要在游戏中才能使用";

    private static final String item_not_exists = "道具不存在";

    private static final Integer must_win_lose_count = 10;

    /**
     * 开始游戏
     * @param args
     * @return
     */
    public Messages startGame(String...args){
        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{

            String userId = args[0];
            Integer playerId = Integer.parseInt(userId);
            if(args.length > 1){
                list.add(new Message(userId, input_format_error));
            } else if(dungeonGameCache.playerAtGame(userId)){
                list.add(new Message(userId, at_game));
            } else if(!isEnoughHp(playerId)){
                // 体力不足
                list.add(new Message(userId, hp_0));
            } else if(isClear(playerId)){
                // 已经通关
                list.add(new Message(userId, already_clear));
            } else{

                // 添加玩家
                dungeonGameCache.addPlayer(userId);
                // 获取玩家关卡信息
                PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);
                if(cacheBean == null){
                    cacheBean.setPlayerId(playerId);
                    cacheBean.setLoseCount(0);
                    cacheBean.setLevel(0);
                    // 添加缓存
                    playerLevelCache.addPlayerLevel(cacheBean);
                }

                LevelBean levelBean = levelInfoConfig.getLevelBean(cacheBean.getLevel());
                // 开始游戏 -1 点 hp
                PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
                playerModel.updatePlayerHP(playerId, player.getHp() - 1);

                list.add(new Message(userId, introduction));
                Message message = levelInfo2Message(userId, levelBean);
                list.add(message);

                logger.info("玩家:{} 开始地下城冒险游戏", playerId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    /**
     *
     * @param args
     * @return
     */
    public Messages playGame(String...args){

        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            String userId = args[0];
            Integer playerId = Integer.parseInt(userId);
            if(args.length != 2){
                list.add(new Message(userId, input_format_error));
            } else if(!dungeonGameCache.playerAtGame(userId)){
                list.add(new Message(userId, not_at_game));
            } else if (!NumUtils.checkDigit(args[1])) {
                list.add(new Message(userId, input_format_error));
            } else if (Integer.parseInt(args[1]) < 0 || Integer.parseInt(args[1]) > 2) {
                list.add(new Message(userId, input_format_error));
            } else {

                PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);
                PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
                LevelBean levelBeanInfo = levelInfoConfig.getLevelBean(cacheBean.getLevel());

                // 获取游戏结果
                int result = judge(levelBeanInfo.getWinningRate());
                // 玩家胜利 || 十次保底赢
                if (result == 1 || cacheBean.getLoseCount() == must_win_lose_count) {
                    for(Message msg : win(player, cacheBean)){
                        list.add(msg);
                    }
                } else {
                    // 挑战失败，更新玩家关卡信息
                    cacheBean.setLoseCount(cacheBean.getLoseCount() + 1);
                    playerLevelCache.updatePlayerLevelSelective(cacheBean);

                    // 退出游戏
                    dungeonGameCache.deletePlayer(userId);
                    list.add(new Message(userId, lost));
                }

                logger.info("玩家：{} 完成一局游戏", playerId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }

        return new Messages(list);
    }

    public Messages showByHpCost(String...args){
        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            String userId = args[0];
            Integer playerId = Integer.parseInt(userId);
            if(args.length != 1){
                list.add(new Message(userId, input_format_error));
            }else if(isReachBuyHpLimit(playerId)){
                // 买了 10 次，达到购买上限
                list.add(new Message(userId, reach_daily_buy_limit));
            }else{
                Integer buyCount = dungeonGameCache.getBuyHpCount(playerId);
                list.add(new Message(userId, buy_hp_cost + (buyCount + 1)));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages buyHp(String...args){
        List<Message> list = new ArrayList<>();

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            String userId = args[0];
            Integer playerId = Integer.parseInt(userId);
            if(args.length != 1){
                list.add(new Message(userId, input_format_error));
            }else if(isReachBuyHpLimit(playerId)){
                // 买了 10 次，达到购买上限
                list.add(new Message(userId, reach_daily_buy_limit));
            }else if(playerModel.getPlayerByPlayerId(playerId).getHp() == 100){
                // 体力满了
                list.add(new Message(userId, hp_is_full));
            }else if(!haveEnoughGold(playerId)){
                // 不够钱
                list.add(new Message(userId, gold_not_enough));
            }else{

                PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
                int buyCount = dungeonGameCache.getBuyHpCount(playerId);
                int cost = buyCount + 1;
                // 减去金币数
                playerModel.updatePlayerGold(playerId, player.getGold() - cost);
                // 增加体力
                playerModel.updatePlayerHP(playerId, player.getHp() + 1);
                // 更新购买记录
                dungeonGameCache.incrBuyHpCount(playerId);
                list.add(new Message(userId, "您花费了 " + (buyCount + 1) + " 金币提升了一点体力"));

                logger.info("用户:{} 购买一点体力", playerId);
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages quitGame(String...args){

        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            String userId = args[0];
            if(args.length != 1){
                list.add(new Message(userId, input_format_error));
            }else if(!dungeonGameCache.playerAtGame(userId)){
                list.add(new Message(userId, not_at_game));
            }else{
                // 玩家退出关卡
                dungeonGameCache.deletePlayer(userId);
                list.add(new Message(userId, quit_game));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages showBag(String...args){
        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            String userId = args[0];
            Integer playerId = Integer.parseInt(userId);
            if(args.length != 1){
                list.add(new Message(userId, input_format_error));
            } else if(isBagEmpty(playerId)){
                list.add(new Message(userId, bag_is_empty));
            } else{
                List<PlayerItemResult> items = bagModel.getAllPlayerItems(playerId);
                StringBuilder sb = new StringBuilder();
                sb.append("==================================================");
                sb.append("\r\r\n");
                sb.append("    您的背包：");
                sb.append("\r\r\n");
                for(PlayerItemResult item : items){
                    if(item.getNum() != 0){
                        ItemResult i = itemModel.getItemByItemId(item.getItemId());
                        sb.append("===>道具名: " + i.getItemName());
                        sb.append("\r\n");
                        sb.append("    道具 ID: " + i.getItemId());
                        sb.append("\r\n");
                        sb.append("    数量：" + item.getNum());
                        sb.append("\r\n");
                        sb.append("    介绍: " + i.getDescription());
                        sb.append("\r\n");
                    }
                }
                sb.append("==================================================");
                list.add(new Message(userId, sb.toString()));
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    public Messages useItem(String...args){
        List<Message> list = new ArrayList<>();
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            String userId = args[0];
            Integer playerId = Integer.parseInt(userId);
            if(args.length != 2){
                list.add(new Message(userId, input_format_error));
            } else if(!dungeonGameCache.playerAtGame(userId)){
                // 未处于游戏状态
                list.add(new Message(userId, item_must_use_at_game));
            } else if(itemModel.getItemByItemId(Integer.parseInt(args[1])) == null){
                // 道具不存在
                list.add(new Message(userId, item_not_exists));
            } else if(isBagEmpty(playerId)){
                // 背包为空
                list.add(new Message(userId, bag_is_empty));
            } else if(!isBagHaveItem(playerId, Integer.parseInt(args[1]))){
                // 背包中没有该道具
                list.add(new Message(userId, bag_not_exists_item));
            } else {
                Integer itemId = Integer.parseInt(args[1]);
                PlayerItemResult pItem = bagModel.getPlayerItem(Integer.parseInt(userId), itemId);
                if(pItem.getItemId() == 1){
                    PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
                    PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);

                    // 更新背包
                    pItem.setNum(pItem.getNum() - 1);
                    pItem.setPlayerId(playerId);
                    bagModel.updatePlayerItem(pItem);

                    ItemResult i = itemModel.getItemByItemId(pItem.getItemId());
                    list.add(new Message("" + player.getPlayerId(), "您使用了道具: " + i.getItemName()));

                    for(Message msg : win(player, cacheBean)){
                        list.add(msg);
                    }
                }
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
        return new Messages(list);
    }

    private List<Message> useItem(PlayerItemResult item){
        List<Message> list = new ArrayList<>();

        return list;
    }

    private List<Message> win(PlayerResult player, PlayerLevelCacheBean cacheBean){
        List<Message> list = new ArrayList<>();
        list.add(new Message("" + player.getPlayerId(), win));
        list.add(new Message("" + player.getPlayerId(), get_a_gold));
        playerModel.updatePlayerGold(player.getPlayerId(), player.getGold()  + 1);

        int playerId = player.getPlayerId();

        List<DropItemInfoBean> dropItemInfos = dropItemConfig.getLevelDropItemInfo(cacheBean.getLevel());
        for(DropItemInfoBean dii : dropItemInfos){
            if(dropItem(dii.getRate())){
                boolean flag = false;
                List<PlayerItemResult> items = bagModel.getAllPlayerItems(playerId);
                if(items == null || items.size() == 0){
                    continue;
                }else{
                    for(PlayerItemResult item : items){
                        // 拥有该道具
                        if(item.getItemId().equals(dii.getItemId())){
                            item.setNum(item.getNum() + 1);
                            item.setPlayerId(playerId);
                            bagModel.updatePlayerItem(item);
                            flag = true;
                        }
                    }
                }

                // 从未获得该道具
                if(!flag){
                    PlayerItemResult item = new PlayerItemResult();
                    item.setNum(1);
                    item.setPlayerId(playerId);
                    item.setItemId(dii.getItemId());
                    item.setPlayerId(playerId);
                    bagModel.addPlayerItem(item);
                }

                ItemInfoBean itemInfoBean = itemConfig.getItemInfoBean(dii.getItemId());
                list.add(new Message("" + player.getPlayerId(), get_a_item + itemInfoBean.getItemName() + ", " +  itemInfoBean.getDescription()));
            }
        }

        // 更新玩家关卡信息
        cacheBean.setLevel(cacheBean.getLevel() + 1);
        cacheBean.setLoseCount(0);
        playerLevelCache.updatePlayerLevelSelective(cacheBean);
        dungeonGameCache.deletePlayer("" + playerId);
        return list;
    }

    private Message levelInfo2Message(String userId, LevelBean levelBean){
        StringBuilder sb = new StringBuilder();
        sb.append("您正在挑战关卡[");
        sb.append(levelBean.getLevelName());
        sb.append("]的");
        sb.append("怪物[");
        sb.append(levelBean.getMonsterBean().getMonsterName());
        sb.append("]");
        return new Message(userId, sb.toString());
    }

    /**
     * 判断输赢
     * @param winningRate
     * @return
     */
    private int judge(int winningRate){
        int randNum = rand.nextInt(100);
        if(randNum < winningRate){
            // 赢
            return 1;
        }else{
            // 输
            return -1;
        }
    }

    /**
     * 是否掉落道具
     * @return
     */
    private boolean dropItem(int dropRate){
        return rand.nextInt(100 ) < dropRate;
    }

    private PlayerLevelCacheBean playerLevelBean2CacheBean(PlayerLevelBean bean){
        PlayerLevelCacheBean cacheBean = new PlayerLevelCacheBean();
        cacheBean.setLevel(bean.getLevel());
        cacheBean.setLoseCount(bean.getLoseCount());
        cacheBean.setPlayerId(bean.getPlayerId());
        return cacheBean;
    }

    private boolean isEnoughHp(Integer playerId){
        return playerModel.getPlayerByPlayerId(playerId).getHp() > 0;
    }

    private boolean isClear(Integer playerId){
        return playerLevelCache.getPlayerLevelByPlayerId(playerId).getLevel() == levelInfoConfig.getLevelNum();
    }

    private boolean isReachBuyHpLimit(Integer playerId){
        return dungeonGameCache.getBuyHpCount(playerId) >= 10;
    }

    private boolean haveEnoughGold(Integer playerId) {
        Integer record = dungeonGameCache.getBuyHpCount(playerId);
        PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
        return player.getGold() > (record + 1);
    }

    private boolean isBagEmpty(Integer playerId){
        List<PlayerItemResult> items = bagModel.getAllPlayerItems(playerId);
        if(items == null || items.size() == 0){
            return true;
        }
        int count = 0;
        for(PlayerItemResult item : items){
            if(item.getNum() != 0){
                count++;
            }
        }
        return count == 0;
    }

    private boolean isBagHaveItem(Integer playerId, Integer itemId){
        List<PlayerItemResult> pis = bagModel.getAllPlayerItems(playerId);
        for(PlayerItemResult pir : pis){
            if(pir.getItemId().equals(itemId) && pir.getNum() > 0){
                return true;
            }
        }
        return false;
    }
}
