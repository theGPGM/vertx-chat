package org.george.dungeon_game.cmd;

import org.george.bag.model.BagModel;
import org.george.bag.model.impl.BagModelImpl;
import org.george.bag.model.bean.PlayerItemResult;
import org.george.config.LevelInfoConfig;
import org.george.dungeon_game.cache.PlayerBuyHpRecordCache;
import org.george.dungeon_game.cache.PlayerLevelCache;
import org.george.dungeon_game.cache.bean.PlayerLevelCacheBean;
import org.george.dungeon_game.dao.PlayerDGameRecordDao;
import org.george.hall.model.pojo.PlayerResult;
import org.george.item.model.pojo.ItemResult;
import org.george.pojo.LevelBean;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;
import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.cache.impl.DungeonGameCacheImpl;
import org.george.dungeon_game.dao.bean.PlayerLevelBean;
import org.george.hall.model.PlayerModel;
import org.george.item.model.ItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonGameCMDs {

    private DungeonGameCache dungeonGameCache = DungeonGameCacheImpl.getInstance();

    private PlayerLevelCache playerLevelCache = PlayerLevelCache.getInstance();

    private PlayerBuyHpRecordCache playerBuyHpRecordCache = PlayerBuyHpRecordCache.getInstance();

    private PlayerDGameRecordDao playerDGameRecordDao = PlayerDGameRecordDao.getInstance();

    private PlayerModel playerModel = PlayerModel.getInstance();

    private BagModel bagModel = BagModelImpl.getInstance();

    private ItemModel itemModel = ItemModel.getInstance();

    private LevelInfoConfig levelInfoConfig = LevelInfoConfig.getInstance();

    private Random rand = new Random();

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
            list.add(new Message(userId, "输入格式错误"));
        }else if(dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, "您已处于游戏状态"));
        }
        else{
            // 检测玩家体力
            int hp = playerModel.getPlayerHP(Integer.parseInt(userId));
            if (hp == 0) {
                list.add(new Message(userId, "您的体力为0， 处于疲劳状态，无法开始游戏"));
            }else{
                // 添加玩家
                dungeonGameCache.addPlayer(userId);
                // 获取玩家关卡信息
                PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);
                if(cacheBean == null){
                    PlayerLevelBean playerLevelBean = playerDGameRecordDao.getPlayerLevelByPlayerId(playerId);
                    if(playerLevelBean == null){
                        // 增加新的玩家关卡数据
                        playerLevelBean = new PlayerLevelBean();
                        playerLevelBean.setLevel(0);
                        playerLevelBean.setLoseCount(0);
                        playerLevelBean.setPlayerId(playerId);
                        playerDGameRecordDao.addPlayerLevel(playerLevelBean);

                        list.add(new Message(userId, "开始进入地下城闯关"));
                        list.add(new Message(userId, playWayInfo()));
                    }
                    // 添加缓存
                    cacheBean = playerLevelBean2CacheBean(playerLevelBean);
                    playerLevelCache.addPlayerLevel(cacheBean);
                }

                //  通关
                if(cacheBean.getLevel() == levelInfoConfig.getLevelNum()){
                    list.add(new Message(userId, "您已经通关了"));
                    dungeonGameCache.deletePlayer(userId);
                }else{
                    // 开始游戏 -1 点 hp
                    playerModel.updatePlayerHP(Integer.parseInt(userId), hp - 1);
                    list.add(new Message(userId, "开始进入地下城闯关"));

                    list.add(new Message(userId, playWayInfo()));
                    LevelBean levelBean = levelInfoConfig.getLevelInfo(cacheBean.getLevel());
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
            list.add(new Message(userId, "输入格式错误"));
        }else if(!dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, "未处于游戏状态"));
        }else {

            String action = args[1];
            if (!checkDigit(action)) {
                list.add(new Message(userId, "输入指令错误"));
            } else if (Integer.parseInt(action) < 0 || Integer.parseInt(action) > 2) {
                list.add(new Message(userId, "输入指令错误"));
            } else {

                PlayerLevelCacheBean cacheBean = playerLevelCache.getPlayerLevelByPlayerId(playerId);
                if(cacheBean == null){
                    PlayerLevelBean bean = playerDGameRecordDao.getPlayerLevelByPlayerId(playerId);
                    cacheBean = playerLevelBean2CacheBean(bean);
                    playerLevelCache.addPlayerLevel(cacheBean);
                }

                PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
                LevelBean levelBeanInfo = levelInfoConfig.getLevelInfo(cacheBean.getLevel());

                // 获取游戏结果
                int result = judge(levelBeanInfo.getWinningRate());
                // 玩家胜利 || 十次保底赢
                if (result == 1 || cacheBean.getLoseCount() == 10) {
                    for(Message msg : win(player, cacheBean)){
                        list.add(msg);
                    }
                } else if (result == 0) {
                    //  平局
                    list.add(new Message(userId, "您和怪物同归于尽了，请重新开始游戏"));
                    dungeonGameCache.deletePlayer(userId);
                } else {
                    // 挑战失败，更新玩家关卡信息
                    cacheBean.setLoseCount(cacheBean.getLoseCount() + 1);
                    PlayerLevelBean bean = playerDGameRecordDao.getPlayerLevelByPlayerId(playerId);
                    bean.setLoseCount(bean.getLoseCount() + 1);
                    playerLevelCache.updatePlayerLevelSelective(cacheBean);
                    playerDGameRecordDao.updateRecordSelective(bean);

                    // 退出游戏
                    dungeonGameCache.deletePlayer(userId);
                    list.add(new Message(userId, "您输给了怪物\r\n请重新挑战"));
                }
            }
        }
        return new Messages(list);
    }

    public Messages showByHpCost(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 1){
            list.add(new Message(userId, "输入格式错误"));
        }else{
            int playerId = Integer.parseInt(userId);
            Integer buyCount = dungeonGameCache.getBuyHpCount(playerId);
            // 买了 10 次，达到购买上限
            if(buyCount == 10){
                list.add(new Message(userId, "今天的购买达到上限，请明天再购买"));
            }else{
                list.add(new Message(userId, "本次购买一点体力的花费：" + (buyCount + 1)));
            }
        }
        return new Messages(list);
    }

    public Messages buyHp(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 1){
            list.add(new Message(userId, "输入格式错误"));
        }else{

            int playerId = Integer.parseInt(userId);
            int buyCount = dungeonGameCache.getBuyHpCount(playerId);
            int cost = buyCount + 1;

            PlayerResult player = playerModel.getPlayerByPlayerId(playerId);
            if(buyCount == 10){
                list.add(new Message(userId, "今天的购买达到上限，请明天再购买"));
            }else if(player.getHp() == 100){
                list.add(new Message(userId, "您的体力已经充满"));
            }else if(player.getGold() < cost){
                list.add(new Message(userId, "您的元宝不够购买体力了"));
            } else {


                Integer gold = player.getGold();
                Integer hp = player.getHp();
                try{
                    // 减去元宝数
                    playerModel.updatePlayerGold(playerId, gold - cost);
                    // 增加体力
                    playerModel.updatePlayerHP(playerId, hp + 1);
                    // 更新购买记录
                    dungeonGameCache.incrBuyHpCount(playerId);
                    list.add(new Message(userId, "您花费了 " + (buyCount + 1) + " 元宝提升了一点体力"));
                }catch (Exception e){

                    e.printStackTrace();
                    list.add(new Message(userId, "不好意思，发生了不知名错误"));
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
            list.add(new Message(userId, "输入格式错误"));
        }else if(!dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, "您未处于游戏状态"));
        }else{
            // 玩家退出关卡
            dungeonGameCache.deletePlayer(userId);
            list.add(new Message(userId, "您退出了游戏"));
        }
        return new Messages(list);
    }

    public Messages showBag(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 1){
            list.add(new Message(userId, "输入格式错误"));
        }else{
            List<PlayerItemResult> items = bagModel.getAllPlayerItems(Integer.parseInt(userId));
            if(items == null || items.size() == 0){
                list.add(new Message(userId, "您的背包为空"));
            }else{
                StringBuilder sb = new StringBuilder();
                sb.append("==================================================");
                sb.append("\r\n");
                sb.append("    您的背包：");
                sb.append("\r\n");
                int count = 0;
                for(PlayerItemResult item : items){
                    if(item.getNum() != 0){
                        count++;
                        ItemResult i = itemModel.getItemByItemId(item.getItemId());
                        sb.append("===>道具名: " + i.getItemName());
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
                    list.add(new Message(userId, "您的背包为空"));
                }
            }
        }
        return new Messages(list);
    }

    public Messages useItem(String...args){
        String userId = args[0];
        List<Message> list = new ArrayList<>();
        if(args.length != 2){
            list.add(new Message(userId, "输入格式错误"));
        }else if(!dungeonGameCache.playerAtGame(userId)){
            list.add(new Message(userId, "道具必须在游戏开始后使用"));
        } else{
            Integer itemId = Integer.parseInt(args[1]);
            ItemResult itemBean = itemModel.getItemByItemId(itemId);
            if(itemBean == null){
                list.add(new Message(userId, "您的背包中不存在该道具"));
            }else{
                PlayerItemResult pItem = bagModel.getPlayerItem(Integer.parseInt(userId), itemId);
                if(pItem == null || pItem.getNum() == 0){
                    list.add(new Message(userId, "您的背包中不存在该道具"));
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
                PlayerLevelBean bean = playerDGameRecordDao.getPlayerLevelByPlayerId(playerId);
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
        list.add(new Message("" + player.getPlayerId(), "您获得本轮游戏的胜利"));
        list.add(new Message("" + player.getPlayerId(), "您获得了一个元宝，可以用来购买体力"));
        playerModel.updatePlayerGold(player.getPlayerId(), player.getGold()  + 1);

        int playerId = player.getPlayerId();

        // 掉落道具
        if (dropItem(10)) {

            boolean flag = false;
            List<PlayerItemResult> items = bagModel.getAllPlayerItems(playerId);
            if(items == null || items.size() == 0){
                flag = true;
            }else{
                for(PlayerItemResult item : items){
                    // 拥有必胜道具
                    if (item.getItemId() == 1) {
                        item.setNum(item.getNum() + 1);
                        item.setPlayerId(playerId);
                        bagModel.updatePlayerItem(item);
                        flag = true;
                    }
                }
            }

            if(!flag){
                PlayerItemResult item = new PlayerItemResult();
                item.setNum(1);
                item.setPlayerId(playerId);
                item.setItemId(1);
                item.setPlayerId(playerId);
                bagModel.addPlayerItem(item);
            }

            list.add(new Message("" + player.getPlayerId(), "您获得了一个通关金币，使用它可以通过一个关卡"));
        }
        // 更新玩家关卡信息
        cacheBean.setLevel(cacheBean.getLevel() + 1);
        cacheBean.setLoseCount(0);
        playerLevelCache.updatePlayerLevelSelective(cacheBean);

        PlayerLevelBean bean = playerDGameRecordDao.getPlayerLevelByPlayerId(playerId);
        bean.setLoseCount(bean.getLevel() + 1);
        bean.setLoseCount(0);
        playerDGameRecordDao.updateRecordSelective(bean);
        dungeonGameCache.deletePlayer("" + playerId);
        return list;
    }

    private Message levelInfo2Message(String userId, LevelBean levelBean){
        StringBuilder sb = new StringBuilder();
        sb.append("您正在挑战关卡[");
        sb.append(levelBean.getLevelName());
        sb.append("]的");
        sb.append("怪物[");
        sb.append(levelBean.getMonster().getMonsterName());
        sb.append("]");
        return new Message(userId, sb.toString());
    }

    private boolean checkDigit(String roomId){
        char[] arr = roomId.toCharArray();
        for(char c : arr){
            if(c > '9' || c < '0') return false;
        }
        return true;
    }

    /**
     * 判断输赢
     * @param winningRate
     * @return
     */
    private int judge(int winningRate){
        int loseRate = winningRate + (100 - winningRate) / 2;
        int target = rand.nextInt(100);
        if(target < winningRate){
            // 赢
            return 1;
        }else if(target < loseRate){
            // 平局
            return 0;
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

    /**
     * 玩法说明
     * @return
     */
    private String playWayInfo(){
        // 玩法说明
        StringBuilder sb = new StringBuilder();
        sb.append("玩法说明:\r\n");
        sb.append("输入play_dgame:[0、1、2]来发出石头、剪头、布与关卡中的怪物猜拳，获得胜利即可通往下一关");
        return sb.toString();
    }

    private PlayerLevelCacheBean playerLevelBean2CacheBean(PlayerLevelBean bean){
        PlayerLevelCacheBean cacheBean = new PlayerLevelCacheBean();
        cacheBean.setLevel(bean.getLevel());
        cacheBean.setLoseCount(bean.getLoseCount());
        cacheBean.setPlayerId(bean.getPlayerId());
        return cacheBean;
    }
}
