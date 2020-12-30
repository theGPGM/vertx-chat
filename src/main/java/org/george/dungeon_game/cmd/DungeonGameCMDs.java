package org.george.dungeon_game.cmd;

import org.george.bag.model.BagModel;
import org.george.bag.model.BagModelImpl;
import org.george.bag.pojo.PlayerItem;
import org.george.dungeon_game.cache.PlayerBuyHpRecordCache;
import org.george.dungeon_game.cache.PlayerLevelCache;
import org.george.dungeon_game.cache.bean.PlayerLevelCacheBean;
import org.george.dungeon_game.dao.PlayerDGameRecordDao;
import org.george.item.dao.bean.ItemBean;
import org.george.item.model.pojo.ItemResult;
import org.george.pojo.Level;
import org.george.common.pojo.Message;
import org.george.common.pojo.Messages;
import org.george.dungeon_game.cache.DungeonGameCache;
import org.george.dungeon_game.cache.DungeonGameCacheImpl;
import org.george.dungeon_game.dao.bean.PlayerLevelBean;
import org.george.hall.model.PlayerModel;
import org.george.hall.model.PlayerModelImpl;
import org.george.hall.pojo.Player;
import org.george.item.model.ItemModel;
import org.george.item.model.impl.ItemModelImpl;

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
                PlayerLevelCacheBean playerLevelCacheBean = playerLevelCache.getPlayerLevelByPlayerId()

                PlayerLevelBean playerLevelBean = dungeonGameCache.getPlayerPlayerLevel(Integer.parseInt(userId));
                // 刚开始玩，没有关卡记录，添加关卡记录
                if(playerLevelBean == null){
                    playerLevelBean = new PlayerLevelBean();
                    playerLevelBean.setLevel(0);
                    playerLevelBean.setLoseCount(0);
                    playerLevelBean.setPlayerId(Integer.parseInt(userId));
                    dungeonGameCache.addPlayerLevel(playerLevelBean);
                    list.add(new Message(userId, "开始进入地下城闯关"));
                    list.add(new Message(userId, playWayInfo()));
                }else if(playerLevelBean.getLevel() == dungeonGameCache.getLevelNum()){ //  通关
                    list.add(new Message(userId, "您已经通关了"));
                    dungeonGameCache.deletePlayer(userId);
                }else{
                    list.add(new Message(userId, "开始进入地下城闯关"));
                    list.add(new Message(userId, playWayInfo()));
                    Level level = dungeonGameCache.getLevelInfo(playerLevelBean.getLevel());
                    Message message = levelInfo2Message(userId, level);
                    list.add(message);

                    // 开始游戏 -1 点 hp
                    playerModel.updatePlayerHP(Integer.parseInt(userId), hp - 1);
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
                PlayerLevelBean playerLevelBean = dungeonGameCache.getPlayerPlayerLevel(Integer.parseInt(userId));
                Player player = playerModel.getPlayerByPlayerId(Integer.parseInt(userId));
                Level levelInfo = dungeonGameCache.getLevelInfo(playerLevelBean.getLevel());

                // 获取游戏结果
                int result = judge(levelInfo.getWinningRate());
                // 玩家胜利 || 十次保底赢
                if (result == 1 || playerLevelBean.getLoseCount() == 10) {
                    for(Message msg : win(player, playerLevelBean)){
                        list.add(msg);
                    }
                } else if (result == 0) { //  平局
                    list.add(new Message(userId, "您和怪物同归于尽了，请重新开始游戏"));
                    dungeonGameCache.deletePlayer(userId);
                } else {  // 失败
                    // 更新玩家关卡信息
                    playerLevelBean.setLoseCount(playerLevelBean.getLoseCount() + 1);
                    dungeonGameCache.updatePlayerLevel(playerLevelBean);
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

            Player player = playerModel.getPlayerByPlayerId(playerId);
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
            List<PlayerItem> items = bagModel.getAllPlayerItems(Integer.parseInt(userId));
            if(items == null || items.size() == 0){
                list.add(new Message(userId, "您的背包为空"));
            }else{
                StringBuilder sb = new StringBuilder();
                sb.append("==================================================");
                sb.append("\r\n");
                sb.append("    您的背包：");
                sb.append("\r\n");
                int count = 0;
                for(PlayerItem item : items){
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
                PlayerItem pItem = bagModel.getPlayerItem(Integer.parseInt(userId), itemId);
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

    private List<Message> useItem(PlayerItem item){
        List<Message> list = new ArrayList<>();
        if(item.getItemId() == 1){
            Player player = playerModel.getPlayerByPlayerId(item.getPlayerId());
            PlayerLevelBean playerLevelBean = dungeonGameCache.getPlayerPlayerLevel(player.getPlayerId());

            // 更新背包
            item.setNum(item.getNum() - 1);
            bagModel.updatePlayerItem(player.getPlayerId(), item);
            ItemResult i = itemModel.getItemByItemId(item.getItemId());
            list.add(new Message("" + player.getPlayerId(), "您使用了道具: " + i.getItemName()));
            for(Message msg : win(player, playerLevelBean)){
                list.add(msg);
            }
        }
        return list;
    }

    private List<Message> win(Player player, PlayerLevelBean playerLevelBean){
        List<Message> list = new ArrayList<>();
        list.add(new Message("" + player.getPlayerId(), "您获得本轮游戏的胜利"));
        list.add(new Message("" + player.getPlayerId(), "您获得了一个元宝，可以用来购买体力"));
        playerModel.updatePlayerGold(player.getPlayerId(), player.getGold()  + 1);

        int playerId = Integer.parseInt("" + player.getPlayerId());

        // 掉落道具
        if (dropItem(10)) {

            boolean flag = false;
            List<PlayerItem> items = bagModel.getAllPlayerItems(playerId);
            if(items == null || items.size() == 0){
                flag = true;
            }else{
                for(PlayerItem playerItem : items){
                    // 拥有必胜道具
                    if (playerItem.getItemId() == 1) {
                        playerItem.setNum(playerItem.getNum() + 1);
                        bagModel.updatePlayerItem(playerId, playerItem);
                        flag = true;
                    }
                }
            }

            if(!flag){
                PlayerItem item = new PlayerItem();
                item.setNum(1);
                item.setPlayerId(playerId);
                item.setItemId(1);
                bagModel.addPlayerItem(playerId, item);
            }

            list.add(new Message("" + player.getPlayerId(), "您获得了一个通关金币，使用它可以通过一个关卡"));
        }
        // 更新玩家关卡信息
        playerLevelBean.setLevel(playerLevelBean.getLevel() + 1);
        playerLevelBean.setLoseCount(0);
        dungeonGameCache.updatePlayerLevel(playerLevelBean);
        dungeonGameCache.deletePlayer("" + playerId);
        return list;
    }

    private Message levelInfo2Message(String userId, Level level){
        StringBuilder sb = new StringBuilder();
        sb.append("您正在挑战关卡[");
        sb.append(level.getLevelName());
        sb.append("]的");
        sb.append("怪物[");
        sb.append(level.getMonster().getMonsterName());
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

    public static void main(String[] args) {
        int count = 0;
        DungeonGameCMDs dungeonGameCMDs = new DungeonGameCMDs();
        for(int i = 0; i < 100000; i++){
            if(dungeonGameCMDs.dropItem(10)){
                count++;
            }
        }
        System.out.println(count);
    }
}
