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
import org.george.dungeon_game.dao.PlayerLevelDao;
import org.george.hall.model.pojo.PlayerResult;
import org.george.item.model.pojo.ItemResult;
import org.george.config.bean.LevelBean;
import org.george.pojo.Message;
import org.george.pojo.Messages;
import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.dao.bean.PlayerLevelBean;
import org.george.hall.model.PlayerModel;
import org.george.item.model.ItemModel;
import org.george.util.NumUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonGameCMDs {

    private DungeonGameCache dungeonGameCache = DungeonGameCache.getInstance();

    private PlayerLevelCache playerLevelCache = PlayerLevelCache.getInstance();

    private PlayerLevelDao playerLevelDao = PlayerLevelDao.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private BagModel bagModel = BagModel.getInstance();

    private ItemModel itemModel = ItemModel.getInstance();

    private LevelInfoConfig levelInfoConfig = LevelInfoConfig.getInstance();

    private DropItemConfig dropItemConfig = DropItemConfig.getInstance();

    private ItemConfig itemConfig = ItemConfig.getInstance();

    private Random rand = new Random();

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

    private static final String unexpected_error_happened = "发生未知异常";

    private static final Integer must_win_lose_count = 10;

    /**
     * 开始游戏
     * @param args
     * @return
     */
    public Messages startGame(String...args){


        String userId = args[0];
        Integer playerId = Integer.parseInt(userId);
        List<Message> list = new ArrayList<>();

        if(args.length > 1){
            list.add(new Message(userId, input_format_error));
        }else if(dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, at_game));
        }
        else{

            // 检测玩家体力
            int hp = playerModel.getPlayerCurrentHP(Integer.parseInt(userId));
            if (hp == 0) {
                list.add(new Message(userId, hp_0));
            }else{
                // 添加玩家
                dungeonGameCache.addPlayer(userId);
                // 获取玩家关卡信息
                PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);
                if(cacheBean == null){
                    PlayerLevelBean bean = playerLevelDao.loadPlayerLevelByPlayerId(playerId);
                    if(bean == null){
                        // 增加新的玩家关卡数据
                        playerLevelDao.addPlayerLevel(playerId);
                        list.add(new Message(userId, introduction));
                        bean = playerLevelDao.loadPlayerLevelByPlayerId(playerId);
                    }
                    // 添加缓存
                    cacheBean = playerLevelBean2CacheBean(bean);
                    playerLevelCache.addPlayerLevel(cacheBean);
                }

                if(cacheBean.getLevel() == levelInfoConfig.getLevelNum()){
                    //  通关
                    list.add(new Message(userId, already_clear));
                    dungeonGameCache.deletePlayer(userId);
                }else{
                    // 开始游戏 -1 点 hp
                    LevelBean levelBean = levelInfoConfig.getLevelBean(cacheBean.getLevel());
                    playerModel.updatePlayerHP(Integer.parseInt(userId), hp - 1);
                    list.add(new Message(userId, introduction));
                    Message message = levelInfo2Message(userId, levelBean);
                    list.add(message);
                }
            }
        }
        return new Messages(list);
    }

    /**
     *
     * @param args
     * @return
     */
    public Messages playGame(String...args){

        String userId = args[0];
        Integer playerId = Integer.parseInt(userId);
        List<Message> list = new ArrayList<>();
        if(args.length != 2){
            list.add(new Message(userId, input_format_error));
        }else if(!dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, not_at_game));
        }else {

            String action = args[1];
            if (!NumUtils.checkDigit(action)) {
                list.add(new Message(userId, input_format_error));
            } else if (Integer.parseInt(action) < 0 || Integer.parseInt(action) > 2) {
                list.add(new Message(userId, input_format_error));
            } else {

                PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);
                if(cacheBean == null){
                    PlayerLevelBean bean = playerLevelDao.loadPlayerLevelByPlayerId(playerId);
                    cacheBean = playerLevelBean2CacheBean(bean);
                    playerLevelCache.addPlayerLevel(cacheBean);
                }

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
                    PlayerLevelBean bean = playerLevelDao.loadPlayerLevelByPlayerId(playerId);
                    bean.setLoseCount(bean.getLoseCount() + 1);
                    playerLevelCache.updatePlayerLevelSelective(cacheBean);
                    playerLevelDao.updateRecordSelective(bean);

                    // 退出游戏
                    dungeonGameCache.deletePlayer(userId);
                    list.add(new Message(userId, lost));
                }
            }
        }
        return new Messages(list);
    }

    public Messages showByHpCost(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 1){
            list.add(new Message(userId, input_format_error));
        }else{
            int playerId = Integer.parseInt(userId);
            Integer buyCount = dungeonGameCache.getBuyHpCount(playerId);
            // 买了 10 次，达到购买上限
            if(buyCount == 10){
                list.add(new Message(userId, reach_daily_buy_limit));
            }else{
                list.add(new Message(userId, buy_hp_cost + (buyCount + 1)));
            }
        }
        return new Messages(list);
    }

    public Messages buyHp(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 1){
            list.add(new Message(userId, input_format_error));
        }else{

            int playerId = Integer.parseInt(userId);
            int buyCount = dungeonGameCache.getBuyHpCount(playerId);
            int cost = buyCount + 1;

            PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
            if(buyCount == 10){
                list.add(new Message(userId, reach_daily_buy_limit));
            }else if(player.getHp() == 100){
                list.add(new Message(userId, hp_is_full));
            }else if(player.getGold() < cost){
                list.add(new Message(userId, gold_not_enough));
            } else {


                Integer gold = player.getGold();
                Integer hp = player.getHp();
                try{
                    // 减去金币数
                    playerModel.updatePlayerGold(playerId, gold - cost);
                    // 增加体力
                    playerModel.updatePlayerHP(playerId, hp + 1);
                    // 更新购买记录
                    dungeonGameCache.incrBuyHpCount(playerId);
                    list.add(new Message(userId, "您花费了 " + (buyCount + 1) + " 金币提升了一点体力"));
                }catch (Exception e){

                    e.printStackTrace();
                    list.add(new Message(userId, unexpected_error_happened));
                    // 失败回滚
                    playerModel.updatePlayerGold(playerId, gold);
                    playerModel.updatePlayerHP(playerId, hp);
                    return new Messages(list);
                }
            }
        }
        return new Messages(list);
    }

    public Messages quitGame(String...args){

        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 1){
            list.add(new Message(userId, input_format_error));
        }else if(!dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, not_at_game));
        }else{
            // 玩家退出关卡
            dungeonGameCache.deletePlayer(userId);
            list.add(new Message(userId, quit_game));
        }
        return new Messages(list);
    }

    public Messages showBag(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 1){
            list.add(new Message(userId, input_format_error));
        }else{
            List<PlayerItemResult> items = bagModel.getAllPlayerItems(Integer.parseInt(userId));
            if(items == null || items.size() == 0){
                list.add(new Message(userId, bag_is_empty));
            }else{
                StringBuilder sb = new StringBuilder();
                sb.append("==================================================");
                sb.append("\r\r\n");
                sb.append("    您的背包：");
                sb.append("\r\r\n");
                int count = 0;
                for(PlayerItemResult item : items){
                    if(item.getNum() != 0){
                        count++;
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

                if(count != 0){
                    list.add(new Message(userId, sb.toString()));
                }else{
                    list.add(new Message(userId, bag_is_empty));
                }
            }
        }
        return new Messages(list);
    }

    public Messages useItem(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 2){
            list.add(new Message(userId, input_format_error));
        }else if(!dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, item_must_use_at_game));
        } else{
            Integer itemId = Integer.parseInt(args[1]);
            ItemResult itemBean = itemModel.getItemByItemId(itemId);
            if(itemBean == null){
                list.add(new Message(userId, bag_not_exists_item));
            }else{
                PlayerItemResult pItem = bagModel.getPlayerItem(Integer.parseInt(userId), itemId);
                if(pItem == null || pItem.getNum() == 0){
                    list.add(new Message(userId, bag_not_exists_item));
                }else{

                    for(Message msg : useItem(pItem)){
                        list.add(msg);
                    }
                }
            }
        }
        return new Messages(list);
    }

    private List<Message> useItem(PlayerItemResult item){
        List<Message> list = new ArrayList<>();
        if(item.getItemId() == 1){
            Integer playerId = item.getPlayerId();
            PlayerResult player = playerModel.getPlayerByPlayerId(playerId);

            PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);
            if(cacheBean == null){
                PlayerLevelBean bean = playerLevelDao.loadPlayerLevelByPlayerId(playerId);
                cacheBean = playerLevelBean2CacheBean(bean);
                playerLevelCache.addPlayerLevel(cacheBean);
            }

            // 更新背包
            item.setNum(item.getNum() - 1);
            item.setPlayerId(playerId);
            bagModel.updatePlayerItem(item);
            ItemResult i = itemModel.getItemByItemId(item.getItemId());
            list.add(new Message("" + player.getPlayerId(), "您使用了道具: " + i.getItemName()));
            for(Message msg : win(player, cacheBean)){
                list.add(msg);
            }
        }
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

        PlayerLevelBean bean = playerLevelDao.loadPlayerLevelByPlayerId(playerId);
        bean.setLevel(bean.getLevel() + 1);
        bean.setLoseCount(0);
        playerLevelDao.updateRecordSelective(bean);

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
}
